package fr.lteconsulting.pomexplorer.commands;

import fr.lteconsulting.pomexplorer.*;
import fr.lteconsulting.pomexplorer.depanalyze.GavLocation;
import fr.lteconsulting.pomexplorer.depanalyze.Location;
import fr.lteconsulting.pomexplorer.graph.relation.GAVRelation;
import fr.lteconsulting.pomexplorer.graph.relation.Relation;
import fr.lteconsulting.pomexplorer.graph.relation.Relation.RelationType;

import java.util.HashSet;
import java.util.Set;

@Command
public class DependsCommand extends AbstractCommand
{
    public DependsCommand(Application application)
    {
        super(application);
    }

    @Help("lists the GAVs directly depending on the one given in parameter")
    public void on(WorkingSession session, ILogger log, GAV gav)
    {
        Set<GAVRelation<Relation>> relations = session.graph().relationsReverse(gav);

        log.html("<br/><b>Directly depending on " + gav + "</b>, " + relations.size() + " GAVs :<br/>");
        log.html("([" + RelationType.DEPENDENCY.shortName() + "]=direct dependency, [" + RelationType.PARENT.shortName() + "]=parent's dependency, [" + RelationType.BUILD_DEPENDENCY.shortName() + "]=build dependency)<br/><br/>");

        Set<GAV> indirectDependents = new HashSet<>();

        for (GAVRelation<Relation> relation : relations)
        {
            GAV source = relation.getSource();

            RelationType type = relation.getRelation().getRelationType();

            log.html("[" + type.shortName() + "] " + source + " ");

            fillTextForDependency(session, log, relation);

            log.html("<br/>");

            Set<GAVRelation<Relation>> indirectRelations = session.graph().relationsReverseRec(source);
            for (GAVRelation<Relation> ir : indirectRelations)
                indirectDependents.add(ir.getSource());
        }

        log.html("<br/><b>Indirectly depending on " + gav + "</b>, " + indirectDependents.size() + " GAVs :<br/>");
        for (GAV d : indirectDependents)
            log.html(d + "<br/>");
    }

    @Help("lists the GAVs that the GAV passed in parameters depends on")
    public void by(WorkingSession session, ILogger log, GAV gav)
    {
        Set<GAVRelation<Relation>> relations = session.graph().relations(gav);

        log.html("<br/><b>" + gav + " directly depends on</b> " + relations.size() + " GAVs :<br/>");
        log.html("([" + RelationType.DEPENDENCY.shortName() + "]=direct dependency, [" + RelationType.PARENT.shortName() + "]=parent's dependency, [" + RelationType.BUILD_DEPENDENCY.shortName() + "]=build dependency)<br/><br/>");

        Set<GAV> indirectDependents = new HashSet<>();

        for (GAVRelation<Relation> relation : relations)
        {
            GAV target = relation.getTarget();
            RelationType type = relation.getRelation().getRelationType();

            log.html("[" + type.shortName() + "] " + target + " ");
            fillTextForDependency(session, log, relation);
            log.html("<br/>");

            Set<GAVRelation<Relation>> indirectRelations = session.graph().relationsRec(target);
            for (GAVRelation<Relation> ir : indirectRelations)
                indirectDependents.add(ir.getTarget());
        }

        log.html("<br/><b>" + gav + " indirectly depends on</b> " + indirectDependents.size() + " GAVs :<br/>");
        for (GAV d : indirectDependents)
            log.html(d + "<br/>");
    }

    private void fillTextForDependency(WorkingSession session, ILogger log, GAVRelation<Relation> relation)
    {
        GAV source = relation.getSource();

        Project sourceProject = session.projects().forGav(source);
        if (sourceProject == null)
        {
            log.html(Tools.warningMessage("(no project for this GAV, dependency locations not analyzed)"));
            return;
        }

        log.html("declared at : ");
        Location location = Tools.findDependencyLocation(session, log, sourceProject, relation);
        log.html(location.toString());

        if (location instanceof GavLocation)
        {
            GavLocation gavLocation = (GavLocation) location;
            if (Tools.isMavenVariable(gavLocation.getUnresolvedGav().getVersion()))
            {
                String property = Tools.getPropertyNameFromPropertyReference(gavLocation.getUnresolvedGav().getVersion());
                Project definitionProject = Tools.getPropertyDefinitionProject(session, gavLocation.getProject(), property);
                log.html(", property ${" + property + "} defined in project " + definitionProject.getGav());
            }
        }
    }
}
