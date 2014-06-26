package com.pfc.ballots.pages.Company;

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
import com.pfc.ballots.entities.Company;
import com.pfc.ballots.entities.Profile;
import com.pfc.ballots.pages.Index;
import com.pfc.ballots.pages.SessionExpired;
import com.pfc.ballots.pages.UnauthorizedAttempt;
import com.pfc.ballots.pages.admin.AdminMail;




public class ListCompanyUsers {

	@SessionState
	private DataSession datasession;
	
	@Property
	private Profile user;
	
	@InjectComponent
	private Zone userGridZone;
	
	@Inject
	private Request request;
	
	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;
	
	@Property
	@Persist
	private String DBName;
	@Property
	@Persist
	private String companyName;
	
	@Persist
	private List<Profile> users;
	
	@Persist
	@Property
	private boolean editing;
	@Persist
	@Property
	private boolean nonavalible;
	
	private enum Actions{
		SAVE,CANCEL
	};
	
	private Actions action;
	//******************************************     DAO    ***************************************************//
	FactoryDao DB4O=FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
	@Persist
	UserDao userDao;
	@Persist
	UserLogedDao userLogedDao;
	
	public void setup(String companyName,String DBName)
	{
		this.DBName=DBName;
		this.companyName=companyName;
		userDao=DB4O.getUsuarioDao(DBName);
		userLogedDao=DB4O.getUserLogedDao(DBName);
		
	}
	public void setupRender()
	{
		users=null;
		nonavalible=false;
		editing=false;
		showSure=false;
	}
	public List<Profile> getUsers()
	{
		if(users==null)
		{
			users=userDao.RetrieveAllProfiles();
		}
		return users;
	}
	
	public void onActionFromEditbut(String id)
	{
		if(request.isXHR())
		{
			user=lookforid(id);
			editprof=new Profile(user);
			editing=true; 
			nonavalible=false;
			
			ajaxResponseRenderer.addRender("userGridZone", userGridZone).addRender("editZone", editZone);
		}
	}
	
	public void onActionFromDeleteuser(String email)
	{
		if(request.isXHR())
		{
			Profile temp=lookforemail(email);
			userLogedDao.deleteByEmail(email);
			userDao.deleteById(temp.getId());
			users.remove(temp);
			
			ajaxResponseRenderer.addRender("userGridZone", userGridZone);
		}
	}
	public void onActionFromMakeOwnerBut(String idNewOwner)
	{
		if(request.isXHR())
		{
			showSure=true;
			toCheck=lookforid(idNewOwner);
			ajaxResponseRenderer.addRender("areuSureZone",areuSureZone).addRender("userGridZone", userGridZone);
		}
	}
	
	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////////// EDIT ZONE //////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@InjectComponent
	private Zone editZone;
	
	@Persist
	@Property
	private Profile editprof;
	
	@InjectComponent
	private Form editForm;
	
	
	public void onSelectedFromSave()
	{
		action=Actions.SAVE;
	}
	public void onSelectedFromCancel()
	{
		action=Actions.CANCEL;
	}
	
	public void onSuccessFromEditForm()
	{
		boolean success=true;
		if(request.isXHR())
		{
		
			if(action==Actions.SAVE)
			{
				user=lookforid(editprof.getId());
				if(!user.equals(editprof))//EL PERFIL HA CAMBIADO
				{
					if(!user.getEmail().equals(editprof.getEmail()))//EL EMAIL HA CAMBIADO
					{
						if(userDao.isProfileRegistred(editprof.getEmail()))//EL EMAIL NUEVO NO ESTA DISPONIBLE
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
			
			
			
			ajaxResponseRenderer.addRender("userGridZone",userGridZone).addRender("editZone",editZone);
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
			ajaxResponseRenderer.addRender("userGridZone",userGridZone).addRender("editZone",editZone);
			}
			System.out.println("FAILURE");
		}
		editForm.clearErrors();
	}
	
	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////////// AREUSURE ZONE //////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@InjectComponent
	private Zone areuSureZone;
	
	@Property
	@Persist
	private boolean showSure;
	
	@Property
	@Persist
	private Profile toCheck;
	
	
	public void onActionFromSuccessChangeOwnerBut()
	{
		if(request.isXHR())
		{
			Profile owner=userDao.getOwner();
			userLogedDao.deleteByEmail(owner.getEmail());
			userLogedDao.deleteByEmail(toCheck.getEmail());
			userDao.setOwner(toCheck.getId());
			users=null;
			showSure=false;
			ajaxResponseRenderer.addRender("areuSureZone",areuSureZone).addRender("userGridZone", userGridZone);
		}
	}
	public void onActionFromCancelChangeOwnerBut()
	{
		if(request.isXHR())
		{
			showSure=false;
			ajaxResponseRenderer.addRender("areuSureZone",areuSureZone).addRender("userGridZone", userGridZone);
		}
	}
	  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ////////////////////////////////////////////////////// UTILS /////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
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
	
	public boolean isOwner()
	{
		if(user.isOwner())
		{
			return true;
		}
		return false;
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////////////// ON ACTIVATE //////////////////////////////////////////////////////// 
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
		if(DBName==null)
		{
			return UnauthorizedAttempt.class;
		}
		switch(datasession.sessionState())
		{
			case 0:
				return UnauthorizedAttempt.class;
			case 1:
				if(datasession.isMainAdmin())
					{return null;}
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
