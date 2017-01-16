package com.ppm.integration.agilesdk.connector.agm.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Domains")
public class Domains extends SimpleEntityCollection<Domain> {

	@Override
	@XmlElement(name = "Domain")
	public List<Domain> getCollection(){
		return super.getCollection();
	}
}
