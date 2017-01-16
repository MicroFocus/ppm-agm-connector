package com.ppm.integration.agilesdk.connector.agm.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Projects")
public class Projects extends SimpleEntityCollection<Project> {

	@Override
	@XmlElement(name = "Project")
	public List<Project> getCollection(){
		return super.getCollection();
	}
}
