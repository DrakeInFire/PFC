package com.pfc.ballots.pages.profile;

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
import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.dao.UserDao;
import com.pfc.ballots.dao.UserLogedDao;
import com.pfc.ballots.data.DataSession;
import com.pfc.ballots.entities.Company;
import com.pfc.ballots.entities.Profile;
import com.pfc.ballots.entities.UserLoged;
import com.pfc.ballots.util.Encryption;

public class ShowProfile {

	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 //////////////////////////////////////////// GENRAL & USER DATA ZONE ////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	FactoryDao DB4O=FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
	@Persist
	private UserDao userDao;
	@Persist
	private CompanyDao companyDao;
	private UserLogedDao userLogedDao;
	
	
	@Persist
	@Property
	private Profile profile;
	@Persist
	@Property
	private Company company;

	@SessionState
	private DataSession datasession;
	
	@Inject
	private ComponentResources componentResources;
	
	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;
	
	@Inject
	private Request request;
	
	@InjectComponent
	private Zone userDataZone;
	
	
	@Persist
	@Property
	private boolean companyUser;
	@Persist
	@Property
	private boolean showData;
	
	
	void setupRender()
	{
		componentResources.discardPersistentFieldChanges();
		userDao=DB4O.getUsuarioDao(datasession.getDBName());
		profile=userDao.getProfileById(datasession.getId());
		showData=true;
		showChange=false;
		showUpdate=false;
		badMail=false;
		if(datasession.isMainUser())
		{
			company=null;
			companyUser=false;
		}
		else
		{
			companyUser=true;
			companyDao=DB4O.getCompanyDao();
			company=companyDao.getCompanyByName(datasession.getCompany());
		}
	}
	
	public void onActionFromChangePass()
	{
		if(request.isXHR())
		{
			showChange=true;
			showData=false;
			ajaxResponseRenderer.addRender("userDataZone", userDataZone).addRender("changePassZone", changePassZone);
		}
	}
	public void onActionFromUpdateProfile()
	{
		if(request.isXHR())
		{
			showData=false;
			showUpdate=true;
			update=new Profile();
			update.copy(profile);
			ajaxResponseRenderer.addRender("userDataZone", userDataZone).addRender("updateProfileZone",updateProfileZone);
		}
	}
	
	public boolean isMainOwner()
	{
		return datasession.isMainOwner();
	}
	
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ///////////////////////////////////////////////// CHANGE PASS ZONE //////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@InjectComponent
	private Zone changePassZone;
	@Persist
	@Property
	private boolean showChange;
	
	@Property
	@Validate("required,minLength=5")
	private String pass;
	@Property
	@Validate("required,minLength=5")
	private String newPass1;
	@Property
	@Validate("required,minLength=5")
	private String newPass2;
	
	@Persist
	@Property
	private boolean badPass;


	@Persist
	@Property
	private boolean badMatch;
	public void onSuccessFromChangePassForm()
	{

		if(request.isXHR())
		{
			boolean success=true;
			String temp=Encryption.getStringMessageDigest(pass, Encryption.SHA1);
			if(!temp.equals(profile.getPassword()))
			{
				success=false;
				badPass=true;
			}
			else
			{
				badPass=false;
			}
			
			if(!newPass2.equals(newPass1))
			{
				success=false;
				badMatch=true;
			}
			else
			{
				badMatch=false;
			}
			if(success)
			{
				profile.setPassword(Encryption.getStringMessageDigest(newPass1, Encryption.SHA1));
				userDao.UpdateById(profile);
				showData=true;
				showChange=false;
				ajaxResponseRenderer.addRender("userDataZone", userDataZone).addRender("changePassZone", changePassZone);
			}
			else
			{
				ajaxResponseRenderer.addRender("changePassZone", changePassZone);
			}
		}
		
	}
	
	public void onActionFromCancelChangePass()
	{
		if(request.isXHR())
		{
			showData=true;
			showChange=false;
			ajaxResponseRenderer.addRender("userDataZone", userDataZone).addRender("changePassZone", changePassZone);

		}
		
	}
	
	  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ///////////////////////////////////////// UPDATE PROFILE ZONE /////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@InjectComponent
	private Zone updateProfileZone;
	@Persist
	@Property
	private boolean showUpdate;
	
	@Persist
	@Property
	private boolean badMail;
	@Property
	@Persist
	private Profile update;
	
	public void onSuccessFromUpdateForm()
	{
		boolean changed=false;
		boolean emailChanged=false;
		if(request.isXHR())
		{
			if(!update.getEmail().equals(profile.getEmail()))
			{
				if(userDao.isProfileRegistred(update.getEmail()))
				{
					badMail=true;
				
				}
				else
				{
					changed=true;
					badMail=false;
					emailChanged=true;
				}
			}
			else
			{
				badMail=false;
			}
			
			//FirstName Check
			
			if(!update.getFirstName().equals(profile.getFirstName()))
			{
				changed=true;
			}
			
			//LastnName Check
			if(!update.getLastName().equals(profile.getLastName()))
			{
				changed=true;
			}
			
			//University Check
			if(update.getUniversity()==null && profile.getUniversity()==null)
			{
				//Must be empty, its a check to avoid not null exception
			}			
			else if(update.getUniversity()==null && profile.getUniversity()!=null)
			{
				changed=true;
			}
			else if(update.getUniversity()!=null && profile.getUniversity()==null)
			{
				changed=true;
			}
			else if(!update.getUniversity().equals(profile.getUniversity()))
			{
				changed=true;
			}
			
			//City Check
			
			if(update.getCity()==null && profile.getCity()==null)
			{
				//Must be empty, its a check to avoid not null exception
			}			
			else if(update.getCity()==null && profile.getCity()!=null)
			{
				changed=true;
			}
			else if(update.getCity()!=null && profile.getCity()==null)
			{
				changed=true;
			}
			else if(!update.getCity().equals(profile.getCity()))
			{
				changed=true;
			}

			//Country check
			if(update.getCountry()==null && profile.getCountry()==null)
			{
				//Must be empty, its a check to avoid not null exception
			}			
			else if(update.getCountry()==null && profile.getCountry()!=null)
			{
				changed=true;
			}
			else if(update.getCountry()!=null && profile.getCountry()==null)
			{
				changed=true;
			}
			else if(!update.getCountry().equals(profile.getCountry()))
			{
				changed=true;
			}
			
			
			if(badMail)
			{
				ajaxResponseRenderer.addRender("updateProfileZone",updateProfileZone);
			}
			else
			{
				if(changed)
				{
					if(emailChanged)
					{
						if(datasession.isCompanyUser())
						{				 
							Company company=datasession.isCompanyOwner(null);
							if(company!=null)
							{
								company.setAdminEmail(update.getEmail());
								companyDao.updateCompany(company);
							}
						}
						userLogedDao=DB4O.getUserLogedDao(datasession.getDBName());
						UserLoged userLoged=userLogedDao.getUserLoged(datasession.getIdSession());
						userLoged.setEmail(update.getEmail());
						userLogedDao.updateUserLoged(userLoged);
						datasession.setEmail(update.getEmail());
					}
					userDao.UpdateById(update);
					profile=update;
					showData=true;
					showUpdate=false;
					ajaxResponseRenderer.addRender("userDataZone", userDataZone).addRender("updateProfileZone",updateProfileZone);	
				}
				else
				{	
					showData=true;
					showUpdate=false;
					ajaxResponseRenderer.addRender("userDataZone", userDataZone).addRender("updateProfileZone",updateProfileZone);	
					
				}

			}
			
		}
	}
	public void onActionFromCancelUpdate()
	{
		if(request.isXHR())
		{
			showData=true;
			showUpdate=false;
			ajaxResponseRenderer.addRender("userDataZone", userDataZone).addRender("updateProfileZone",updateProfileZone);
		}
	}
	
}
