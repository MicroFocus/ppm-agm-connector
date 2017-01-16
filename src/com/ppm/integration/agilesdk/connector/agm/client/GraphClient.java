package com.ppm.integration.agilesdk.connector.agm.client;

import com.ppm.integration.agilesdk.connector.agm.model.helper.EntityComplexTypeReader;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Entities;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.EntityComplexType;
import org.apache.commons.lang.StringUtils;

import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Attribute;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.GraphResult;

public class GraphClient extends Client {

	private final String domain;
	private final String project;
	private final String graphName;
	private EntityComplexType graphEntity = null;

	public GraphClient(String baseURL, String domain, String project, String graphName) {
		super(baseURL);

		this.domain = domain;
		this.project = project;
		this.graphName = graphName;
	}

	public EntityComplexType getGraphEntity(){
		if(this.graphEntity==null){
			Client metaClient = new Client(this.baseURL);
			if(this.proxyHost!=null || this.proxyPort > -1){
				metaClient.proxy(proxyHost, proxyPort);
			}
			metaClient.auth(this.getCookies());
			
			Entities entities = metaClient.oneResource(String.format("/qcbin/rest/domains/%s/projects/%s/analysis-items", this.domain, this.project)
					, new FieldQuery("name", ValueQuery.eq(graphName)))
					.get(Entities.class);

			for(EntityComplexType e : entities.getEntity()){
				this.graphEntity = e;
				break;
			}
		}
		return this.graphEntity;
	}

	public GraphResult getGraphResult(FieldQuery... queries){

		String graphId = new EntityComplexTypeReader(getGraphEntity()).strValue("id");
		if(!StringUtils.isEmpty(graphId)){
			GraphResult result = this.oneResource(String.format("/qcbin/rest/domains/%s/projects/%s/graphs/%s/result", this.domain,this.project,graphId), queries)
					.get(GraphResult.class);

			Attribute attrName = new Attribute();
			attrName.setName("GraphName");
			attrName.setValue(this.graphName);
			result.getDataTable().getMetadataAttributes().getAttribute().add(attrName);

			return result;
		}

		return null;
	}

	@Override
	protected String getURLParamNameForFieldQuery(){
		return "add-params";
	}

	@Override
	public Client proxy(String host, int port){
		super.proxy(host, port);
		this.proxyHost = host;
		this.proxyPort = port;
		return this;
	}
	
	private String proxyHost = null;
	private int proxyPort = -1;
}
