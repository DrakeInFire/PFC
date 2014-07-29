package com.pfc.ballots.pages.Census;

import java.util.List;

import javax.inject.Inject;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;

import com.pfc.ballots.dao.CensusDao;
import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.dao.UserDao;
import com.pfc.ballots.data.DataSession;
import com.pfc.ballots.entities.Census;
import com.pfc.ballots.pages.Index;
import com.pfc.ballots.pages.SessionExpired;
import com.pfc.ballots.pages.UnauthorizedAttempt;
import com.pfc.ballots.pages.admin.AdminMail;

public class AdminCensus {

	
	FactoryDao DB4O=FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
	@Persist
	UserDao userDao;
	@Persist
	CensusDao censusDao;
	
	@SessionState
	private DataSession datasession;
	
	@Inject
	ComponentResources componentResources;
	
	@InjectPage
	private UsersCounted usersCounted;
	
	@InjectComponent
	private Zone censusGridZone;
	
	@Inject
	private Request request;
	
	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;
		
	@Property
	private Census census;
	
	@Persist
	private List<Census> censuses;
	
	
	public void setupRender()
	{
		componentResources.discardPersistentFieldChanges();
		userDao=DB4O.getUsuarioDao(datasession.getDBName());
		censusDao= DB4O.getCensusDao(datasession.getDBName());
		searched=false;
	}
	
	public List<Census> getCensuses()
	{
		if(censuses==null)
		{
			censuses=censusDao.retrieveAll();
		}
		if(searched)
		{
			String id=userDao.getIdByEmail(email);
			if(id==null)
			{
				censuses=null;
			}
			else
			{
				censuses=censusDao.getByOwnerId(id);
			}
		}
		return censuses;
	}
	public Object onActionFromDetailsbut(String idCensus)
	{
		usersCounted.setup(idCensus);
		return usersCounted;
	}
	public void onActionFromRemovebut(String idCensus)
	{
		if(request.isXHR())
		{
			for(Census temp:censuses)
			{
				if(temp.getId().equals(idCensus))
				{
					userDao.removeCensusToProfiles(temp.getUsersCounted(), idCensus);
					censuses.remove(temp);
				}
			}
			censusDao.deleteById(idCensus);
			ajaxResponseRenderer.addRender("censusGridZone", censusGridZone);
		}
	}
	
	

	  ////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////// SEARCH ZONE //////////////////////////////////// 
	////////////////////////////////////////////////////////////////////////////////////
	
	@Property
	@Persist
	private String email;

	@Persist
	private boolean searched;
	
	public void onValidateFromSearchForm()
	{
		email=email.toLowerCase();
	}

	public void onSuccessFromSearchForm()
	{
		System.out.println("Email->"+email);
		if(request.isXHR())
		{
			searched=true;
			ajaxResponseRenderer.addRender("censusGridZone", censusGridZone);
			
		}
	}
	
	public void onActionFromShowAll()
	{
		if(request.isXHR())
		{
			censuses=null;
			searched=false;
			ajaxResponseRenderer.addRender("censusGridZone", censusGridZone);
		}
	}
	
	  ////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////// ON ACTIVATE //////////////////////////////////// 
	////////////////////////////////////////////////////////////////////////////////////
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
