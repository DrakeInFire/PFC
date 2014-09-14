package com.pfc.ballots.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.query.Constraint;
import com.db4o.query.Query;
import com.pfc.ballots.entities.Ballot;
import com.pfc.ballots.entities.Profile;
/**
 * Implementation of the interface BallotDao for the DB4O database
 * 
 * @author Mario Temprano Martin
 * @version 2.0 JUL-2014
 *
 */


public class BallotDaoDB4O implements BallotDao{
	String sep=System.getProperty("file.separator");
	String PATH;
	String ruta=System.getProperty("user.home")+sep+"BallotsFiles"+sep;
	EmbeddedConfiguration config = null;
	ObjectContainer DB=null;
	
	Calendar calAct;
	Calendar calEnd;
	Calendar calStart;
	
	
	
	public BallotDaoDB4O(String DBName)
	{
		calAct=new GregorianCalendar();
		calEnd=new GregorianCalendar();
		calStart=new GregorianCalendar();
		if(DBName==null)
		{
			PATH=ruta+"DB4Obbdd.dat";
		}
		else
		{
			PATH=ruta+DBName;
		}
	}
	
	
	//*************************************************Store******************************************************
	/**
	 * Stores a ballot 
	 * @param ballot that will be stored
	 */
	public void store(Ballot ballot)
	{
		open();
		try
		{
			if(!testBallot(ballot.getName()))
			{
				DB.store(ballot);
				System.out.println("[DB4O]Ballot was Stored");
			}
			else
			{
				System.out.println("[DB4O]Warning:Email was already in use");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("[DB4O]ERROR:Profile could not be stored");
		}
		finally
		{
			close();
		}
		
	}
	//***************************************** retrievers ********************************************//

	/**
	 * Retrieves a ballot's information from its id
     * 
     * @param Id Id of the ballot that will be retrieved
     * @return Ballot Ballot that will be retrieved
	 */
	public Ballot getById(String id) {
		
		open();
		try
		{
			Ballot temp=new Ballot();
			temp.setId(id);
			ObjectSet result=DB.queryByExample(temp);
			Ballot x;
			if(result.hasNext())
			{
				x=(Ballot)result.next();
				updateStateBallot(x);
				return x;
			}
			return null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			close();
		}
		return null;
	}

	public List<String> getIdByCensusId(String idCensus)
	{
		List<String> list=new LinkedList<String>();
		open();
		try
		{
			Query query =DB.query();
			query.constrain(Ballot.class);
			if(idCensus!=null)
			{
				query.descend("idCensus").constrain(idCensus).endsWith(false);
				ObjectSet result=query.execute();
				while(result.hasNext())
				{
					Ballot x=(Ballot) result.next();
					list.add(x.getId());
				}
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			close();
		}
		return list;
	}
	
	/**
	 * Retrieves all ballots from their ids
	 * 
	 * @param List<String> ids list of ids of the ballots that will be retrieved
	 * @return List<Ballot> list of retrieved ballots
	 */
	public List<Ballot> getById(List<String> ids) {
		open();
		List<Ballot> list=new LinkedList<Ballot>();
		try
		{
			Ballot temp=new Ballot();
			Ballot x;
			for(int i=ids.size()-1;i>0;i--)
			{
				temp.setId(ids.get(i));
				ObjectSet result=DB.queryByExample(temp);
				if(result.hasNext())
				{
					x=(Ballot)result.next();
					updateStateBallot(x);
					list.add(x);
				}
			}
			return list;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			list.clear();
			return list;
		}
		finally
		{
			close();
		}
	}
	
	
	public List<Ballot> getByExample(Ballot example)
	{
		open();
		List<Ballot> list=new LinkedList<Ballot>();
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		try
		{

			Query query =DB.query();
			query.constrain(Ballot.class);
			if(example.getName()!=null)
				{query.descend("name").constrain(example.getName()).endsWith(false);}
			if(example.getId()!=null)
				{query.descend("id").constrain(example.getId()).endsWith(false);}
			if(example.getIdOwner()!=null)
			{query.descend("idOwner").constrain(example.getIdOwner()).endsWith(false);}
			if(example.getIdCensus()!=null)
			{query.descend("idCensus").constrain(example.getIdCensus()).endsWith(false);}
			if(example.getMethod()!=null)
			{query.descend("method").constrain(example.getMethod());}
			if(example.isEnded())
			{query.descend("ended").constrain(example.isEnded());}
			if(example.isActive())
			{query.descend("active").constrain(example.isActive());}
			if(example.isNotStarted())
			{query.descend("notStarted").constrain(example.isActive());}
			if(example.isPrivat())
			{query.descend("privat").constrain(example.isPrivat());}
			if(example.isPublica())
			{query.descend("publica").constrain(example.isPublica());}
			if(example.isTeaching())
			{query.descend("teaching").constrain(example.isTeaching());}
			ObjectSet result=query.execute();
			while(result.hasNext())
			{
				Ballot x=(Ballot) result.next();
				boolean dateOk=true;
				if(example.getEndDate()!=null)
				{
					if(!format.format(example.getEndDate()).equals(format.format(x.getEndDate())))
					{
						dateOk=false;
					}
				}
				if(example.getStartDate()!=null)
				{
					if(!format.format(example.getStartDate()).equals(format.format(x.getStartDate())))
					{
						dateOk=false;
					}
				}
				if(dateOk)
				{
					list.add(x);
				}
				
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			close();
		}
		return list;
	}
	public List<Ballot> getByExample(List<String> ids,Ballot example)
	{
		
		open();
		List<Ballot> list=new LinkedList<Ballot>();
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		try
		{

			Query query =DB.query();
			query.constrain(Ballot.class);
			if(example.getName()!=null)
				{query.descend("name").constrain(example.getName()).endsWith(false);}
			if(example.getId()!=null)
				{query.descend("id").constrain(example.getId()).endsWith(false);}
			if(example.getIdOwner()!=null)
			{query.descend("idOwner").constrain(example.getIdOwner()).endsWith(false);}
			if(example.getIdCensus()!=null)
			{query.descend("idOwner").constrain(example.getIdCensus()).endsWith(false);}
			if(example.isEnded())
			{query.descend("ended").constrain(example.isEnded());}
			if(example.isActive())
			{query.descend("active").constrain(example.isActive());}
			if(example.isNotStarted())
			{query.descend("notStarted").constrain(example.isActive());}
			if(example.isPrivat())
			{query.descend("privat").constrain(example.isPrivat());}
			if(example.isPublica())
			{query.descend("publica").constrain(example.isPublica());}
			if(example.isTeaching())
			{query.descend("teaching").constrain(example.isTeaching());}
			if(example.getMethod()!=null)
			{
				query.descend("method").constrain(example.getMethod());
			}
			ObjectSet result=query.execute();
			while(result.hasNext())
			{
				Ballot x=(Ballot) result.next();
				boolean idOk=false;
				for(String id:ids)
				{
					if(example.getId().equals(x.getId()))
					{
						idOk=true;
					}
				}
				if(idOk)
				{
					boolean dateOk=true;
					if(example.getEndDate()!=null)
					{
						if(!format.format(example.getEndDate()).equals(format.format(x.getEndDate())))
						{
							dateOk=false;
						}
					}
					if(example.getStartDate()!=null)
					{
						if(!format.format(example.getStartDate()).equals(format.format(x.getStartDate())))
						{
							dateOk=false;
						}
					}
					if(dateOk)
					{
						list.add(x);
					}
				}
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			close();
		}
		return list;
	}
	
	
	
	/**
	 * Retrieves all ballots from their ids
	 * 
	 * @param List<String> ids list of ids of the ballots that will be retrieved
	 * @param List<Ballot> nonActive , list of non active ballots
	 * @param List<Ballot> active , list of active ballots
	 * @param List<Ballot> ended , list of ended ballots
	 * @return List<Ballot> a complete list
	 */
	public List<Ballot> getById(List<String> ids,List<Ballot> nonActive,List<Ballot> active,List<Ballot> ended) {
		open();
		List<Ballot> list=new LinkedList<Ballot>();
		try
		{
			Ballot temp=new Ballot();
			Ballot x;
			for(int i=ids.size()-1;i>=0;i--)
			{
				temp.setId(ids.get(i));
				ObjectSet result=DB.queryByExample(temp);
				if(result.hasNext())
				{
					x=(Ballot)result.next();
					
					switch(updateStateBallot(x))
					{
						case 0:
							nonActive.add(x);
							break;
						case 1:
							active.add(x);
							break;
						case 2:
							ended.add(x);
							break;
						default:
							break;
							
					}
					list.add(x);
				}
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			list.clear();
			return list;
		}
		finally
		{
			close();
		}
		return list;
	}
	
	public List<Ballot> getById(List<String> ids,List<Ballot> nonActive,List<Ballot> active,List<Ballot> ended,int num)
	{
		open();
		List<Ballot> list=new LinkedList<Ballot>();
		try
		{
			Ballot temp=new Ballot();
			Ballot x;
			int n=0;
			int a=0;
			int e=0;
			System.out.println("ids SIZE"+ids.size());
			for(int i=ids.size()-1;i>=0;i--)
			{
				System.out.println("I="+i);
				temp.setId(ids.get(i));
				ObjectSet result=DB.queryByExample(temp);
				if(result.hasNext())
				{
					x=(Ballot)result.next();
					
					switch(updateStateBallot(x))
					{
						case 0:
							if(n<num)
							{
								n++;
								nonActive.add(x);
							}
							break;
						case 1:
							if(a<num)
							{
								a++;
								active.add(x);
							}
								
							break;
						case 2:
							if(e<num)
							{
								e++;
								ended.add(x);
							}
							break;
						default:
							break;
							
					}
					list.add(x);
				}
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			list.clear();
			return list;
		}
		finally
		{
			close();
		}
		return list;
	
	}
	 public List<Ballot> getPublics(){
			open();
			List<Ballot> list=new LinkedList<Ballot>();
			try
			{
				Query query=DB.query();
				query.constrain(Ballot.class);
				query.descend("publica").constrain(true);
				query.descend("endDate").orderDescending();
				ObjectSet result=query.execute();
				while(result.hasNext())
				{
					Ballot x=(Ballot)result.next();
					updateStateBallot(x);
					list.add(x);
					
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				list.clear();
				return list;
			}
			finally
			{
				close();
			}
			return list;
		}
	
	 public List<Ballot> getPublics(List<Ballot> nonActive,List<Ballot> active,List<Ballot> ended){
		open();
		List<Ballot> list=new LinkedList<Ballot>();
		try
		{
			Query query=DB.query();
			query.constrain(Ballot.class);
			query.descend("publica").constrain(true);
			query.descend("endDate").orderDescending();
			ObjectSet result=query.execute();
			while(result.hasNext())
			{
				Ballot x=(Ballot)result.next();
				list.add(x);
				switch(updateStateBallot(x))
				{
					case 0:
						nonActive.add(x);
						break;
					case 1:
						active.add(x);
						break;
					case 2:
						ended.add(x);
						break;
					default:
						break;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			list.clear();
			return list;
		}
		finally
		{
			close();
		}
		return list;
	}
	 public List<Ballot> getPublics(List<Ballot> nonActive,List<Ballot> active,List<Ballot> ended,int num){
			open();
			List<Ballot> list=new LinkedList<Ballot>();
			try
			{
				Ballot temp=new Ballot();
				temp.setPublica(true);
				int n=0;
				int a=0;
				int e=0;
				Query query=DB.query();
				query.constrain(Ballot.class);
				query.descend("publica").constrain(true);
				query.descend("endDate").orderDescending();
				ObjectSet result=query.execute();
				while(result.hasNext())
				{
					Ballot x=(Ballot)result.next();
					list.add(x);
					switch(updateStateBallot(x))
					{
					case 0:
						if(n<num)
						{
							n++;
							nonActive.add(x);
						}
						break;
					case 1:
						if(a<num)
						{
							a++;
							active.add(x);
						}
							
						break;
					case 2:
						if(e<num)
						{
							e++;
							ended.add(x);
						}
						break;
						default:
							break;
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				list.clear();
				return list;
			}
			finally
			{
				close();
			}
			return list;
		}
	/**
	 * Retrieves all ballots 
	 * @return List<Ballot> list of ballots returned
	 */
	
	public List<Ballot> retrieveAll() {
		List<Ballot> ballots=new LinkedList<Ballot>();
		open();
		try
		{
				Query query=DB.query();
				query.constrain(Ballot.class);
				ObjectSet result = query.execute();
				Ballot x;
				while(result.hasNext())
				{
					x=(Ballot)result.next();
					updateStateBallot(x);
					ballots.add(x);
				}
				System.out.println("[DB4O]All ballots was retrieved");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("[DB4O]ERROR:All ballots could not be retrieved");
			ballots.clear();
			return ballots;
		}
		finally
		{
			close();
		}
		
		return ballots;
	}

	/**
	 * Retrieves all ballots 
	 * @return List<Ballot> list of ballots returned
	 */
	
	public List<Ballot> retrieveAllSort() {
		List<Ballot> ballots=new LinkedList<Ballot>();
		open();
		try
		{
				Query query=DB.query();
				query.constrain(Ballot.class);
				query.descend("endDate").orderDescending();
				ObjectSet result = query.execute();
				Ballot x;
				while(result.hasNext())
				{
					x=(Ballot)result.next();
					updateStateBallot(x);
					ballots.add(x);
				}
				System.out.println("[DB4O]All ballots was retrieved");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("[DB4O]ERROR:All ballots could not be retrieved");
			ballots.clear();
			return ballots;
		}
		finally
		{
			close();
		}
		
		return ballots;
	}
	

	/**
	 * Retrieves the information of not counted ballots from ballots' ids
	 * @param List<String> ids of ballots to retrieve
	 * @return List<Ballot> list of ballots that aren't counted yet
	 */
	public List<Ballot> getEndedNotCountedById(List<String> ids)
	{
		open();
		List<Ballot> list=new LinkedList<Ballot>();
		try
		{
			Ballot temp=new Ballot();
			temp.setEnded(true);
			Ballot x;
			for(int i=0;i<ids.size();i++)
			{
				temp.setId(ids.get(i));
				ObjectSet result=DB.queryByExample(temp);
				if(result.hasNext())
				{
					x=(Ballot)result.next();
					updateStateBallot(x);
					if(!x.isCounted())
						{list.add(x);}
				}
			}
			return list;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			list.clear();
			return list;
		}
		finally
		{
			close();
		}
	}
	/**
	 * Retrieves all ballots of a user by his id
	 * @param idOwner id of the user that are going to retieve his ballots
	 * @return List<Ballot> list of ballots of the user
	 */
	public List<Ballot> getByOwnerId(String idOwner)
	{
		open();
		List<Ballot> list=new LinkedList<Ballot>();
		try
		{
			Ballot temp=new Ballot();
			temp.setIdOwner(idOwner);
			ObjectSet result=DB.queryByExample(temp);
			Ballot x;
			while(result.hasNext())
			{
				x=(Ballot)result.next();
				updateStateBallot(x);
				list.add(x);
			}
			return list;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			list.clear();
			return list;
		}
		finally
		{
			close();
		}
		
	}
	public List<Ballot> getByOwnerIdSorted(String idOwner)
	{
		open();
		List<Ballot> list=new LinkedList<Ballot>();
		try
		{
			
			Query query=DB.query();
			query.constrain(Ballot.class);
			query.descend("endDate").orderDescending();
			query.descend("idOwner").constrain(idOwner);
			ObjectSet result = query.execute();
			Ballot x;
			while(result.hasNext())
			{
				x=(Ballot)result.next();
				updateStateBallot(x);
				list.add(x);
			}
			
			return list;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			list.clear();
			return list;
		}
		finally
		{
			close();
		}
		
	}
	
	/**
	 *  Retrieves a list of ballots from their idCensus
	 *  @param idCensus 
	 *  @Return List<Ballot> list of ballots with idcensus 
	 */
	public List<Ballot> getByIdCensus(String idCensus) {
		open();
		List<Ballot> list=new LinkedList<Ballot>();
		try
		{
			Ballot temp=new Ballot();
			temp.setIdCensus(idCensus);
			ObjectSet result=DB.queryByExample(temp);
			Ballot x;
			while(result.hasNext())
			{
				x=(Ballot)result.next();
				updateStateBallot(x);
				list.add(x);
			}
			return list;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			list.clear();
			return list;
		}
		finally
		{
			close();
		}
		
	}
	//************************************************* Delete ****************************************************//
	
	/**
	 * Deletes a ballot from its id
	 * @param id id of the ballot to delete
	 * 
	 */
	public void deleteBallotById(String id) {
		open();
		try
		{
			Ballot temp=new Ballot();
			temp.setId(id);
			ObjectSet result=DB.queryByExample(temp);
			if(result.hasNext())
			{
				DB.delete((Ballot)result.next());
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			close();
		}
		
	}
	/**
	 * Updates a ballot
	 * @param updateBallot ballot to update
	 */
	public void updateBallot(Ballot updatedBallot)
	{
		open();
		try
		{
			Ballot temp=new Ballot();
			temp.setId(updatedBallot.getId());
			ObjectSet result=DB.queryByExample(temp);
			if(result.hasNext())
			{
				DB.delete((Ballot)result.next());
				DB.store(updatedBallot);
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			close();
		}

	}
	
	
	
	
	
	
	//******************************************** tools **********************************************//
	
	
	/**
	 * Retrieve if a ballot name is in use
	 * @param name name to consult
	 * @return boolean
	 */
	public boolean isNameInUse(String name)
	{
		open();
		try
		{
			return testBallot(name);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			close();
		}
		return true;
	}
	
	/**
	 * Retrieve if a ballot is ended
	 * @param idBallot id from the ballot to consult
	 * @return boolean
	 */
	public boolean isEnded(String idBallot)
	{
		Ballot ballot=getById(idBallot);
		if(ballot==null)
		{
			return false;
		}
		else
		{
			if(ballot.isEnded())
			{
				return true;
			}
			return false;
		}
	}
	/**
	 * Check the state of a ballot and modify it if is needed
	 * @param ballot
	 * @return 0->not Started 1->active 2->ended
	 */
	private int updateStateBallot(Ballot ballot)
	{
		calStart.setTime(ballot.getStartDate());
		calEnd.setTime(ballot.getEndDate());
		
		if(ballot.isEnded())
		{
			return 2;
		}
		if(calStart.after(calAct))//NO HA EMPEZADO
		{
			return 0;
		}
		if(calEnd.before(calAct))//HA TERMINADO
		{
			if(!ballot.isEnded())
			{
				DB.delete(ballot);
				ballot.setEnded(true);
				ballot.setActive(false);
				ballot.setNotStarted(false);
				DB.store(ballot);
			}
			return 2;
		}
		else
		{
			if(!ballot.isActive())
			{
				DB.delete(ballot);
				ballot.setEnded(false);
				ballot.setActive(true);
				ballot.setNotStarted(false);
				DB.store(ballot);
			}
			return 1;
		}
	}
	/**
	 * Checks if a ballot is ended and update its data
	 * @param ballot to check
	 * @return boolean 
	 */
	private boolean updateEndBallot(Ballot ballot)//Marca como terminada la votacion y la actualiza
	{
		if(!ballot.isEnded())
		{
			calEnd.setTime(ballot.getEndDate());
			if(calEnd.before(calAct))
			{
				DB.delete(ballot);
				ballot.setEnded(true);
				DB.store(ballot);
				return true;
			}
		}
		return false;
	}
	
	
	
	/**
	 * Checks if a ballot name is in use
	 * @param ballotName to check
	 * @return boolean
	 */
	private boolean testBallot(String ballotName)
	{
		Ballot temp=new Ballot();
		temp.setName(ballotName);
		
		Query query =DB.query();
		query.descend("name").constrain(ballotName).endsWith(false);
		ObjectSet result=query.execute();
		if(result.hasNext())
		{
			return true;
		}
		return false;
	}
	
	//********************************************Open and Close DB************************************//
			/**
			 * Opens the database
			 */
			private void open()
			{
				config=Db4oEmbedded.newConfiguration();
				config.common().objectClass(Ballot.class).cascadeOnUpdate(true);
				try
				{
					
					DB=Db4oEmbedded.openFile(config, PATH);
					System.out.println("[DB4O]Database was open");
					
				}
				catch(Exception e)
				{
					System.out.println("[DB4O]ERROR:Database could not be open");
					e.printStackTrace();
				}
			}
			/**
			 * Closes the database
			 */
			private void close()
			{
				DB.close();
				System.out.println("[DB4O]Database was closed");
			}


	




}
