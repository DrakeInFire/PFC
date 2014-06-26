package com.pfc.ballots.pages.Company;

import javax.inject.Inject;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;

import com.pfc.ballots.dao.CompanyDao;
import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.dao.UserDao;
import com.pfc.ballots.data.DataSession;
import com.pfc.ballots.entities.Company;
import com.pfc.ballots.entities.Profile;
import com.pfc.ballots.pages.Index;
import com.pfc.ballots.pages.SessionExpired;
import com.pfc.ballots.pages.UnauthorizedAttempt;
import com.pfc.ballots.pages.admin.AdminMail;
import com.pfc.ballots.util.Encryption;
import com.pfc.ballots.util.UUID;

public class CreateCompany {
	
	@SessionState
	private DataSession datasession;
	
	@Inject
    private ComponentResources componentResources;
	
	
	/// Form objects
	@Persist
	@Property
	private Company company;
	@Persist
	@Property
	private Profile profile;
	
	@Property
	private String password;
	@Property
	private String repeat;

	//Form booleans
	@Property
	@Persist(PersistenceConstants.FLASH)
	private boolean isnotPassOk;
	@Property
	@Persist(PersistenceConstants.FLASH)
	private boolean isnotCompanyNameAvalible;
	@Property
	@Persist(PersistenceConstants.FLASH)
	private boolean isnotDBNameAvalible;
	
	//FirstTime enter in the page
	private boolean isnotFirstTime;
	
	
	//****************************************Initialize DAO****************************//
		FactoryDao DB4O =FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
		CompanyDao companyDao= DB4O.getCompanyDao();
		UserDao userDao =null;
		

	void setupRender() 
	{
		if(!isnotFirstTime)
		{
			profile=new Profile();
			company=new Company();
			isnotFirstTime=true;
		}
		else if(!isnotPassOk && !isnotCompanyNameAvalible && !isnotDBNameAvalible)
		{
			componentResources.discardPersistentFieldChanges();
			profile=new Profile();
			company=new Company();
		}
	}

	void onSuccess()
	{
		company.setCompanyName(company.getCompanyName().toLowerCase());
		if(companyDao.isCompanyRegistred(company.getCompanyName()))
		{
			isnotCompanyNameAvalible=true;
		}
		company.setDBName(company.getDBName()+".dat");
		if(companyDao.isDBNameRegistred(company.getDBName()))
		{
			isnotDBNameAvalible=true;
		}
		if(!password.equals(repeat))
		{
			isnotPassOk=true;
		}
		//If all data are correct, store them in database
		if(!isnotPassOk && !isnotCompanyNameAvalible && !isnotDBNameAvalible)
		{
			userDao=DB4O.getUsuarioDao(company.getDBName());
			company.setId(UUID.generate());
			profile.setId(UUID.generate());
			company.setIdAdmin(profile.getId());
			company.setRegDatetoActual();
			profile.setRegDatetoActual();
			String encrypt=Encryption.getStringMessageDigest(password, Encryption.SHA1);
			profile.setAdmin(true);
			profile.setEmail(company.getAdminEmail());
			profile.setPassword(encrypt);
			profile.setPlain(password);
			profile.setOwner(true);
			
			userDao.store(profile);
			companyDao.store(company);
			componentResources.discardPersistentFieldChanges();
			
		}
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
		switch(datasession.sessionState())
		{
			case 0:
				return UnauthorizedAttempt.class;
			case 1:
				if(datasession.isMainAdmin())
				{
					return null;
				}
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
