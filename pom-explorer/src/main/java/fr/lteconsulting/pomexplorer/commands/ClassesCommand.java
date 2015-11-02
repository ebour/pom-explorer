package fr.lteconsulting.pomexplorer.commands;

import fr.lteconsulting.pomexplorer.*;
import fr.lteconsulting.pomexplorer.javac.JavaSourceAnalyzer;

import java.util.List;

@Command
public class ClassesCommand extends AbstractCommand
{
    public ClassesCommand(Application application)
    {
        super(application);
    }

    @Help("gives the java classes provided by the session's gavs")
    public void main(WorkingSession session, ILogger log)
    {
        providedBy(session, log, null);
    }

    @Help("gives the java classes provided by the session's gavs, filtered by the given parameter")
    public void providedBy(WorkingSession session, ILogger log, FilteredGAVs gavFilter)
    {
        if (gavFilter == null)
        {
            log.html(Tools.warningMessage("You should specify a GAV filter"));
            return;
        }

        log.html("<br/>GAV list filtered with '" + gavFilter + "' :<br/>");

        for (GAV gav : gavFilter.getGavs(session))
        {
            List<String> classes = GavTools.analyseProvidedClasses(session, gav, log);
            if (classes == null)
            {
                log.html(Tools.warningMessage("No class provided by gav " + gav));
                continue;
            }

            for (String className : classes)
                log.html(className + "<br/>");
        }
    }

    /*
     * parse all the Java source files in the gav's project directory and
     * extract all referenced fqns.
     *
     * substract the gav's provided classes from this set, to get external
     * references
     */
    @Help("gives the fqn list of referenced classes by the session's gavs, filtered by the given parameter")
    public void referencedBy(WorkingSession session, ILogger log, FilteredGAVs gavFilter)
    {
        JavaSourceAnalyzer analyzer = new JavaSourceAnalyzer();

        for (GAV gav : gavFilter.getGavs(session))
        {
            Project project = session.projects().forGav(gav);
            if (project == null)
                continue;

            analyzer.analyzeProject(project, true, log);
        }
    }
}
