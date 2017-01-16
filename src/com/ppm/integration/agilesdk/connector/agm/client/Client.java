package com.ppm.integration.agilesdk.connector.agm.client;

import com.ppm.integration.agilesdk.connector.agm.model.Credential;
import com.ppm.integration.agilesdk.connector.agm.model.Domain;
import com.ppm.integration.agilesdk.connector.agm.model.Domains;
import com.ppm.integration.agilesdk.connector.agm.model.Projects;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Audits;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.Entities;
import com.ppm.integration.agilesdk.connector.agm.model.jaxb.EntityComplexType;
import com.kintana.core.logging.LogManager;
import com.kintana.core.logging.Logger;
import com.mercury.itg.core.ContextFactory;
import com.mercury.itg.core.model.Context;
import com.mercury.itg.core.user.model.User;
import org.apache.commons.lang.StringUtils;
import org.apache.wink.client.*;
import org.apache.wink.client.handlers.ClientHandler;
import org.apache.wink.client.handlers.HandlerContext;

import javax.ws.rs.core.Application;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Client {
    protected static final Logger logger = LogManager.getLogger("com.hp.ppm.integration.AGM");
    private User _currentUser = null;
	private static final DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");

    private static final int PAGE_SIZE = 100;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(3);


    public void setCurrentUser(User user) {
    	_currentUser = user;
    }

    protected void debug(String agmURL) {
        StringBuffer sb = new StringBuffer();
        if(_currentUser != null) {
            sb.append("(user ID=");
            sb.append(_currentUser.getUserId());
            sb.append("; user name=");
            sb.append(_currentUser.getUsername());
            sb.append(")");
        }

        sb.append(agmURL);
        logger.error(sb.toString());
    }

	protected String baseURL = "https://agilemanager-ast.saas.hp.com";

	private List<String> cookieLWSSO = null;

	private ClientConfig config;

	public Client(String baseURL){
        try {
            Context ctx = ContextFactory.getThreadContext();
            _currentUser = (User)ctx.get(Context.USER);
        } catch(Exception ex) {
            // if error, do nothing
        }

		this.baseURL = baseURL.trim();
		if(this.baseURL.endsWith("/")){
			this.baseURL = this.baseURL.substring(0, this.baseURL.length()-1);
		}


		this.config = new ClientConfig().handlers(new ClientHandler(){

			@Override
			public ClientResponse handle(ClientRequest req, HandlerContext context)
					throws Exception {

				req.getHeaders().add("Content-Type", "application/xml");
				req.getHeaders().add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

				URI origURI = req.getURI();
				if(!isAuthURI(origURI)){
					StringBuffer sb = new StringBuffer();
					sb.append(origURI.toString());
					if(StringUtils.isEmpty(origURI.getQuery())){
						sb.append('?');
					}else{
						sb.append('&');
					}

					sb.append("alt=application/xml");
					sb.append("&TENANTID=0");
					sb.append("&bypass-cache=y");
					req.setURI(URI.create(sb.toString()));
					req.getHeaders().put("Cookie", cookieLWSSO);

					//System.out.println(cookieLWSSO);
				}

				debug(req.getURI().toString());


				return context.doChain(req);
			}
		})
		.applications(new Application() {
			@Override
			public Set<Class<?>> getClasses() {
				Set<Class<?>> clazz = new HashSet<Class<?>>();
				clazz.add(JAXBProvider.class);
				return clazz;
			}
        });
	}

	public Client proxy(String host, int port){
		this.config.proxyHost(host).proxyPort(port);
		return this;
	}

	public Client auth(String username, String password){

		Credential c = new Credential();
		c.user = username;
		c.password = password;

		ClientResponse resp = null;
		resp = this.oneResource(AUTHORIZATION_SIGN_IN_URL).post(c);

		if(resp.getStatusCode()!=200){
			throw new AgmClientException("AGM_API","ERROR_AUTHENTICATION_FAILED");
		}

		this.cookieLWSSO = resp.getHeaders().get("Set-Cookie");
		return this;
	}

	public Client auth(List<String> cookies){
		this.cookieLWSSO = cookies;
		return this;
	}

	public List<String> getCookies(){
		return this.cookieLWSSO;
	}

	protected String getURLParamNameForFieldQuery(){
		return "query";
	}

	protected String getURLParamNameForVisibleFields(){
		return "fields";
	}

    public Resource oneResource(String url, FieldQuery... queries){
        return oneResource(url, null, queries);
    }

    public Resource oneResource(String url, String[] fields, FieldQuery... queries){

		Resource rsc = new RestClient(this.config).resource(baseURL + url);

		if(fields!=null && fields.length > 0){
			rsc.queryParam(getURLParamNameForVisibleFields(),StringUtils.join(fields, ','));
		}

		if(queries.length > 0){
			StringBuffer sb = new StringBuffer();
			sb.append("%7B");
			boolean isFirst = true;
			for(FieldQuery q : queries){
				if(isFirst){
					isFirst = false;
				}else{
					sb.append(';');
				}

				sb.append(q.toQueryString());
			}
			sb.append("%7D");

			rsc.queryParam(getURLParamNameForFieldQuery(), sb.toString());
		}

		return rsc;
	}

    public Resource allEntityResource(String url, FieldQuery... queries){
        return allEntityResource(url, null, queries);
    }

    public Resource allEntityResource(final String url, final String[] fields, final FieldQuery... queries){

        final List<MethodInvoke> methodInvokes = new LinkedList<MethodInvoke>();
        final Resource[] proxyResource = new Resource[1];

        proxyResource[0] = (Resource) Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{Resource.class},
                new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method,
                                         Object[] args) throws Throwable {

                        if (method.getReturnType().equals(Resource.class)) {

                            // queryParam "page-size" / "start-index" are not supported
                            if (!("queryParam".equals(method.getName())
                                    && ("page-size".equals(args[0].toString())
                                    || "start-index".equals(args[0].toString()))
                            )) {
                                methodInvokes.add(new MethodInvoke(method, args));
                                return proxyResource[0];
                            }

                        } else if ("get".equals(method.getName()) && args.length == 1 && args[0].equals(Entities.class)) {

                            Resource rsc = oneResource(url, fields, queries);
                            for (MethodInvoke log : methodInvokes) {
                                log.method.invoke(rsc, log.args);
                            }

                            final Entities kickoff = rsc.get(Entities.class);

                            final int totalResults = kickoff.getTotalResults();
                            if (totalResults > kickoff.getEntity().size()) {
                                final List<EntityComplexType> allResults = new LinkedList<EntityComplexType>();
                                allResults.addAll(kickoff.getEntity());

                                List<Future<Entities>> tasks = new LinkedList<Future<Entities>>();

                                int startAt = kickoff.getEntity().size() + 1; // Start at 1
                                while (startAt <= totalResults) {
                                    final int myStartAt = startAt;
                                    tasks.add(executorService.submit(new Callable<Entities>() {

                                        @Override
                                        public Entities call() throws Exception {

                                            Resource rsc = oneResource(url, fields, queries);
                                            for (MethodInvoke log : methodInvokes) {
                                                log.method.invoke(rsc, log.args);
                                            }

                                            return rsc
                                                    .queryParam("start-index", myStartAt)
                                                    .queryParam("page-size", PAGE_SIZE)
                                                    .get(Entities.class);
                                        }
                                    }));

                                    startAt += PAGE_SIZE;
                                }

                                for (Future<Entities> res : tasks) {
                                    allResults.addAll(res.get().getEntity());
                                }

                                return new Entities() {
                                    @Override
                                    public int getTotalResults() {
                                        return kickoff.getTotalResults();
                                    }

                                    @Override
                                    public List<EntityComplexType> getEntity() {
                                        return allResults;
                                    }
                                };
                            } else {
                                return kickoff;
                            }
                        }


                        throw new java.lang.IllegalAccessException(String.format("Illegal Access To Method \"%s\"", method.getName()));
                        //InvocationTargetException ite
                    }
                });

        return proxyResource[0];
    }


    static class MethodInvoke {
        Method method;
        Object[] args;

        MethodInvoke(Method m, Object[] args){
            this.method = m;
            this.args = args;
        }
    }

	protected boolean isAuthURI(URI uri){
		String path = uri.getPath();
		return AUTHORIZATION_SIGN_IN_URL.equals(path) || AUTHORIZATION_SIGN_OUT_URL.equals(path);
	}


	public static final String AUTHORIZATION_SIGN_IN_URL  = "/qcbin/authentication-point/alm-authenticate";
	public static final String AUTHORIZATION_SIGN_OUT_URL = "/qcbin/authentication-point/logout";

	public List<Domain> getDomains(){
		Domains domains = oneResource("/qcbin/rest/domains").get(Domains.class);
		return domains.getCollection();
	}

	/*  workplan2 */
	public  Projects getProjects(String domainName)
	{
		return oneResource(String.format("/qcbin/rest/domains/%s/projects", domainName)).get(Projects.class);
	}

	public Entities getWorkSpaces(String domainName, String projectName)
	{
		return allEntityResource(String.format("/qcbin/rest/domains/%s/projects/%s/product-groups", domainName, projectName))
				.get(Entities.class);
	}

	public Entities getAllReleases(String domainName,String projectName, String workSpaceId)
	{
		return  allEntityResource(String.format("/qcbin/rest/domains/%s/projects/%s/releases", domainName, projectName)
                , new FieldQuery("product-group-id", ValueQuery.eq(workSpaceId)))
				.get(Entities.class);
	}

	public Entities getCurrentReleases(String domainName, String projectName, String workSpaceId)
	{
		String nowDate = dateformat.format(new Date());

		return  allEntityResource(String.format("/qcbin/rest/domains/%s/projects/%s/releases", domainName, projectName)
                , new FieldQuery("start-date", ValueQuery.lte(nowDate))
                , new FieldQuery("end-date", ValueQuery.gte(nowDate))
                , new FieldQuery("product-group-id", ValueQuery.eq(workSpaceId)))
			.get(Entities.class);
	}

	public Entities getReleases(String domainName, String projectName, String workSpaceId)
	{
		return  allEntityResource(String.format("/qcbin/rest/domains/%s/projects/%s/releases", domainName, projectName)
				, new FieldQuery("product-group-id", ValueQuery.eq(workSpaceId)))
				.get(Entities.class);
	}

	public Entities getReleaseById(String domainName,String projectName, String releaseId)
	{
		return allEntityResource(String.format("/qcbin/rest/domains/%s/projects/%s/releases", domainName, projectName)
                , new FieldQuery("id", ValueQuery.eq(releaseId)))
			.get(Entities.class);
	}

	public Entities getTasks(String domainName,String projectName, String releaseId)
	{
		return allEntityResource(String.format("/qcbin/rest/domains/%s/projects/%s/project-tasks", domainName, projectName)
                , new FieldQuery("release-backlog-item.release-id", ValueQuery.eq(releaseId))
                , new FieldQuery("release-backlog-item.is-leaf", ValueQuery.eq("Y")))
			.get(Entities.class);
	}

	public Entities getUserStoryByReleaseBlockItem(String domainName,String projectName, String releaseId)
	{
		return allEntityResource(String.format("/qcbin/rest/domains/%s/projects/%s/release-backlog-items", domainName, projectName)
                , new FieldQuery("release-id", ValueQuery.eq(releaseId))
                , new FieldQuery("is-leaf", ValueQuery.eq("Y")))
			.get(Entities.class);
	}

	public Entities getSprintsByParentId(String domain,String project, String releaseId)
	{
		return allEntityResource(String.format("/qcbin/rest/domains/%s/projects/%s/release-cycles", domain, project),
                new FieldQuery("parent-id", ValueQuery.val(releaseId)))
			.get(Entities.class);
	}


	/*  timesheet */
	public Entities getCurrentReleases(String domainName, String projectName)
	{
		String nowDate = dateformat.format(new Date());

		return  allEntityResource(String.format("/qcbin/rest/domains/%s/projects/%s/releases", domainName, projectName)
                , new FieldQuery("start-date", ValueQuery.lte(nowDate))
                , new FieldQuery("end-date", ValueQuery.gte(nowDate)))
			.get(Entities.class);
	}

	public Entities getReleases(String domainName, String projectName)
	{
		String nowDate = dateformat.format(new Date());

		return  allEntityResource(String.format("/qcbin/rest/domains/%s/projects/%s/releases", domainName, projectName))
				.get(Entities.class);
	}

	public List<EntityComplexType> getSprints(String domainName ,String projectName) {

		final Entities sprints = allEntityResource(String.format("/qcbin/rest/domains/%s/projects/%s/release-cycles", domainName, projectName))
			.get(Entities.class);

		return sprints.getEntity();
	}

	public List<EntityComplexType> getCompletedTasks(String domainName, String projectName, String userName) {

		final Entities tasks = allEntityResource(
                String.format("/qcbin/rest/domains/%s/projects/%s/project-tasks", domainName, projectName)
                , new String[]{"id", "status", "invested", "release-backlog-item.release-id", "release-backlog-item.sprint-id"}
                , new FieldQuery("status", ValueQuery.eq("Completed"))
                , new FieldQuery("assigned-to", ValueQuery.eq(userName))
                , new FieldQuery("release-backlog-item.is-leaf", ValueQuery.eq("Y")))
			.get(Entities.class);

		return tasks.getEntity();
	}


    public Audits getTaskAudits(String domainName, String projectName, String taskId) {

        return oneResource(
                String.format("/qcbin/rest/domains/%s/projects/%s/project-tasks/%s/audits", domainName, projectName, taskId)
        ).get(Audits.class);

    }

}
