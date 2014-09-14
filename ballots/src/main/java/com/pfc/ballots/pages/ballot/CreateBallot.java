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
import com.pfc.ballots.dao.BordaDao;
import com.pfc.ballots.dao.CensusDao;
import com.pfc.ballots.dao.EmailAccountDao;
import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.dao.KemenyDao;
import com.pfc.ballots.dao.RangeVotingDao;
import com.pfc.ballots.dao.RelativeMajorityDao;
import com.pfc.ballots.dao.UserDao;
import com.pfc.ballots.dao.VoteDao;
import com.pfc.ballots.data.BallotKind;
import com.pfc.ballots.data.DataSession;
import com.pfc.ballots.data.Method;
import com.pfc.ballots.encoder.CensusEncoder;
import com.pfc.ballots.entities.Ballot;
import com.pfc.ballots.entities.Census;
import com.pfc.ballots.entities.EmailAccount;
import com.pfc.ballots.entities.Profile;
import com.pfc.ballots.entities.Vote;
import com.pfc.ballots.entities.ballotdata.Borda;
import com.pfc.ballots.entities.ballotdata.Kemeny;
import com.pfc.ballots.entities.ballotdata.RangeVoting;
import com.pfc.ballots.entities.ballotdata.RelativeMajority;
import com.pfc.ballots.pages.Index;
import com.pfc.ballots.pages.SessionExpired;
import com.pfc.ballots.pages.UnauthorizedAttempt;
import com.pfc.ballots.pages.admin.AdminMail;
import com.pfc.ballots.util.GenerateDocentVotes;
import com.pfc.ballots.util.Mail;
import com.pfc.ballots.util.UUID;

/**
 * 
 * BallotWizzard class is the controller for the BallotWizzard page that
 * allow to create a new ballot
 * 
 * @author Mario Temprano Martin
 * @version 1.0 JUN-2014
 */

public class CreateBallot {
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
	
	static final private String[] NUMBERS2_7 = new String[] { "2", "3", "4","5","6","7" };
	static final private String[] NUMBERS2_15 = new String[] { "2", "3", "4","5","6","7","8","9","10","11","12","13","14","15" };
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
	BordaDao bordaDao;
	RangeVotingDao rangeDao;
	UserDao userDao;
	EmailAccountDao emailAccountDao;
	 /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ////////////////////////////////////////////////////// INITIALIZE ///////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Initialize values
	 */
	public void setupRender()
	{
		componentResources.discardPersistentFieldChanges();
		
		
		mayRelModel=NUMBERS2_15;
		bordaModel=NUMBERS2_7;
		rangeModel=NUMBERS2_7;
		
		numOpt=2;
		bordaOpt1=2;
		bordaOpt2=2;
		rangeOpt=2;
		
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
		showGeneral=false;
		showErrorGeneral=false;
		showBadName=false;
		
		showType=true;
		showNormalCensus=false;
		showBadDate=false;
		showErrorType=false;
		
		showMayRel=false;
		showErrorMayRel=false;
		
		showKemeny=false;
		showErrorKemeny=false;
		showBadCharKemeny=false;
		
		showBorda=false;
		showBordaFill=false;
		showBordaRepeat=false;
		showBordaBadChar=false;
		
		showRange=false;
		showRangeMax=false;
		showRangeValues=false;
		showRangeFill=false;
		showRangeBadChar=false;
		showRangeRepeated=false;
		showRangeBadNum=false;
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
	
	@Property
	@Persist
	private String numVotes;
	
	
	
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
	
	
	
	public boolean isShowExist()
	{
		if(ballotKind==BallotKind.PUBLICA)
			return true;
		else
			return existCensus;
	}
	
	/**
	 * Controls the change of the ballot kind
	 * and corresponding wizard changes
	 * @param kind
	 */
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
		if(kind==BallotKind.PRIVADA)
		{
			showNormalCensus=true;
		}
		if(kind==BallotKind.PUBLICA)
		{
			censusModel=NO_CENSUS;
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
	
	/**
	 * Checks if the data of the ballot are correct
	 */
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
		else if(ballotKind==BallotKind.PRIVADA || ballotKind==BallotKind.PUBLICA)
		{
			
			if(censusNormal==null && ballotKind==BallotKind.PRIVADA)
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
			
			if(census==null)
			{
				showErrorType=true;
			}
			startDate=new Date();
			endDate=new Date();
		}
	
		
	}
	
	public boolean isTeaching()
	{
		if(ballotKind==BallotKind.DOCENTE)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isShowPublic()
	{
		if(ballotKind==BallotKind.PUBLICA)
			return true;
		else
			return false;
	}
	/**
	 * If the data are correct, give access to the next
	 * step of the wizard 
	 */
	public void onSuccessFromTypeForm()
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
				}
			}
			if(showErrorGeneral || showBadName)
			{
				ajaxResponseRenderer.addRender("typeZone", typeZone);
			}
			
			else if(ballotKind==BallotKind.SENSIBLE)
			{
				ajaxResponseRenderer.addRender("typeZone", typeZone);
			}
			else if(showErrorType||showBadDate)
				ajaxResponseRenderer.addRender("typeZone", typeZone);
			else
			{
				
				if(!valueOk(census))
				{
					ajaxResponseRenderer.addRender("typeZone", typeZone);
				}
				else if(method==Method.MAYORIA_RELATIVA)
				{
					showType=false;
					showMayRel=true;
					//options=new ArrayList<String>();options.add("1");options.add("2");
					ajaxResponseRenderer.addRender("typeZone", typeZone).addRender("mayRelZone",mayRelZone);
				}
				else if(method==Method.KEMENY)
				{
					showType=false;
					showKemeny=true;
					ajaxResponseRenderer.addRender("typeZone", typeZone).addRender("kemenyZone",kemenyZone);
				}
				else if(method==Method.BORDA)
				{
					showType=false;
					showBorda=true;
					ajaxResponseRenderer.addRender("typeZone", typeZone).addRender("bordaZone", bordaZone);
				}
				else if(method==Method.RANGE_VOTING)
				{
					showType=false;
					showRange=true;
					ajaxResponseRenderer.addRender("typeZone", typeZone).addRender("rangeZone",rangeZone);
				}
				
				
			}
		}
		
		
	}
	
	@Persist
	@Property
	private boolean badValue;
	
	public boolean valueOk(String num)
	{
		if(ballotKind==BallotKind.DOCENTE)
		{
			if(isNumeric(num))
			{
				if(Integer.parseInt(num) < 100000)
				{
					return true;
				}
				else
				{
					badValue=true;
					return false;
				}
			}
			else
			{
				badValue=true;
				return false;
			}
		}
		else{
			return true;
		}
		
	}
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ////////////////////////////////////////////////// MAYORIA REL ZONE /////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@InjectComponent
	private Zone mayRelZone;
	@Property
	@Persist
	private boolean showMayRel;
	@Property
	@Persist
	private boolean showErrorMayRel;
	@Property
	@Persist
	private boolean showRepeatedMayRel;
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
	@Property
	@Persist
	private String mayRelOp8;
	@Property
	@Persist
	private String mayRelOp9;
	@Property
	@Persist
	private String mayRelOp10;
	@Property
	@Persist
	private String mayRelOp11;
	@Property
	@Persist
	private String mayRelOp12;
	@Property
	@Persist
	private String mayRelOp13;
	@Property
	@Persist
	private String mayRelOp14;
	@Property
	@Persist
	private String mayRelOp15;

	
	private String option;
	public String getOption() {
		System.out.println("get");
		return option;
	}
	public void setOption(String option) {
		System.out.println("set");
		this.option = option;
	}
	
	/**
	 * Controls the number of opitions for the relative majority
	 * @param str
	 */
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
	public boolean isShowMay8()
	{
		if(numOpt>=8)
			return true;
		return false;
	}
	public boolean isShowMay9()
	{
		if(numOpt>=9)
			return true;
		return false;
	}
	public boolean isShowMay10()
	{
		if(numOpt>=10)
			return true;
		return false;
	}
	public boolean isShowMay11()
	{
		if(numOpt>=11)
			return true;
		return false;
	}
	public boolean isShowMay12()
	{
		if(numOpt>=12)
			return true;
		return false;
	}
	public boolean isShowMay13()
	{
		if(numOpt>=13)
			return true;
		return false;
	}
	public boolean isShowMay14()
	{
		if(numOpt>=14)
			return true;
		return false;
	}
	public boolean isShowMay15()
	{
		if(numOpt>=15)
			return true;
		return false;
	}
	/**
	 * Checks the options of the relative majority
	 */
	 public void onValidateFromMayRelForm()
	 {
		 showErrorMayRel=false;
		 showRepeatedMayRel=false;
		 if(mayRelOp1==null || mayRelOp2==null)
		 {
			
			 showErrorMayRel=true;
		 }
		
		 System.out.println("NUMOP->"+numOpt);
		 switch(numOpt)
		 {
		 	case 15:
		 		if(mayRelOp15==null){showErrorMayRel=true;}
		 	case 14:
		 		if(mayRelOp14==null){showErrorMayRel=true;}
		 	case 13:
		 		if(mayRelOp13==null){showErrorMayRel=true;}
		 	case 12:
		 		if(mayRelOp12==null){showErrorMayRel=true;}
		 	case 11:
		 		if(mayRelOp11==null){showErrorMayRel=true;}
		 	case 10:
		 		if(mayRelOp10==null){showErrorMayRel=true;}
		 	case 9:
		 		if(mayRelOp9==null){showErrorMayRel=true;}
		 	case 8:
		 		if(mayRelOp8==null){showErrorMayRel=true;}
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
			 if(numOpt>=8)
			 {
				 listOptions.add(mayRelOp8);
			 }
			 if(numOpt>=9)
			 {
				 listOptions.add(mayRelOp9);
			 }
			 if(numOpt>=10)
			 {
				 listOptions.add(mayRelOp10);
			 }
			 if(numOpt>=11)
			 {
				 listOptions.add(mayRelOp11);
			 }
			 if(numOpt>=12)
			 {
				 listOptions.add(mayRelOp12);
			 }
			 if(numOpt>=13)
			 {
				 listOptions.add(mayRelOp13);
			 }
			 if(numOpt>=14)
			 {
				 listOptions.add(mayRelOp14);
			 }
			 if(numOpt>=15)
			 {
				 listOptions.add(mayRelOp15);
			 }
			 for(int x=0;x<listOptions.size();x++)
			 {
				 for(int i=x+1;i<listOptions.size();i++)
				 {
					 if(listOptions.get(x).toLowerCase().equals(listOptions.get(i).toLowerCase()))
					 {
						showRepeatedMayRel=true; 
					 }
						 
				 }
			 }
			 relativeMajority=new RelativeMajority(listOptions);
		 }
	 }
	 
	 /**
	  * Stores all the necessary data of the ballot
	  * @return
	  */
	 public Object onSuccessFromMayRelForm()
	 {
		if(request.isXHR())
		 {
			 if(showErrorMayRel || showRepeatedMayRel)
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
					 this.sendMail(datasession.getId(), ballot);
					 ballot.setEnded(true);
					 ballot.setCounted(true);
					 voteDao.store(vote);
				 }
				 else if(ballotKind==BallotKind.PUBLICA){
					 ballot.setIdCensus("none");
				 }
				 else
				 {
					 boolean creatorInCensus=false;
					 this.sendMail(censusNormal,ballot);
					 for(String idUser:censusNormal.getUsersCounted())
					 {
						 	if(idUser.equals(datasession.getId())){creatorInCensus=true;}
						 
						 voteDao.store(new Vote(ballot.getId(),idUser));//Almacena vote con ids de users censados
					 }
					 if(!creatorInCensus)
					 {
						 voteDao.store(new Vote(ballot.getId(),datasession.getId()));
						 this.sendMail(datasession.getId(), ballot);
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
	@Property
	@Persist
	private boolean showBadCharKemeny;
	
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
	/**
	 * Checks if Kameny options are correct
	 */
	public void onValidateFromKemenyForm()
	{
		showErrorKemeny=false;
		showRepeatKemeny=false;
		showBadCharKemeny=false;

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
		
		//Comprobación de caracter "-". No puede estar ya que lo utiliza la biblioteca de calculo para separar argumentos
		
		
		if(cat1_op1.contains("-")||cat1_op2.contains("-")||cat2_op1.contains("-")||cat2_op2.contains("-"))
		{
			showBadCharKemeny=true;
		}
		
		if(showErrorKemeny==false && showRepeatKemeny==false && showBadCharKemeny==false)
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
	
	 /**
	  * Stores all the necessary data of the ballot
	  * @return
	  */
	public Object onSuccessFromKemenyForm()
	{
		if(showErrorKemeny || showRepeatKemeny || showBadCharKemeny)
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
				this.sendMail(datasession.getId(),ballot);
				ballot.setEnded(true);
				ballot.setCounted(true);
				voteDao.store(vote);
				
			}else if(ballotKind==BallotKind.PUBLICA){
				 ballot.setIdCensus("none");
			 }
			else//Votacion Normal
			{
				 boolean creatorInCensus=false;
				 this.sendMail(censusNormal, ballot);
				 for(String idUser:censusNormal.getUsersCounted())
				 {
					 	if(idUser.equals(datasession.getId())){creatorInCensus=true;}
					 
					 	voteDao.store(new Vote(ballot.getId(),idUser));//Almacena vote con ids de users censados
				 }
				 if(!creatorInCensus)
				 {
					 voteDao.store(new Vote(ballot.getId(),datasession.getId()));
					 this.sendMail(datasession.getId(),ballot);
				 }
				 
			 }
			 
			 kemenyDao.store(kemeny);
			 ballotDao.store(ballot);
			 return Index.class;
			
		}
		return null;
	}
	
  	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ////////////////////////////////////////////////////// BORDA ZONE ///////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@InjectComponent
	private Zone bordaZone;
	
	@InjectComponent
	private Zone bordaForm1Zone;
	
	@InjectComponent
	private Zone bordaForm2Zone;
	
	@Persist
	@Property
	private boolean showBorda;
	
	
	@Property
	@Persist 
	private String [] bordaModel;
	
	@Persist
	private int bordaOpt1;
	
	@Property
	@Persist
	@Validate("required")
	private String bordaNumOpt1;
	
	
	@Persist
	@Validate("required")
	private int bordaOpt2;
	
	
	@Property
	@Persist
	@Validate("required")
	private String bordaNumOpt2;
	
	@Property
	@Persist
	private String bcat1;
	@Property
	@Persist
	private String b1Opt1;
	@Property
	@Persist
	private String b1Opt2;
	@Property
	@Persist
	private String b1Opt3;
	@Property
	@Persist
	private String b1Opt4;
	@Property
	@Persist
	private String b1Opt5;
	@Property
	@Persist
	private String b1Opt6;
	@Property
	@Persist
	private String b1Opt7;
	
	@Persist
	@Property
	private boolean showBordaFill;
	
	@Persist
	@Property
	private boolean showBordaRepeat;
	
	@Persist
	@Property
	private boolean showBordaBadChar;
	
	//Primera zona
	
	public boolean isShowB1Opt3()
	{
		if (bordaOpt1>=3)
			return true;
		else
			return false;
	}
	public boolean isShowB1Opt4()
	{
		if (bordaOpt1>=4)
			return true;
		else
			return false;
	}
	public boolean isShowB1Opt5()
	{
		if (bordaOpt1>=5)
			return true;
		else
			return false;
	}
	public boolean isShowB1Opt6()
	{
		if (bordaOpt1>=6)
			return true;
		else
			return false;
	}
	public boolean isShowB1Opt7()
	{
		if (bordaOpt1>=7)
			return true;
		else
			return false;
	}
	@Property
	@Persist
	private String bcat2;
	@Property
	@Persist
	private String b2Opt1;
	@Property
	@Persist
	private String b2Opt2;
	@Property
	@Persist
	private String b2Opt3;
	@Property
	@Persist
	private String b2Opt4;
	@Property
	@Persist
	private String b2Opt5;
	@Property
	@Persist
	private String b2Opt6;
	@Property
	@Persist
	private String b2Opt7;
	
	@Persist
	private List<String> bordaList1;
	@Persist
	private List<String> bordaList2;
	
	public boolean isShowB2Opt3()
	{
		if (bordaOpt2>=3)
			return true;
		else
			return false;
	}
	public boolean isShowB2Opt4()
	{
		if (bordaOpt2>=4)
			return true;
		else
			return false;
	}
	public boolean isShowB2Opt5()
	{
		if (bordaOpt2>=5)
			return true;
		else
			return false;
	}
	public boolean isShowB2Opt6()
	{
		if (bordaOpt2>=6)
			return true;
		else
			return false;
	}
	public boolean isShowB2Opt7()
	{
		if (bordaOpt2>=7)
			return true;
		else
			return false;
	}
	
	public void  onValueChangedFromBordaSel1(String str)
	{
		bordaOpt1=Integer.parseInt(str);
		ajaxResponseRenderer.addRender("bordaForm1Zone", bordaForm1Zone);
	}
	
	
	public void  onValueChangedFromBordaSel2(String str)
	{
		bordaOpt2=Integer.parseInt(str);
		ajaxResponseRenderer.addRender("bordaForm2Zone", bordaForm2Zone);
	}
	
	
	public void onValidateFromBordaForm()
	{
		showBordaFill=false;
		showBordaRepeat=false;
		showBordaBadChar=false;
		bordaList1=new LinkedList<String>();
		bordaList2=new LinkedList<String>();
		if(bcat1==null || bcat2==null)
		{
			showBordaFill=true;
		}
		switch(bordaOpt1)
		{
			case 7:
				if(b1Opt7==null){showBordaFill=true;}
				else if(b1Opt7.contains("-")){showBordaBadChar=true;}
			case 6:
				if(b1Opt6==null){showBordaFill=true;}
				else if(b1Opt6.contains("-")){showBordaBadChar=true;}
			case 5:
				if(b1Opt5==null){showBordaFill=true;}
				else if(b1Opt5.contains("-")){showBordaBadChar=true;}
			case 4:
				if(b1Opt4==null){showBordaFill=true;}
				else if(b1Opt4.contains("-")){showBordaBadChar=true;}
			case 3:
				if(b1Opt3==null){showBordaFill=true;}
				else if(b1Opt3.contains("-")){showBordaBadChar=true;}
			case 2:
				if(b1Opt2==null){showBordaFill=true;}
				else if(b1Opt2.contains("-")){showBordaBadChar=true;}
				if(b1Opt1==null){showBordaFill=true;}
				else if(b1Opt1.contains("-")){showBordaBadChar=true;}
				break;
			default:
				showBordaFill=true;
		}
		switch(bordaOpt2)
		{
			case 7:
				if(b2Opt7==null){showBordaFill=true;}
				else if(b2Opt7.contains("-")){showBordaBadChar=true;}
			case 6:
				if(b2Opt6==null){showBordaFill=true;}
				else if(b2Opt6.contains("-")){showBordaBadChar=true;}
			case 5:
				if(b2Opt5==null){showBordaFill=true;}
				else if(b2Opt5.contains("-")){showBordaBadChar=true;}
			case 4:
				if(b2Opt4==null){showBordaFill=true;}
				else if(b2Opt4.contains("-")){showBordaBadChar=true;}
			case 3:
				if(b2Opt3==null){showBordaFill=true;}
				else if(b2Opt3.contains("-")){showBordaBadChar=true;}
			case 2:
				if(b2Opt2==null){showBordaFill=true;}
				else if(b2Opt2.contains("-")){showBordaBadChar=true;}
				if(b2Opt1==null){showBordaFill=true;}
				else if(b2Opt1.contains("-")){showBordaBadChar=true;}
				break;
			default:
				showBordaFill=true;
		}
		if(showBordaFill==false)
		{
			bordaList1.add(b1Opt1);
			bordaList1.add(b1Opt2);
			bordaList2.add(b2Opt1);
			bordaList2.add(b2Opt2);
			
			if(bordaOpt1>=3)
				bordaList1.add(b1Opt3);
			
			if(bordaOpt2>=3)
				bordaList2.add(b2Opt3);
			
			if(bordaOpt1>=4)
				bordaList1.add(b1Opt4);
			
			if(bordaOpt2>=4)
				bordaList2.add(b2Opt4);
			
			if(bordaOpt1>=5)
				bordaList1.add(b1Opt5);
			
			if(bordaOpt2>=5)
				bordaList2.add(b2Opt5);
			
			if(bordaOpt1>=6)
				bordaList1.add(b1Opt6);

			if(bordaOpt2>=6)
				bordaList2.add(b2Opt6);
			
			if(bordaOpt1>=7)
				bordaList1.add(b1Opt7);

			if(bordaOpt2>=7)
				bordaList2.add(b2Opt7);
			
			
			
			for(int x=0;x<bordaList1.size();x++)
			{
				for(int i=x+1;i<bordaList1.size();i++)
				{
					if(bordaList1.get(x).toLowerCase().equals(bordaList1.get(i).toLowerCase()))
						showBordaRepeat=true;
				}
				for(int i=0;i<bordaList2.size();i++)
				{
					if(bordaList1.get(x).toLowerCase().toLowerCase().equals(bordaList2.get(i).toLowerCase()))
						showBordaRepeat=true;
				}
				
			}
			for(int x=0;x<bordaList2.size();x++)
			{
				for(int i=x+1;i<bordaList2.size();i++)
				{
					if(bordaList2.get(x).toLowerCase().equals(bordaList2.get(i).toLowerCase()))
							showBordaRepeat=true;
				}
			}
			
			
		}
		
		
	}
	
	
	public Object onSuccessFromBordaForm()
	{
		if(request.isXHR())
		{
			if(showBordaRepeat || showBordaFill || showBordaBadChar)
			{
				ajaxResponseRenderer.addRender("bordaZone", bordaZone);
			}
			else
			{
				List<List<String>> listaOpciones=new LinkedList<List<String>>();
				listaOpciones.add(bordaList1);
				listaOpciones.add(bordaList2);
				Borda borda=new Borda(listaOpciones);
				borda.addCategory(bcat1);
				borda.addCategory(bcat2);
				ballot=setBallotData();
				borda.setId(UUID.generate());
				ballot.setIdBallotData(borda.getId());
				borda.setBallotId(ballot.getId());
				 
				voteDao=DB4O.getVoteDao(datasession.getDBName());
				bordaDao=DB4O.getBordaDao(datasession.getDBName());
				
				if(ballot.isTeaching())
				{
					ballot.setIdCensus("none");
					borda.setVotes(GenerateDocentVotes.generateBorda(borda.getBordaOptions(), Integer.parseInt(census)));
					borda.calcularBorda();
					ballot.setEnded(true);
					Vote vote=new Vote(ballot.getId(),datasession.getId(),true);//Almacena vote para docente(solo el creador)
					this.sendMail(datasession.getId(),ballot);
					ballot.setEnded(true);
					ballot.setCounted(true);
					voteDao.store(vote);
				}
				else if(ballotKind==BallotKind.PUBLICA){
					 ballot.setIdCensus("none");
				 }
				else//Votacion Normal
				{
					 boolean creatorInCensus=false;
					 this.sendMail(censusNormal, ballot);
					 for(String idUser:censusNormal.getUsersCounted())
					 {
						 	if(idUser.equals(datasession.getId())){creatorInCensus=true;}
						 
						 	voteDao.store(new Vote(ballot.getId(),idUser));//Almacena vote con ids de users censados
					 }
					 if(!creatorInCensus)
					 {
						 voteDao.store(new Vote(ballot.getId(),datasession.getId()));
						 this.sendMail(datasession.getId(),ballot);
					 }
					 
				 }
				 
				 bordaDao.store(borda);
				 ballotDao.store(ballot);
				 return Index.class;
				
			}
					
		}
		return null;
	}
	
	 /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ////////////////////////////////////////////////// RANGE ZONE /////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@InjectComponent
	private Zone rangeZone;
	@Property
	@Persist
	private boolean showRange;
	
	@Property
	@Persist
	private RangeVoting range;
	
	
	@Property
	@Persist 
	private String [] rangeModel;
	
	
	@Persist
	@Property
	private int rangeOpt;
	
	@Property
	@Persist
	@Validate("required")
	private String rangeNumOpt;
	
	@Property
	@Persist
	private boolean showRangeMax;
	@Property
	@Persist
	private boolean showRangeValues;
	@Property
	@Persist
	private boolean showRangeFill;
	@Property
	@Persist
	private boolean showRangeBadChar;
	@Property
	@Persist
	private boolean showRangeBadNum;
	@Property
	@Persist
	private boolean showRangeRepeated;
	
	
	@Property
	@Persist
	private String maxRange;
	@Property
	@Persist
	private String minRange;
	
	@Property
	@Persist
	private String rangeOpt1;
	@Property
	@Persist
	private String rangeOpt2;
	@Property
	@Persist
	private String rangeOpt3;
	@Property
	@Persist
	private String rangeOpt4;
	@Property
	@Persist
	private String rangeOpt5;
	@Property
	@Persist
	private String rangeOpt6;
	@Property
	@Persist
	private String rangeOpt7;
	@Property
	@Persist
	private List<String> rangeOptions;
	
	public boolean isShowRange3()
	{
		if (rangeOpt>=3)
			return true;
		else
			return false;
	}
	public boolean isShowRange4()
	{
		if (rangeOpt>=4)
			return true;
		else
			return false;
	}
	public boolean isShowRange5()
	{
		if (rangeOpt>=5)
			return true;
		else
			return false;
	}
	public boolean isShowRange6()
	{
		if (rangeOpt>=6)
			return true;
		else
			return false;
	}
	public boolean isShowRange7()
	{
		if (rangeOpt>=7)
			return true;
		else
			return false;
	}
	public void  onValueChangedFromRangeSel(String str)
	{
		rangeOpt=Integer.parseInt(str);
		ajaxResponseRenderer.addRender("rangeZone", rangeZone);
	}
	
	public void onValidateFromRangeForm()
	{
		showRangeMax=false;
		showRangeValues=false;
		showRangeRepeated=false;
		showRangeBadNum=false;
		showRangeBadNum=false;
		showRangeFill=false;
		
		if(minRange==null)
		{
			minRange="0";
		}
		else if(isNumeric(minRange))
		{
			showRangeBadNum=true;
		}
			
		if(maxRange==null)
		{
			showRangeMax=true;
		}
		else
		{
			if(isNumeric(maxRange))
			{
				showRangeBadNum=true;
			}
			else if(Integer.parseInt(minRange)>=Integer.parseInt(maxRange))
			{
				showRangeValues=true;
			}
		}
		switch(rangeOpt)
		{
			case 7:
				if(rangeOpt7==null){showRangeFill=true;}
				else if(rangeOpt7.contains("ñ")){showRangeBadChar=true;}
			case 6:
				if(rangeOpt6==null){showRangeFill=true;}
				else if(rangeOpt6.contains("ñ")){showRangeBadChar=true;}
			case 5:
				if(rangeOpt5==null){showRangeFill=true;}
				else if(rangeOpt5.contains("ñ")){showRangeBadChar=true;}
			case 4:
				if(rangeOpt4==null){showRangeFill=true;}
				else if(rangeOpt4.contains("ñ")){showRangeBadChar=true;}
			case 3:
				if(rangeOpt3==null){showRangeFill=true;}
				else if(rangeOpt3.contains("ñ")){showRangeBadChar=true;}
			case 2:
				if(rangeOpt2==null){showRangeFill=true;}
				else if(rangeOpt2.contains("ñ")){showRangeBadChar=true;}
				if(rangeOpt1==null){showRangeFill=true;}
				else if(rangeOpt1.contains("ñ")){showRangeBadChar=true;}
				break;
			default:
				showRangeFill=true;
		}
		
		if(showRangeFill==false)
		{
			rangeOptions=new LinkedList<String>();
			rangeOptions.add(rangeOpt1);
			rangeOptions.add(rangeOpt2);
			
			if(rangeOpt>=3)
				rangeOptions.add(rangeOpt3);
			
			if(rangeOpt>=4)
				rangeOptions.add(rangeOpt4);
			if(rangeOpt>=5)
				rangeOptions.add(rangeOpt5);
			if(rangeOpt>=6)
				rangeOptions.add(rangeOpt6);
			if(rangeOpt>=7)
				rangeOptions.add(rangeOpt7);
			
		}
		for(int x=0;x<rangeOptions.size();x++)
		{
			for(int i=x+1;i<rangeOptions.size();i++)
			{
				if(rangeOptions.get(x).toLowerCase().equals(rangeOptions.get(i).toLowerCase()))
				{
					showRangeRepeated=true;
				}
			}
		}
		
	}
	public Object onSuccessFromRangeForm()
	{
		if(request.isXHR())
		{
			if(showRangeBadChar || showRangeRepeated || showRangeFill || showRangeValues || showRangeMax)
			{
				ajaxResponseRenderer.addRender("rangeZone", rangeZone);
				return null;
			}
			range=new RangeVoting(rangeOptions);
			
			ballot=setBallotData();
			range.setId(UUID.generate());
			ballot.setIdBallotData(range.getId());
			range.setBallotId(ballot.getId());
			range.setMinValue(Integer.parseInt(minRange));
			
			range.setMaxValue(Integer.parseInt(maxRange));
			
			System.out.println("MINIMO->"+range.getMinValue());
			System.out.println("MAXIMO->"+range.getMaxValue());
			voteDao=DB4O.getVoteDao(datasession.getDBName());
			rangeDao=DB4O.getRangeVotingDao(datasession.getDBName());
			if(ballot.isTeaching())
			{
				ballot.setIdCensus("none");
				range.setVotes(GenerateDocentVotes.generateRangeVoting(range.getOptions(), Integer.parseInt(census),range.getMinValue(), range.getMaxValue()));
				range.calcularRangeVoting();
				ballot.setEnded(true);
				Vote vote=new Vote(ballot.getId(),datasession.getId(),true);//Almacena vote para docente(solo el creador)
				this.sendMail(datasession.getId(),ballot);
				ballot.setEnded(true);
				ballot.setCounted(true);
				voteDao.store(vote);
			}
			else if(ballotKind==BallotKind.PUBLICA){
				 ballot.setIdCensus("none");
			 }
			else//Votacion Normal
			{
				 boolean creatorInCensus=false;
				 this.sendMail(censusNormal, ballot);
				 for(String idUser:censusNormal.getUsersCounted())
				 {
					 	if(idUser.equals(datasession.getId())){creatorInCensus=true;}
					 
					 	voteDao.store(new Vote(ballot.getId(),idUser));//Almacena vote con ids de users censados
				 }
				 if(!creatorInCensus)
				 {
					 voteDao.store(new Vote(ballot.getId(),datasession.getId()));
					 this.sendMail(datasession.getId(),ballot);
				 }
				 
			 }
			 
			 rangeDao.store(range);
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
			if(from.equals("fromBorda"))
			{
				showType=true;
				showBorda=false;
				ajaxResponseRenderer.addRender("typeZone", typeZone).addRender("bordaZone",bordaZone);
			}
			if(from.equals("fromRange"))
			{
				showType=true;
				showRange=false;
				ajaxResponseRenderer.addRender("typeZone", typeZone).addRender("rangeZone",rangeZone);
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
		if(ballotKind==BallotKind.PRIVADA)
		{
			newBallot.setNotStarted(true);
			newBallot.setIdCensus(censusNormal.getId());
		}
		else if(ballotKind==BallotKind.DOCENTE)
		{
			newBallot.setTeaching(true);
		}
		else
		{
			newBallot.setPublica(true);
		}
		newBallot.setMethod(method);
		newBallot.setStartDate(cal1.getTime());
		newBallot.setEndDate(cal2.getTime());
		return newBallot;
	}
	
	/**
	 * Report the users that are able to vote in the created ballot
	 * @param censo
	 * @param ballotMail
	 */
	private void sendMail(Census censo,Ballot ballotMail)
	{
		userDao=DB4O.getUsuarioDao(datasession.getDBName());
		emailAccountDao=DB4O.getEmailAccountDao();
		List<Profile> usersToMail=userDao.getProfileById(censo.getUsersCounted());
		EmailAccount account=emailAccountDao.getAccount();
		
		String metodo=null;
		String subject;
		String txt;
		
		
		if(ballot.getMethod()==Method.MAYORIA_RELATIVA)
		{
			metodo="Mayoria Relativa";
		}
		else if(ballot.getMethod()==Method.KEMENY)
		{
			metodo="Kemeny";
		}
		
		
		if(ballotMail.isTeaching())
		{
			subject="Votacion docente "+metodo+": "+ballotMail.getName();
			txt="La votacion docente "+ballotMail.getName()+" ha sido realizada con el metodo "+metodo+"<br/><br/>Su descripcion es:<br/>"+ballotMail.getDescription();
		}
		else
		{
			subject="Ya puedes Votar en: "+ballotMail.getName();
			txt="Ya tiene acceso a la votacion ("+metodo+"): "+ballotMail.getName()+"<br/><br/>La descripcion de la votacion es:<br/>"+ballotMail.getDescription();
		}
		
		for(Profile emailDestino:usersToMail)
		{
			Mail.sendMail(account.getEmail(), account.getPassword(), emailDestino.getEmail(), subject, txt);
		}
	}
	/**
	 * Report a users that are able to vote in the created ballot
	 * @param censo
	 * @param ballotMail
	 */
	private void sendMail (String idUser,Ballot ballotMail)
	{
		userDao=DB4O.getUsuarioDao(datasession.getDBName());
		emailAccountDao=DB4O.getEmailAccountDao();
		String emailDestino=userDao.getEmailById(idUser);
		EmailAccount account=emailAccountDao.getAccount();
		
		SimpleDateFormat dateFormat;
		
		String metodo=null;
		String subject;
		String txt;
		
		if(ballot.getMethod()==Method.MAYORIA_RELATIVA)
		{
			metodo="Mayoria Relativa";
		}
		else if(ballot.getMethod()==Method.KEMENY)
		{
			metodo="Kemeny";
		}
		
		if(ballotMail.isTeaching())
		{
			subject="Votacion docente "+metodo+": "+ballotMail.getName();
			txt="La votacion docente "+ballotMail.getName()+" ha sido realizada con el metodo "+metodo+"<br/><br/>Su descripcion es:<br/>"+ballotMail.getDescription();
		}
		else
		{			
			dateFormat =new SimpleDateFormat("dd/MM/yyyy");
			String fecha = dateFormat.format(ballot.getStartDate());
			dateFormat=new SimpleDateFormat("HH:mm");
			String hora= dateFormat.format(ballot.getStartDate());
			
			subject="Nueva votación: "+ballotMail.getName();
			txt="Ha sido invitado a participación a la votación ("+metodo+"): "+ballotMail.getName()+ "que dara comienzo el "+fecha+" a las "+hora+
					"<br/><br/>La descripcion de la votacion es:<br/>"+ballotMail.getDescription();
					
		}

		
		Mail.sendMail(account.getEmail(), account.getPassword(), emailDestino, subject, txt);
	}
	
	private boolean isNumeric(String cadena)
	{
		try {
			Integer.parseInt(cadena);
			return true;
		} catch (NumberFormatException nfe){
			return false;
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
					if(datasession.isMaker())
					{
						return null;
					}
					return UnauthorizedAttempt.class;
				case 2:
					return null;
				case 3:
					return SessionExpired.class;
				default:
					return Index.class;
			}
			
		}
	
}
