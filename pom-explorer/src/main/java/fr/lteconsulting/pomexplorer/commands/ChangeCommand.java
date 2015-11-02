package fr.lteconsulting.pomexplorer.commands;

import fr.lteconsulting.pomexplorer.GAV;
import fr.lteconsulting.pomexplorer.ILogger;
import fr.lteconsulting.pomexplorer.PomSection;
import fr.lteconsulting.pomexplorer.Tools;
import fr.lteconsulting.pomexplorer.WorkingSession;
import fr.lteconsulting.pomexplorer.changes.ChangeSetManager;
import fr.lteconsulting.pomexplorer.changes.GavChange;
import fr.lteconsulting.pomexplorer.depanalyze.GavLocation;

@Command
public class ChangeCommand
{
	@Help( "changes the GAV version and also in dependent projects. Parameters : gav, newVersion" )
	public static void gav( CommandOptions options, WorkingSession session, ILogger log, GAV originalGav, GAV newGav )
	{
		log.html( "<b>Changing</b> " + originalGav + " to " + newGav + "<br/><br/>" );

		ChangeSetManager changes = new ChangeSetManager();

		GavLocation loc = new GavLocation( session.projects().forGav( originalGav ), PomSection.PROJECT, originalGav );
		changes.addChange( new GavChange( loc, newGav ), "changing " + originalGav + " to " + newGav );

		changes.resolveChanges( session, log );

		Tools.printChangeList( log, changes );

		CommandTools.maybeApplyChanges( session, options, log, changes );
	}
}
