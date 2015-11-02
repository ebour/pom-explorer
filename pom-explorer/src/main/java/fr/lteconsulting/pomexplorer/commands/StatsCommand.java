package fr.lteconsulting.pomexplorer.commands;

import fr.lteconsulting.pomexplorer.Application;
import fr.lteconsulting.pomexplorer.GAV;
import fr.lteconsulting.pomexplorer.ILogger;
import fr.lteconsulting.pomexplorer.WorkingSession;
import fr.lteconsulting.pomexplorer.graph.relation.Relation;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.StrongConnectivityInspector;

import java.util.Set;

@Command
public class StatsCommand extends AbstractCommand
{
    public StatsCommand(Application application)
    {
        super(application);
    }

    @Help("general statistics on the session")
    public void main(WorkingSession session, ILogger log)
    {
        log.html("There are " + session.graph().gavs().size() + " gavs<br/>");

        StrongConnectivityInspector<GAV, Relation> conn = new StrongConnectivityInspector<>(session.graph().internalGraph());
        log.html("There are " + conn.stronglyConnectedSets().size() + " strongly connected components<br/>");

        ConnectivityInspector<GAV, Relation> ccon = new ConnectivityInspector<>(session.graph().internalGraph());
        log.html("There are " + ccon.connectedSets().size() + " weakly connected components<br/>");

        CycleDetector<GAV, Relation> cycles = new CycleDetector<GAV, Relation>(session.graph().internalGraph());
        log.html("Is there cycles ? " + cycles.detectCycles() + "<br/>");
    }

    @Help("gives the details of the connected components of the pom graph")
    public void components(WorkingSession session, ILogger log)
    {
        ConnectivityInspector<GAV, Relation> ccon = new ConnectivityInspector<>(session.graph().internalGraph());
        log.html("There are " + ccon.connectedSets().size() + " weakly connected components<br/>");

        for (Set<GAV> gavs : ccon.connectedSets())
        {
            log.html("<br/>Set of connected GAVs :<br/>");
            for (GAV gav : gavs)
            {
                log.html("- " + gav + "<br/>");
            }
        }
    }
}
