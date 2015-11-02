package fr.lteconsulting.pomexplorer.commands;

import fr.lteconsulting.pomexplorer.ILogger;
import fr.lteconsulting.pomexplorer.PomAnalyzer;
import fr.lteconsulting.pomexplorer.WorkingSession;

@Command
public class AnalyzeCommand
{
	@Help( "analyse all the pom files in a directory, recursively" )
	public void directory( WorkingSession session, ILogger log, String directory )
	{
		log.html( "Analyzing directoy '" + directory + "'...<br/>" );

		PomAnalyzer analyzer = new PomAnalyzer();
		analyzer.analyze( directory, session, log );

		log.html( "Analyzis completed for '" + directory + "'.<br/>" );
	}
}
