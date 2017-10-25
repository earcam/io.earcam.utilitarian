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

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "echo")
@XmlAccessorType(FIELD)
public class Echo {

	@XmlAttribute
	private String original;

	@XmlAttribute
	private String echoed;


	public Echo()
	{}


	public Echo(String original, String echoed)
	{
		this.original = original;
		this.echoed = echoed;
	}


	public String getOriginal()
	{
		return original;
	}


	public void setOriginal(String original)
	{
		this.original = original;
	}


	public String getEchoed()
	{
		return echoed;
	}


	public void setEchoed(String echoed)
	{
		this.echoed = echoed;
	}
}
