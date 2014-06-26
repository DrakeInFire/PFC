package com.pfc.ballots.data;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import org.apache.tapestry5.services.Request;

import com.pfc.ballots.dao.CompanyDao;
import com.pfc.ballots.dao.EmailAccountDao;
import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.dao.UserLogedDao;
import com.pfc.ballots.entities.Company;
import com.pfc.ballots.entities.EmailAccount;
import com.pfc.ballots.entities.Profile;
import com.pfc.ballots.util.Mail;
import com.pfc.ballots.util.UUID;

public class DataSession {
	
	public static final long SESSION_TIME = 45;
	
	private String DBName;
	private String company;
	private String idSession;
	private boolean owner;
	private String id;//registred user id
	private String email;
	private Date logDate;
	private boolean admin;
	private boolean maker;
	private boolean loged;
	private FactoryDao DB4O =FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
	private CompanyDao companyDao=DB4O.getCompanyDao();
	private EmailAccountDao emailDao=null;
	private UserLogedDao lgdDao=null;
	
	@Inject
	private Request request;
	
	
	public DataSession()
	{
		logout();
		emailDao=DB4O.getEmailAccountDao();
	}
	 ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////// GETTER AND SETTER //////////////////////////////////////////
   ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	public String getDBName() {
		return DBName;
	}
	
	public void setDBName(String dBName) {
		DBName = dBName;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public Date getLogDate()
	{
		return logDate;
	}
	
	public void setLogDateCurrentTime()
	{
		logDate=new Date();
	}
	
	public boolean isLoged() {
		return loged;
	}

	public void setLoged(boolean loged) {
		this.loged = loged;
	}
	
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	
	public boolean isAdmin() {
		return admin;
	}
	
	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public boolean isMaker() {
		return maker;
	}

	public void setMaker(boolean maker) {
		this.maker = maker;
	}
	public String getIdSession() {
		return idSession;
	}
	public void setIdSession(String idSession) {
		this.idSession = idSession;
	}
	public boolean isOwner() {
		return owner;
	}
	public void setOwner(boolean owner) {
		this.owner = owner;
	}
	

	 //////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////// LOGIN UTILITY /////////////////////////////////////////////
   //////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	//Normal login
	public void login(Profile profile)	
	{
		company="main";
		setIdSession(UUID.generate());
		setOwner(profile.isOwner());
		id=profile.getId();
		email=profile.getEmail();
		maker=profile.isMaker();
		admin=profile.isAdmin();
		this.DBName=null;//This means that is main database
		logDate=new Date();
		loged=true;
		lgdDao=DB4O.getUserLogedDao(DBName);
		lgdDao.clearSessions(SESSION_TIME);
	}
	//Login for companies
	public void login(Profile profile,Company company)
	{
		setIdSession(UUID.generate());
		this.company=company.getCompanyName();
		owner=profile.isOwner();
		id=profile.getId();
		email=profile.getEmail();
		maker=profile.isMaker();
		admin=profile.isAdmin();
		this.DBName=company.getDBName();
		logDate=new Date();
		loged=true;
		lgdDao=DB4O.getUserLogedDao(DBName);
		lgdDao.clearSessions(SESSION_TIME);
	}
	
	
	public void logout()
	{
		if(lgdDao!=null)
			lgdDao.clearSessions(SESSION_TIME);
		id=null;
		company=null;
		maker=false;
		admin=false;
		DBName=null;
		logDate=null;
		email=null;
		loged=false;
		lgdDao=null;
		owner=false;
		setIdSession(null);
		
	}
	 ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////// SESSION STATE //////////////////////////////////////////////
   ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 
	 * return an int with the state of the session
	 * 		0->UserLogedIn;
	 * 		1->AdminLoged
	 * 		2->MainAdminLoged no email of the apliction configured
	 * 		3->not loged
	 * 		4->Session expired or kicked from server
	 */
	
	
	public int sessionState()
	{	
		if(!isLoged())
			return 3;
		else
		{
			Date actualDate=new Date();
			Calendar calActualDate=Calendar.getInstance();
			Calendar calLogDate=Calendar.getInstance();
			
			calActualDate.setTime(actualDate);
			calLogDate.setTime(logDate);
			long milisActualDate=calActualDate.getTimeInMillis();
			long milisLogDate=calLogDate.getTimeInMillis();
			long diff=milisActualDate-milisLogDate;
			long diffMin=diff/(60*1000);
			if(diffMin>SESSION_TIME)
			{
				lgdDao.delete(idSession);
				logout();
				request.getSession(true).invalidate();
				return 5;
			}
			else
			{
				if(lgdDao.isLogedIn(idSession))
				{
					if(isAdmin())
					{
						EmailAccount account=emailDao.getAccount();
						if(account==null)
						{
							return 2;
						}
						if(isMainAdmin() && !Mail.checkAccount(emailDao.getAccount()))
						{
							return 2;
						}
						return 1;
					}
					else
					{
						return 0;
					}
				
				}
				else
				{
					logout();
					request.getSession(true).invalidate();
					return 4;
				}
			}
		}
		
	}
	
	 ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////// SESSION UTILS //////////////////////////////////////////////
   ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	public boolean isMainOwner()
	{
		if(company.equals("main") && owner==true)
		{
			return true;
		}
		return false;
	}
	public boolean isCompanyOwner()
	{
		if(company.equals("main"))
		{
			return false;
		}
		else if(owner)
		{
			return true;
		}
		return false;
	}
	public Company isCompanyOwner(String leftNull)
	{
		Company temp=companyDao.getCompanyByName(company);
		if(temp==null)
		{
			return null;
		}
		if(temp.getIdAdmin().equals(id))
		{
			return temp;
		}
		return null;
	}
	public boolean isCompanyUser()
	{
		if(DBName==null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	public boolean isMainUser()
	{
		if(DBName==null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}


	public boolean isMainAdmin(){
		if(isAdmin() && isMainUser())
			return true;
		return false;
	}
	public boolean isCompanyAdmin()
	{
		if(isAdmin() && !isMainUser())
		{
			return true;
		}
		return false;
	}
	

	public boolean isMailWorking()
	{
		return Mail.checkAccount(emailDao.getAccount());
	}


}
