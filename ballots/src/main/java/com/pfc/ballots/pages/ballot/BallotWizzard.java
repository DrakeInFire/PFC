package com.pfc.ballots.pages.ballot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.beaneditor.Width;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.SelectModelFactory;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;

import com.pfc.ballots.dao.BallotDao;
import com.pfc.ballots.dao.CensusDao;
import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.dao.KemenyDao;
import com.pfc.ballots.dao.RelativeMajorityDao;
import com.pfc.ballots.dao.VoteDao;
import com.pfc.ballots.data.BallotKind;
import com.pfc.ballots.data.DataSession;
import com.pfc.ballots.data.Method;
import com.pfc.ballots.encoder.CensusEncoder;
import com.pfc.ballots.entities.Ballot;
import com.pfc.ballots.entities.Census;
import com.pfc.ballots.entities.Vote;
import com.pfc.ballots.entities.ballotdata.Kemeny;
import com.pfc.ballots.entities.ballotdata.RelativeMajority;
import com.pfc.ballots.pages.Index;
import com.pfc.ballots.pages.SessionExpired;
import com.pfc.ballots.pages.UnauthorizedAttempt;
import com.pfc.ballots.pages.admin.AdminMail;
import com.pfc.ballots.util.GenerateDocentVotes;
import com.pfc.ballots.util.UUID;


public class BallotWizzard {
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
	
	@Persist
	private Ballot ballot;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ////////////////////////////////////////////////////////// DAO //////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	FactoryDao DB4O=FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
	@Persist
	CensusDao censusDao;
	@Persist
	BallotDao ballotDao;
	VoteDao voteDao;
	RelativeMajorityDao relativeMajorityDao;
	KemenyDao kemenyDao;
	 /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ////////////////////////////////////////////////////// INITIALIZE ///////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void setupRender()
	{
		componentResources.discardPersistentFieldChanges();
		
		
		mayRelModel=NUMBERS2_7;
		numOpt=2;
		
		
		
		initializeBooleans();
		
		cal3=new GregorianCalendar();
		cal3.setTime(new Date());
		ballotDao=DB4O.getBallotDao(datasession.getDBName());
		censusDao=DB4O.getCensusDao(datasession.getDBName());
		voteDao=DB4O.getVoteDao(datasession.getDBName());
		censuses=censusDao.getByOwnerId(datasession.getId());
		if(censuses.size()==0)
		{
			existCensus=false;
		}
		else
		{
			existCensus=true;
		}
		censusNormalModel=selectModelFactory.create(censuses, "censusName");
		
		censusModel=NO_CENSUS;
		
	}
	public void initializeBooleans()
	{
		showGeneral=true;
		showErrorGeneral=false;
		showBadName=false;
		
		showType=false;
		showNormalCensus=false;
		showBadDate=false;
		showErrorType=false;
		
		showMayRel=false;
		showErrorMayRel=false;
		
		showKemeny=false;
		showErrorKemeny=false;
	}
	
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ///////////////////////////////////////////////////// GENERAL ZONE //////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@InjectComponent
	private Zone generalZone;

	@Property
	@Persist
	private boolean showGeneral;
	@Property
	@Persist
	private boolean showErrorGeneral;
	@Property
	@Persist
	private boolean showBadName;
	@Persist
	@Property
	@Validate("minLength=5")
	private String ballotName;
	@Width(value = 75)
	@Persist
	@Property
	//@Validate("minLength=20")
	private String description;


	public void onSuccessFromGeneralForm()
	{
		if(request.isXHR())
		{
			if(ballotName==null|| description==null)
			{
				showErrorGeneral=true;
			}
			else
			{
				if(ballotDao.isNameInUse(ballotName))
				{
					showBadName=true;
				}
				else
				{
					showErrorGeneral=false;
					showGeneral=false;
					showType=true;
				}
			}
			ajaxResponseRenderer.addRender("generalZone", generalZone).addRender("typeZone", typeZone);
		}
	}
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ///////////////////////////////////////////////////// Type ZONE /////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@InjectComponent
	private Zone typeZone;
	@Property
	@Persist
	private boolean showType;
	
	static final private String[] CENSUS_DOCENT = new String[] {"10","20","30","40"};
	static final private String[] NO_CENSUS = new String[] {};
	@Property
    private String dateInFormatStr = "dd/MM/yyyy";
	
	@Property
	@Persist
	private boolean showErrorType;
	@Property
	@Persist
	private boolean showBadDate;
	@Property
	@Persist
	private boolean showNormalCensus;
	@Property
	@Persist
	private boolean existCensus;
	@Property
	@Persist
	private Method method;
	
	@Property
	@Persist
	private BallotKind ballotKind;
	@Property
	@Persist
	private String[] censusModel;
	@Property
	@Persist
	private String census;
	
	@Property
	@Persist
	private SelectModel censusNormalModel;
	@Inject
	private SelectModelFactory selectModelFactory;
	@Persist
	private List<Census> censuses;
	@Property
	@Persist
	private Census censusNormal;
	public CensusEncoder getCensusNormalEncoder()
	{
	  return new CensusEncoder(censuses);
	}
	@Property
	@Persist
	@Validate("regexp=^([0-2]?[0-9]|3[0-1])/([0-1][0-2]|0[0-9]|[0-9])/2[0-9][1-9][4-9]$")
	private Date startDate;
	@Property
	@Persist
	@Validate("regexp=^([0-1]?[0-9]|2[0-3])\\:[0-5][0-9]\\:[0-5][0-9]$")
	private String startHour;
	@Property
	@Persist
	@Validate("regexp=^([0-2]?[0-9]|3[0-1])/([0-1][0-2]|0[0-9]|[0-9])/2[0-9][1-9][4-9]$")
	private Date endDate;
	@Property
	@Persist
	@Validate("regexp=^([0-1]?[0-9]|2[0-3])\\:[0-5][0-9]\\:[0-5][0-9]$")
	private String endHour;
	
	
	public void onValueChangedFromBallotKindSelect(BallotKind kind)
	{
		showNormalCensus=false;
		if(kind==null)
		{
			censusModel=NO_CENSUS;
		}
		if(kind==BallotKind.DOCENTE)
		{
			censusModel=CENSUS_DOCENT;
		}
		if(kind==BallotKind.NORMAL)
		{
			showNormalCensus=true;
		}
		ajaxResponseRenderer.addRender("typeZone", typeZone);
	}
	

	@Persist
	private Calendar cal1;
	@Persist
	private Calendar cal2;
	@Persist
	private Calendar cal3;
	public void onValidateFromTypeForm()
	{
		cal1=new GregorianCalendar();
		cal2=new GregorianCalendar();
		
		
		showErrorType=false;
		showBadDate=false;
		
		if(method==null|| ballotKind==null)
		{
			showErrorType=true;
		}
		else if(ballotKind==BallotKind.NORMAL)
		{
			
			if(censusNormal==null)
			{
				showErrorType=true;
				System.out.println("NULL");
			}
			if(startDate==null)
			{
				System.out.println("START DATE NULL");
				Date date=new Date();
				cal1.setTime(date);
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy "); // Set your date format
				String currentData = sdf.format(date);
				try
				{
				startDate= sdf.parse(currentData);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				System.out.println(startDate);
				
				if(startHour==null)
				{
					String hour=String.valueOf(cal1.get(Calendar.HOUR_OF_DAY));
					System.out.println(hour);
					if(Integer.parseInt(hour)<10)
					{
						hour="0"+hour;
					}
					String min=String.valueOf(cal1.get(Calendar.MINUTE));
					System.out.println(min);
					if(Integer.parseInt(min)<10)
					{
						min="0"+min;
					}
					String sec=String.valueOf(cal1.get(Calendar.SECOND));
					System.out.println(sec);
					if(Integer.parseInt(sec)<10)
					{
						sec="0"+sec;
					}
					startHour=hour+":"+min+":"+sec;
				}
			}
			else
			{
				System.out.println(startDate);
				cal1.setTime(startDate);
			}
			
			if(startHour!=null)
			{
				cal1.setTime(startDate);
				System.out.println("START HOUR");
				String [] temp= startHour.split(":");
				
				cal1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(temp[0]));
				cal1.set(Calendar.MINUTE, Integer.parseInt(temp[1]));
				cal1.set(Calendar.SECOND, Integer.parseInt(temp[2]));
				
			}
			
				
			if(endDate==null)
			{
				showBadDate=true;
				showErrorType=true;
			}
			else
			{
				cal2.setTime(endDate);
				if(endHour!=null)
				{
					String temp[] =endHour.split(":");
					cal2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(temp[0]));
					cal2.set(Calendar.MINUTE, Integer.parseInt(temp[1]));
					cal2.set(Calendar.SECOND, Integer.parseInt(temp[2]));
				}
			}
			
			if(!showBadDate)
			{
				
				System.out.println("start-->"+cal1.getTimeInMillis());
				System.out.println("end---->"+cal2.getTimeInMillis());
				System.out.println("actual->"+cal3.getTimeInMillis());
				if(cal1.getTimeInMillis()<cal3.getTimeInMillis())
				{
					System.out.println("start < actual");
					showBadDate=true;
				}
				if(cal2.getTimeInMillis()<cal1.getTimeInMillis())
				{
					System.out.println("end < start");
					showBadDate=true;
				}
				System.out.println(cal1.getTime());
				System.out.println(cal2.getTime());
			}
		
		
		}
		else if(ballotKind==BallotKind.DOCENTE)
		{
			System.out.println("DOCNETE");
			if(census==null)
			{
				showErrorType=true;
			}
			startDate=new Date();
			endDate=new Date();
		}
		
	}
	public void onSuccessFromTypeForm()
	{
		if(request.isXHR())
		{
			if(showErrorType||showBadDate)
				ajaxResponseRenderer.addRender("typeZone", typeZone);
			else
			{
				
				showType=false;
				if(method==Method.MAYORIA_RELATIVA)
				{
					showMayRel=true;
					//options=new ArrayList<String>();options.add("1");options.add("2");
					ajaxResponseRenderer.addRender("typeZone", typeZone).addRender("mayRelZone",mayRelZone);
				}
				else if(method==Method.KEMENY)
				{
					showKemeny=true;
					ajaxResponseRenderer.addRender("typeZone", typeZone).addRender("kemenyZone",kemenyZone);
				}
				
			}
		}
		
		
	}
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ////////////////////////////////////////////////// MAYORIA REL ZONE /////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	static final private String[] NUMBERS2_7 = new String[] { "2", "3", "4","5","6","7" };
	@InjectComponent
	private Zone mayRelZone;
	@Property
	@Persist
	private boolean showMayRel;
	@Property
	@Persist
	private boolean showErrorMayRel;
	
	@Persist
	private RelativeMajority relativeMajority;
	
	@Property
	@Persist 
	@Validate("required")
	private String mayRelNumOp;
	@Persist
	private int numOpt;
	@Property
	@Persist 
	private String [] mayRelModel;
	
	@Property
	@Persist
	private String mayRelOp1;
	@Property
	@Persist
	private String mayRelOp2;
	@Property
	@Persist
	private String mayRelOp3;
	@Property
	@Persist
	private String mayRelOp4;
	@Property
	@Persist
	private String mayRelOp5;
	@Property
	@Persist
	private String mayRelOp6;
	@Property
	@Persist
	private String mayRelOp7;
	
	private String option;
	public String getOption() {
		System.out.println("get");
		return option;
	}
	public void setOption(String option) {
		System.out.println("set");
		this.option = option;
	}
	
	
	public void  onValueChangedFromMayRelSel(String str)
	{
		numOpt=Integer.parseInt(str);
		ajaxResponseRenderer.addRender("mayRelZone", mayRelZone);
	}
	
	 
	public boolean isShowMay3()
	{
		if(numOpt>=3)
			return true;
		return false;
	}
	public boolean isShowMay4()
	{
		if(numOpt>=4)
			return true;
		return false;
	}
	public boolean isShowMay5()
	{
		if(numOpt>=5)
			return true;
		return false;
	}
	public boolean isShowMay6()
	{
		if(numOpt>=6)
			return true;
		return false;
	}
	public boolean isShowMay7()
	{
		if(numOpt>=7)
			return true;
		return false;
	}
	
	 public void onValidateFromMayRelForm()
	 {
		 showErrorMayRel=false;
		 if(mayRelOp1==null || mayRelOp2==null)
		 {
			
			 showErrorMayRel=true;
		 }
		 System.out.println("NUMOP->"+numOpt);
		 switch(numOpt)
		 {
		 	case 7:
		 		if(mayRelOp7==null){showErrorMayRel=true;}
		 	case 6:
		 		if(mayRelOp6==null){showErrorMayRel=true;}
		 	case 5:
		 		if(mayRelOp5==null){showErrorMayRel=true;}
		 	case 4:
		 		if(mayRelOp4==null){showErrorMayRel=true;}
		 	case 3:
		 		if(mayRelOp3==null){showErrorMayRel=true;}
		 		break;
		 	default:
		 		showErrorMayRel=false;
		 }
		 if(!showErrorMayRel)//añadir las opciones
		 {
			 List<String> listOptions=new LinkedList<String>();
			 listOptions.add(mayRelOp1);
			 listOptions.add(mayRelOp2);
			 if(numOpt>=3)
			 {
				 listOptions.add(mayRelOp3);
			 }
			 if(numOpt>=4)
			 {
				 listOptions.add(mayRelOp4);
			 }
			 if(numOpt>=5)
			 {
				 listOptions.add(mayRelOp5);
			 }
			 if(numOpt>=6)
			 {
				 listOptions.add(mayRelOp6);
			 }
			 if(numOpt>=7)
			 {
				 listOptions.add(mayRelOp7);
			 }
		
			 relativeMajority=new RelativeMajority(listOptions);
		 }
	 }
	 public Object onSuccessFromMayRelForm()
	 {
		if(request.isXHR())
		 {
			 if(showErrorMayRel)
			 {
				 ajaxResponseRenderer.addRender("mayRelZone", mayRelZone);
			 }
			 else //No hay errores
			 {
				 ballot=setBallotData();
				 relativeMajority.setId(UUID.generate());
				 ballot.setIdBallotData(relativeMajority.getId());
				 relativeMajority.setBallotId(ballot.getId());
				 
				 voteDao=DB4O.getVoteDao(datasession.getDBName());
				 relativeMajorityDao=DB4O.getRelativeMajorityDao(datasession.getDBName());
				 
				 if(ballot.isTeaching())//Votacion Docente
				 {
					 //Genera votos aleatoriamente para la votacion docente
					 ballot.setIdCensus("none");
					 relativeMajority.setVotes(GenerateDocentVotes.generateRelativeMajority(relativeMajority.getOptions(), Integer.parseInt(census)));
					 //HACER RECUENTO VOTOS AQUI PARA DOCENTES
					 relativeMajority.calcularMayoriaRelativa();
					 Vote vote=new Vote(ballot.getId(),datasession.getId(),true);//Almacena vote para docente(solo el creador)
					 ballot.setEnded(true);
					 ballot.setCounted(true);
					 voteDao.store(vote);
				 }
				 else//Votacion Normal
				 {
					 boolean creatorInCensus=false;
					 for(String idUser:censusNormal.getUsersCounted())
					 {
						 	if(idUser.equals(datasession.getId())){creatorInCensus=true;}
						 
						 voteDao.store(new Vote(ballot.getId(),idUser));//Almacena vote con ids de users censados
					 }
					 if(!creatorInCensus)
					 {
						 voteDao.store(new Vote(ballot.getId(),datasession.getId()));
					 }
					 
				 }
				 
				 relativeMajorityDao.store(relativeMajority);
				 ballotDao.store(ballot);
				 return Index.class;
			 }
		 }
		return null;
	 }
	
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ////////////////////////////////////////////////////// KEMENY ZONE //////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@InjectComponent
	private Zone kemenyZone;
	
	@Property
	@Persist
	private boolean showKemeny;
	@Property
	@Persist
	private boolean showErrorKemeny;
	
	@Property
	@Persist
	private boolean showRepeatKemeny;
	
	@Persist
	private Kemeny kemeny;
	
	@Persist
	@Property
	@Validate("minLength=3")
	private String cat1;
	@Persist
	@Property
	@Validate("minLength=3")
	private String cat2;
	
	@Persist
	@Property
	@Validate("minLength=3")
	private String cat1_op1;
	@Persist
	@Property
	@Validate("minLength=3")
	private String cat1_op2;
	
	@Persist
	@Property
	@Validate("minLength=3")
	private String cat2_op1;
	@Persist
	@Property
	@Validate("minLength=3")
	private String cat2_op2;
	
	public void onValidateFromKemenyForm()
	{
		showErrorKemeny=false;
		showRepeatKemeny=false;

		if(cat1==null || cat2==null || cat1_op1==null || cat1_op2==null || cat2_op1==null || cat2_op2==null )
		{
			showErrorKemeny=true;
		}
		if(cat1.toLowerCase().equals(cat2))
		{
			showRepeatKemeny=true;
		}
		if(cat1_op1.toLowerCase().equals(cat1_op2) || cat1_op1.toLowerCase().equals(cat2_op1) || cat1_op1.toLowerCase().equals(cat2_op2))
		{
			showRepeatKemeny=true;
		}
		if(cat1_op2.toLowerCase().equals(cat2_op1.toLowerCase()) || cat1_op2.toLowerCase().equals(cat2_op1.toLowerCase()) ||cat1_op2.toLowerCase().equals(cat2_op2.toLowerCase()))
		{
			showRepeatKemeny=true;

		}
		if(cat2_op1.toLowerCase().equals(cat2_op2.toLowerCase()))
		{
			showRepeatKemeny=true;
		}
		if(showErrorKemeny==false && showRepeatKemeny==false)
		{
			List<String> categories=new LinkedList<String>();
			categories.add(cat1);
			categories.add(cat2);
			List<String> option1=new LinkedList<String>();
			option1.add(cat1_op1);
			option1.add(cat1_op2);
			List<String> option2=new LinkedList<String>();
			option2.add(cat2_op1);
			option2.add(cat2_op2);
			List<List<String>>options=new LinkedList<List<String>>();
			options.add(option1);
			options.add(option2);
			
			kemeny=new Kemeny(options,categories);
		}
	} 
	public Object onSuccessFromKemenyForm()
	{
		if(showErrorKemeny && showRepeatKemeny)
		{
			ajaxResponseRenderer.addRender("kemenyZone",kemenyZone);
		}
		else
		{
			ballot=setBallotData();
			kemeny.setId(UUID.generate());
			ballot.setIdBallotData(kemeny.getId());
			kemeny.setBallotId(ballot.getId());
			 
			voteDao=DB4O.getVoteDao(datasession.getDBName());
			kemenyDao=DB4O.getKemenyDao(datasession.getDBName());
			
			if(ballot.isTeaching())
			{
				ballot.setIdCensus("none");
				kemeny.setVotes(GenerateDocentVotes.generateKemeny(kemeny.getOptionPairs(), Integer.parseInt(census)));
				kemeny.calcularKemeny();
				ballot.setEnded(true);
				Vote vote=new Vote(ballot.getId(),datasession.getId(),true);//Almacena vote para docente(solo el creador)
				ballot.setEnded(true);
				ballot.setCounted(true);
				voteDao.store(vote);
				
			}
			else//Votacion Normal
			{
				 boolean creatorInCensus=false;
				 for(String idUser:censusNormal.getUsersCounted())
				 {
					 	if(idUser.equals(datasession.getId())){creatorInCensus=true;}
					 
					 	voteDao.store(new Vote(ballot.getId(),idUser));//Almacena vote con ids de users censados
				 }
				 if(!creatorInCensus)
				 {
					 voteDao.store(new Vote(ballot.getId(),datasession.getId()));
				 }
				 
			 }
			 
			 kemenyDao.store(kemeny);
			 ballotDao.store(ballot);
			 return Index.class;
			
		}
		return null;
	}
	  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ///////////////////////////////////////////////////// BACK EVENT /////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void onBack(List<String> list)
	{
		String from=list.get(0);
		String to=list.get(1);
		if(to.equals("toGeneral"))
		{
			showType=false;
			showGeneral=true;
			ajaxResponseRenderer.addRender("generalZone", generalZone).addRender("typeZone", typeZone);
		}
		if(to.equals("toType"))
		{
			if(from.equals("fromMayRel"))
			{
				showType=true;
				showMayRel=false;
				ajaxResponseRenderer.addRender("typeZone", typeZone).addRender("mayRelZone",mayRelZone);
			}
			if(from.equals("fromKemeny"))
			{
				showType=true;
				showKemeny=false;
				ajaxResponseRenderer.addRender("typeZone", typeZone).addRender("kemenyZone",kemenyZone);
			}
		}
	}
	  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ////////////////////////////////////////////////////////// TOOLS /////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private Ballot setBallotData()
	{
		
		Ballot newBallot=new Ballot();
		newBallot.setId(UUID.generate());
		newBallot.setName(ballotName);
		newBallot.setDescription(description);
		newBallot.setIdOwner(datasession.getId());
		if(ballotKind==BallotKind.NORMAL)
		{
			newBallot.setIdCensus(censusNormal.getId());
		}
		else
		{
			newBallot.setTeaching(true);
		}
		newBallot.setMethod(method);
		newBallot.setStartDate(cal1.getTime());
		newBallot.setEndDate(cal2.getTime());
		return newBallot;
	}
	
	  ////////////////////////////////////////////////////////////////////////////////////
		 /////////////////////////////////// ON ACTIVATE //////////////////////////////////// 
		////////////////////////////////////////////////////////////////////////////////////
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
					if(datasession.isMaker())
					{
						return null;
					}
					return UnauthorizedAttempt.class;
				case 1:
					return null;
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
