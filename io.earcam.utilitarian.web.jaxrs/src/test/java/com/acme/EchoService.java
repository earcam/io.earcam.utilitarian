/*-
 * #%L
 * io.earcam.utilitarian.web
 * %%
 * Copyright (C) 2017 earcam
 * %%
 * SPDX-License-Identifier: (BSD-3-Clause OR EPL-1.0 OR Apache-2.0 OR MIT)
 *
 * You <b>must</b> choose to accept, in full - any individual or combination of
 * the following licenses:
 * <ul>
 * 	<li><a href="https://opensource.org/licenses/BSD-3-Clause">BSD-3-Clause</a></li>
 * 	<li><a href="https://www.eclipse.org/legal/epl-v10.html">EPL-1.0</a></li>
 * 	<li><a href="https://www.apache.org/licenses/LICENSE-2.0">Apache-2.0</a></li>
 * 	<li><a href="https://opensource.org/licenses/MIT">MIT</a></li>
 * </ul>
 * #L%
 */
package com.acme;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("echo")
public class EchoService {

	@Produces(APPLICATION_JSON)
	@Path("{sound}")
	@GET
	public Echo echo(@PathParam("sound") String original)
	{
		if("quack".equalsIgnoreCase(original.trim())) {
			return new Echo(original, original);
		}
		return new Echo(original, original + " " + original);
	}


	@Consumes(APPLICATION_JSON)
	@Produces(APPLICATION_JSON)
	@POST
	@Path("echo")
	public Echo echoEcho(Echo original)
	{
		if("quack".equalsIgnoreCase(original.getEchoed().trim())) {
			return original;
		}
		return new Echo(original.getEchoed(), original.getEchoed() + " " + original.getEchoed());
	}
}
