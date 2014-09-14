package com.pfc.ballots.pages.ballot;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Zone;
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
import com.pfc.ballots.data.BallotKind;
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
 *  BallotList class is the controller for the BallotList page that
 * 	provides a list of the bllotats created by the current user
 * 
 * @author Mario Temprano Martin
 * @version 2.0 JUL-2014
 */
public class BallotList {
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
	
	@InjectPage
	private AddUsers addUsers;
	
	@SessionAttribute
	private String contextResultBallotId;
	@SessionAttribute
	private String contextBallotId;
	
	@Persist
	private List<Ballot> allBallots;
	
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
		showMine=true;
		showBasic=true;
		email=null;
		ballotDao=DB4O.getBallotDao(datasession.getDBName());
		voteDao=DB4O.getVoteDao(datasession.getDBName());
		userDao=DB4O.getUsuarioDao(datasession.getDBName());
		censusDao=DB4O.getCensusDao(datasession.getDBName());
		
		if(datasession.isAdmin())
		{
			
			ballots=ballotDao.retrieveAllSort();
		}
		else
		{
			ballots=ballotDao.getById(voteDao.getBallotsWithParticipation(datasession.getId()));
			allBallots=ballotDao.getPublics();
			for(Ballot temp:allBallots)
			{
				ballots.add(temp);
			}
			//ballots=ballotDao.getByOwnerId(datasession.getId());
		}
		
		
		kemenyDao=DB4O.getKemenyDao(datasession.getDBName());
		relMayDao=DB4O.getRelativeMajorityDao(datasession.getDBName());
		bordaDao=DB4O.getBordaDao(datasession.getDBName());
		rangeDao=DB4O.getRangeVotingDao(datasession.getDBName());
		
	}
	
	
	
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////////////// GRID ZONE ///////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	@Property
	@Persist
	private List<RelativeMajority> relMays;
	@Property
	@Persist
	private List<Kemeny> kemenys;
	*/
	
	
	
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
	
	@Persist
	@Property
	private boolean showMine;
	
	@Property
	@Persist
	private BallotKind kind;
	
	public boolean getIsAdmin()
	{
		return datasession.isAdmin();
	}

	public String getUserName()
	{
		return userDao.getEmailById(ballot.getIdOwner());
	}

	public boolean isMyBallot()
	{
		if(datasession.isAdmin())
		{
			return true;
		}
		if(ballot.getId()==datasession.getId())
		{
			return true;
		}
		return false;
	}
	public boolean isCanVote()
	{
		if(ballot.isEnded())
		{
			return false;
		}
		if(ballot.isPublica())
		{
			return true;
		}
		if(voteDao.getVoteByIds(ballot.getId(), datasession.getId())!=null)
		{
			return true;
		}
		return false;
	}
	public String getCensusName()
	{
		Census temp=censusDao.getById(ballot.getIdCensus());
		if(temp==null)
		{
			return"/";
		}
		else
		{
			return temp.getCensusName();
		}
			
	}
	public boolean isNotemptyBallots()
	{
		if(ballots==null)
		{
			return false;
		}
		if(ballots.size()==0)
		{
			return false;
		}
		return true;
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
		addUsers.setup(idBallot,BallotList.class);
		return addUsers;
	}
	public Object onActionFromVoteBallot(String id)
	{
		contextBallotId=id;
		return VoteBallot.class;
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
			ajaxResponseRenderer.addRender("gridZone",gridZone ).addRender("areuSureZone",areuSureZone).addRender("searchZone",searchZone);
			return null;
		}
		else
		{
			return Index.class;
		}
		
		
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
	public void onActionFromMineBut()
	{
		if(request.isXHR())
		{
			showMine=false;
			ballots=ballotDao.getByOwnerIdSorted(datasession.getId());
			ajaxResponseRenderer.addRender("gridZone",gridZone );
		}
	}
	public void onActionFromAllBut()
	{
		if(request.isXHR())
		{
			showMine=true;
			if(datasession.isAdmin())
			{
				ballots=ballotDao.retrieveAllSort();
			}
			else
			{
				ballots=ballotDao.getById(voteDao.getBallotsWithParticipation(datasession.getId()));
				allBallots=ballotDao.getPublics();
				for(Ballot temp:allBallots)
				{
					ballots.add(temp);
				}
			}
			ajaxResponseRenderer.addRender("gridZone",gridZone );
		}
	}
	
	public Object onActionFromResultBallot(String id)
	{
		contextResultBallotId=id;
		return ResultBallot.class;
	}
	public Object onActionFromAddBallot()
	{
		return CreateBallot.class;
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
			ajaxResponseRenderer.addRender("gridZone",gridZone ).addRender("areuSureZone",areuSureZone).addRender("searchZone",searchZone);
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
			ajaxResponseRenderer.addRender("gridZone",gridZone ).addRender("areuSureZone",areuSureZone).addRender("searchZone",searchZone);			
		}
	}
	
	  	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		 //////////////////////////////////////////////////// SEARCH ZONE ////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@InjectComponent
	private Zone searchZone;
	@Persist
	@Property
	private String email;
	@Persist
	@Property
	private Method method;
	
	@Persist
	@Property
	private String name;
	
	@Persist
	@Property
	private Date startDate;
	
	@Persist
	@Property
	private Date endDate;
	
	
	@Persist
	@Property
	private boolean showBasic;
	
	public boolean isShowSearch()
	{
		if(showAreuSure)
		{
			return false;
		}
		return true;
	}
	
	public void onActionFromAdvancedBut()
	{
		if(request.isXHR())
		{
			showBasic=false;
			email=null;
			kind=null;
			method=null;
			name=null;
			startDate=null;
			endDate=null;
			ajaxResponseRenderer.addRender("searchZone",searchZone);
		}
	}
	public void onActionFromBasicBut()
	{
		if(request.isXHR())
		{
			showBasic=true;
			email=null;
			kind=null;
			method=null;
			name=null;
			startDate=null;
			endDate=null;
			ajaxResponseRenderer.addRender("searchZone",searchZone);
		}
	}
	
	public void onSuccessFromBasicForm()
	{
		if(request.isXHR())
		{
			if(email==null)
			{
				ballots=null;
			}
			else
			{
				String id=userDao.getIdByEmail(email);
				if(id==null)
				{
					ballots=null;
				}
				else
				{
					Ballot x=new Ballot();
					x.setIdOwner(id);
					if(datasession.isAdmin())
					{
						ballots=ballotDao.getByExample(x);
					}
					else
					{
						ballots=ballotDao.getByExample(voteDao.getBallotsWithParticipation(datasession.getId()), x);
					}
				}
			}
			ajaxResponseRenderer.addRender("gridZone",gridZone);
		}
	}
	public void onSuccessFromAdvancedForm()
	{
		if(request.isXHR())
		{
			Ballot x=new Ballot();
			if(email!=null)
			{
				x.setIdOwner(userDao.getIdByEmail(email));
			}
			if(name!=null)
			{
				x.setName(name);
			}
			if(kind==BallotKind.DOCENTE)
			{
				x.setTeaching(true);
			}
			else if (kind==BallotKind.PRIVADA)
			{
				x.setPrivat(true);
			}
			else if(kind==BallotKind.PUBLICA)
			{
				x.setPublica(true);
			}
			
			if(method!=null)
			{
				System.out.println("Metodo"+method);
				x.setMethod(method);
			}
			if(startDate!=null)
			{
				x.setStartDate(startDate);
			}
			if(endDate!=null)
			{
				x.setEndDate(endDate);
			}
			
			
			///BUSQUEDA
			if(datasession.isAdmin())
			{
				ballots=ballotDao.getByExample(x);
			}
			else
			{
				ballots=ballotDao.getByExample(voteDao.getBallotsWithParticipation(datasession.getId()), x);
			}
			ajaxResponseRenderer.addRender("gridZone",gridZone);
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
		switch(datasession.sessionState())
		{
			case 0:
				return Index.class;
			case 1:
				return null;
			case 2:
				return null;
			case 3:
				return SessionExpired.class;
			default:
				return Index.class;
		}
		
	}
}
