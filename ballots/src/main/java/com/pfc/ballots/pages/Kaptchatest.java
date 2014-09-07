package com.pfc.ballots.pages;

import javax.inject.Inject;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;

import com.pfc.ballots.dao.BallotDao;
import com.pfc.ballots.dao.BordaDao;
import com.pfc.ballots.dao.CensusDao;
import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.dao.KemenyDao;
import com.pfc.ballots.dao.RangeVotingDao;
import com.pfc.ballots.dao.RelativeMajorityDao;
import com.pfc.ballots.dao.VoteDao;
import com.pfc.ballots.data.DataSession;
import com.pfc.ballots.data.Method;
import com.pfc.ballots.entities.Ballot;
import com.pfc.ballots.entities.Vote;
import com.pfc.ballots.entities.ballotdata.RelativeMajority;



public class Kaptchatest {

	@SessionState
	private DataSession datasession;
	
	@Inject
	private ComponentResources componentResources;
	
	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;
	
	@Inject
	private Request request;
	
	@Property
	@Persist
	private Ballot ballot;
	@Persist
	@Property
	private Vote vote;

	
	@Property
    @SessionAttribute
	private String contextBallotId;
	
	@Property
	private final StringValueEncoder stringValueEncoder = new StringValueEncoder();
	
	
	  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////////////// DAO ///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	FactoryDao DB4O=FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
	@Persist
	CensusDao censusDao;
	@Persist
	BallotDao ballotDao;
	@Persist
	VoteDao voteDao;
	@Persist
	RelativeMajorityDao relativeMajorityDao;
	@Persist
	KemenyDao kemenyDao;
	@Persist
	BordaDao bordaDao;
	@Persist
	RangeVotingDao rangeDao;
	
	
	public void setupRender()
	{
		componentResources.discardPersistentFieldChanges();
		ballotDao=DB4O.getBallotDao(datasession.getDBName());
		ballot=ballotDao.getById(contextBallotId);
		
		voteDao=DB4O.getVoteDao(datasession.getDBName());
		vote=voteDao.getVoteByIds(contextBallotId, datasession.getId());
		
		if(ballot.getMethod()==Method.MAYORIA_RELATIVA)
		{
			relativeMajorityDao=DB4O.getRelativeMajorityDao(datasession.getDBName());
			relMay=relativeMajorityDao.getByBallotId(contextBallotId);
			relMayVote=relMay.getOptions().get(0);
		}
		ReCaptcha c = ReCaptchaFactory.newReCaptcha("6Ldn5fkSAAAAAAkLfCNbA1gnqFggJfWRg6jUZ0IC" , "6Ldn5fkSAAAAAFC2G27FOGPLu_TlV44dahHzKq8a", false);
        captcha = c.createRecaptchaHtml(null, null);
	
	}
	  //////////////////////////////////////////////////////////////////////////////////////////
	 //////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	@Property
	private String recaptcha_challenge_field;
	@Property
    private String recaptcha_response_field;
	
	
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		 ////////////////////////////////////////////////////// MAYORIA RELATIVA /////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		@Persist
		@Property
		private RelativeMajority relMay;
		@Property
		private String relMayVote;
		@Property
		private String relMayOption;
		
		
		@Property
	    private String captcha;
		
		/**
		 * Stores the majority relative vote
		 * @return
		 */
		public Object onSuccessFromRelativeMajorityForm()
		{
			 ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
		        reCaptcha.setPrivateKey("6Le_suISAAAAAM99aFgDImQLRhscPZp2Jtg2HZ38");
		
		        String challenge = request.getParameter("recaptcha_challenge_field");
		        String uresponse = request.getParameter("recaptcha_response_field");
		        String remoteAddr = request.getRemoteHost();
		        ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, uresponse);
		        if (reCaptchaResponse.isValid()) {
		        	System.out.println("SI");
		        }
		        else
		        {
		        	System.out.println("NOPE");
		        }
		        
		        
			/*
			vote=voteDao.getVoteByIds(contextBallotId, datasession.getId());
			ballot=ballotDao.getById(contextBallotId);
			
			if(ballot!=null||!ballot.isEnded()||!vote.isCounted())//comprueba si la votacion existe,si no ha terminado y si no ha votado el usuario
			{
				vote.setCounted(true);
				System.out.println("SI");
				voteDao.updateVote(vote);
				relMay.addVote(relMayVote);
				relativeMajorityDao.update(relMay);
			}
			*/
			return Index.class;
		}
		public boolean isShowRelativeMajority()
		{
			if(ballot==null)
			{
				return false;
			}
			if(ballot.getMethod()==Method.MAYORIA_RELATIVA)
			{
				return true;
			}
			return false;
				
		}
		
		public void onActivate()
		{
			
		}
}
