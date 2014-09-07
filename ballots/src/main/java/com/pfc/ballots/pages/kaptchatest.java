package com.pfc.ballots.pages;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;

public class kaptchatest {
	
	@Property
	private boolean captchaValid;
	 
	@OnEvent(value = "validate", component = "myForm")
	void validate(){
	   if(!captchaValid){
	      System.out.println("NOT MATCH");
	   }
	   else
		   System.out.println("MATCH");
	}
}
