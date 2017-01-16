package com.ppm.integration.agilesdk.connector.agm.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SimpleEntityCollection<T> implements Iterable<T> {

	private final List<T> container = new LinkedList<T>();

	public List<T> getCollection(){
		return container;
	}

	public SimpleEntityCollection<T> add(T p){
		container.add(p);
		return this;
	}

	@Override
	public Iterator<T> iterator() {
		return container.iterator();
	}
}
