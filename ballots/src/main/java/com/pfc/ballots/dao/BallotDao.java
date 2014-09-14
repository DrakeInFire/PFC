package com.pfc.ballots.dao;
/**
 * Dao Interface to retrieves ballot entity
 * 
 * @author Mario Temprano Martin
 * @Version 2.0 JUL-2014
 * 
 */
import java.util.List;

import com.pfc.ballots.entities.Ballot;

public interface BallotDao {

	public void store(Ballot ballot);
	
	public List<Ballot> retrieveAll();
	public List<Ballot> retrieveAllSort();
	public Ballot getById(String id);

	public List<String> getIdByCensusId(String idCensus);
	public List<Ballot> getById(List<String> ids);
	public List<Ballot> getById(List<String> ids,List<Ballot> nonActive,List<Ballot> active,List<Ballot> ended);
	public List<Ballot> getById(List<String> ids,List<Ballot> nonActive,List<Ballot> active,List<Ballot> ended,int num);
	public List<Ballot> getPublics();
	public List<Ballot> getPublics(List<Ballot> nonActive,List<Ballot> active,List<Ballot> ended);
    public List<Ballot> getPublics(List<Ballot> nonActive,List<Ballot> active,List<Ballot> ended,int num);
	public List<Ballot> getByOwnerId(String idOwner);
	public List<Ballot> getByOwnerIdSorted(String idOwner);
	
	//Cuando se pasen las votaciones que tiene un usuario devolvera las que not has sido contavilizadas
	public List<Ballot> getEndedNotCountedById(List<String> ids);
	public boolean isNameInUse(String name);
	public boolean isEnded(String idBallot);
	
	public void deleteBallotById(String id);
	public void updateBallot(Ballot UpdatedBallot);
	
	public List<Ballot> getByExample(Ballot example);
	public List<Ballot> getByExample(List<String> ids,Ballot example);
	
	public List<Ballot> getByIdCensus(String idCensus);
	/*
	public List<Ballot> getNotStartedBallots();
	public List<Ballot> getActiveBallots();
	public List<Ballot> getEndedBallots();
	*/
}
