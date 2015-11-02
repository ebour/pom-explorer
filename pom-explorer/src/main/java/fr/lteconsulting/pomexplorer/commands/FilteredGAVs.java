package fr.lteconsulting.pomexplorer.commands;

import fr.lteconsulting.pomexplorer.Application;
import fr.lteconsulting.pomexplorer.GAV;
import fr.lteconsulting.pomexplorer.Tools;
import fr.lteconsulting.pomexplorer.WorkingSession;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Commodity class to handle GAV filtering by name in commands
 */
public class FilteredGAVs extends AbstractCommand
{
    private String filter = "";

    public FilteredGAVs(Application application)
    {
        super(application);
    }

    public void setFilter(String filter)
    {
        if (filter != null)
            filter = filter.toLowerCase();
        this.filter = filter;
    }

    public String getFilter()
    {
        return filter;
    }

    public boolean accept(GAV gav)
    {
        return gav != null && (filter == null || gav.toString().toLowerCase().contains(filter));
    }

    public List<GAV> getGavs(WorkingSession session)
    {
        Stream<GAV> stream;

        if (filter != null)
            stream = session.graph().gavs().stream().filter(gav -> gav.toString().toLowerCase().contains(filter));
        else
            stream = session.graph().gavs().stream();

        List<GAV> res = new ArrayList<>();

        stream.sorted(Tools.gavAlphabeticalComparator).forEachOrdered(gav -> res.add(gav));

        return res;
    }
}
