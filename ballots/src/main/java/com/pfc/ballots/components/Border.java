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
import com.pfc.ballots.pages.About;
import com.pfc.ballots.pages.Contact;
import com.pfc.ballots.pages.Index;
import com.pfc.ballots.pages.MethodsInfo;
import com.pfc.ballots.pages.Census.AdminCensus;
import com.pfc.ballots.pages.Census.CensusList;
import com.pfc.ballots.pages.Census.CreateCensus;
import com.pfc.ballots.pages.Company.CreateCompany;
import com.pfc.ballots.pages.Company.ListCompany;
import com.pfc.ballots.pages.admin.AdminCreateProfile;
import com.pfc.ballots.pages.admin.AdminMail;
import com.pfc.ballots.pages.admin.LogList;
import com.pfc.ballots.pages.admin.UserList;
import com.pfc.ballots.pages.ballot.BallotList;
import com.pfc.ballots.pages.ballot.BallotWizzard;
import com.pfc.ballots.pages.ballot.CreateBallot;
import com.pfc.ballots.pages.ballot.PublicBallots;
import com.pfc.ballots.pages.ballot.ShowBallotAdmin;
import com.pfc.ballots.pages.profile.CreateProfile;
import com.pfc.ballots.pages.profile.ProfileByFile;
import com.pfc.ballots.pages.profile.ShowProfile;
import com.pfc.ballots.pages.users.CompanyLogIn;
import com.pfc.ballots.pages.users.LogIn;
/**
 * Border class is a component that provides the menu interface for all the application
 * 
 * @author Mario Temprano Martín
 * @version 2.0 FEB-2014
 *
 */
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
	
	/**
	 * Initialize border values to avoid errors
	 */
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
	/**
	 * 
	 * @return if the user is a maker
	 */
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
	/**
	 * 
	 * @return if the user is admin
	 */
	public boolean isAdmin()
	{
		return datasession.isAdmin();
	}
	
	/**
	 * 
	 * @return if the user is an admin of the main application
	 */
	public boolean isMainAdmin()
	{
		if(datasession.isMainUser()&& datasession.isAdmin())
			return true;
		else 
			return false;
	}
	/**
	 * 
	 * @return if the user is loged
	 */
	public boolean isLoged()
	{
			return datasession.isLoged();
	}
	
	/**
	 * Logout(lateral menu) the user from DB and clear session data
	 */
	public Object onActionFromLogout()
	{
		logedDao=DB4O.getUserLogedDao(datasession.getDBName());
		logedDao.delete(datasession.getIdSession());
		datasession.logout();
		visibilityCompany=false;
		visibilityUser=false;
		visibilityBallot=false;
		componentResources.discardPersistentFieldChanges();
		request.getSession(true).invalidate();
		restoreBorder();
		return Index.class;
	}
	/**
	 * Logout(upper menu) the user from DB and clear session data
	 */
	public Object onActionFromLogout2()
	{
		logedDao=DB4O.getUserLogedDao(datasession.getDBName());
		logedDao.delete(datasession.getIdSession());
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
	
	/**
	 * Section is the context submitted by the eventlink to identify each link
	 * in the menu of the border component for close or open drop menus. 
	 */
	Object onMenu(String section)
	{									
		Object page=null;
		if(section.equals("my-profile"))//This handle the lateral menu
		{
			visibilityUser=false;
			visibilityBallot=false;
			visibilityCompany=false;
			visibilityCensus=false;
			page=ShowProfile.class;
		}
		if(section.equals("my-ballots"))
		{
			visibilityUser=false;
			visibilityBallot=false;
			visibilityCompany=false;
			visibilityCensus=false;
			page=BallotList.class;
		}
		else if(section.equals("admin-mail"))
		{
			visibilityUser=false;
			visibilityBallot=false;
			visibilityCompany=false;
			visibilityCensus=false;
			page=AdminMail.class;
		}
		else if(section.equals("censusz"))		
		{
			visibilityUser=false;
			visibilityBallot=false;
			visibilityCompany=false;
			if(visibilityCensus)
				{visibilityCensus=false;}
			else
				{visibilityCensus=true;}
		}
		else if(section.equals("show-admincensus"))//////
		{
			visibilityUser=false;
			visibilityBallot=false;
			visibilityCompany=false;
			visibilityCensus=false;
			page=AdminCensus.class;
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
				page=AdminCreateProfile.class;
			}
		else if(section.equals("user-list"))
			{
				page=UserList.class;
			}
		else if(section.equals("user-file"))
			{
				profileByFile.setup(datasession.getDBName());
				page=profileByFile;
			}
		else if(section.equals("log-list"))
			{
			page=LogList.class;
			}
		else if(section.equals("new-company"))
			{
			page=CreateCompany.class;
			}
		else if(section.equals("list-company"))
			{
			page=ListCompany.class;
			}
		else if(section.equals("create-ballot"))
			{
			page=CreateBallot.class;
			}
		else if(section.equals("show-adminBallot"))
			{
			page=BallotList.class;
			}
		else							//This handle the upper menu
		{
			visibilityCompany=false;
			visibilityUser=false;
			visibilityBallot=false;
			visibilityCensus=false;
			if(section.equals("index"))
				{page=Index.class;}
			else if(section.equals("about"))
				{page=About.class;}
			else if(section.equals("publics"))
				{page=PublicBallots.class;}
			else if(section.equals("new-ballot"))
				{page=BallotWizzard.class;}
			else if(section.equals("new-user1"))
			{
				if(datasession.isAdmin())
				{
					page=AdminCreateProfile.class;
				}
				else
				{
					page=CreateProfile.class;
				}
			}
			else if(section.equals("contact"))
				{page=Contact.class;}
			else if(section.equals("LogIn"))
				{page=LogIn.class;}
			else if(section.equals("methods"))
				{page=MethodsInfo.class;}
			else if(section.equals("company-login"))
				{
					page=CompanyLogIn.class;
				}
		}
		if(request.isXHR())
		{
			ajaxResponseRenderer.addRender("censusZone",censusZone).addRender("userZone", userZone).addRender("ballotZone", ballotZone).addRender("companyZone", companyZone);
		}
		return page;
	}
}
