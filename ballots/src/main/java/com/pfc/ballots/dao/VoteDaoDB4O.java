package com.pfc.ballots.dao;

import java.util.LinkedList;
import java.util.List;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.EmbeddedConfiguration;
import com.pfc.ballots.entities.Vote;
/**
 * 
 * Implementation of the interface VoteDao for the DB4O database
 * 
 * @author Mario Temprano Martin
 * @version 1.0 JUL-2014
 *
 */
public class VoteDaoDB4O implements VoteDao {
	
	String sep=System.getProperty("file.separator");
	String PATH;
	String ruta=System.getProperty("user.home")+sep+"BallotsFiles"+sep;
	EmbeddedConfiguration config = null;
	ObjectContainer DB=null;
	
	
	
	public VoteDaoDB4O(String DBName)
	{
		if(DBName==null)
		{
			PATH=ruta+"DB4Obbdd.dat";
		}
		else
		{
			PATH=ruta+DBName;
		}
	}
	//********************************************* store *********************************************//
	/**
	 * Stores a Vote
	 * @param vote to store
	 */
	public void store(Vote vote)
	{
		open();
		try
		{
			DB.store(vote);
			System.out.println("[DB4O]Vote stored");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("[DB4O]ERROR:Vote could not be stored");
		}
		finally
		{
			close();
		}
	}
	/**
	 * Stores a list of votes
	 * @param votes list to store
	 */
	public void store(List<Vote> votes)
	{
		open();
		try
		{
			for(Vote temp:votes)
			{
				DB.store(temp);
			}
			System.out.println("[DB4O]Votes stored");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("[DB4O]ERROR:Vote could not be stored");
		}
		finally
		{
			close();
		}
	}
	public void addVotes(List<String> idsUser,List<String> idsBallot)
	{
		open();
		try
		{
			for(String idBallot:idsBallot)
			{
				for(String idUser:idsUser)
				{
					Vote temp=new Vote();
					temp.setIdBallot(idBallot);
					temp.setIdUser(idUser);
					
					ObjectSet result=DB.queryByExample(temp);
					if(!result.hasNext())
					{
						temp.setCounted(false);
						DB.store(temp);
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
	}
	//******************************************** getters*********************************************//
	/**
	 * Retrieves the votes of a ballot from its id
	 * @param idBallot id of the ballot to retrieve their votes
	 * @return List<Vote>
	 */
	public List<Vote> getBallotVotes(String idBallot)
	{
		open();
		List<Vote> list=new LinkedList<Vote>();
		try
		{
			Vote temp=new Vote();
			temp.setIdBallot(idBallot);
			ObjectSet result=DB.queryByExample(temp);
			while(result.hasNext())
			{
				list.add((Vote)result.next());
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
	 * Retrieves a Vote from its idBallot and its idUser
	 * @param idBallot id of the ballot 
	 * @param iduser id of the user
	 * @return Vote 
	 */
	public Vote getVoteByIds(String idBallot,String idUser)
	{
		open();
		try
		{
			ObjectSet result=DB.queryByExample(new Vote(idBallot,idUser));
			if(result.hasNext())
			{
				return (Vote)result.next();
			}
			return null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			close();
		}
	}
	/**
	 * Retrieves the votes of an user from its id
	 * @param idUser id of the user to retrieve his votes
	 * @return List<Vote>
	 */
	public List<Vote> getVotesUser(String idUser)
	{
		open();
		List<Vote> list=new LinkedList<Vote>();
		try
		{
			Vote temp=new Vote();
			temp.setIdUser(idUser);
			ObjectSet result=DB.queryByExample(temp);
			while(result.hasNext())
			{
				list.add((Vote)result.next());
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
	 * Retrieves a list of the ids of the ballots that the user can vote
	 * @param idUser id of the user
	 * @return List<String>
	 */
	public List<String> getBallotsWithParticipation(String idUser)
	{
		open();
		List<String> list=new LinkedList<String>();
		try
		{
			Vote temp=new Vote();
			temp.setIdUser(idUser);
			ObjectSet result=DB.queryByExample(temp);
			while(result.hasNext())
			{
				list.add(((Vote)result.next()).getIdBallot());
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
	 * Checks if a user already votes in a ballot
	 * @param idBallot id of the ballot to check
	 * @param idUser id of the user to check
	 * @return boolean
	 */
	public boolean isVoted(String idBallot,String idUser)
	{
		open();
		try
		{
			ObjectSet result=DB.queryByExample(new Vote(idBallot,idUser));
			if(result.hasNext())
			{
				return ((Vote)result.next()).isCounted();
			}
			return false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			close();
		}
	}
	//************************************************* Update ****************************************//
	/**
	 * Updates a vote
	 * @param vote vote to update
	 */
		public void updateVote(Vote vote)
		{
			open();
			try
			{
				ObjectSet result=DB.queryByExample(new Vote(vote.getIdBallot(),vote.getIdUser()));
				if(result.hasNext())
				{
					DB.delete(result.next());
					DB.store(vote);
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
	//************************************************* Delete ****************************************//
	/**
	 * Deletes the votes of a ballot from its id
	 * @param idBallot id of the ballot
	 */
		public void deleteVoteOfBallot(String idBallot)
	{
		open();
		try
		{
			Vote temp=new Vote();
			temp.setIdBallot(idBallot);
			ObjectSet result=DB.queryByExample(temp);
			while(result.hasNext())
			{
				DB.delete(result.next());
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
	 * Deletes a the votes of a list of users of a list of ballots
	 * @Param List<string> idUser list of user to delete their votes
	 * @Param List<String idBalot list of ballots where is going to delete the votes
	 */
	public void deleteVotesNotDone(List<String> idsUser,List<String> idsBallot)
	{
		open();
		try
		{
			for(String idBallot:idsBallot)
			{
				for(String idUser:idsUser)
				{
					Vote temp=new Vote();
					temp.setIdBallot(idBallot);
					temp.setIdUser(idUser);
					ObjectSet result=DB.queryByExample(temp);
					if(result.hasNext())
					{
						temp=(Vote)result.next();
						if(temp.isCounted()==false)
						{
							DB.delete(temp);
						}
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
	}
	//********************************************Open and Close DB************************************//
	/**
	 * Opens database
	 */
	private void open()
	{
		config=Db4oEmbedded.newConfiguration();
		config.common().objectClass(Vote.class).cascadeOnUpdate(true);
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
	 * Closes database
	 */
	private void close()
	{
		DB.close();
		System.out.println("[DB4O]Database was closed");
	}


}
