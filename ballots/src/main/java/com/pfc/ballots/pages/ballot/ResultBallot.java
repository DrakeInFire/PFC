package com.pfc.ballots.pages.ballot;

import javax.inject.Inject;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
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
import com.pfc.ballots.entities.ballotdata.Kemeny;
import com.pfc.ballots.entities.ballotdata.RelativeMajority;

public class ResultBallot {

	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 //////////////////////////////////////////////////// GENERAL STUFF //////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@SessionState
	private DataSession datasession;
	
	@Inject
	private ComponentResources componentResources;
	
	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;
	
	@SessionAttribute
	private String contextResultBallotId;
	
	@Inject
	private Request request;
	
	@Persist
	@Property
	private Ballot ballot;
	
	@Property 
	private boolean noBallot;
	
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
	RelativeMajorityDao relMayDao;
	KemenyDao kemenyDao;
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////// INITIALIZE ///////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void setupRender()
	{
		componentResources.discardPersistentFieldChanges();
		
		ballotDao=DB4O.getBallotDao(datasession.getDBName());
		ballot=ballotDao.getById(contextResultBallotId);
		if(ballot==null)
		{
			noBallot=true;
		}
		else if(ballot.getMethod()==Method.MAYORIA_RELATIVA)
		{
			relMayDao= DB4O.getRelativeMajorityDao(datasession.getDBName());
			relMay=relMayDao.getByBallotId(ballot.getId());
			
			if(ballot.isEnded()==true && ballot.isCounted()==false && relMay!=null)
			{
				relMay.calcularMayoriaRelativa();
				ballot.setCounted(true);
				relMayDao.update(relMay);
				ballotDao.updateBallot(ballot);
			}
			for(String vote:relMay.getVotes())
			{
				System.out.println("VOTE->"+vote);
			}
		}
		else if(ballot.getMethod()==Method.KEMENY)
		{
			kemenyDao=DB4O.getKemenyDao(datasession.getDBName());
			kemeny=kemenyDao.getByBallotId(contextResultBallotId);
			if(ballot.isEnded()==true && ballot.isCounted()==false && kemeny!=null)
			{
				kemeny.calcularKemeny();
				ballot.setCounted(true);
				kemenyDao.update(kemeny);
				ballotDao.updateBallot(ballot);
			}
			
		}
		
		
		
	}
	
	  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////// MAYORIA RELATIVA //////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Persist
	@Property
	RelativeMajority relMay;
	
	@Property
	private String option;
	
	public String getRelMayVote()
	{
		return String.valueOf(relMay.getResultOption(option));
	}
	public boolean getShowRelMay()
	{
		if(ballot!=null && ballot.getMethod()==Method.MAYORIA_RELATIVA)
			{return true;}
		return false;
	}

	  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////// MAYORIA RELATIVA //////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Property
	@Persist
	Kemeny kemeny;
	
	@Property
	private String permutation;
	public boolean getShowKemeny()
	{
		if(ballot!=null && ballot.getMethod()==Method.KEMENY)
			{return true;}
		return false;
	}
	public String getKemenyVote()
	{
		return String.valueOf(kemeny.getResult(permutation));
	}
	
	
}
