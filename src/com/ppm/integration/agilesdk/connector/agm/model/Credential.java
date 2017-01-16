package com.ppm.integration.agilesdk.connector.agm.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="alm-authentication")
public class Credential {

	@XmlElement(name="user")
	public String user;

	@XmlElement(name="password")
	public String password;
}
