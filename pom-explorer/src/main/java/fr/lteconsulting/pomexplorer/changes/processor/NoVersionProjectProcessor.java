package fr.lteconsulting.pomexplorer.changes.processor;

import fr.lteconsulting.pomexplorer.*;
import fr.lteconsulting.pomexplorer.changes.GavChange;
import fr.lteconsulting.pomexplorer.changes.IChangeSet;

/**
 * When one wants to change a project version, maybe the project's version is
 * defined by the project's parent's version. In this case, the change should be
 * mutated to change the parent's version !
 *
 * @author Arnaud
 */
public class NoVersionProjectProcessor extends AbstractGavChangeProcessor
{
    @Override
    public void processChange(WorkingSession session, ILogger log, GavChange change, IChangeSet changeSet)
    {
        // change should be a PROJECT GavChange
        if (change.getLocation().getSection() != PomSection.PROJECT)
            return;

        GAV projectGav = change.getLocation().getGav();
        Project project = session.projects().forGav(projectGav);
        if (project == null)
        {
            if (log != null)
                log.html(Tools.warningMessage("cannot find project for gav " + projectGav));
            return;
        }

        // project version should be null
        if (project.getUnresolvedPom().getModel().getVersion() != null)
            return;

        // and project should have a parent
        GAV parentProjectGav = session.graph().parent(projectGav);
        if (parentProjectGav == null)
            return;

        // in this case:

        // TODO should be only done if the version changes. And the GAV change
        // may apply partilally on the current project if it concerns its
        // groupId or artifactId

        log.html(Tools.warningMessage("updating the parent version, because the project itself lacks a version"));

        // remove the change from the change set
        changeSet.invalidateChange(change);

        // add changing the parent's version in the change set
        GAV parentProjectGavModified = new GAV(parentProjectGav.getGroupId(), parentProjectGav.getArtifactId(), change.getNewGav().getVersion());
        changeSet.addChange(new GavChange(session.projects().forGav(parentProjectGav), PomSection.PROJECT, parentProjectGav, parentProjectGavModified), change);
    }
}
