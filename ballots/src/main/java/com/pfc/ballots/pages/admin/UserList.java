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
	private Zone nomailgrid;
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
	@Property
	@Persist
	private List<Profile> nomails;
	@Property
	private Profile nomail;
	@Property
	@Persist
	private boolean showNoMail;
	
	
	@Persist
	@Property
	private boolean editing;
	@Property
	private boolean nonavalible;
	@Persist
	private boolean normalEdit;
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
		nomails=userDao.getNoMailProfiles();
		if(nomails==null || nomails.size()==0)
		{showNoMail=false;}
		else
		{showNoMail=true;}
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
				nomail=lookforidnomail(editprof.getId());
				
				
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
							if(nomail!=null)
							{
								if(!editprof.getEmail().contains("@nomail"))
								{
									nomails.remove(nomail);
								}
							}
							else
							{
								if(editprof.getEmail().contains("@nomail"))
								{
									nomails.add(user);
								}
							}
							
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
			
			
			ajaxResponseRenderer.addRender("usergrid",usergrid).addRender("editZone",editZone).addRender("nomailgrid",nomailgrid);
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
			ajaxResponseRenderer.addRender("usergrid",usergrid).addRender("editZone",editZone);
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
			normalEdit=true;
			nonavalible=false;
			user=lookforid(id);
			editprof=new Profile(user);
			ajaxResponseRenderer.addRender("usergrid",usergrid).addRender("nomailgrid",nomailgrid).addRender("editZone",editZone);
		}
	}
	public void onActionFromEditnomail(String id)
	{
		if(request.isXHR())
		{
			editing=true;
			normalEdit=false;
			nonavalible=false;
			user=lookforid(id);
			editprof=new Profile(user);
			ajaxResponseRenderer.addRender("usergrid",usergrid).addRender("nomailgrid",nomailgrid).addRender("editZone",editZone);
		}
	}
	
	public void onActionFromEndbut()
	{
		if(request.isXHR())
		{
			editing=false;
			ajaxResponseRenderer.addRender("usergrid",usergrid).addRender("editZone",editZone);
		}
	}
	public void onActionFromDeleteuser(String email)
	{
		if(request.isXHR())
		{
			userDao.deleteByEmail(email);
			userLogedDao.deleteByEmail(email);
			if(email.contains("@nomail"))
			{
			nomail=lookfornomail(email);
			nomails.remove(nomail);
			}
			user=lookforemail(email);
			users.remove(user);
			ajaxResponseRenderer.addRender("nomailgrid",nomailgrid).addRender("usergrid",usergrid);
		}
	}
	public void onActionFromDeletenomail(String email)
	{
		if(request.isXHR())
		{
			userDao.deleteByEmail(email);
			userLogedDao.deleteByEmail(email);
			nomail=lookfornomail(email);
			nomails.remove(nomail);
			user=lookforemail(email);
			users.remove(user);
			ajaxResponseRenderer.addRender("nomailgrid",nomailgrid).addRender("usergrid",usergrid);
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
	private Profile lookfornomail(String email)
	{
		for(int i=0;i<nomails.size();i++)
		{
			if(nomails.get(i).getEmail().equals(email))
			{
				return nomails.get(i);
			}
		}
		return null;
		
	}
	private Profile lookforidnomail(String id)
	{
		for(int i=0;i<nomails.size();i++)
		{
			if(nomails.get(i).getId().equals(id))
			{
				return nomails.get(i);
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
