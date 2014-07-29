package com.pfc.ballots.dao;

import java.util.List;

import com.pfc.ballots.entities.Census;

public interface CensusDao {

	
	public void store(Census census);
	public List<Census> retrieveAll();
	public Census getById(String id);
	public List<Census> getByOwnerId(String idOwner);
	public List<Census> getCensusById(List<String> ids);
	public void update(Census census);
	public void deleteById(String id);
	public void deleteAllCensusOfOwner(String idOwner);
	
	public boolean isNameInUse(String name,String idOwner);
	
	public boolean deleteProfileOfCensus(List<String>idCensus,String idProfile);

}
