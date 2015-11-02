package fr.lteconsulting.pomexplorer;

import fr.lteconsulting.pomexplorer.commands.Commands;
import fr.lteconsulting.pomexplorer.commands.Commands.CommandCallInfo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CommandsTest
{
	@Test
	public void testOne()
    {
		Commands cmd = AppFactory.get().commands();
		ILogger log = new ILogger()
		{
			@Override
			public void html( String log )
			{
				System.out.println( log );
			}

			@Override
			public String prompt( String question )
			{
				return "";
			}
		};

		assertCommand( cmd.findMethodForCommand( "gav li".split( " " ), log ), "GavsCommand", "list" );
		assertCommand( cmd.findMethodForCommand( "gAv List".split( " " ), log ), "GavsCommand", "list" );
	}

	private void assertCommand( CommandCallInfo info, String command, String method )
	{
		assertNotNull( info );
		assertEquals( info.command.getClass().getSimpleName(), command);
		assertEquals( info.method.getName(), method );
	}
}
