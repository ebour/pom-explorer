package fr.lteconsulting.pomexplorer.commands;

import fr.lteconsulting.pomexplorer.Application;

/**
 * Created by ebour on 02/11/15.
 */
public class AbstractCommand
{

    private final Application application;

    public AbstractCommand(Application application)
    {
        this.application = application;
    }

    public Commands commands()
    {
        return application.commands();
    }
}
