package com.pfc.ballots.pages.profile;

import java.util.Random;

import javax.inject.Inject;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Secure;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.Validate;

import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.dao.ProfileCensedInDao;
import com.pfc.ballots.dao.UserDao;
import com.pfc.ballots.data.DataSession;
import com.pfc.ballots.entities.Profile;
import com.pfc.ballots.entities.ProfileCensedIn;
import com.pfc.ballots.pages.Index;
import com.pfc.ballots.pages.SessionExpired;
import com.pfc.ballots.pages.UnauthorizedAttempt;
import com.pfc.ballots.pages.admin.AdminMail;
import com.pfc.ballots.util.Encryption;
import com.pfc.ballots.util.UUID;

/**
 * 
 * CreateProfile class is the controller for the CreateProfile page that
 * allows to create a new user
 * 
 * @author Mario Temprano Martin
 * @version 1.0 FEB-2014
 */
@Secure
public class CreateProfile {

	@SessionState
	private DataSession datasession;
	
	@Inject
    private ComponentResources componentResources;

	@Property
	@Persist(PersistenceConstants.FLASH)
	private String captcha1;
	@Property
	@Persist(PersistenceConstants.FLASH)
	private String captcha2;
	@Property 
	@Persist(PersistenceConstants.FLASH)
	private String captchaValue;
	
	@Property 
	@Persist(PersistenceConstants.FLASH)
	private boolean badSecurity;
	
	final String [] caracteresEspeciales={"!","¡","@","|","#","$","%","&","/","(",")","=","¿","?","*","+","-","_"};
	
	//****************************************Initialize DAO****************************//
	FactoryDao DB4O =FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
	@Persist
	UserDao dao;
	ProfileCensedInDao censedInDao;
	
	
	/**
	 *  Initialize data
	 */
	void setupRender() 
	{
		
		dao=DB4O.getUsuarioDao(datasession.getDBName());
		censedInDao=DB4O.getProfileCensedInDao(datasession.getDBName());
		if(!isnotFirstTime)
		{
			profile=new Profile();
			isnotFirstTime=true;
		}
		else if(!isnotPassOk && !isnotAvalible)
		{
			//componentResources.discardPersistentFieldChanges();
			//profile=new Profile();
		}
		if(profile==null || !badCaptcha || isnotPassOk || isnotAvalible)
		{
			System.out.println("ALALAL");
			profile=new Profile();
		}
		Random r=new Random();
		captcha1=String.valueOf(r.nextInt(101));
		r=new Random();
		captcha2=String.valueOf(r.nextInt(101));
		
	}
	//////////////////////////////////////////////////////// PAGE STUFF /////////////////////////////////////
	
	@Property
	@Persist
	private Profile profile;
	
	@Validate("required")
	@Property
	private String password;
	@Validate("required")
	@Property
	private String repeat;
	
	@Property
	@Persist(PersistenceConstants.FLASH)
	private boolean isnotPassOk;
	@Property
	@Persist(PersistenceConstants.FLASH)
	private boolean badCaptcha;

	@Property
	@Persist(PersistenceConstants.FLASH)
	private boolean isnotAvalible;
	
	@Persist
	private boolean isnotFirstTime;
	
	
	/**
	 * Stores a new users
	 */
	Object onSuccess()
	{
		dao=DB4O.getUsuarioDao(datasession.getDBName());
		
		if(isNumeric(captchaValue))
		{
			if(Integer.parseInt(captchaValue)!=Integer.parseInt(captcha1)+Integer.parseInt(captcha2))
			{
				badCaptcha=true;
			}
			else
			{
				badCaptcha=false;
			}
		}
		else
		{
			badCaptcha=true;
		}
		if(password==null || repeat==null)
		{
			
		}
		else if(!password.equals(repeat))
		{
			isnotPassOk=true;
		}
		
		if(dao.isProfileRegistred(profile.getEmail()))
		{
			isnotAvalible=true;
		}
		badSecurity=true;
		for(String car:caracteresEspeciales)
		{
			if(password.contains(car))
			{
				badSecurity=false;
			}
		}
		if(!isnotPassOk && !isnotAvalible && !badCaptcha && !badSecurity)
		{
			
			//Encryption password,and store in database
			censedInDao=DB4O.getProfileCensedInDao(datasession.getDBName());
		
			String encrypt=Encryption.getStringMessageDigest(password, Encryption.SHA1);
			profile.setPassword(encrypt);
			profile.setId(UUID.generate());
			profile.setRegDatetoActual();
			ProfileCensedIn censedIn=new ProfileCensedIn(profile.getId());
			censedInDao.store(censedIn);
			dao.store(profile);
			System.out.println("Nonull");
			componentResources.discardPersistentFieldChanges();
			
			return ProfileStored.class;
		}
		
		return null;
		
			
	}
	private boolean isNumeric(String cadena)
	{
		try {
			Integer.parseInt(cadena);
			return true;
		} catch (NumberFormatException nfe){
			return false;
		}
	}

	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////////////// ON ACTIVATE //////////////////////////////////////////////////////// 
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	* Controls if the user can enter in the page
	* @return another page if the user can't enter
	*/
	public Object onActivate()
	{
		switch(datasession.sessionState())
		{
			case 0:
				return null;
			case 1:
				return Index.class;
			case 2:
				return Index.class;
			case 3:
				return SessionExpired.class;
			default:
				return Index.class;
		}
	}
}
