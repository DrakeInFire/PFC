package com.pfc.ballots.pages;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.ioc.annotations.*;
import org.apache.tapestry5.corelib.components.*;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.alerts.AlertManager;

import com.pfc.ballots.dao.BallotDao;
import com.pfc.ballots.dao.CensusDao;
import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.dao.KemenyDao;
import com.pfc.ballots.dao.RelativeMajorityDao;
import com.pfc.ballots.dao.VoteDao;
import com.pfc.ballots.data.DataSession;
import com.pfc.ballots.data.Method;
import com.pfc.ballots.entities.Ballot;
import com.pfc.ballots.entities.Vote;
import com.pfc.ballots.entities.ballotdata.Kemeny;
import com.pfc.ballots.entities.ballotdata.RelativeMajority;
import com.pfc.ballots.pages.ballot.ResultBallot;
import com.pfc.ballots.pages.ballot.VoteBallot;

/**
 * Start page of application ballots.
 */
public class Index
{
	  	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		 //////////////////////////////////////////////////// GENERAL STUFF //////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		@SessionState
		private DataSession datasession;
		
		@Inject
		private ComponentResources componentResources;
		
		
		  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		 /////////////////////////////////////////////////////// DAO ///////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		FactoryDao DB4O=FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
		@Persist
		BallotDao ballotDao;
		@Persist
		VoteDao voteDao;
		@Persist
		RelativeMajorityDao relMayDao;
		@Persist
		KemenyDao kemenyDao;
		
		
		  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		 ///////////////////////////////////////////////////// INITIALIZE //////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/*
		public void setupRender()
		{
			componentResources.discardPersistentFieldChanges();
			if(datasession==null)
			{
				datasession=new DataSession();
			}
			if(datasession.isLoged())
			{
				ballotDao=DB4O.getBallotDao(datasession.getDBName());
				
				
				voteDao=DB4O.getVoteDao(datasession.getDBName());
				relMayDao=DB4O.getRelativeMajorityDao(datasession.getDBName());
				kemenyDao=DB4O.getKemenyDao(datasession.getDBName());
				currentBallots=ballotDao.getById(voteDao.getBallotsWithParticipation(datasession.getId()));//Obtiene las votaciones
				
				
				/*for(Ballot ballot_temp:currentBallots)
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
						  	ballotDao.updateBallot(ballot_temp);
						}
						else if(ballot_temp.getMethod()==Method.KEMENY)
						{
							Kemeny kemeny=kemenyDao.getByBallotId(ballot_temp.getId());
							kemeny.calcularKemeny();
							kemenyDao.update(kemeny);
							ballot_temp.setCounted(true);
							ballotDao.updateBallot(ballot_temp);
						}
					}
					
				}
				
			}
		}*/
		
		
		public void setupRender()
		{
			componentResources.discardPersistentFieldChanges();
			if(datasession.isLoged())
			{
				ballotDao=DB4O.getBallotDao(datasession.getDBName());
				voteDao=DB4O.getVoteDao(datasession.getDBName());
				
				nonActiveBallots=new LinkedList<Ballot>();
				activeBallots=new LinkedList<Ballot>();
				endedBallots=new LinkedList<Ballot>();
				currentBallots=ballotDao.getById(voteDao.getBallotsWithParticipation(datasession.getId()),nonActiveBallots,activeBallots,endedBallots);
			}
		}
		

		
		
		  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		 /////////////////////////////////////////////////////// PAGE //////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		@Property
		@Persist
		private List<Ballot> nonActiveBallots;
		@Property
		@Persist
		private List<Ballot> activeBallots;
		@Property
		@Persist
		private List<Ballot> endedBallots;
		@Property
		@Persist
		private List<Ballot> currentBallots;
		@Property
		private Ballot ballot;
		
		public boolean isShowRegistred()
		{
			if(datasession.isLoged())
				{return true;}
			return false;
		}
		
		public String getMetodo()
		{
			if(ballot.getMethod()==Method.MAYORIA_RELATIVA)
			{
				return "MAYORIA RELATIVA";
			}
			else
			{
				return "KEMENY";
			}
		}
		
		public boolean isShowNotStarted()
		{
			if(nonActiveBallots.size()==0)
				return false;
			return true;
		}
		public boolean isShowActive()
		{
			if(activeBallots.size()==0)
				return false;
			return true;
		}
		public boolean isShowEnded()
		{
			if(endedBallots.size()==0)
				return false;
			return true;
		}
		public boolean isShowCurrent()
		{
			if(currentBallots.size()==0)
				return false;
			return true;
		}
		
	    @SessionAttribute
		private String contextBallotId;
		public Object onActionFromVoteBallot(String id)
		{
			contextBallotId=id;
			return VoteBallot.class;
		}
		
		@SessionAttribute
		private String contextResultBallotId;
		public Object onActionFromResultBallot(String id)
		{
			contextResultBallotId=id;
			return ResultBallot.class;
		}
		
		
		
}
