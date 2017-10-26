/*-
 * #%L
 * io.earcam.utilitarian.site.deploy.netlify
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
package io.earcam.utilitarian.site.deploy.netlify;

import static io.earcam.unexceptional.Closing.closeAfterAccepting;

import java.io.OutputStream;

import javax.annotation.WillClose;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

public class Site {

	private static final String FIELD_NAME = "name";
	private static final String FIELD_CUSTOM_DOMAIN = "custom_domain";
	private static final String FIELD_FORCE_SSL = "force_ssl";
	private static final String FIELD_SSL = "ssl";
	private static final String FIELD_MANAGED_DNS = "managed_dns";
	private static final String FIELD_ID = "id";

	// all params are optional
	private String name;
	private String customDomain;
	private boolean forceSsl;
	private boolean ssl;
	private boolean managedDns;
	private String id;


	public void writeJson(@WillClose OutputStream output)
	{
		closeAfterAccepting(Json::createWriter, output, toJsonObject(), JsonWriter::write);
	}


	public JsonObject toJsonObject()
	{
		JsonObjectBuilder builder = Json.createObjectBuilder();
		addNotNullable(builder, FIELD_NAME, name());
		addNotNullable(builder, FIELD_ID, id());
		addNotNullable(builder, FIELD_CUSTOM_DOMAIN, customDomain());
		builder.add(FIELD_FORCE_SSL, forceSsl());
		builder.add(FIELD_SSL, ssl());
		builder.add(FIELD_MANAGED_DNS, managedDns());
		return builder.build();
	}


	private static void addNotNullable(JsonObjectBuilder builder, String field, String value)
	{
		if(value != null) {
			builder.add(field, value);
		}
	}


	public static Site fromJsonObject(JsonObject json)
	{
		Site site = new Site();
		site.setName(json.getString(FIELD_NAME, null));
		site.setId(json.getString(FIELD_ID, null));
		site.setCustomDomain(json.getString(FIELD_CUSTOM_DOMAIN, null));
		site.setForceSsl(json.getBoolean(FIELD_FORCE_SSL, false));
		site.setSsl(json.getBoolean(FIELD_SSL, false));
		site.setManagedDns(json.getBoolean(FIELD_MANAGED_DNS, false));
		return site;
	}


	public void setName(String name)
	{
		this.name = name;
	}


	public void setCustomDomain(String customDomain)
	{
		this.customDomain = customDomain;
	}


	public void setForceSsl(boolean forceSsl)
	{
		this.forceSsl = forceSsl;
	}


	public void setSsl(boolean ssl)
	{
		this.ssl = ssl;
	}


	public void setManagedDns(boolean managedDns)
	{
		this.managedDns = managedDns;
	}


	public void setId(String id)
	{
		this.id = id;
	}


	public String name()
	{
		return name;
	}


	public String customDomain()
	{
		return customDomain;
	}


	public boolean forceSsl()
	{
		return forceSsl;
	}


	public boolean ssl()
	{
		return ssl;
	}


	public boolean managedDns()
	{
		return managedDns;
	}


	public String id()
	{
		return id;
	}
}
