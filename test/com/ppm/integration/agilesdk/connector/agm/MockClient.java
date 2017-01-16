package com.ppm.integration.agilesdk.connector.agm;

import com.ppm.integration.agilesdk.connector.agm.client.Client;
import com.ppm.integration.agilesdk.connector.agm.client.EntitiesClient;
import com.ppm.integration.agilesdk.connector.agm.model.Domain;
import com.ppm.integration.agilesdk.connector.agm.model.Domains;
import com.ppm.integration.agilesdk.connector.agm.model.Project;
import com.ppm.integration.agilesdk.connector.agm.model.Projects;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.AuditComplexType;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Audits;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Entities;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.EntityComplexType;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.FieldComplexType;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.EntityComplexType.RelatedEntities;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.FieldsComplexType;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

public class MockClient extends EntitiesClient{
	
	public MockClient(String baseURL){
		super(baseURL);
	}
	
	public Client auth(String username, String password){
		return this;
	}
	public Client auth(List<String> cookies){
		return this;
	}
	public Client proxy(String host, int port){
		return this;
	}
	private FieldComplexType GetMyFieldComplexType(String key, String Value){
		class MyFieldComplexType extends FieldComplexType{
			public List<FieldComplexType.Value> SetValue(List<FieldComplexType.Value> vlist){
				return value=vlist;
			}
		}
		FieldComplexType.Value va1=new FieldComplexType.Value();
		va1.setAlias("Alias_test");
		va1.setReferenceValue("ReferenceValue_test");		
		va1.setValue(Value);
		ArrayList<FieldComplexType.Value> vlist=new ArrayList<FieldComplexType.Value>();
		vlist.add(va1);		
		MyFieldComplexType fieldc=new MyFieldComplexType();
		fieldc.setName(key);
		fieldc.SetValue(vlist);
		return fieldc;
		
	}
	private Entities SetEntities()
	{
		class MyEntity extends Entities{
			public List<EntityComplexType> setEntity(List<EntityComplexType> entityList){
				return entity=entityList;
			}
		}
		class MyFieldsComplexType extends FieldsComplexType{
			public List<FieldComplexType> SetField(List<FieldComplexType> listFiled){
				return field=listFiled;
			}
		}
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 01, 02);
		Date start_date=cal.getTime();
		cal.set(2016, 01, 12);
		Date end_date=cal.getTime();

		cal.set(2016, 01, 02);
		Date creat_date=cal.getTime();
		cal.set(2016, 01, 02);
		Date status_date = cal.getTime();
		SimpleDateFormat sp = new SimpleDateFormat("yyyy-MM-dd");

		ArrayList<FieldComplexType> fieldlist1 = new ArrayList<FieldComplexType>();
		fieldlist1.add(GetMyFieldComplexType("start-date",sp.format(start_date)));
		fieldlist1.add(GetMyFieldComplexType("end-date",sp.format(end_date)));
		fieldlist1.add(GetMyFieldComplexType("creation-date",sp.format(creat_date)));
		fieldlist1.add(GetMyFieldComplexType("name","lutian"));
		fieldlist1.add(GetMyFieldComplexType("id","12"));
		fieldlist1.add(GetMyFieldComplexType("entity-name","Lutian June"));
		fieldlist1.add(GetMyFieldComplexType("status","Done"));
		fieldlist1.add(GetMyFieldComplexType("status-date","status_date"));
		fieldlist1.add(GetMyFieldComplexType("assigned-to","Wang Lingyan"));
		fieldlist1.add(GetMyFieldComplexType("release-backlog-item-id","12"));
		fieldlist1.add(GetMyFieldComplexType("sprint-id","12"));
		fieldlist1.add(GetMyFieldComplexType("parent-id","12"));
		fieldlist1.add(GetMyFieldComplexType("release-id","12"));
		
		MyFieldsComplexType fcomplextype1=new MyFieldsComplexType();
		fcomplextype1.SetField(fieldlist1);
		
		EntityComplexType entity1=new EntityComplexType();
		entity1.setFields(fcomplextype1);
		
		class MyRelatedEntities extends RelatedEntities{
			public List<EntityComplexType.RelatedEntities.Relation> setRelation(List<EntityComplexType.RelatedEntities.Relation> relat) {
				return this.relation=relat;
			}
		}
		MyRelatedEntities reentities1=new MyRelatedEntities();
		ArrayList<EntityComplexType.RelatedEntities.Relation> relat= new ArrayList<EntityComplexType.RelatedEntities.Relation>();
		EntityComplexType.RelatedEntities.Relation r1=new EntityComplexType.RelatedEntities.Relation();
		r1.setAlias("release-backlog-item");
		r1.setEntity(entity1);
		relat.add(r1);
		reentities1.setRelation(relat);
		entity1.setRelatedEntities(reentities1);
		entity1.setType("EntityComplexType111");	
		
		ArrayList<EntityComplexType> entityList1 = new ArrayList<EntityComplexType>();
		entityList1.add(entity1);		
		MyEntity tasks = new MyEntity();
		tasks.setEntity(entityList1);
		tasks.setTotalResults(123);
		return tasks;	
	}
	public Entities getAllReleases(String domainName,String projectName, String workSpaceId)
	{
		return SetEntities();
	}
	public Entities getCurrentReleases(String domainName, String projectName)
	{
		return SetEntities();
	}
	public Entities getCurrentReleases(String domainName, String projectName, String workSpaceId)
	{
		return SetEntities();
	}
	public Entities getReleaseById(String domainName,String projectName, String releaseId)
	{
		return SetEntities();
	}
	public Entities getReleases(String domainName, String projectName)
	{
		return SetEntities();
	}
	public Entities getReleases(String domainName, String projectName, String workSpaceId)
	{
		return SetEntities();
	}
	public Entities getSprintsByParentId(String domain,String project, String releaseId)
	{
		return SetEntities();
	}
	public Entities getTasks(String domainName,String projectName, String releaseId)
	{
		return SetEntities();
	}

	public Entities getUserStoryByReleaseBlockItem(String domainName,String projectName, String releaseId)
	{
		return SetEntities();
	}
	public Entities getWorkSpaces(String domainName, String projectName)
	{
		return SetEntities();
	}
	public List<String> getCookies(){
		return Arrays.asList(new String[]{"parmeter1","PARMETER1","parmeter"});
	}
	public List<EntityComplexType> getCompletedTasks(String domainName, String projectName, String userName) 
	{
		return SetEntities().getEntity();
	}
	
	public List<EntityComplexType> getSprints(String domainName ,String projectName) 
	{
		return SetEntities().getEntity();
	}
	protected boolean isAuthURI(URI uri){
		return true;
	}
	
	public List<Domain> getDomains(){
		Domain domain1=new Domain();
		domain1.name="domain111";
//		Domain domain2=new Domain();
//		domain2.name="domain222";
		Domains domains = new Domains();
		domains.add(domain1);
//		domains.add(domain2);
		return domains.getCollection();
	}
	public  Projects getProjects(String domainName)
	{
		Project pro1=new Project();
		pro1.name="Project111";
//		Project pro2=new Project();
//		pro2.name="Project222";
		Projects pros = new Projects();
		pros.add(pro1);
//		pros.add(pro2);
		return pros;
	}
    public Audits getTaskAudits(String domainName, String projectName, String taskId)
    {
    	class MyAudits extends Audits{
    		MyAudits(){
    			audit=new ArrayList<AuditComplexType>();
			}
		}		
    	MyAudits audits = new MyAudits();
    	audits.setTotalResults(123);
        return audits;
    }	
}
