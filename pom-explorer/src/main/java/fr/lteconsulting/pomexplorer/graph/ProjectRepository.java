package fr.lteconsulting.pomexplorer.graph;

import fr.lteconsulting.pomexplorer.GAV;
import fr.lteconsulting.pomexplorer.Project;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProjectRepository
{
    private final Map<GAV, Project> projects = new HashMap<>();

    public boolean contains(GAV gav)
    {
        return projects.containsKey(gav);
    }

    public void add(Project project)
    {
        projects.put(project.getGav(), project);
    }

    public Project forGav(GAV gav)
    {
        return projects.get(gav);
    }

    public int size()
    {
        return projects.size();
    }

    public Set<GAV> keySet()
    {
        return projects.keySet();
    }

    public Collection<Project> values()
    {
        return projects.values();
    }
}