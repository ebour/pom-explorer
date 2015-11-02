package fr.lteconsulting.pomexplorer;

import fr.lteconsulting.pomexplorer.graph.PomGraph;
import fr.lteconsulting.pomexplorer.graph.ProjectRepository;

import java.util.HashSet;
import java.util.Set;

/**
 * Some projects can be pinned as needed to be always up to date
 * <p>
 * <p>
 * watch all recursive dependencies with local project.
 * <p>
 * for each one, build the project if a change is detected
 * <p>
 * process changes with a customizable delay, so builds are made in the best
 * possible order
 * <p>
 * builds should be cancellable (kill the build process simply)
 *
 * @author Arnaud
 */
public class WorkingSession
{
    private String mavenSettingsFilePath = null;

    private String mavenShellCommand = "mvn";

    private final GitRepositories gitRepositories = new GitRepositories();

    private final ProjectRepository projects = new ProjectRepository();

    private final PomGraph graph = new PomGraph();

    private final Set<Project> maintainedProjects = new HashSet<>();

    private final ProjectsWatcher projectsWatcher = new ProjectsWatcher();

    public WorkingSession()
    {

    }

    public void configure(ApplicationSettings settings)
    {
        this.mavenSettingsFilePath = settings.getDefaultMavenSettingsFile();
    }

    public PomGraph graph()
    {
        return graph;
    }

    public ProjectRepository projects()
    {
        return projects;
    }

    public GitRepositories repositories()
    {
        return gitRepositories;
    }

    public Set<Project> maintainedProjects()
    {
        return maintainedProjects;
    }

    public void cleanBuildList()
    {

    }

    public String getMavenSettingsFilePath()
    {
        return mavenSettingsFilePath;
    }

    public void setMavenSettingsFilePath(String mavenSettingsFilePath)
    {
        this.mavenSettingsFilePath = mavenSettingsFilePath;
    }

    public String getMavenShellCommand()
    {
        return mavenShellCommand;
    }

    public void setMavenShellCommand(String mavenShellCommand)
    {
        this.mavenShellCommand = mavenShellCommand;
    }

    public String getDescription()
    {
        return "<div><b>WorkingSession " + System.identityHashCode(this) + "</b><br/>" + "Maven configuration file : " + (mavenSettingsFilePath != null ? mavenSettingsFilePath : "(system default)") + "<br/>" + "Maven shell command : "
                + (mavenShellCommand != null ? mavenShellCommand : "(null)") + "<br/>" + projects.size() + " projects<br/>" + graph.gavs().size() + " GAVs<br/>" + graph.relations().size() + " relations<br/></div>";
    }

    public ProjectsWatcher projectsWatcher()
    {
        return projectsWatcher;
    }
}
