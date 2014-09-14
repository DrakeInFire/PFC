package com.pfc.ballots.pages.ballot;

import javax.inject.Inject;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

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
import com.pfc.ballots.entities.ballotdata.Borda;
import com.pfc.ballots.entities.ballotdata.Kemeny;
import com.pfc.ballots.entities.ballotdata.RangeVoting;
import com.pfc.ballots.entities.ballotdata.RelativeMajority;
import com.pfc.ballots.pages.Index;
import com.pfc.ballots.pages.SessionExpired;


@Import(library = "context:js/charts.js")
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
	
	@Inject
    private JavaScriptSupport javaScriptSupport;
	
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
	BordaDao bordaDao;
	RangeVotingDao rangeDao;
	
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////// INITIALIZE ///////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Initialize values
	 */
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
			
			
			if(!ballot.isEnded())
			{
				relMay.calcularMayoriaRelativa();
			}
			if(ballot.isEnded()==true && ballot.isCounted()==false && relMay!=null)
			{
				relMay.calcularMayoriaRelativa();
				ballot.setCounted(true);
				relMayDao.update(relMay);
				ballotDao.updateBallot(ballot);
			}
		}
		else if(ballot.getMethod()==Method.KEMENY)
		{
			kemenyDao=DB4O.getKemenyDao(datasession.getDBName());
			kemeny=kemenyDao.getByBallotId(contextResultBallotId);
			
			if(!ballot.isEnded())
			{
				kemeny.calcularKemeny();
			}
			if(ballot.isEnded()==true && ballot.isCounted()==false && kemeny!=null)
			{
				kemeny.calcularKemeny();
				ballot.setCounted(true);
				kemenyDao.update(kemeny);
				ballotDao.updateBallot(ballot);
			}
			
		}
		else if(ballot.getMethod()==Method.BORDA)
		{
			bordaDao=DB4O.getBordaDao(datasession.getDBName());
			borda=bordaDao.getByBallotId(contextResultBallotId);
			
			if(!ballot.isEnded())
			{
				borda.calcularBorda();
			}
			if(ballot.isEnded()==true && ballot.isCounted()==false && borda!=null)
			{
				borda.calcularBorda();
				ballot.setCounted(true);
				bordaDao.update(borda);
				ballotDao.updateBallot(ballot);
			}
		}
		else if(ballot.getMethod()==Method.RANGE_VOTING)
		{
			rangeDao=DB4O.getRangeVotingDao();
			range=rangeDao.getByBallotId(contextResultBallotId);
			
			if(!ballot.isEnded())
			{
				range.calcularRangeVoting();
			}
			if(ballot.isEnded()==true && ballot.isCounted()==false && range!=null)
			{
				range.calcularRangeVoting();
				ballot.setCounted(true);
				rangeDao.update(range);
				ballotDao.updateBallot(ballot);
			}
		}
		
		
		
		
	}
	/**
	 * JavaScript controller
	 */
	public void afterRender()
	{
		System.out.println("AFTER");
		JSONArray array=new JSONArray();
	
		JSONObject obj=new JSONObject();
		obj.put("title", ballot.getName());
		array.put(obj);
		
	
		if(ballot.getMethod()==Method.MAYORIA_RELATIVA)
		{
			
			for(String option:relMay.getOptions())
			{
				obj=new JSONObject();
				obj.put("option", option);
				obj.put("value", relMay.getResultOption(option));
				array.put(obj);
			}
			
			System.out.println("tamaño->"+array.toString());
			javaScriptSupport.addInitializerCall("charts_may",array.toString());
		}
		if(ballot.getMethod()==Method.KEMENY)
		{
			for(String permutation:kemeny.getPermutations())
			{
				obj=new JSONObject();
				obj.put("option", permutation);
				obj.put("value", kemeny.getResult(permutation));
				array.put(obj);
			}
			javaScriptSupport.addInitializerCall("charts_kem",array.toString());
		}
		if(ballot.getMethod()==Method.BORDA)
		{
			for(String bordOpt:borda.getBordaOptions())
			{
				obj=new JSONObject();
				obj.put("option", bordOpt);
				obj.put("value", borda.getResult(bordOpt));
				array.put(obj);
			}
			System.out.println("tamaño->"+array.toString());
			javaScriptSupport.addInitializerCall("charts_bor",array.toString());
		}
		if(ballot.getMethod()==Method.RANGE_VOTING)
		{
			for(String rangeOption:range.getOptions())
			{
				obj=new JSONObject();
				obj.put("option", rangeOption);
				obj.put("value",  range.getResult(rangeOption));
				array.put(obj);
			}
			System.out.println("tamaño->"+array.toString());
			javaScriptSupport.addInitializerCall("charts_range",array.toString());
		}
		
	}
	
	  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////// MAYORIA RELATIVA //////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Relative Majority Data
	 */
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

	public String getRelMayNum()
	{
		return String.valueOf(relMay.getVotes().size());
	}
	
	  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////////// KEMENY ////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Kemeney Data
	 */
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
	public String getKemNum()
	{
		return String.valueOf(kemeny.getVotes().size());
	}
	  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 //////////////////////////////////////////////////// BORDA ////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Borda Data
	 */
	@Property
	@Persist
	Borda borda;
	
	@Property
	private String bordaOpt;
	
	public boolean getShowBorda()
	{
		if(ballot!=null && ballot.getMethod()==Method.BORDA)
			{return true;}
		return false;
	}
	public String getBordaVote()
	{
		return String.valueOf(borda.getResult(bordaOpt));
	}
	public String getBordaNum()
	{
		return String.valueOf(borda.getVotes().size());
	}
	 ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 //////////////////////////////////////////////////// RANGE ////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * RangeVoting Data
	 */
	@Property
	@Persist
	RangeVoting range;

	@Property
	private String rangeOpt;
	
	public boolean getShowRange()
	{
		if(ballot!=null && ballot.getMethod()==Method.RANGE_VOTING)
			{return true;}
		return false;
	}
	public String getRangeVote()
	{
		return String.valueOf(range.getResult(rangeOpt));
	}
	public String getRangeNum()
	{
		return String.valueOf(range.getVotes().size());
	}
	public String getMaxValue()
	{
		return String.valueOf(range.getMaxValue());
	}
	public String getMinValue()
	{
		return String.valueOf(range.getMinValue());
	}
	
	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////////////// ON ACTIVATE //////////////////////////////////////////////////////// 
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	* Controls if the user can enter in the page
	* @return another page if the user can't enter
	*/
	public Object onActivate()
	{
		if(contextResultBallotId==null)
		{
			return Index.class;
		}
		switch(datasession.sessionState())
		{
			case 0:
				return null;
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
