package fr.lteconsulting.pomexplorer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for simple App.
 */
public class AppTest
{
    @Test
	public void testOpenVersions()
	{
		checkOpenVersion("15.6.0", "16.6.0-SNAPSHOT");
		checkOpenVersion("15060", "15061-SNAPSHOT");
		checkOpenVersion("15060.0", "15061.0-SNAPSHOT");
		checkOpenVersion("15060.0.5", "15061.0.5-SNAPSHOT");
	}

	private void checkOpenVersion(String closed, String opened)
	{
		final GAV newGav = Tools.openGavVersion(new GAV("group", "artifact", closed));
		assertEquals(opened, newGav.getVersion());
	}
}
