package com.pfc.ballots.pages.Census;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;

import com.pfc.ballots.dao.BallotDao;
import com.pfc.ballots.dao.CensusDao;
import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.dao.ProfileCensedInDao;
import com.pfc.ballots.dao.UserDao;
import com.pfc.ballots.dao.VoteDao;
import com.pfc.ballots.data.DataSession;
import com.pfc.ballots.entities.Ballot;
import com.pfc.ballots.entities.Census;
import com.pfc.ballots.entities.Profile;
import com.pfc.ballots.pages.Index;
import com.pfc.ballots.pages.SessionExpired;
import com.pfc.ballots.pages.UnauthorizedAttempt;
import com.pfc.ballots.pages.admin.AdminMail;
/**
 * 
 * UsersCounted class is the controller for the UsersCounted page that
 * shows user counted in a census
 * 
 * @author Mario Temprano Martin
 * @version 1.0 MAY-2014
 */

public class UsersCounted {

	@Persist
	private String censusId;
	@Persist
	private Census census;
	
	@SessionState
	private DataSession datasession;
	
	@Inject
	ComponentResources componentResources;
	
	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;
	
	@Inject
	private Request request;
	
	//////////////////////////////////////////////////// DAO  ////////////////////////////////////////////////
	FactoryDao DB4O=FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
	@Persist
	CensusDao censusDao;
	@Persist
	UserDao userDao;
	@Persist
	ProfileCensedInDao censedInDao;
	BallotDao ballotDao;
	VoteDao voteDao;
	
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////// BUILDER AND SETUP ///////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Initialize data
	 */
	public void setupRender()
	{
		censusDao=DB4O.getCensusDao(datasession.getDBName());
		userDao=DB4O.getUsuarioDao(datasession.getDBName());
		censedInDao=DB4O.getProfileCensedInDao(datasession.getDBName());
		
		showUsersCountedZone=false;
		showNameZone=false;
		nameNotAvalible=false;
		showBasicSearch=false;
		showAdvancedSearch=false;
		showSearchType=false;
		email=null;
		example=null;
		editUsers=null;
		
		
	}
	/**
	 * Set the id of the census to show 
	 * @param censusId
	 */
	public void setup(String censusId)
	{
		this.censusId=censusId;
		census=null;
		censusName=null;
	}
	
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ///////////////////////////////////////////////// USERS COUNTED STUFF ///////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@InjectComponent
	private Zone usersCountedZone;
	
	@Persist
	private String censusName;
	
	@Persist
	private List<Profile> users;
	
	@Property
	private Profile user;
	
	@Property
	@Persist
	private boolean showUsersCountedZone;
	
	public void setCensusName(String censusName)
	{
		this.censusName=censusName;
	}
	public String getCensusName()
	{
		if(censusName==null || census==null)
		{
			census=censusDao.getById(censusId);
			censusName=census.getCensusName();
		}
		return censusName;
	}
	public List<Profile> getUsers()
	{
		if(users==null)
		{
			users=userDao.getProfileById(census.getUsersCounted());
		}
		return users;
	}
	
	/**
	 * Shows an edit form to make changes in a census
	 */
	public void onActionFromEditbut()
	{
		showUsersCountedZone=true;
		showNameZone=true;
		nameNotAvalible=false;
		showBasicSearch=true;
		showSearchType=true;
		profile=new Profile();
		
		editUsers= new LinkedList<Profile>();
		map=new HashMap<String,Profile>();
		for(Profile temp:users)
		{
			editUsers.add(temp);
			map.put(temp.getId(), temp);
		}
		
		if(request.isXHR())
		{
			ajaxResponseRenderer.addRender("usersCountedZone", usersCountedZone).addRender("nameZone",nameZone).addRender("upperBackZone",upperBackZone)
								.addRender("lowerBackZone", lowerBackZone).addRender("basicSearchZone", basicSearchZone).addRender("searchTypeZone",searchTypeZone)
								.addRender("searchListZone", searchListZone).addRender("usersCountedEditZone",usersCountedEditZone)
								.addRender("saveCancelZone",saveCancelZone);
		}
	}
	
	

	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////////////// NAME ZONE STUFF //////////////////////////////////////////////////// 
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
	@InjectComponent
	private Zone nameZone;
	
	@Property
	@Persist
	private boolean showNameZone;
	
	@Property
	@Persist
	private boolean nameNotAvalible;
	
	/**
	 * Changes the census name 
	 */
	public void onSuccessFromNameForm()
	{
		if(!census.getCensusName().equals(censusName))
		{	
			if(censusDao.isNameInUse(censusName, datasession.getId()))
			{
				nameNotAvalible=true;
				if(request.isXHR())
				{
					ajaxResponseRenderer.addRender("nameZone",nameZone);
				}
			}
			else
			{
				nameNotAvalible=false;
				census.setCensusName(censusName);
				censusDao.update(census);
			}
		}
	}
	/**
	 * Cancels the change of the name
	 */
	public void onActionFromCancelNameBut()
	{
		censusName=census.getCensusName();
		if(request.isXHR())
		{
			ajaxResponseRenderer.addRender("nameZone", nameZone);
		}
	}
	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 //////////////////////////////////////////////////// BASIC SEARCH STUFF //////////////////////////////////////////////////// 
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@InjectComponent
	private Zone searchTypeZone;
	
	@Property
	@Persist
	private boolean showSearchType;
	
	@Property
	@Persist
	private Profile example;
	
	/**
	 * Changes the kind of search
	 */
	public void onSwitchSearch()
	{
		if(showBasicSearch)
		{
			showBasicSearch=false;
			showAdvancedSearch=true;
		}
		else
		{
			email=null;
			showBasicSearch=true;
			showAdvancedSearch=false;
		}
		if(request.isXHR())
		{
			ajaxResponseRenderer.addRender("searchTypeZone", searchTypeZone).addRender("basicSearchZone", basicSearchZone).addRender("advancedSearchZone", advancedSearchZone);
		}
	}
	/**
	 * Resets the search values
	 */
	public void onActionFromReset()
	{
		firstName=null;
		lastName=null;
		email=null;
		profile=new Profile();
		if(request.isXHR())
		{
			ajaxResponseRenderer.addRender("basicSearchZone", basicSearchZone).addRender("advancedSearchZone", advancedSearchZone);
		}
	}
	
	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 //////////////////////////////////////////////////// BASIC SEARCH STUFF //////////////////////////////////////////////////// 
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@InjectComponent
	private Zone basicSearchZone;
	
	
	@Property
	@Persist
	private boolean showBasicSearch;
	
	@Property
	@Persist
	private String email;
	/**
	 * Checks the basic search values
	 */
	public void onValidateFromBasicSearchForm()
	{
		example=null;
		if(email!=null)
		{
			if(email.trim().length()==0)
			{
				email=null;
				
			}
			else
			{
				example=new Profile();
				example.setEmail(email);
			}
		}
	}
	/**
	 * Allows the basic search
	 */
	public void onSuccessFromBasicSearchForm()
	{
		if(request.isXHR())
		{
			ajaxResponseRenderer.addRender("searchListZone", searchListZone);
		}
	}
	/**
	 * Shows all the users
	 */
	public void onActionFromShowAll()
	{
		example=new Profile();
		if(request.isXHR())
		{
			ajaxResponseRenderer.addRender("searchListZone", searchListZone);
		}
	}
	
	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 //////////////////////////////////////////////////// ADVANCED ZONE STUFF /////////////////////////////////////////////////// 
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@InjectComponent
	private Zone advancedSearchZone;
	
	@Property
	@Persist
	private boolean showAdvancedSearch;
	
	@Persist
	@Property
	private String firstName;
	
	@Persist
	@Property
	private String lastName;
	
	@Property
	@Persist
	private Profile profile;
	
	/**
	 * Checks the advanced search values
	 */
	public void onValidateFromAdvancedSearchForm()
	{
		if(firstName!=null)
		{
			if(firstName.trim().length()==0)
				firstName=null;
		}
		profile.setFirstName(firstName);
		if(lastName!=null)
		{
			if(lastName.trim().length()==0)
				lastName=null;
		}
		profile.setLastName(lastName);
		if(profile.getUniversity()!=null)
		{
			if(profile.getUniversity().trim().length()==0)
				profile.setUniversity(null);
		}
		if(profile.getCity()!=null)
		{
			if(profile.getCity().trim().length()==0)
				profile.setCity(null);
		}
		if(profile.getCountry()!=null)
		{
			if(profile.getCountry().trim().length()==0)
				profile.setCountry(null);
		}
	}
	/**
	 * Allows the basic search
	 */
	public void onSuccessFromAdvancedSearchForm()
	{
		example=new Profile(profile);
		if(request.isXHR())
		{
			ajaxResponseRenderer.addRender("searchListZone", searchListZone);
		}
	}
	
	
	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////// SEARCH LIST ZONE STUFF /////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@InjectComponent
	private Zone searchListZone;
	
	@Persist
	private List<Profile> searchList;
	
	public boolean isShowSearchList()
	{
		return (example==null)? false:true;
	}
	
	public List<Profile> getSearchList()
	{
		if(example==null)
		{
			System.out.println("return null");
			searchList=null;
			
		}
		else
		{
			System.out.println("BY Example");
			
			searchList= userDao.getByExample(example);
		}
		return searchList;
	}
	public boolean isAdd()
	{
		Profile temp=map.get(user.getId());
		if(temp!=null)
		{
			return true;
		}
		return false;
	}
	/**
	 * Add a user to the census
	 * @param add
	 */
	public void setAdd(boolean add)
	{
		if(add)
		{
			Profile temp=map.get(user.getId());
			if(temp==null)
			{
				map.put(user.getId(),user);
				editUsers.add(user);
			}
		}
		else
		{
			Profile temp=map.get(user.getId());
			if(temp!=null)
			{
				map.remove(user.getId());
				editUsers.remove(temp);
			}
		}
		
	}
	/**
	 * Allows to add the selected users to the census
	 */
	public void onSuccessFromAddForm()
	{
		if(request.isXHR())
		{
			ajaxResponseRenderer.addRender("searchListZone", searchListZone).addRender("usersCountedEditZone", usersCountedEditZone).addRender("saveCancelZone",saveCancelZone);
			
		}
	}
	/**
	 * Adds all the users to the census
	 */
	public void onActionFromAddAll()
	{
		for(Profile temp:searchList)
		{
			if(map.get(temp.getId())==null)
			{
				map.put(temp.getId(), temp);
				editUsers.add(temp);
			}
		}
		ajaxResponseRenderer.addRender("searchListZone", searchListZone).addRender("usersCountedEditZone", usersCountedEditZone).addRender("saveCancelZone",saveCancelZone);
	}
	
	
		
	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////// USERS COUNTED EDIT ZONE STUFF //////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	@InjectComponent
	private Zone usersCountedEditZone;
	
	@Persist
	@Property
	private List<Profile> editUsers;
	
	
	
	@Persist
	private Map<String,Profile> map;
	
	public boolean isShowCountedEdit()
	{
		if(map!=null && showSearchType)
		{
			if(map.size()!=0)
				return true;
		}
		return false;
	}
	
	
	public boolean isRemove()
	{
		return false;
	}
	/**
	 * removes a user of the census
	 * @param remove
	 */
	public void setRemove(boolean remove)
	{
		if(remove)
		{
			Profile temp=map.get(user.getId());
			map.remove(user.getId());
			editUsers.remove(temp);
		}
	}
	/**
	 * Allows to remove a user of the census
	 */
	public void onSuccessFromRemoveForm()
	{
		if(request.isXHR())
		{
			ajaxResponseRenderer.addRender("searchListZone", searchListZone).addRender("usersCountedEditZone", usersCountedEditZone).addRender("saveCancelZone",saveCancelZone);
		}
	}
	/**
	 * Removes all the users of the census
	 */
	public void onActionFromRemoveAll()
	{
		map.clear();
		editUsers.clear();
		if(request.isXHR())
		{
			ajaxResponseRenderer.addRender("searchListZone", searchListZone).addRender("usersCountedEditZone", usersCountedEditZone).addRender("saveCancelZone",saveCancelZone);
		}
	}
	
	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     /////////////////////////////////////////////////// SAVE OR CANCEL CENSUS ////////////////////////////////////////////////// 
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@InjectComponent
	private Zone saveCancelZone;
	
	public boolean isShowSave()
	{
		if(showSearchType && map.size()!=0)
		{
			return true;
		}
		return false;
	}
	public boolean isShowCancel()
	{
		if(showSearchType)
		{
			return true;
		}
		return false;
	}
	/**
	 * Cancel the edition of the census
	 */
	public void onActionFromCancelbut()
	{
		editUsers.clear();
		map.clear();
		for(Profile temp:users)
		{
			editUsers.add(temp);
			map.put(temp.getId(), temp);
		}
		
		
		if(request.isXHR())
		{
			ajaxResponseRenderer.addRender("searchListZone", searchListZone).addRender("usersCountedEditZone", usersCountedEditZone).addRender("saveCancelZone",saveCancelZone);
		}
	}
	/**
	 * Update the census
	 */
	public void onActionFromSavebut()
	{
		Census old=new Census(census);
		census.emptyUsersCounted();
		users=editUsers;
		for(Profile temp:editUsers)
		{
			census.addIdToUsersCounted(temp.getId());
		}
		List<String> added=new LinkedList<String>();
		List<String> removed=new LinkedList<String>();
		old.calcDifference(census, added, removed);
		censedInDao.addAndRemoveIds(old.getId(), added, removed);
		censusDao.update(census);
		ballotDao=DB4O.getBallotDao(datasession.getDBName());
		List<String> idBallot=ballotDao.getIdByCensusId(census.getId());
		System.out.println("SIZE"+idBallot.size());
		voteDao=DB4O.getVoteDao(datasession.getDBName());
		voteDao.addVotes(added, idBallot);
		voteDao.deleteVotesNotDone(removed, idBallot);
	}
	
	
	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ///////////////////////////////////////////////////  BACK BUTTONS STUFF //////////////////////////////////////////////////// 
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@InjectComponent
	private Zone upperBackZone;
	@InjectComponent
	private Zone lowerBackZone;
	
	
	public void onBack()
	{
		censusName=census.getCensusName();
		showUsersCountedZone=false;
		showNameZone=false;
		showBasicSearch=false;
		showAdvancedSearch=false;
		showSearchType=false;
		
		profile=null;
		firstName=null;
		
		editUsers=null;
		map=null;
		
		if(request.isXHR())
		{
			ajaxResponseRenderer.addRender("usersCountedZone", usersCountedZone).addRender("nameZone",nameZone).addRender("upperBackZone",upperBackZone)
								.addRender("lowerBackZone", lowerBackZone).addRender("searchTypeZone",searchTypeZone)
								.addRender("basicSearchZone", basicSearchZone).addRender("advancedSearchZone", advancedSearchZone).addRender("searchListZone", searchListZone)
								.addRender("usersCountedEditZone",usersCountedEditZone).addRender("saveCancelZone",saveCancelZone);
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
				if(datasession.isMaker() && censusId!=null)
				{
					return null;
				}
				return UnauthorizedAttempt.class;
			case 2:
				if(censusId==null)
				{
					return Index.class;
				}
				return null;
			case 3:
				return SessionExpired.class;
			default:
				return Index.class;
		}
		
	}
	
}
