package com.pfc.ballots.pages.ballot;

import java.util.List;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;

import com.pfc.ballots.dao.BallotDao;
import com.pfc.ballots.dao.BordaDao;
import com.pfc.ballots.dao.CensusDao;
import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.dao.KemenyDao;
import com.pfc.ballots.dao.RangeVotingDao;
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
import com.pfc.ballots.pages.SessionExpired;
import com.pfc.ballots.pages.UnauthorizedAttempt;
/**
 * 
 * ShowBallotAdmin class is the controller for the ShowBallotAdmin page that
 * provides the administration of the ballots
 * 
 * @author Mario Temprano Martin
 * @version 2.0 JUL-2014
 */
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
	@Persist
	BordaDao bordaDao;
	@Persist
	RangeVotingDao rangeDao;
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////// INITIALIZE ///////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Initialize data
	 * (check if the ballots are ended and non counted and calculate the results)
	 */
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
		bordaDao=DB4O.getBordaDao(datasession.getDBName());
		rangeDao=DB4O.getRangeVotingDao(datasession.getDBName());
	}
	
	
	
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////////////// GRID ZONE ///////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@InjectPage
	private AddUsers addUsers;
	
	
	
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
	/**
	 * Delete a ballot 
	 * @param idBallot
	 * @return
	 */
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
	
	
	public boolean isShowAdd()
	{
		if(ballot.isEnded())
			return false;
		if(ballot.isPublica())
			return false;
		return true;
	}
	/**
	 * Redirects to a page for add users
	 * @param idBallot
	 * @return 
	 */
	public Object onActionFromAddUsersBut(String idBallot)
	{
		addUsers.setup(idBallot,ShowBallotAdmin.class);
		return addUsers;
	}
	/**
	 * Finish a ballot and calculate the result
	 * @param idBallot
	 * @return
	 */
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
	
	/**
	 * Controls the actions to take when is sure to delete/finish 
	 * a ballot in the dialog "is sure delete/finish a ballot"
	 */
	public void onActionFromIsSure()
	{
		if(request.isXHR())
		{
			if(optionDelete==true)
			{
				ballotDao.deleteBallotById(ballotSure.getId());
				voteDao.deleteVoteOfBallot(ballotSure.getId());
				if(ballotSure.getMethod()==Method.MAYORIA_RELATIVA)
				{
					relMayDao.deleteByBallotId(ballotSure.getId());
					//relMays=relMayDao.retrieveAll();
				}
				if(ballotSure.getMethod()==Method.KEMENY)
				{
					kemenyDao.deleteByBallotId(ballotSure.getId());
					//kemenys=kemenyDao.retrieveAll();
				}
				if(ballotSure.getMethod()==Method.BORDA)
				{
					bordaDao.deleteByBallotId(ballotSure.getId());
				}
				if(ballotSure.getMethod()==Method.RANGE_VOTING)
				{
					rangeDao.deleteByBallotId(ballotSure.getId());
				}
				ballots=ballotDao.retrieveAll();
			}
			else
			{
				ballotSure.setEnded(true);		
				ballotSure.setActive(false);
				ballotDao.updateBallot(ballotSure);
				ballots=ballotDao.retrieveAll();
		
			}
				
			
			
			showGrid=true;
			showAreuSure=false;
			ajaxResponseRenderer.addRender("gridZone",gridZone ).addRender("areuSureZone",areuSureZone);
		}
		
	}
	/**
	 * Cancel the dialog "is sure delete/finish a ballot"
	 */
	public void onActionFromNotSure()
	{
		if(request.isXHR())
		{
			showGrid=true;
			showAreuSure=false;
			ajaxResponseRenderer.addRender("gridZone",gridZone ).addRender("areuSureZone",areuSureZone);			
		}
	}
	
	  ////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////// ON ACTIVATE //////////////////////////////////// 
	////////////////////////////////////////////////////////////////////////////////////
	/**
	* Controls if the user can enter in the page
	* @return another page if the user can't enter
	*/
	public Object onActivate()
	{
		return Index.class;
		/*switch(datasession.sessionState())
		{
			case 0:
				return Index.class;
			case 1:
				return UnauthorizedAttempt.class;
			case 2:
				return null;
			case 3:
				return SessionExpired.class;
			default:
				return Index.class;
		}*/
		
	}
}
