package fr.lteconsulting.pomexplorer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MavenBuildTask
{
	public Boolean build( WorkingSession session, Project project )
	{
		try
		{
			log( project, "start ..." );
			Process p = Runtime.getRuntime().exec( new String[] { session.getMavenShellCommand(), "install", "-N", "-DskipTests" }, null, project.getPomFile().getParentFile() );

			BufferedReader reader = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
			String line = "";
			while( (line = reader.readLine()) != null )
			{
				log( project, line );
			}

			p.waitFor();

			log( project, "done (" + p.exitValue() + ")." );

			return p.exitValue() == 0;
		}
		catch( IOException | InterruptedException e )
		{
			log( project, "error ! " + e );

			return false;
		}
	}

	private void log( Project project, String message )
	{
		message = Tools.buildMessage( "[building " + project.getGav() + "] " + message );
		System.out.println(message);
	}
}
