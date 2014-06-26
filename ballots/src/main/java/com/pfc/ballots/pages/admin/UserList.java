package com.pfc.ballots.pages.admin;

import java.util.List;

import javax.inject.Inject;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Form;
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

public class UserList {


	@InjectComponent
	private Zone usergrid;
	@InjectComponent
	private Zone editZone;
	@InjectComponent
	private Form editForm;
	
	@Inject
	private Request request;
	
	@SessionState
	private DataSession datasession;
	@Property
	@Persist
	private List<Profile> users;
	@Property
	private Profile user;
	
	@Persist
	@Property
	private boolean editing;
	@Property
	private boolean nonavalible;
	
	@Persist
	@Property
	private Profile editprof;
	
	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;
	
	private enum Actions{
		SAVE,CANCEL
	};
	
	private Actions action;
	
	//****************************************Initialize DAO****************************//
	FactoryDao DB4O =FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
	@Persist
	UserDao userDao;
	@Persist
	UserLogedDao userLogedDao;
	
	public void setupRender()
	{
		editing=false;
		userDao=DB4O.getUsuarioDao(datasession.getDBName());
		users=userDao.RetrieveAllProfilesSortLastLog();
		userLogedDao=DB4O.getUserLogedDao(datasession.getDBName());
		editprof=null;
	}
	
	public boolean isOwner()
	{
		if(user.isOwner())
			return true;
		else
			return false;
	}
	public void onSelectedFromSave()
	{
		action=Actions.SAVE;
	}
	public void onSelectedFromCancel()
	{
		action=Actions.CANCEL;
	}
	
	void onSuccessFromEditForm()
	{
		boolean success=true;
		if(request.isXHR())
		{
			if(action==Actions.SAVE)
			{	
				user=lookforid(editprof.getId());
				if(!editprof.equals(user))
				{
					if(!user.getEmail().equals(editprof.getEmail()))
					{
						if(userDao.isProfileRegistred(editprof.getEmail()))
						{
							nonavalible=true;
							success=false;
						}
						
					}
					if(success)
					{
						userLogedDao.deleteByEmail(user.getEmail());
						userDao.UpdateById(editprof);
						user.copy(editprof);
						editing=false;
					}
					
				}
				else
				{
					editing=false;
				}
			}
			if(action==Actions.CANCEL)
			{
				editing=false;
			}
			
			
			ajaxResponseRenderer.addRender(usergrid).addRender(editZone);
			System.out.println("SUCCESS");
		}
	}
	
	void onFailureFromEditForm()
	{
		
		if(request.isXHR())
		{
			if(action==Actions.CANCEL)
			{
			editing=false;
			ajaxResponseRenderer.addRender(usergrid).addRender(editZone);
			}
			System.out.println("FAILURE");
		}
		editForm.clearErrors();
	}
	
	
	public void onActionFromEditbut(String id)
	{
		if(request.isXHR())
		{
			editing=true;
			nonavalible=false;
			user=lookforid(id);
			editprof=new Profile(user);
			ajaxResponseRenderer.addRender(usergrid).addRender(editZone);
		}
	}
	
	public void onActionFromEndbut()
	{
		if(request.isXHR())
		{
			editing=false;
			ajaxResponseRenderer.addRender(usergrid).addRender(editZone);
		}
	}
	public void onActionFromDeleteuser(String email)
	{
		if(request.isXHR())
		{
			userDao.deleteByEmail(email);
			userLogedDao.deleteByEmail(email);
			user=lookforemail(email);
			users.remove(user);
			ajaxResponseRenderer.addRender(usergrid);
		}
	}
	private Profile lookforemail(String email)
	{
		for(int i=0;i<users.size();i++)
		{
			if(users.get(i).getEmail().equals(email))
			{
				return users.get(i);
			}
		}
		return null;
		
	}
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
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////////////// ON ACTIVATE /////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
				return null;
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
