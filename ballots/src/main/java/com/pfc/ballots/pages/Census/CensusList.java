package com.pfc.ballots.pages.Census;

import java.util.List;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;

import com.pfc.ballots.dao.CensusDao;
import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.dao.UserDao;
import com.pfc.ballots.data.DataSession;
import com.pfc.ballots.entities.Census;
import com.pfc.ballots.pages.Index;
import com.pfc.ballots.pages.SessionExpired;
import com.pfc.ballots.pages.UnauthorizedAttempt;
import com.pfc.ballots.pages.admin.AdminMail;

public class CensusList {

	
	FactoryDao DB4O=FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
	UserDao userDao=null;
	CensusDao censusDao=null;
	
	
	@InjectPage
	private UsersCounted usersCounted;
	
	@Property
	private Census census;
	

	@SessionState
	private DataSession datasession;
	public CensusList()
	{
		userDao=DB4O.getUsuarioDao(datasession.getDBName());
		censusDao= DB4O.getCensusDao(datasession.getDBName());
		
	}
	
	public List<Census> getCensuses()
	{
		//SUSTITUIR POR CENSUS  DE EL USUARIO
		return censusDao.getByOwnerId(datasession.getId());
	}
	public void onActionFromRemoveBut(String idCensus)
	{
		Census tempCensus=censusDao.getById(idCensus);
		userDao.removeCensusToProfiles(tempCensus.getUsersCounted(), idCensus);
		censusDao.deleteById(idCensus);		
	}
	
	public Object onActionFromDetailsBut(String idCensus)
	{
		usersCounted.setup(idCensus);
		return usersCounted;
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
				if(datasession.isMaker())
				{
					return null;
				}
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

