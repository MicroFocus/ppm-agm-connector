package com.ppm.integration.agilesdk.connector.agm.model.helper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ppm.integration.agilesdk.connector.agm.model.jaxb.EntityComplexType;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.EntityComplexType.RelatedEntities;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.EntityComplexType.RelatedEntities.Relation;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.FieldComplexType;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.FieldComplexType.Value;

public class EntityComplexTypeReader {

	private final Logger logger = Logger.getLogger(this.getClass());
	private static final DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");

	private final EntityComplexType source;
	private Map<String,String> fieldDict;

	public EntityComplexTypeReader(EntityComplexType source){
		this.source = source;
	}

	public EntityComplexTypeReader getRelatedEntityReaderByAlias(String alias){
		RelatedEntities re = this.source.getRelatedEntities();
		Relation r = null;
		for(Relation rl : re.getRelation()){
			if(alias.equals(rl.getAlias())){
				r = rl;
				break;
			}
		}

		return new EntityComplexTypeReader(r.getEntity());
	}

	public String strValue(String name){
		if(fieldDict==null){
			List<FieldComplexType> fields = this.source.getFields().getField();
			fieldDict = new HashMap<String,String>(fields.size());

			for(FieldComplexType f : fields){
				List<Value> value = f.getValue();

				if(value.size() > 0){
					fieldDict.put(f.getName(), f.getValue().get(0).getValue());
				}
			}
		}

		return fieldDict.get(name);
	}

	public int intValue(String name, int defaultValue){
		int result = defaultValue;

		String str = strValue(name);
		if(!StringUtils.isEmpty(str)){
			try{
				result = Integer.parseInt(str);
			}catch(NumberFormatException e){
				logger.warn(e.getMessage());
			}
		}
		return result;
	}

	public double doubleValue(String name, double defaultValue){
		double result = defaultValue;

		String str = strValue(name);
		if(!StringUtils.isEmpty(str)){
			try{
				result = Double.parseDouble(str);
			}catch(NumberFormatException e){
				logger.warn(e.getMessage());
			}
		}
		return result;
	}

    public Double doubleObjectValue(String name) {
        String str = strValue(name);
        if (!StringUtils.isEmpty(str)) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

	public Date dateValue(String name, Date defaultValue){
		String strValue = strValue(name);

		if(!StringUtils.isEmpty(strValue)){
			try {
				return dateformat.parse( strValue );
			} catch (ParseException e) {
				return defaultValue;
			}
		}

		return defaultValue;
	}

	public Iterable<String> keys(){
		strValue("");
		return fieldDict.keySet();
	}

	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		boolean isFirst = true;
		for(String k : keys()){
			if(isFirst){
				isFirst = false;
			}else{
				sb.append(',');
			}
			sb.append(k);
			sb.append(':');
			sb.append(strValue(k));
		}
		return sb.toString();
	}
}
