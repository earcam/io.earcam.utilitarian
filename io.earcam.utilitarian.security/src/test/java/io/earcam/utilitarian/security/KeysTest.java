package io.earcam.utilitarian.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class KeysTest {


	@Test
	public void canGenerateDefaultRsa()
	{
		assertThat(Keys.rsa(), is(not(nullValue())));
	}


	@Test
	public void canGenerateDefaultDsa()
	{
		assertThat(Keys.dsa(), is(not(nullValue())));
	}
}
