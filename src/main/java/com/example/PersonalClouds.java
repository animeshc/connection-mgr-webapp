package com.example;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.PathParam;
import javax.ws.rs.*;
import clouds.client.basic.*;
import java.util.ArrayList;
import xdi2.core.*;
import xdi2.messaging.*;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3Statement;
import java.util.StringTokenizer;
import java.util.UUID;
import xdi2.core.impl.memory.MemoryGraph;
import xdi2.core.util.iterators.ReadOnlyIterator;
import javax.ws.rs.core.Context;
//import org.apache.cxf.jaxrs.ext.MessageContext;
import javax.servlet.http.HttpServletRequest;
//import org.mortbay.jetty.Request;
import java.util.List;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import java.util.Hashtable;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.io.UnsupportedEncodingException;

/**
 * Root resource (exposed at "personalclouds" path)
 */
@Path("personalclouds")
public class PersonalClouds {

	//@Context
    //private org.apache.cxf.jaxrs.ext.MessageContext mc; 
    //@Context
    //private ServletContext sc;
    //private UriInfo ui;
	 @Context
	 HttpHeaders headers;

	 private static Hashtable<String,String> passwordMap = new Hashtable<String,String>();

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
/*
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getCloudNumber(@PathParam("cloudname") String cloudName) {
        return "For CloudName :" + cloudName + "\n";
    }
*/
  @GET 
  @Produces(MediaType.TEXT_PLAIN)
  public String getListOfCloudNames() {
	return "Not implemented yet !\n";
	}

  @GET @Path("{cloudname}")
  @Produces(MediaType.TEXT_PLAIN)
  public String getInfo(@PathParam("cloudname") String cloudName,
										@DefaultValue("") @QueryParam("auth_token") String auth_token) 
	{
	String values = "";
	values += "{";
	PersonalCloud myPC = PersonalCloud.open(XDI3Segment.create(cloudName), auth_token , PersonalCloud.XRI_S_DEFAULT_LINKCONTRACT, "");
   String cloudNumber = myPC.getCloudNumber().toString();
	Graph g = myPC.getWholeGraph();
       ContextNode root = g.getRootContextNode();
       ReadOnlyIterator<Literal> literals = root.getAllLiterals();
       while(literals.hasNext()){
          Literal literal = literals.next();
			 String litValue = literal.getContextNode().toString();
			 if (litValue.indexOf(cloudNumber) != -1) {
				litValue = litValue.substring(cloudNumber.length()); 
				litValue = litValue.substring(0,litValue.length() -1 );
          	values += "\"" + litValue + "\"" + ":" ;
          	values += "\"" + literal.getLiteralDataString()+ "\""  ;
          	values += ",";
			 }
       }
	values = values.substring(0,values.length() -1);
	values += "}";
	return values;
	}
	@PUT @Path("{cloudname}")
  @Produces(MediaType.TEXT_PLAIN)
  public String setInfo(@PathParam("cloudname") String cloudName,
                              @DefaultValue("") @QueryParam("auth_token") String auth_token,
									@DefaultValue("") @QueryParam("names") String names ,
									@DefaultValue("") @QueryParam("values") String values	)
   {
		PersonalCloud myPC = PersonalCloud.open(XDI3Segment.create(cloudName), auth_token , PersonalCloud.XRI_S_DEFAULT_LINKCONTRACT, "");
		String cloudNumber = myPC.getCloudNumber().toString();
   	ArrayList<XDI3Statement> setStmts = new ArrayList<XDI3Statement>();
		StringTokenizer nameTokens = new StringTokenizer(names,",");
		StringTokenizer valueTokens = new StringTokenizer(values,",");
		while(nameTokens.hasMoreElements()){
			String nameToken = nameTokens.nextElement().toString();
			String valueToken = valueTokens.nextElement().toString();
			XDI3Statement setStmt = XDI3Statement.create(cloudNumber + nameToken + "&/&/\"" + valueToken + "\"");
			setStmts.add(setStmt);
		}
   	MessageResult result = myPC.setXDIStmts(setStmts);
   	String responseStr = result.toString();

		return responseStr;
	}

  @GET @Path("{cloudname}/friends")
  @Produces(MediaType.TEXT_PLAIN)
  public String getFriendsList(@PathParam("cloudname") String cloudName,
                              @DefaultValue("") @QueryParam("auth_token") String auth_token)
   {
   String responseStr = "Not implemented yet!\n" ;
   return responseStr;
   }
/*
 * create a new friend relationship
 */
  @POST @Path("{cloudname}/friends/{friend}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String addFriend(@PathParam("cloudname") String cloudName,
										@PathParam("friend") String friend,
                              @DefaultValue("") @QueryParam("auth_token") String auth_token)
   {
   String friendCloudNumber = PersonalCloud.findCloudNumber(friend,"");
	PersonalCloud myPC = PersonalCloud.open(XDI3Segment.create(cloudName), auth_token , PersonalCloud.XRI_S_DEFAULT_LINKCONTRACT, "");
   String cloudNumber = myPC.getCloudNumber().toString();
	ArrayList<XDI3Statement> setStmts = new ArrayList<XDI3Statement>();
	setStmts.add(XDI3Statement.create(cloudNumber + "/+friend/" + friendCloudNumber));
	MessageResult result = myPC.setXDIStmts(setStmts);
   String responseStr = result.toString();
   return responseStr;
   }
/*
 *  * delete a friend relationship
 *   */
  @DELETE @Path("{cloudname}/friends/{friend}")
  @Produces(MediaType.TEXT_PLAIN)
  public String deleteFriend(@PathParam("cloudname") String cloudName,
                              @PathParam("friend") String friend,
                              @DefaultValue("") @QueryParam("auth_token") String auth_token)
   {
   String cloudNumber = PersonalCloud.findCloudNumber(cloudName,"");
   String friendCloudNumber = PersonalCloud.findCloudNumber(friend,"");
   PersonalCloud myPC = PersonalCloud.open(XDI3Segment.create(cloudName), auth_token , PersonalCloud.XRI_S_DEFAULT_LINKCONTRACT, "");
   ArrayList<XDI3Statement> delStmts = new ArrayList<XDI3Statement>();
   delStmts.add(XDI3Statement.create(cloudNumber + "/+friend/" + friendCloudNumber));
   MessageResult result = myPC.delXDIStmts(delStmts,null);
   String responseStr = result.toString();
   return responseStr;
   }
/*
 * Read a named profile from a friend's personal cloud
 */
	@GET @Path("{cloudname}/friends/{friend}/profiles/{profile}")
  @Produces(MediaType.TEXT_PLAIN)
  public String getProfileFromFriend(@PathParam("cloudname") String cloudName,
                              @PathParam("friend") String friend,
                              @PathParam("profile") String profile,
                              @DefaultValue("") @QueryParam("auth_token") String auth_token)
   {
   PersonalCloud myPC = PersonalCloud.open(XDI3Segment.create(cloudName), auth_token , PersonalCloud.XRI_S_DEFAULT_LINKCONTRACT, "");
   String cloudNumber = myPC.getCloudNumber().toString() ; //PersonalCloud.findCloudNumber(cloudName,"");
	PersonalCloud friendPC = PersonalCloud.open(XDI3Segment.create(friend),myPC.getCloudNumber(),XDI3Segment.create("$public$do"), "");
   String friendCloudNumber = friendPC.getCloudNumber().toString() ; //PersonalCloud.findCloudNumber(friend,"");
	XDI3Segment link_contract = XDI3Segment.create(friendCloudNumber + "+friend$do");
	friendPC.setLinkContractAddress(link_contract);
   String values = new String("{");

       XDI3Segment query = XDI3Segment.create(friendCloudNumber + "+profiles" + "[<+" + profile + ">]");
       MessageResult result = friendPC.getXDIStmts(query,false);

       MemoryGraph response = (MemoryGraph) result.getGraph();
       ContextNode root = response.getRootContextNode();
       ReadOnlyIterator<Literal> literals = root.getAllLiterals();
       while(literals.hasNext()){
          Literal literal = literals.next();
          String litValue = literal.getContextNode().toString();
          litValue = litValue.substring(cloudNumber.length());
          litValue = litValue.substring(0,litValue.length() -1 );
          values += "\"" + litValue + "\""   + ":" ;
          values += "\"" + literal.getLiteralDataString() + "\"";
          values += ",";
       }
	values = values.substring(0,values.length()-1);
	values += "}";
   return values;

   }

/*
 * get a list of all profiles
 */
  @GET @Path("{cloudname}/profiles")
  @Produces(MediaType.TEXT_PLAIN)
  public String getListOfProfiles(@PathParam("cloudname") String cloudName,
                              @DefaultValue("") @QueryParam("auth_token") String auth_token)
   {
   String responseStr = "Not implemented yet!" + "\n" ;
   return responseStr;
   }
/*
 * create a new profile
 */
  @POST @Path("{cloudname}/profiles/{profile}")
  @Produces(MediaType.TEXT_PLAIN)
  public String addProfile(@PathParam("cloudname") String cloudName,
										@PathParam("profile") String profile,
                              @DefaultValue("") @QueryParam("auth_token") String auth_token,
                              @DefaultValue("") @QueryParam("fields") String fields
										)
   {
   String cloudNumber = PersonalCloud.findCloudNumber(cloudName,"");
	PersonalCloud myPC = PersonalCloud.open(XDI3Segment.create(cloudName), auth_token , PersonalCloud.XRI_S_DEFAULT_LINKCONTRACT, "");
	StringTokenizer tokens = new StringTokenizer(fields,",");
	ArrayList<XDI3Statement> setStmts = new ArrayList<XDI3Statement>();
	setStmts.add(XDI3Statement.create(cloudNumber + "/()/" + "+" + profile));
	while (tokens.hasMoreElements()) {
		String fieldName = tokens.nextElement().toString();
		String reqUUID = "<!:uuid:" + UUID.randomUUID().toString() + ">";
		setStmts.add(XDI3Statement.create(cloudNumber + "+profiles" + "[<+" + profile + ">]" + reqUUID + "/$ref/" + cloudNumber  + fieldName ));
		}
	MessageResult result = myPC.setXDIStmts(setStmts);
   String responseStr = result.toString();
   return responseStr;
   }
/*
 *  delete a profile
 */
  @DELETE @Path("{cloudname}/profiles/{profile}")
  @Produces(MediaType.TEXT_PLAIN)
  public String deleteProfile(@PathParam("cloudname") String cloudName,
                              @PathParam("profile") String profile,
                              @DefaultValue("") @QueryParam("auth_token") String auth_token
                              )
   {
   String cloudNumber = PersonalCloud.findCloudNumber(cloudName,"");
   PersonalCloud myPC = PersonalCloud.open(XDI3Segment.create(cloudName), auth_token , PersonalCloud.XRI_S_DEFAULT_LINKCONTRACT, "");
   ArrayList<XDI3Statement> delStmts = new ArrayList<XDI3Statement>();
   delStmts.add(XDI3Statement.create(cloudNumber + "+profiles" + "/()/" + "[<+" + profile + ">]"));
   MessageResult result = myPC.delXDIStmts(delStmts,null);
   String responseStr = result.toString();
   return responseStr;
   }
/*
 * update access to a profile
 */

  @PUT @Path("{cloudname}/profiles/{profile}")
  @Produces(MediaType.TEXT_PLAIN)
  public String groupAccessToProfile(@PathParam("cloudname") String cloudName,
                              @PathParam("profile") String profile,
                              @DefaultValue("") @QueryParam("auth_token") String auth_token,
                              @DefaultValue("") @QueryParam("group_access") String group_access
                              )
   {
   PersonalCloud myPC = PersonalCloud.open(XDI3Segment.create(cloudName), auth_token , PersonalCloud.XRI_S_DEFAULT_LINKCONTRACT, "");
   String cloudNumber = myPC.getCloudNumber().toString();
   ArrayList<XDI3Statement> stmts = new ArrayList<XDI3Statement>();
	MessageResult result = null;
	if (group_access.startsWith("+")){
   	stmts.add(XDI3Statement.create(cloudNumber + group_access + "$do" +  "/$get/" + cloudNumber + "+profiles" + "[<+" + profile + ">]"));
   	result = myPC.setXDIStmts(stmts);
	} 
	if (group_access.startsWith("-")){
   	stmts.add(XDI3Statement.create(cloudNumber + group_access + "$do" +  "/$get/" + cloudNumber + "+profiles" + "[<+" + profile + ">]"));
   	result = myPC.delXDIStmts(stmts,null);
	} 

   String responseStr = result.toString();
   return responseStr;
   }

/*
 * retrieve all data elements in the named profils
 */
  @GET @Path("{cloudname}/profiles/{profile}")
  @Produces(MediaType.TEXT_PLAIN)
  public String getProfile(@PathParam("cloudname") String cloudName,
  									@PathParam("profile") String profile,
                              @DefaultValue("") @QueryParam("auth_token") String auth_token,
										@DefaultValue("") @QueryParam("link_contract") String link_contract)
   {
   PersonalCloud myPC = PersonalCloud.open(XDI3Segment.create(cloudName), auth_token , PersonalCloud.XRI_S_DEFAULT_LINKCONTRACT, "");
   String cloudNumber = myPC.getCloudNumber().toString();
	String values = new String("{");
		
		 XDI3Segment query = XDI3Segment.create(cloudNumber + "+profiles" + "[<+" + profile + ">]");
		 MessageResult result = myPC.getXDIStmts(query,true);
		 
		 MemoryGraph response = (MemoryGraph) result.getGraph();
		 ContextNode root = response.getRootContextNode();
		 ReadOnlyIterator<Literal> literals = root.getAllLiterals();
		 while(literals.hasNext()){
			 Literal literal = literals.next();
			 String litValue = literal.getContextNode().toString();
          litValue = litValue.substring(cloudNumber.length());
          litValue = litValue.substring(0,litValue.length() -1 );
			 values += "\"" + litValue +"\"" + ":" ;
			 values += "\"" + literal.getLiteralDataString() + "\"";
			 values += ",";
		 }
	values = values.substring(0,values.length()-1);
	values += "}";
   return values;
   }

	/*
	*
	*/
	@POST @Path("{cloudnumber}/connect/request")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String getLoginForm(@PathParam("cloudnumber") String respondingPartyCloudNumber ,
										@FormParam("xdimessage") String connectRequest,
										@FormParam("discoverykey") String respondingPartyCloudName,
										@FormParam("returnurl") String successurl,
										@FormParam("returnurl") String failureurl,
										@FormParam("relayState") String relayState)
	{
		//PersonalCloud pc = PersonalCloud.open(XDI3Segment.create(respondingPartyCloudNumber),XDI3Segment.create("$anon"),XDI3Segment.create("$public$do"),"");	
		System.out.println("\n\n\nConnect Request endpoint -> connect/request\n\n\n");
		if(respondingPartyCloudNumber == null || connectRequest == null || respondingPartyCloudName == null || successurl == null ){
			return new String("<html><body>All mandatory parameters in the request are not provided. These are <b>cloudnumber</b> , <b>xdimessage</b> , <b>discoverykey</b> and <b>returnurl</b></body></html>");
		}
		String secrettoken = null;
        for (Cookie c : headers.getCookies().values()) {
            System.out.println("Cookie Name: " + c.getName());
            System.out.println("Cookie value: " + c.getValue());
				if (passwordMap.get(c.getValue()) != null){
					secrettoken = passwordMap.get(c.getValue());
					break;
				}
        }
		if(secrettoken != null){
			PersonalCloud pc = PersonalCloud.open(XDI3Segment.create(respondingPartyCloudNumber),secrettoken,XDI3Segment.create("$do"),"");
			if (pc.linkContractExists(connectRequest)) {
				return pc.autoSubmitForm(respondingPartyCloudName,connectRequest,successurl,relayState);
			}
			else {
				return pc.showApprovalForm(connectRequest, respondingPartyCloudNumber, secrettoken,successurl,failureurl,respondingPartyCloudName,relayState);
			}
		}
		else {
			PersonalCloud pc = PersonalCloud.open(XDI3Segment.create(respondingPartyCloudNumber),XDI3Segment.create(respondingPartyCloudNumber),XDI3Segment.create("$public$do"),"");	
			return pc.showAuthenticationForm(connectRequest,respondingPartyCloudName,respondingPartyCloudNumber,successurl,failureurl,relayState);
		}
	}

	@POST @Path("{cloudnumber}/connect/authorize")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)

	public Response getApprovalForm(@PathParam("cloudnumber") String respondingPartyCloudNumber ,
											@FormParam("connectRequest") String connectRequest,
											@FormParam("secrettoken") String in_secrettoken,
											@FormParam("successurl") String successurl,
											@FormParam("failureurl") String failureurl,
											@FormParam("relayState") String relayState,
											@FormParam("cloudname") String cloudname)
	{
		System.out.println("\n\n\nConnect Authorize endpoint -> connect/authorize\n\n\n");
			String resultHTML = new String("<html>Hello World!</html>");
		  String secrettoken = null;
        for (Cookie c : headers.getCookies().values()) {
            System.out.println("Cookie Name: " + c.getName());
            System.out.println("Cookie value: " + c.getValue());
				if (c.getName().equals("RC_SESSIONID") && passwordMap.get(c.getValue()) != null){
					secrettoken = passwordMap.get(c.getValue());
					break;
				}
        }
		  if(secrettoken == null){
         System.out.println("No cookie found, secrettoken is null!");
			if (in_secrettoken == null || in_secrettoken.isEmpty()) {
		  		PersonalCloud pc = PersonalCloud.open(XDI3Segment.create(respondingPartyCloudNumber),XDI3Segment.create(respondingPartyCloudNumber),XDI3Segment.create("$public$do"),"");
         	System.out.println("No password provided either, showing auth form!");
				resultHTML = pc.showAuthenticationForm(connectRequest,cloudname,respondingPartyCloudNumber,successurl,failureurl,relayState);
      		InputStream entity = null;
      		try{
         		entity = new ByteArrayInputStream( resultHTML.getBytes( "UTF-8" ) );
      		}catch(UnsupportedEncodingException ex){
        			System.out.println("UnsupportedEncodingException");
      		}
				return Response.ok(entity).build();
			}
			else {
				secrettoken = in_secrettoken;
			}
		}
		System.out.println("secrettoken is not null. It is " + secrettoken);
		Date now = new Date();
		PersonalCloud pc = PersonalCloud.open(XDI3Segment.create(respondingPartyCloudNumber),secrettoken,XDI3Segment.create("$do"),"");
		if (pc.linkContractExists(connectRequest)) {
			resultHTML = pc.autoSubmitForm(cloudname,connectRequest,successurl,relayState);
		}
		else {
			resultHTML =  pc.showApprovalForm(connectRequest, respondingPartyCloudNumber, secrettoken,successurl,failureurl,cloudname,relayState);
		}
		InputStream entity = null;
		try{
			entity = new ByteArrayInputStream( resultHTML.getBytes( "UTF-8" ) );
		}catch(UnsupportedEncodingException ex){
			System.out.println("UnsupportedEncodingException");
	   }
		passwordMap.put(respondingPartyCloudNumber,secrettoken);
		System.out.println("Putting in map .. name :" + respondingPartyCloudNumber + ", value: " + secrettoken);
		return Response.ok(entity).cookie(new NewCookie("RC_SESSIONID",respondingPartyCloudNumber,"/","neustar.biz","Cookie from Neustar COnnection Manager",3600,false)).build();
	}

	@POST @Path("{cloudnumber}/connect/approve/")
   @Produces(MediaType.TEXT_HTML)
   @Consumes(MediaType.APPLICATION_FORM_URLENCODED)

   public String processAprovalForm(@PathParam("cloudnumber") String respondingPartyCloudNumber ,
                                 @FormParam("linkContractTemplateAddress") String linkContractTemplateAddress,
											@FormParam("connectRequest") String connectRequest,
                                 @FormParam("relyingPartyCloudNumber") String relyingPartyCloudNumber,
                                 //@FormParam("authToken") String secrettoken,
											@FormParam("successurl") String successurl,
											@FormParam("failureurl") String failureurl,
											@FormParam("cloudname") String cloudname,
											@FormParam("relayState") String relayState,
											@FormParam("buttonClicked") String buttonClicked,
											@FormParam("fieldchoices") List<String> fieldChoices)
                                 
	{
		//HttpServletRequest request = mc.getHttpServletRequest();
		//String [] selectedValues = request.getParameterValues("fieldchoices");
		System.out.println("\n\n\nConnect Approve endpoint -> connect/approve\n\n\n");
		System.out.println("Clicked="+buttonClicked);
		//System.out.println(fieldChoices);
		System.out.println("All passwords:\n" + passwordMap + "\n");
		String secrettoken = null;
        for (Cookie c : headers.getCookies().values()) {
            System.out.println("Cookie Name: " + c.getName());
            System.out.println("Cookie value: " + c.getValue());
				if (passwordMap.get(c.getValue()) != null){
					secrettoken = passwordMap.get(c.getValue());
					break;
				}
        }
        if(secrettoken == null){
         PersonalCloud pc = PersonalCloud.open(XDI3Segment.create(respondingPartyCloudNumber),XDI3Segment.create(respondingPartyCloudNumber),XDI3Segment.create("$public$do"),"");
         return pc.showAuthenticationForm(connectRequest,cloudname,respondingPartyCloudNumber,successurl,failureurl,relayState);
        }

		PersonalCloud pc = PersonalCloud.open(XDI3Segment.create(respondingPartyCloudNumber),secrettoken,XDI3Segment.create("$do"),"");
		if (buttonClicked != null && buttonClicked.equals("Reject")) {
			return pc.autoSubmitRejectForm(cloudname,connectRequest,failureurl,relayState);
		}
		String [] selectedValues = fieldChoices.toArray(new String[fieldChoices.size()]);
		return pc.processApprovalForm(linkContractTemplateAddress, relyingPartyCloudNumber, respondingPartyCloudNumber, secrettoken,selectedValues,successurl,failureurl,cloudname,relayState);
		//return new String("<html><body>Hello World!</body></html>");
	}
  @GET @Path("/disconnect/admin/")
  @Produces(MediaType.TEXT_HTML)
  public String showDisconnectForm() {
	String result = new String("<html><head>");
	result += "<script language=\"JavaScript\">";
	result += "function buttonClick(val){ document.getElementById(\"buttonClicked\").value = val; } ";
	result += "</script></head><body><div>";
	result += "<form action=\"http://mycloud.neustar.biz:8080/myapp/personalclouds/disconnect/process/\" name=\"adminForm\" method=\"POST\">"; 
	result += "Connection to (ACME/Bob) cloud number ( [@]!:uuid:e0178407-b7b6-43f9-e017-8407b7b643f9 ) : <input type=\"text\" name=\"requestingparty\"  /> <br>";
	result += "Connection from (Alice)cloud number ( [=]!:uuid:0707f2ff-4266-9f14-0707-f2ff42669f14 ) : <input type=\"text\" name=\"respondingparty\"  /> <br>";
	result += "(Alice's) password : <input type=\"password\" name=\"secrettoken\"  /> <br>";
	result += "<input type=\"hidden\" name=\"bClicked\" id=\"buttonClicked\"  /> <br>";
	result += "<input type=\"submit\" value=\"Disconnect\" onclick=\" buttonClick('Disconnect'); return true;\"/>";
	result += "<input type=\"submit\" value=\"Logout\" onclick=\" buttonClick('Logout'); return true;\"/>";
	result += "</form></div></body></html>";

   return result;
   }
	@POST @Path("/disconnect/process/")
	@Produces(MediaType.TEXT_HTML)
	public String processDisconnect(@FormParam("requestingparty") String requestingparty,
												@FormParam("respondingparty") String respondingparty,
												@FormParam("bClicked") String buttonClicked,
												@FormParam("secrettoken") String secrettoken)
	{
		PersonalCloud pc = PersonalCloud.open(XDI3Segment.create(respondingparty),secrettoken,XDI3Segment.create("$do"),"");
		System.out.println("Clicked="+buttonClicked);
		if(buttonClicked != null && buttonClicked.equals("Disconnect")) {
			return pc.processDisconnectRequest(requestingparty,respondingparty);
		} else {
			if (respondingparty != null) {
				passwordMap.remove(respondingparty);
			} else {
				System.out.println("respondingparty is not provided!");
			}
			return new String("<html><body>Session Cache has been removed for the user</body></html>");
		}
	}


}
