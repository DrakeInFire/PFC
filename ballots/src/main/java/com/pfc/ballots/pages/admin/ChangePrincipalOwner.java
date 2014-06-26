package com.pfc.ballots.pages.admin;

import java.util.List;

import javax.inject.Inject;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;

import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.dao.UserDao;
import com.pfc.ballots.dao.UserLogedDao;
import com.pfc.ballots.data.DataSession;
import com.pfc.ballots.entities.Profile;
import com.pfc.ballots.pages.Index;
import com.pfc.ballots.pages.SessionExpired;
import com.pfc.ballots.pages.UnauthorizedAttempt;

public class ChangePrincipalOwner {


	@SessionState
	private DataSession datasession;
	@Inject
	private Request request;
	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;
	
	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 //////////////////////////////////////////////////////// DAO ///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	FactoryDao DB4O=FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
	@Persist
	UserDao userDao;
	@Persist
	UserLogedDao userLogedDao;
	
	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ////////////////////////////////////////////////// INITIALIZE PAGE /////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	public void setupRender()
	{
		showSure=false;
		userDao=DB4O.getUsuarioDao();		//A esta pagina solo podra acceder el propietario de la pagina principal, por lo que se instancia la BD principal
		userLogedDao=DB4O.getUserLogedDao();
	}
	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////////// USERGRID ZONE //////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@InjectComponent
	private Zone userGridZone;
	
	@Persist
	private List<Profile> users;
	@Property
	private Profile user;
	
	public List<Profile> getUsers()
	{
		if(users==null)
		{
			users=userDao.RetrieveAllProfiles();
			Profile temp=lookforid(datasession.getId());
			users.remove(temp);
		}
		return users;
	}
	
	public void onActionFromMakeOwnerBut(String newOwnerId)
	{
		if(request.isXHR())
		{
			newOwner=lookforid(newOwnerId);
			showSure=true;
			
			ajaxResponseRenderer.addRender("userGridZone", userGridZone).addRender("areuSureZone", areuSureZone);
		}
	}
	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////////// AREUSURE ZONE //////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@InjectComponent
	private Zone areuSureZone;

	@Property
	@Persist
	private boolean showSure;
	
	@Persist
	@Property
	private Profile newOwner;
	
	public Object onActionFromIsSureBut()
	{
		if(request.isXHR())
		{
			userLogedDao.deleteByEmail(newOwner.getEmail());
			userDao.setOwner(newOwner.getId());
			datasession.setOwner(false);
			return Index.class;
		}
		return null;
	}
	public void onActionFromNotSureBut()
	{
		if(request.isXHR())
		{
			showSure=false;
			ajaxResponseRenderer.addRender("userGridZone", userGridZone).addRender("areuSureZone", areuSureZone);
		}
		
	}
	
	
	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////////////// UTILS //////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	private Profile lookforid(String id)
	{
		for(int i=0;i<users.size();i++)
		{
			if(users.get(i).getId().equals(id))
			{
				return users.get(i);
			}
		}
		
		return null;
	}
	
     	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		 ///////////////////////////////////////////////// ONACTIVATE ///////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 *  * return an int with the state of the session
	 * 		0->UserLogedIn;
	 * 		1->AdminLoged
	 * 		2->MainAdminLoged no email of the apliction configured
	 * 		3->not loged
	 * 		4->Session expired or kicked from server
	 */
	public Object onActivate()
	{
		switch(datasession.sessionState())
		{
			case 0:
				return UnauthorizedAttempt.class;
			case 1:
				if(datasession.isMainOwner())
					return null;
				else
					return UnauthorizedAttempt.class;
			case 2:
				return AdminMail.class;
			case 3:
				return Index.class;
			case 4:
				return SessionExpired.class;
			default:
				return Index.class;
		}
			
	}

}
