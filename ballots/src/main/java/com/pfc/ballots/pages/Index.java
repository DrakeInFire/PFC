package com.pfc.ballots.pages;

import java.util.Date;
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
import com.pfc.ballots.dao.RelativeMajorityDao;
import com.pfc.ballots.dao.VoteDao;
import com.pfc.ballots.data.DataSession;
import com.pfc.ballots.entities.Ballot;
import com.pfc.ballots.entities.Vote;
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
		 ///////////////////////////////////////////////////// INITIALIZE //////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
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
				currentBallots=ballotDao.getById(voteDao.getBallotsWithParticipation(datasession.getId()));//Obtiene las votaciones
			}
		}
		
		  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		 /////////////////////////////////////////////////////// DAO ///////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		FactoryDao DB4O=FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
		@Persist
		BallotDao ballotDao;
		@Persist
		VoteDao voteDao;
		
		
		  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		 /////////////////////////////////////////////////////// PAGE //////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
		
	    @SessionAttribute
		private String contextBallotId;
		public Object onActionFromVoteBallot(String id)
		{
			contextBallotId=id;
			return VoteBallot.class;
		}
		
		
		
		
}
