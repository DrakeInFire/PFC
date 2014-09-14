package com.pfc.ballots.pages.profile;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

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

import com.pfc.ballots.dao.CensusDao;
import com.pfc.ballots.dao.CompanyDao;
import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.dao.ProfileCensedInDao;
import com.pfc.ballots.dao.UserDao;
import com.pfc.ballots.dao.UserLogedDao;
import com.pfc.ballots.data.DataSession;
import com.pfc.ballots.data.Sex;
import com.pfc.ballots.entities.Census;
import com.pfc.ballots.entities.Company;
import com.pfc.ballots.entities.Profile;
import com.pfc.ballots.entities.ProfileCensedIn;
import com.pfc.ballots.entities.UserLoged;
import com.pfc.ballots.pages.Index;
import com.pfc.ballots.pages.SessionExpired;
import com.pfc.ballots.util.Encryption;



@Secure
public class ShowProfile {

	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 //////////////////////////////////////////// GENRAL & USER DATA ZONE ////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@SessionState
	private DataSession datasession;
	
	@Inject
	private ComponentResources componentResources;
	
	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;
	
	@Inject
	private Request request;
	
	
	//////////////////////////////////////////////////////// DAO //////////////////////
	FactoryDao DB4O=FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
	@Persist
	private UserDao userDao;
	@Persist
	private CompanyDao companyDao;
	private UserLogedDao userLogedDao;
	private CensusDao censusDao;
	private ProfileCensedInDao censedInDao;
	

	/**
	 * Initialize data
	 */
	void setupRender()
	{
		componentResources.discardPersistentFieldChanges();
		userDao=DB4O.getUsuarioDao(datasession.getDBName());
		profile=userDao.getProfileById(datasession.getId());
		showData=true;
		showChange=false;
		showUpdate=false;
		badMail=false;
		showSure=false;
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
	
	/////////////////////////////////////////////////// PAGE STUFF ///////////////////////////////
	
	@Persist
	@Property
	private Profile profile;
	@Persist
	@Property
	private Company company;


	
	@InjectComponent
	private Zone userDataZone;
	
	
	@Persist
	@Property
	private boolean companyUser;
	@Persist
	@Property
	private boolean showData;
	
	/**
	 * Shows a change pass form
	 */
	public void onActionFromChangePass()
	{
		if(request.isXHR())
		{
			showChange=true;
			showData=false;
			ajaxResponseRenderer.addRender("userDataZone", userDataZone).addRender("changePassZone", changePassZone);
		}
	}
	/**
	 * Shows an update profile form
	 */
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
	public boolean isNotOwner()
	{
		if(datasession.isOwner())
		{
			return false;
		}
		return true;
	}
	
	public void onActionFromDeleteBut()
	{
		if(request.isXHR())
		{
			showData=false;
			showSure=true;
			ajaxResponseRenderer.addRender("userDataZone", userDataZone).addRender("areuSureZone",areuSureZone);
		}
	}
	
	public String getSex()
	{
		if(profile.getSex()==Sex.HOMBRE)
		{
			return "Hombre";
		}
		if(profile.getSex()==Sex.MUJER)
		{
			return "Mujer";
		}
		return "";
		
	}
	public String getFechaNac()
	{
		DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
		return format.format(profile.getFechaNac());
	}
	
	
	
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////// ARE U SURE TO DELETE ////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@InjectComponent
	private Zone areuSureZone;
	@Persist
	@Property
	private boolean showSure;
	
	/**
	 * it's activate when the user press the button yes
	 * for delete his account
	 * 
	 * @return Index page
	 */
	public Object onActionFromYesSureBut()
	{
		censedInDao=DB4O.getProfileCensedInDao(datasession.getDBName());
		censusDao=DB4O.getCensusDao(datasession.getDBName());
		userLogedDao=DB4O.getUserLogedDao(datasession.getDBName());
		
		ProfileCensedIn censedIn=censedInDao.getProfileCensedIn(datasession.getId());
		censusDao.removeUserCountedOfCensus(censedIn.getInCensus(), datasession.getId());
		censedInDao.delete(datasession.getId());
		userDao.deleteById(datasession.getId());
		
		datasession.logout();
		request.getSession(true).invalidate();
		
		return Index.class;
	}
	/**
	 * it's activate when the user press the button no
	 * for no delete his account
	 */
	public void onActionFromNotSureBut()
	{
		if(request.isXHR())
		{
			showData=true;
			showSure=false;
			ajaxResponseRenderer.addRender("userDataZone", userDataZone).addRender("areuSureZone",areuSureZone);
		}
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
	@Validate("required")
	private String pass;
	@Property
	@Validate("required,minLength=8")
	private String newPass1;
	@Property
	@Validate("required,minLength=8")
	private String newPass2;
	
	@Persist
	@Property
	private boolean samePass;
	
	@Persist
	@Property
	private boolean badPass;
	@Persist
	@Property
	private boolean badSecurity;


	final String [] caracteresEspeciales={"!","¡","@","|","#","$","%","&","/","(",")","=","¿","?","*","+","-","_"};
	@Persist
	@Property
	private boolean badMatch;
	/**
	 * Changes the pass 
	 */
	public void onSuccessFromChangePassForm()
	{

		if(request.isXHR())
		{	//profile.setPassword(Encryption.getStringMessageDigest(newPass1, Encryption.SHA1));
			//userDao.UpdateById(profile);
			boolean success=true;
			badPass=false;
			badMatch=false;
			samePass=false;
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
			badSecurity=true;
			for(String car:caracteresEspeciales)
			{
				if(newPass1.contains(car))
				{
					badSecurity=false;
				}
			}
			
			if(success && !badSecurity)
			{
				if(profile.getPassword().equals(newPass1))
				{
					samePass=false;
					ajaxResponseRenderer.addRender("changePassZone", changePassZone);
				}
				else
				{
					profile.setPassword(Encryption.getStringMessageDigest(newPass1, Encryption.SHA1));
					userDao.UpdateById(profile);
					showData=true;
					showChange=false;
					ajaxResponseRenderer.addRender("userDataZone", userDataZone).addRender("changePassZone", changePassZone);
				}
			}
			else
			{
				System.out.println("CHECk");
				ajaxResponseRenderer.addRender("changePassZone", changePassZone);
			}
		}
		
	}
	/**
	 * Cancels change the pass
	 */
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
	
	
	/**
	 * Updates the profiles
	 */
	public void onSuccessFromUpdateForm()
	{
		boolean igual=false;
		boolean emailChanged=false;
		if(request.isXHR())
		{
			
			igual=update.equals(profile);
			
			if(!igual)
			{
				System.out.println("CHANGED");
				
				if(!update.getEmail().equals(profile.getEmail()))
				{
					if(userDao.isProfileRegistred(update.getEmail()))
					{
						badMail=true;
					
					}
					else
					{
						badMail=false;
						emailChanged=true;
					}
				}
				else
				{
					badMail=false;
				}
			}
			
			
			if(badMail)
			{
				ajaxResponseRenderer.addRender("updateProfileZone",updateProfileZone);
			}
			else
			{
				if(!igual)
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
						censusDao=DB4O.getCensusDao(datasession.getDBName());
						List<Census> censusOwner=censusDao.getByOwnerId(update.getId());
						censusDao.changeEmailOfCensus(censusOwner, update.getEmail());
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
	/**
	 * Cancels the update
	 */
	public void onActionFromCancelUpdate()
	{
		if(request.isXHR())
		{
			showData=true;
			showUpdate=false;
			ajaxResponseRenderer.addRender("userDataZone", userDataZone).addRender("updateProfileZone",updateProfileZone);
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
					return Index.class;
				case 1:
					return null;
				case 2:
					return null;
	 			case 3:
					return SessionExpired.class;
				default:
					return Index.class;
			}
			
		}
}
