package fr.lteconsulting.pomexplorer.depanalyze;

import fr.lteconsulting.pomexplorer.GAV;
import fr.lteconsulting.pomexplorer.PomSection;
import fr.lteconsulting.pomexplorer.Project;
import org.apache.maven.model.Dependency;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependency;

public class GavLocation extends Location
{
    private final PomSection section;

    private GAV gav;

    private GAV unresolvedGav;

    public GavLocation(Project project, PomSection section, MavenDependency resolved)
    {
        this(project, section, new GAV(resolved.getGroupId(), resolved.getArtifactId(), resolved.getVersion()), null);
    }

    public GavLocation(Project project, PomSection section, Dependency readden)
    {
        this(project, section, null, null);

        setReadDependency(readden);
    }

    public GavLocation(Project project, PomSection section, GAV gav)
    {
        this(project, section, gav, gav);
    }

    public GavLocation(Project project, PomSection section, GAV resolvedGav, GAV unresolvedGav)
    {
        super(project, null);

        this.section = section;
        this.gav = resolvedGav;
        this.unresolvedGav = unresolvedGav;
    }

    public void setReadDependency(Dependency readden)
    {
        unresolvedGav = new GAV(readden.getGroupId(), readden.getArtifactId(), readden.getVersion());
    }

    public PomSection getSection()
    {
        return section;
    }

    public GAV getGav()
    {
        if (gav != null)
            return gav;

        return unresolvedGav;
    }

    public GAV getResolvedGav()
    {
        return gav;
    }

    public GAV getUnresolvedGav()
    {
        return unresolvedGav;
    }

    @Override
    public String toString()
    {
        String res = "[" + section + "] ";

        if (gav != null && unresolvedGav != null)
        {
            if (gav.equals(unresolvedGav))
                res += gav.toString();
            else
                res += "[*] " + gav + " / " + unresolvedGav;
        }
        else
        {
            if (gav != null)
                res = gav.toString();
            else if (unresolvedGav != null)
                res += "[unresolved]" + unresolvedGav;
            else
                res += "[!!!] NULL";
        }

        return res;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((gav == null) ? 0 : gav.hashCode());
        result = prime * result + ((section == null) ? 0 : section.hashCode());
        result = prime * result + ((unresolvedGav == null) ? 0 : unresolvedGav.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        GavLocation other = (GavLocation) obj;
        if (gav == null)
        {
            if (other.gav != null)
                return false;
        }
        else if (!gav.equals(other.gav))
            return false;
        if (section != other.section)
            return false;
        if (unresolvedGav == null)
        {
            if (other.unresolvedGav != null)
                return false;
        }
        else if (!unresolvedGav.equals(other.unresolvedGav))
            return false;
        return true;
    }
}
