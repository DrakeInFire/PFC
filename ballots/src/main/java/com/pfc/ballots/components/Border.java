package com.pfc.ballots.components;

import java.util.Locale;

import javax.inject.Inject;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.services.PersistentLocale;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;

import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.dao.LogDao;
import com.pfc.ballots.dao.UserDao;
import com.pfc.ballots.dao.UserLogedDao;
import com.pfc.ballots.data.DataSession;
import com.pfc.ballots.pages.Index;
import com.pfc.ballots.pages.MethodsInfo;
import com.pfc.ballots.pages.Census.CensusList;
import com.pfc.ballots.pages.Census.CreateCensus;
import com.pfc.ballots.pages.Company.CreateCompany;
import com.pfc.ballots.pages.Company.ListCompany;
import com.pfc.ballots.pages.admin.LogList;
import com.pfc.ballots.pages.admin.UserList;
import com.pfc.ballots.pages.profile.CreateProfile;
import com.pfc.ballots.pages.profile.ProfileByFile;
import com.pfc.ballots.pages.users.CompanyLogIn;
import com.pfc.ballots.pages.users.LogIn;

public class Border {

	/***************************************** Ajax menu stuff *******************************************************************/
	@Inject
	private Request request;
	
	@SessionState
	private DataSession datasession;
	@InjectComponent
	private Zone censusZone;
	@InjectComponent
	private Zone userZone;
	@InjectComponent
	private Zone ballotZone;
	@InjectComponent
	private Zone companyZone;
	
	
	@InjectPage
	private ProfileByFile profileByFile;
	
	@SessionAttribute
	@Property
	private Boolean visibilityUser;
	
	@SessionAttribute
	@Property
	private Boolean visibilityBallot;
	@SessionAttribute
	@Property
	private Boolean visibilityCompany;
	@SessionAttribute
	@Property
	private Boolean visibilityCensus;
    @Inject
    private ComponentResources componentResources;

	
	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;
	
	FactoryDao DB4O = FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
    UserLogedDao logedDao=null;
	
	 
	/****************************************  Constructor **********************************************************************/
	public Border()
	{
		if(visibilityUser==null || visibilityBallot==null || visibilityCompany==null || visibilityCensus==null)
		{
			visibilityUser=new Boolean("false");
			visibilityBallot=new Boolean("false");
			visibilityCompany=new Boolean("false");
			visibilityCensus=new Boolean("false");
		}
		if(datasession==null)
		{
			datasession=new DataSession();
		}


	}
	public void restoreBorder()
	{
		if(visibilityUser==null || visibilityBallot==null || visibilityCompany==null || visibilityCensus==null)
		{
			visibilityUser=new Boolean("false");
			visibilityBallot=new Boolean("false");
			visibilityCompany=new Boolean("false");
			visibilityCensus=new Boolean("false");
		}
		if(datasession==null)
		{
			datasession=new DataSession();
		}


	}
	/************************************* locale (languages stuff) **************************************************************/
	
	@Inject
	private PersistentLocale persistentLocale;
	@Inject
	private Locale currentLocale;
	
	@Persist
	private String localeLabel;
	
	
	//get locale label ------------------------------------------------------------------------------------------------------------
	public String getLocaleLabel()
	{	
		if (localeLabel == null)
		{
			if (currentLocale.equals(Locale.ENGLISH)) {
				
				localeLabel = new Locale("es").getDisplayName(Locale.getDefault());
			}
		}
		else
		{
			localeLabel = new Locale("en").getDisplayName(Locale.ENGLISH);	
		} 
		
		return localeLabel;
	}
	
	//on action from switch locale ------------------------------------------------------------------------------------------------
	void onActionFromSwitchlocale()
	{
		localeLabel = currentLocale.getDisplayName(currentLocale);
		
		if (currentLocale.equals(Locale.ENGLISH))
		{
			persistentLocale.set(Locale.getDefault());
		}
		else
		{
			persistentLocale.set(Locale.ENGLISH);
		}
	}
	
	public boolean isMaker()
	{
		if(datasession.isMaker())
		{
			return true;
		}
		else if(datasession.isAdmin())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	public boolean isAdmin()
	{
		return datasession.isAdmin();
	}
	public boolean isMainAdmin()
	{
		if(datasession.isMainUser()&& datasession.isAdmin())
			return true;
		else 
			return false;
	}
	public boolean isLoged()
	{
		
			return datasession.isLoged();
		

		
	}
	public Object onActionFromLogout()
	{
		logedDao=DB4O.getUserLogedDao(datasession.getDBName());
		logedDao.delete(datasession.getEmail());
		datasession.logout();
		visibilityCompany=false;
		visibilityUser=false;
		visibilityBallot=false;
		componentResources.discardPersistentFieldChanges();
		request.getSession(true).invalidate();
		restoreBorder();
		return Index.class;
	}
	/***************************************** Ajax menu Event Handler **********************************************************/
	
	/*
	 * Section is the context submitted by the eventlink to identify each link
	 * in the menu of the border component for close or open drop menus. 
	 */
	Object onMenu(String section)
	{									
		Object page=null;
		if(section.equals("censusz"))		//This handle the lateral menu
		{
			visibilityUser=false;
			visibilityBallot=false;
			visibilityCompany=false;
			if(visibilityCensus)
				{visibilityCensus=false;}
			else
				{visibilityCensus=true;}
		}
		else if(section.equals("userz"))		
		{
			visibilityBallot=false;
			visibilityCompany=false;
			visibilityCensus=false;
			if(visibilityUser)
				{visibilityUser=false;}
			else
				{visibilityUser=true;}
			
		}
		else if(section.equals("ballotz"))
		{
			visibilityCensus=false;
			visibilityCompany=false;
			visibilityUser=false;
			if(visibilityBallot)
				{visibilityBallot=false;}
			else
				{visibilityBallot=true;}
			
		}
		else if(section.equals("companyz"))
		{
			visibilityUser=false;
			visibilityBallot=false;
			visibilityCensus=false;
			if(visibilityCompany)
				{visibilityCompany=false;}
			else
				{visibilityCompany=true;}
		}
		else if(section.equals("new-census"))
			{
				page=CreateCensus.class;
			}
		else if(section.equals("show-census"))
			{
				page=CensusList.class;
			}
		else if(section.equals("new-user2"))
			{
			page=CreateProfile.class;}
		else if(section.equals("user-list"))
			{
			
			page=UserList.class;}
		else if(section.equals("user-file"))
			{
				profileByFile.setup(datasession.getDBName());
				page=profileByFile;
				
			}
		else if(section.equals("log-list"))
			{
			page=LogList.class;}
		else if(section.equals("new-company"))
			{
			page=CreateCompany.class;}
		else if(section.equals("list-company"))
			{
			page=ListCompany.class;}
		else							//This handle the upper menu
		{
			visibilityCompany=false;
			visibilityUser=false;
			visibilityBallot=false;
			visibilityCensus=false;
			if(section.equals("index"))
				{page=Index.class;}
			if(section.equals("new-user1"))
				{page=CreateProfile.class;}
			if(section.equals("LogIn"))
				{page=LogIn.class;}
			if(section.equals("methods"))
				{page=MethodsInfo.class;}
			if(section.equals("company-login"))
				{page=CompanyLogIn.class;}
		}
		if(request.isXHR())
		{
			ajaxResponseRenderer.addRender("censusZone",censusZone).addRender("userZone", userZone).addRender("ballotZone", ballotZone).addRender("companyZone", companyZone);
		}
		return page;
	}
}
