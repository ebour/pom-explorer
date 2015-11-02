package fr.lteconsulting.pomexplorer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.lteconsulting.pomexplorer.commands.*;
import org.jgrapht.DirectedGraph;

import com.google.gson.Gson;

import fr.lteconsulting.pomexplorer.graph.relation.Relation;
import fr.lteconsulting.pomexplorer.webserver.Message;
import fr.lteconsulting.pomexplorer.webserver.MessageFactory;
import fr.lteconsulting.pomexplorer.webserver.WebServer;
import fr.lteconsulting.pomexplorer.webserver.XWebServer;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import sun.reflect.Reflection;

public class AppFactory
{
	private static final AppFactory INSTANCE = new AppFactory();

	private AppFactory()
	{
	}

	public static AppFactory get()
	{
		return INSTANCE;
	}

	private final List<WorkingSession> sessions = new ArrayList<>();

	private Commands commands;

	private ApplicationSettings settings;

	private WebServer webServer;

	public List<WorkingSession> sessions()
	{
		return sessions;
	}

	public Commands commands() {
		if( commands == null )
		{
			commands = new Commands();

			final Reflections reflections = new Reflections(new ConfigurationBuilder()
																	.addUrls(ClasspathHelper.forJavaClassPath()));
			final Set<Class<?>> commandClasses = reflections.getTypesAnnotatedWith(Command.class);

			for(Class commandClass : commandClasses)
			{
				final Object commandClassInstance = newInstanceOf( commandClass );
				if(commandClassInstance != null)
				{
					commands.addCommand( commandClassInstance );
				}
			}
		}

		return commands;
	}

	public ApplicationSettings getSettings()
	{
		if( settings == null )
		{
			settings = new ApplicationSettings();
			settings.load();
		}

		return settings;
	}

	public WebServer webServer()
	{
		if( webServer == null )
			webServer = new WebServer( xWebServer );

		return webServer;
	}

	private XWebServer xWebServer = new XWebServer()
	{
		@Override
		public void onNewClient( Client client ) {
			System.out.println( "New client " + client.getId() );

			final String talkId = MessageFactory.newGuid();

			// running the default script
			List<String> commands = Tools.readFileLines( "welcome.commands" );
			for( String command : commands )
			{
				if( command.isEmpty() || command.startsWith( "#" ) )
					continue;

				if( command.startsWith( "=" ) )
				{
					String message = command.substring( 1 );
					if( message.isEmpty() )
						message = "<br/>";
					client.sendHtml( talkId, message );
				}
				else
				{
					AppFactory.get().commands().takeCommand(client, createLogger(client, talkId), command);
				}
			}

			client.sendClose( talkId );
		}

		@Override
		public void onWebsocketMessage( Client client, String messageText )
		{
			Gson gson = new Gson();
			Message message = gson.fromJson( messageText, Message.class );
			if( message == null )
			{
				client.sendHtml( MessageFactory.newGuid(), Tools.warningMessage( "null message received !" ) );
				return;
			}

			if( "text/command".equals( message.getPayloadFormat() ) )
			{
				AppFactory.get().commands().takeCommand( client, createLogger( client, message.getTalkGuid() ), message.getPayload() );
			}
			else if( "hangout/reply".equals( message.getPayloadFormat() ) )
			{
				for( int i = 0; i < waitingHangouts.size(); i++ )
				{
					HangOutHandle handle = waitingHangouts.get( i );
					if( handle.message.getGuid().equals( message.getResponseTo() ) )
					{
						handle.answer = message.getPayload();
						handle.waitingAnswer = false;
						synchronized( handle )
						{
							handle.notify();
						}
					}
				}
			}
			else
			{
				client.sendHtml( message.getTalkGuid(), Tools.warningMessage( "ununderstood message " + messageText + ".<br/>" ) );
			}

			client.sendClose( message.getTalkGuid() );

		}

		@Override
		public String onGraphQuery( String sessionIdString )
		{
			List<WorkingSession> sessions = AppFactory.get().sessions();
			if( sessions == null || sessions.isEmpty() )
				return "No session available. Go to main page !";

			WorkingSession session = null;

			try
			{
				Integer sessionId = Integer.parseInt( sessionIdString );
				if( sessionId != null )
				{
					for( WorkingSession s : sessions )
					{
						if( System.identityHashCode( s ) == sessionId )
						{
							session = s;
							break;
						}
					}
				}
			}
			catch( Exception e )
			{
			}

			if( session == null )
				session = sessions.get( 0 );

			DirectedGraph<GAV, Relation> g = session.graph().internalGraph();

			GraphDto dto = new GraphDto();
			dto.gavs = new HashSet<>();
			dto.relations = new HashSet<>();
			for( GAV gav : g.vertexSet() )
			{
				dto.gavs.add( gav.toString() );

				for( Relation relation : g.outgoingEdgesOf( gav ) )
				{
					GAV target = g.getEdgeTarget( relation );
					EdgeDto edge = new EdgeDto( gav.toString(), target.toString(), relation );
					dto.relations.add( edge );
				}
			}

			Gson gson = new Gson();
			String result = gson.toJson( dto );

			return result;
		}

		@Override
		public void onClientLeft( Client client )
		{
			System.out.println( "Client left." );
		}
	};

	private final List<HangOutHandle> waitingHangouts = new ArrayList<>();

	private class HangOutHandle
	{
		final Message message;

		String answer;

		boolean waitingAnswer;

		public HangOutHandle( Message message )
		{
			this.message = message;
		}
	}

	private ILogger createLogger( Client client, String talkId )
	{
		return new ILogger()
		{
			@Override
			public void html( String log )
			{
				client.sendHtml( talkId, log );
			}

			@Override
			public String prompt( String question )
			{
				Message message = client.sendHangOutText( talkId, question );
				HangOutHandle handle = new HangOutHandle( message );
				waitingHangouts.add( handle );

				handle.waitingAnswer = true;
				synchronized( handle )
				{
					while( handle.waitingAnswer )
					{
						try
						{
							handle.wait();
						}
						catch( InterruptedException e )
						{
							e.printStackTrace();
						}
					}
				}

				return handle.answer;
			}
		};
	}

	private Object newInstanceOf(Class commandClass)
	{
		try
		{
			return commandClass.newInstance();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	static class EdgeDto
	{
		String from;

		String to;

		String label;

		Relation relation;

		public EdgeDto( String from, String to, Relation relation )
		{
			this.from = from;
			this.to = to;
			this.relation = relation;

			label = relation.toString();
		}
	}

	static class GraphDto
	{
		Set<String> gavs;

		Set<EdgeDto> relations;
	}
}
