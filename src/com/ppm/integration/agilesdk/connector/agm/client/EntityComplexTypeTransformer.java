package com.ppm.integration.agilesdk.connector.agm.client;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.ppm.integration.agilesdk.connector.agm.model.helper.EntityComplexTypeReader;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.EntityComplexType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

public class EntityComplexTypeTransformer {

	public static List<EntityComplexTypeReader> wrap( List<EntityComplexType> raw, Filter... f ){

		List<EntityComplexTypeReader> result = new LinkedList<EntityComplexTypeReader>();
		for(final EntityComplexType r : raw){
			final EntityComplexTypeReader reader = new EntityComplexTypeReader(r);
			if( f.length == CollectionUtils.countMatches(Arrays.asList(f),new Predicate(){
				@Override
				public boolean evaluate(Object arg0) {
					return ((Filter)arg0).isMatched(reader);
				}
			})){
				result.add(reader);
			}
		}

		return result;
	}


	public interface Filter {
		boolean isMatched( EntityComplexTypeReader entity );
	}
}
