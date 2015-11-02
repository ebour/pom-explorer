package fr.lteconsulting.pomexplorer.commands;

import fr.lteconsulting.pomexplorer.Application;
import fr.lteconsulting.pomexplorer.ILogger;

@Command
public class HelpCommand extends AbstractCommand
{
    public HelpCommand(Application application)
    {
        super(application);
    }

    @Help("gives this message")
    public void main(ILogger log)
    {
        log.html(commands().help());
    }

}
