package fr.lteconsulting.pomexplorer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import fr.lteconsulting.pomexplorer.commands.*;

import fr.lteconsulting.pomexplorer.graph.relation.Relation;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

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
