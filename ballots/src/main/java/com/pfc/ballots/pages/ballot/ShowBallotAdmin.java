package com.pfc.ballots.pages.ballot;

import java.util.List;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;

import com.pfc.ballots.dao.BallotDao;
import com.pfc.ballots.dao.CensusDao;
import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.dao.KemenyDao;
import com.pfc.ballots.dao.RelativeMajorityDao;
import com.pfc.ballots.dao.UserDao;
import com.pfc.ballots.dao.VoteDao;
import com.pfc.ballots.data.DataSession;
import com.pfc.ballots.data.Method;
import com.pfc.ballots.entities.Ballot;
import com.pfc.ballots.entities.Census;
import com.pfc.ballots.entities.ballotdata.Kemeny;
import com.pfc.ballots.entities.ballotdata.RelativeMajority;
import com.pfc.ballots.pages.Index;

public class ShowBallotAdmin {
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 //////////////////////////////////////////////////// GENERAL STUFF //////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@SessionState
	private DataSession datasession;
	
	@Inject
	private ComponentResources componentResources;
	
	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;
	
	@Inject
	private Request request;
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ////////////////////////////////////////////////////////// DAO //////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	FactoryDao DB4O=FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
	@Persist
	CensusDao censusDao;
	@Persist
	UserDao userDao;
	@Persist
	BallotDao ballotDao;
	@Persist
	VoteDao voteDao;
	@Persist
	RelativeMajorityDao relMayDao;
	@Persist
	KemenyDao kemenyDao;
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////// INITIALIZE ///////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void setupRender()
	{
		showAreuSure=false;
		showGrid=true;
		ballotDao=DB4O.getBallotDao(datasession.getDBName());
		voteDao=DB4O.getVoteDao(datasession.getDBName());
		userDao=DB4O.getUsuarioDao(datasession.getDBName());
		censusDao=DB4O.getCensusDao(datasession.getDBName());
		ballots=ballotDao.retrieveAll();
		kemenyDao=DB4O.getKemenyDao(datasession.getDBName());
		relMayDao=DB4O.getRelativeMajorityDao(datasession.getDBName());
		
		
		for(Ballot ballot_temp:ballots)
		{
			if(ballot_temp.isEnded()==true && ballot_temp.isCounted()==false)
			{
				if(ballot_temp.getMethod()==Method.MAYORIA_RELATIVA)
				{
					//relMayDao=DB4O.getRelativeMajorityDao(datasession.getDBName());
					RelativeMajority relMay=relMayDao.getByBallotId(ballot_temp.getId());
				  	relMay.calcularMayoriaRelativa();
				  	relMayDao.update(relMay);
				  	ballot_temp.setCounted(true);
				  	ballotDao.updateBallot(ballot);
				}
				if(ballot_temp.getMethod()==Method.KEMENY)
				{
					//CALCULAR KEMENY AQUI
				}
			}
			
		}
		kemenys=kemenyDao.retrieveAll();
		relMays=relMayDao.retrieveAll();
		
	}
	
	
	
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////////////// GRID ZONE ///////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Property
	@Persist
	private List<RelativeMajority> relMays;
	@Property
	@Persist
	private List<Kemeny> kemenys;
	
	
	
	
	@InjectComponent
	private Zone gridZone;
	
	@Property
	@Persist
	private boolean showGrid;
	
	@Persist
	@Property
	private List<Ballot> ballots;
	
	@Property
	private Ballot ballot;
	

	public String getUserName()
	{
		return userDao.getEmailById(ballot.getIdOwner());
	}

	
	public String getCensusName()
	{
		Census temp=censusDao.getById(ballot.getIdCensus());
		if(temp==null)
		{
			return "DOCENTE";
		}
		else
		{
			return temp.getCensusName();
		}
			
	}
	public Object onActionFromDeleteBallot(String idBallot)
	{
		if(request.isXHR())
		{
			showGrid=false;
			showAreuSure=true;
			optionDelete=true;
			ballotSure=ballotDao.getById(idBallot);
			ajaxResponseRenderer.addRender("gridZone",gridZone ).addRender("areuSureZone",areuSureZone);
			return null;
		}
		else
		{
			return Index.class;
		}
		
		
	}
	
	public Object onActionFromFinishBallot(String idBallot)
	{
		if(request.isXHR())
		{
			showGrid=false;
			showAreuSure=true;
			optionDelete=false;
			ballotSure=ballotDao.getById(idBallot);
			ajaxResponseRenderer.addRender("gridZone",gridZone ).addRender("areuSureZone",areuSureZone);
			return null;
		}
		else
		{
			return Index.class;
		}
	}
	public void onActionFromDeleteAllMaj()
	{
		if(request.isXHR())
		{
			relMayDao.deleteAll();
			relMays=null;
			ajaxResponseRenderer.addRender("gridZone",gridZone ).addRender("areuSureZone",areuSureZone);
		}
	}
	public void onActionFromDeleteAllKem()
	{
		if(request.isXHR())
		{
			kemenyDao.deleteAll();
			kemenys=null;
			ajaxResponseRenderer.addRender("gridZone",gridZone ).addRender("areuSureZone",areuSureZone);
		}
	}
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 //////////////////////////////////////////////////// AREUSURE ZONE //////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@InjectComponent
	private Zone areuSureZone;
	@Persist
	@Property
	private Ballot ballotSure;
	
	@Property
	@Persist
	private boolean showAreuSure;
	@Property
	@Persist
	private boolean optionDelete;
	
	public String getUserSure()
	{
		return userDao.getEmailById(ballotSure.getIdOwner());
	}
	public void onActionFromIsSure()
	{
		if(request.isXHR())
		{
			if(optionDelete==true)
			{
				ballotDao.deleteBallotById(ballotSure.getId());
				voteDao.deleteVoteOfBallot(ballotSure.getId());
				if(ballotSure.getMethod()==Method.MAYORIA_RELATIVA)
					{relMayDao.deleteByBallotId(ballotSure.getId());
					relMays=relMayDao.retrieveAll();}
				if(ballotSure.getMethod()==Method.KEMENY)
					{kemenyDao.deleteByBallotId(ballotSure.getId());
					 kemenys=kemenyDao.retrieveAll();}
				ballots=ballotDao.retrieveAll();
			}
			else
			{
				ballotSure.setEnded(true);
				ballotSure.setCounted(true);
				ballotDao.updateBallot(ballotSure);
				ballots=ballotDao.retrieveAll();
				
				if(ballotSure.getMethod()==Method.MAYORIA_RELATIVA)
				{
					//relMayDao=DB4O.getRelativeMajorityDao(datasession.getDBName());
					RelativeMajority relMay=relMayDao.getByBallotId(ballotSure.getId());
					if(relMay==null)
					{
						System.out.println("NULL");
					}
					else
					{
						System.out.println("NOT NULL");
						relMay.calcularMayoriaRelativa();
					}
					//System.out.println("BALLOTID"+relMay.getBallotId());
					relMayDao.update(relMay);
					relMays=relMayDao.retrieveAll();
				}
				if(ballotSure.getMethod()==Method.KEMENY)
				{
					Kemeny kemeny=kemenyDao.getByBallotId(ballotSure.getId());
					if(kemeny==null)
					{
						System.out.println("NULL");
					}
					else
					{
						System.out.println("NOT NULL");
						kemeny.calcularKemeny();
						kemenyDao.update(kemeny);
						kemenys=kemenyDao.retrieveAll();
					}
		
				}
				
			}
				
			
			
			showGrid=true;
			showAreuSure=false;
			ajaxResponseRenderer.addRender("gridZone",gridZone ).addRender("areuSureZone",areuSureZone);
		}
		
	}
	public void onActionFromNotSure()
	{
		if(request.isXHR())
		{
			showGrid=true;
			showAreuSure=false;
			ajaxResponseRenderer.addRender("gridZone",gridZone ).addRender("areuSureZone",areuSureZone);			
		}
	}
}
