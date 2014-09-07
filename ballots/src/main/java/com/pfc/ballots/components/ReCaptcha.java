package com.pfc.ballots.components;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentAction;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Request;



public class ReCaptcha {
	 @Parameter(defaultPrefix = BindingConstants.LITERAL, allowNull = false, required = true)
	    private String privateKey;
	 
	    @Parameter(defaultPrefix = BindingConstants.LITERAL, allowNull = false, required = true)
	    @Property
	    private String publicKey;
	 
	    @Parameter
	    private boolean valid;
	 
	    @Parameter
	    private String error;
	 
	    @Inject
	    private FormSupport formSupport;
	 
	    @Inject
	    private ComponentResources resources;
	 
	    @Inject
	    private Request request;
	 
	    @Inject
	    private HttpServletRequest servletRequest;
	 
	    private static final ComponentAction<ReCaptcha> PROCESS_SUBMISSION_ACTION = new
	        ProcessSubmissionAction();
	 
	    private static final String RECAPTCHA_RESPONSE_FIELD = "recaptcha_response_field";
	 
	    private static final String RECAPTCHA_CHALLENGE_FIELD = "recaptcha_challenge_field";
	 
	    private static final String VERIFY_URL = "http://www.google.com/recaptcha/api/verify";
	 
	    @SetupRender
	    void addProcessSubmissionAction() {
	        if(formSupport == null){
	           throw new RuntimeException(String.format(
	                 "Component %s must be enclosed by a Form component.",
	                    resources.getCompleteId()));
	        }
	 
	        formSupport.store(this, PROCESS_SUBMISSION_ACTION);
	    }
	 
	    private static class ProcessSubmissionAction implements ComponentAction<ReCaptcha> {
	   
	        public void execute(ReCaptcha component) {
	            component.processSubmission();
	        }
	    }
	 
	    private void processSubmission() {
	        String response = request.getParameter(RECAPTCHA_RESPONSE_FIELD);
	        String challenge = request.getParameter(RECAPTCHA_CHALLENGE_FIELD);
	 
	        valid = verifyResponse(challenge, response, servletRequest.getRemoteAddr());
	    }
	 
	    private boolean verifyResponse(String challenge, String response, String ip) {
	        Map<String, String> parameters = new HashMap<String, String>();
	 
	        parameters.put("privatekey", privateKey);
	        parameters.put("challenge", challenge);
	        parameters.put("response", response);
	        parameters.put("remoteip", ip);
	        error = post(parameters);
	 
	        return error == null;
	    }
	 
	    public String post(Map<String, String> parameters) {
	        try {
	            HttpURLConnection connection = 
	                (HttpURLConnection) new URL(VERIFY_URL).openConnection();
	            String data = "";
	 
	            for(String key : parameters.keySet()) {
	                data += key + "=" + parameters.get(key) + "&";
	            }
	 
	            connection.setDoInput(true);
	            connection.setDoOutput(true);
	            connection.setRequestMethod("POST");
	 
	            PrintWriter writer = new PrintWriter(connection.getOutputStream());
	 
	            writer.write(data);
	            writer.flush();
	 
	            BufferedReader reader = new BufferedReader(
	                new InputStreamReader(connection.getInputStream()));
	 
	            String status = reader.readLine();
	            String error = null;
	 
	            if("false".equalsIgnoreCase(status)) {
	                error = reader.readLine();
	            }
	            connection.disconnect();
	             
	            return error;
	 
	 
	        } catch(Exception ex) {
	            throw new RuntimeException("Could not post to : " + VERIFY_URL, ex);
	        }
	 
	    }
	}