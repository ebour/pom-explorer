package fr.lteconsulting.pomexplorer.commands;

import fr.lteconsulting.pomexplorer.AppFactory;
import fr.lteconsulting.pomexplorer.ILogger;

@Command
public class HelpCommand
{
	@Help( "gives this message" )
	public void main( ILogger log )
	{
		log.html( AppFactory.get().commands().help() );
	}
}
