package fr.lteconsulting.pomexplorer;

import fr.lteconsulting.pomexplorer.commands.Command;
import fr.lteconsulting.pomexplorer.commands.Commands;
import fr.lteconsulting.pomexplorer.graph.relation.Relation;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;

public class Application
{
    private Commands commands;

    private ApplicationSettings settings;

    public Commands commands()
    {
        if (commands == null)
        {
            commands = new Commands();

            final Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .addUrls(ClasspathHelper.forJavaClassPath()));
            final Set<Class<?>> commandClasses = reflections.getTypesAnnotatedWith(Command.class);

            for (Class commandClass : commandClasses)
            {
                final Object commandClassInstance = newInstanceOf(commandClass, this);
                if (commandClassInstance != null)
                {
                    commands.addCommand(commandClassInstance);
                }
            }
        }

        return commands;
    }

    public ApplicationSettings getSettings()
    {
        if (settings == null)
        {
            settings = new ApplicationSettings();
            settings.load();
        }

        return settings;
    }

    private Object newInstanceOf(Class commandClass, Application application)
    {
        try
        {
            return commandClass.getConstructor(Application.class).newInstance(application);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    static class EdgeDto
    {
        String from;

        String to;

        String label;

        Relation relation;

        public EdgeDto(String from, String to, Relation relation)
        {
            this.from = from;
            this.to = to;
            this.relation = relation;

            label = relation.toString();
        }
    }

    static class GraphDto
    {
        Set<String> gavs;

        Set<EdgeDto> relations;
    }
}
