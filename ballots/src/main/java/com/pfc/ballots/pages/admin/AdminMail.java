package com.pfc.ballots.pages.admin;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Secure;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;

import com.pfc.ballots.dao.AboutTextDao;
import com.pfc.ballots.dao.EmailAccountDao;
import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.dao.UserDao;
import com.pfc.ballots.data.DataSession;
import com.pfc.ballots.entities.AboutText;
import com.pfc.ballots.entities.EmailAccount;
import com.pfc.ballots.entities.Profile;
import com.pfc.ballots.pages.About;
import com.pfc.ballots.pages.Index;
import com.pfc.ballots.pages.SessionExpired;
import com.pfc.ballots.pages.UnauthorizedAttempt;
import com.pfc.ballots.util.Encryption;
import com.pfc.ballots.util.Mail;
/**
 * 
 * AdminMail class is the controller for the AdminMail page that
 * provides the administration for the mail of the application
 * 
 * @author Mario Temprano Martin
 * @version 1.0 JUN-2014
 */

@Secure
public class AdminMail {

	
  	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 //////////////////////////////////////////////////////// DAO ///////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	FactoryDao DB4O=FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
	@Persist
	UserDao userDao;
	@Persist
	EmailAccountDao emailAccountDao;
	@Persist 
	AboutTextDao aboutDao;

	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 //////////////////////////////////////////////////// GENERAL STUFF //////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@SessionState
	private DataSession datasession;
	
	@Inject
	private ComponentResources componentResources;
	
	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;
	
	@Inject
	private Request request;
	
	@Persist 
	@Property
	private EmailAccount emailAccount;
	
	@Property
	@Persist
	private boolean badPass;
	@Property
	@Persist
	private boolean badMatch;
	@Property
	@Persist
	private boolean badServer;
	@Property
	@Persist
	private boolean badCombination;
	
	/**
	 * Initialize variables
	 */
	public void setupRender()
	{
		componentResources.discardPersistentFieldChanges();
		emailAccountDao=DB4O.getEmailAccountDao();
		userDao=DB4O.getUsuarioDao();
		emailAccount=emailAccountDao.getAccount();
		aboutDao=DB4O.getAboutTextDao();
		emailform=new EmailAccount();
		settings=false;
		change=false;
		badPass=false;
		badMatch=false;
		badServer=false;
		changeAccount=false;
		showAbout=false;
		badCombination=false;
	}
	
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ////////////////////////////////////////////////////// NEW EMAIL ZONE ///////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@InjectComponent
	private Zone newEmailZone;
	
	@Property
	@Persist
	private EmailAccount emailform;
	
	@Property
	@Persist
	@Validate("required,minLength=5")
	private String pass1;
	
	@Property
	@Persist
	@Validate("required,minLength=5")
	private String pass2;
	
	
	public boolean isShowNewZone()
	{
		if(emailAccount==null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Success for the newForm form that stores a new 
	 * email data 
	 */
	public void onSuccessFromNewForm()
	{
		boolean success=true;
		if(request.isXHR())
		{
			if(Mail.isValidEmail(emailform.getEmail()))
			{
				badServer=false;
			}
			else
			{
				badServer=true;
				success=false;
			}
			if(pass1.equals(pass2))
			{
				badMatch=false;
			}
			else
			{
				badMatch=true;
				success=false;
			}
			/*  
			 * 	Dos ifs que verifiquen la misma variable
			 *	en este si todo el formulario esta correcto comprueba la valided del password
			 *	y en caso de ser incorreco notifica
			 */
			if(success)
			{
				emailform.setPassword(pass1);
				if(Mail.checkAccount(emailform))
				{
					badCombination=false;
				}
				else
				{
					badCombination=true;
					success=false;
				}
			}
			/*
			 * En caso de ser correcto entrara aqui simplemente guardara los datos y
			 * volvera a la pantalla principal de la pagina
			 */
			if(success)
			{
				emailAccount=emailform;
				emailAccountDao.setEmailAccount(emailform);
				ajaxResponseRenderer.addRender("newEmailZone", newEmailZone).addRender("emailSettingsZone", emailSettingsZone);
			}
			else
			{
				ajaxResponseRenderer.addRender("newEmailZone", newEmailZone);
			}
		}
	}
	
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////////// EMAIL SETTINGS ZONE /////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@InjectComponent
	private Zone emailSettingsZone;
	
	@Persist
	private boolean settings;
	

	public boolean isShowSettings()
	{
		if(isShowNewZone())
		{
			return false;
		}
		else
		{
			if(settings==false)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}
	/**
	 * 
	 * @return if the email-pass combination
	 */
	public boolean isValidCombination()
	{
		if(Mail.checkAccount(emailAccount))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Activate a form to store a new email account
	 */
	public void onActionFromChangeAccount()
	{
		System.out.println("CHANGE ACCOUNT");
		if(request.isXHR())
		{
			changeAccount=true;
			settings=true;
			badPass=false;
			badMatch=false;
			badServer=false;
			badCombination=false;
			emailform=new EmailAccount();
			ajaxResponseRenderer.addRender("emailSettingsZone", emailSettingsZone).addRender("changeAccountZone", changeAccountZone);
			
		}
	}
	/**
	 * Delete the email account
	 */
	public void onActionFromDeleteAccount()
	{
		if(request.isXHR())
		{
			showSure=true;
			settings=true;
			ajaxResponseRenderer.addRender("areusureZone",areusureZone).addRender("emailSettingsZone",emailSettingsZone);
			/*emailAccountDao.deleteEmailAccount();
			emailAccount=null;
			
			ajaxResponseRenderer.addRender("newEmailZone", newEmailZone).addRender("emailSettingsZone", emailSettingsZone);
*/
		}
	}
	/**
	 * Activate a form to change the password of the account
	 */
	public void onActionFromChangePass()
	{
		System.out.println("CHANGE PASS");
		if(request.isXHR())
		{
			settings=true;
			change=true;
			badPass=false;
			badMatch=false;
			badCombination=false;
			badServer=false;
			ajaxResponseRenderer.addRender("emailSettingsZone", emailSettingsZone).addRender("changePassZone", changePassZone);
		}
	}
	
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ///////////////////////////////////////////////////// CHANGE PASS ZONE //////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@InjectComponent
	private Zone changePassZone;
	
	@Persist
	private boolean change;
	@Property
	@Validate("required,minLength=5")
	private String adminPass;
	
	@Persist
	@Property
	private String newPass1;
	@Persist
	@Property
	private String newPass2;
	
	
	public boolean isShowChange()
	{
		if(isShowNewZone())
		{
			return false;
		}
		else
		{
			if(change==true)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}
	
	/**
	 * Checks if the new email pass is correct and store it 
	 */
	public void onSuccessFromChangePassForm()
	{
		boolean success=true;
		if(request.isXHR())
		{
			String pass=userDao.getProfileById(datasession.getId()).getPassword();
			String pass2=Encryption.getStringMessageDigest(adminPass, Encryption.SHA1);
			if(pass.equals(pass2))
			{
				badPass=false;
			}
			else
			{
				badPass=true;
				success=false;
			}
			if(newPass1.equals(newPass2))
			{
				badMatch=false;
			}
			else
			{
				badMatch=true;
				success=false;
			}
			
			/*
			 * Funciona igual que el doble if success anterior
			 */
			if(success)
			{
				emailform.setPassword(newPass1);
				if(Mail.checkAccount(emailform))
				{
					badCombination=false;
				}
				else
				{
					badCombination=true;
					success=false;
				}
			}
			if(success)
			{
				change=false;
				settings=false;
				emailAccount.setPassword(newPass1);
				emailAccountDao.updateEmailAccount(emailAccount);
				ajaxResponseRenderer.addRender("changePassZone", changePassZone).addRender("emailSettingsZone", emailSettingsZone);
			}
			else
			{
				ajaxResponseRenderer.addRender("changePassZone", changePassZone);
			}
			
		}
	}
	/**
	 * Cancel the form to change the email password
	 */
	public void onActionFromCancelChangePass()
	{
		if(request.isXHR())
		{
			change=false;
			settings=false;
			ajaxResponseRenderer.addRender("changePassZone", changePassZone).addRender("emailSettingsZone", emailSettingsZone);
		}
	}
	
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////////// CHANGE ACCOUNT ZONE /////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Persist
	private boolean changeAccount;
	
	@InjectComponent
	private Zone changeAccountZone;
	
	@Property
	@Validate("required,minLength=5")
	private String adminPassword;
	
	@Property
	@Validate("required,minLength=5")
	private String password1;
	@Property
	@Validate("required,minLength=5")
	private String password2;
	
	
	public boolean isShowChangeAccount()
	{
		if(isShowNewZone())
		{
			return false;
		}
		else
		{
			if(changeAccount==true)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}
	/**
	 * Stores a new email account for the application
	 */
	public void onSuccessFromChangeAccountForm()
	{
		boolean success=true;
		if(request.isXHR())
		{
			//Checks the admin password
			String pass=userDao.getProfileById(datasession.getId()).getPassword();
			String pass2=Encryption.getStringMessageDigest(adminPassword, Encryption.SHA1);
			if(pass.equals(pass2))
			{
				badPass=false;
			}
			else
			{
				badPass=true;
				success=false;
			}
			//Checks if the email is valid
			if(Mail.isValidEmail(emailform.getEmail()))
			{
				badServer=false;
			}
			else
			{
				badServer=true;
				success=false;
			}
			//Checks if the passwords fields are equals
			if(password1.equals(password2))
			{
				badMatch=false;
			}
			else
			{
				badMatch=true;
				success=false;
			}
			
			
			if(success)
			{
				emailform.setPassword(password1);
				if(Mail.checkAccount(emailform))
				{
					badCombination=false;
				}
				else
				{
					badCombination=true;
					success=false;
				}
			}
			
			if(success)
			{
				emailform.setPassword(password1);
				changeAccount=false;
				settings=false;
				emailAccountDao.updateEmailAccount(emailform);
				emailAccount=emailform;
				ajaxResponseRenderer.addRender("changeAccountZone", changeAccountZone).addRender("emailSettingsZone", emailSettingsZone);
			}
			else
			{
				ajaxResponseRenderer.addRender("changeAccountZone", changeAccountZone);
			}
			
		}
		
	}
	/**
	 * Cancels the form to store a new email account for the application
	 */
	public void onActionFromCancelChangeAccount()
	{
		if(request.isXHR())
		{
			changeAccount=false;
			settings=false;
			ajaxResponseRenderer.addRender("changeAccountZone", changeAccountZone).addRender("emailSettingsZone", emailSettingsZone);

			
		}
	}
	
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ///////////////////////////////////////////////////// ARE U SURE ZONE ///////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	@InjectComponent
	private Zone areusureZone;
	
	@Persist
	@Property
	private boolean showSure;
	
	public void onActionFromImSure()
	{
		if(request.isXHR())
		{
			showSure=false;
			settings=false;
			emailAccountDao.deleteEmailAccount();
			emailAccount=null;
			
			ajaxResponseRenderer.addRender("newEmailZone", newEmailZone).addRender("emailSettingsZone", emailSettingsZone).addRender("areusureZone",areusureZone);
		}
	}
	public void onActionFromNotSure()
	{
		if(request.isXHR())
		{
			showSure=false;
			settings=false;
			ajaxResponseRenderer.addRender("areusureZone",areusureZone).addRender("emailSettingsZone",emailSettingsZone);
		}
	}
	
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 //////////////////////////////////////////////////////// SHOW ABOUT /////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@InjectComponent
	private Zone aboutZone;
	
	@Persist
	@Property
	private boolean showAbout;
	
	@Persist
	@Property
	private String aboutText;
	@Persist
	@Property
	private String head;
	
	public void onActionFromEditAboutBut()
	{
		if(request.isXHR())
		{
			AboutText temp=aboutDao.getAboutText();
			if(temp!=null)
			{
				aboutText=temp.getText();
				head=temp.getHead();
			}
			showAbout=true;
			settings=true;
			ajaxResponseRenderer.addRender("aboutZone",aboutZone).addRender("emailSettingsZone",emailSettingsZone);
		}
	}
	
	public Object onSuccessFromAboutForm()
	{
		if(request.isXHR())
		{
			AboutText temp=new AboutText();
			temp.setText(aboutText);
			temp.setHead(head);
			aboutDao.updateAboutText(temp);
			showAbout=false;
			settings=false;
			return About.class;
		}
		return null;
	}
	public void onActionFromBackBut()
	{
		if(request.isXHR())
		{
			showAbout=false;
			settings=false;
			ajaxResponseRenderer.addRender("aboutZone",aboutZone).addRender("emailSettingsZone",emailSettingsZone);
		}
	}
	public Object onActionFromDefaultText()
	{
		aboutDao.deleteAboutText();
		return About.class;
		
	}
	
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////////////// ON ACTIVATE /////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * Controls if the user can enter in the page
	 * @return another page if the user can't enter
	 */
	 public Object onActivate()
	 {
		 switch(datasession.sessionState())
			{
				case 0:
					return Index.class;
				case 1:
					return UnauthorizedAttempt.class;
				case 2:
					if(datasession.isMainUser())
					{
						return null;
					}
					return UnauthorizedAttempt.class;
				case 3:
					return SessionExpired.class;
				default:
					return Index.class;
			}
		}
}
