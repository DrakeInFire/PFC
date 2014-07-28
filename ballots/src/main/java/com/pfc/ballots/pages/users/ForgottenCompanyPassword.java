package com.pfc.ballots.pages.users;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;

import com.pfc.ballots.dao.CompanyDao;
import com.pfc.ballots.dao.EmailAccountDao;
import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.dao.UserDao;
import com.pfc.ballots.data.DataSession;
import com.pfc.ballots.entities.Company;
import com.pfc.ballots.entities.EmailAccount;
import com.pfc.ballots.entities.Profile;
import com.pfc.ballots.pages.profile.SuccessPassChange;
import com.pfc.ballots.util.Encryption;
import com.pfc.ballots.util.Mail;
import com.pfc.ballots.util.PasswordGenerator;

public class ForgottenCompanyPassword {
	
	private FactoryDao DB4O =FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
	@Persist
	private CompanyDao companyDao;
	private UserDao userDao;
	@Persist
	private EmailAccountDao emailAccountDao;
	
	@SessionState
	private DataSession datasession;
	
	@Inject
	private Request request;
	
	@Inject
	private ComponentResources componentResources;
	
	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;
	
	public void setupRender()
	{
		componentResources.discardPersistentFieldChanges();
		emailAccountDao=DB4O.getEmailAccountDao();
		companyDao=DB4O.getCompanyDao();
		badCompany=false;
		badEmail=false;
		badSend=false;
	}

	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////// FORM ZONE //////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@InjectComponent
	private Zone formZone;
	@Property
	@Validate("required,minLength=5")
	private String companyName;
	@Property
	@Validate("required,regexp=^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
	private String email;
	
	@Property
	@Persist
	private boolean badCompany;
	@Property
	@Persist
	private boolean badEmail;
	@Property
	@Persist
	private boolean badSend;
	
	public Object onSuccessFromForgottenForm()
	{
		if(request.isXHR())
		{
			Company company=companyDao.getCompanyByName(companyName);
			if(company==null)
			{
				badCompany=true;
				ajaxResponseRenderer.addRender("formZone", formZone);
			}
			else
			{
				badCompany=false;
				userDao=DB4O.getUsuarioDao(company.getDBName());
				Profile profile=userDao.getProfileByEmail(email);
				if(profile==null)
				{
					badEmail=true;
					ajaxResponseRenderer.addRender("formZone", formZone);
				}
				else
				{
					badEmail=false;
					EmailAccount account =emailAccountDao.getAccount();
					String newPass=PasswordGenerator.getPassword();
					profile.setPassword(Encryption.getStringMessageDigest(newPass, Encryption.SHA1));
					String msg="Su nueva contraseña es :"+newPass;
					if(Mail.sendMail(account.getEmail(), account.getPassword(), email, "NewPass:noReply", msg))
					{
						badSend=false;
						userDao.UpdateByEmail(profile);
						return SuccessPassChange.class;
					}
					else
					{
						badSend=true;
						ajaxResponseRenderer.addRender("formZone", formZone);
					}
						
					
				}
				
			}
		}
		return null;
	}
}
