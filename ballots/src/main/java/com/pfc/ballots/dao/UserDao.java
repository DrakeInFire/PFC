package com.pfc.ballots.dao;

import java.util.List;

import com.pfc.ballots.entities.Profile;

public interface UserDao {

	public void store(Profile profile);
	public void store(List<Profile> profiles);
	public List<Profile> RetrieveAllProfiles();
	public List<Profile> RetrieveAllProfilesSortLastLog();
	public List<Profile> getByExample(Profile example);
	public Profile getProfileByEmail(String Email);
	public Profile getProfileById(String id);
	public List<Profile> getProfileById(List<String> id);
	public List<Profile> getNoMailProfiles();
	public String getEmailById(String Id);
	public String getIdByEmail(String email);
	public void setOwner(String idNewOwner);
	public Profile getOwner();
	
	
	public void UpdateByEmail(String Email,Profile updatedProfile);
	public void UpdateByEmail(Profile updatedProfile);
	public void UpdateById(Profile updatedProfile);
	public void updateList(List<Profile> updated);
	public void deleteByEmail(String Email);
	public void deleteById(String id);
	public boolean isNoMailRegistred(String Email);
	public boolean isProfileRegistred(String Email);
	public boolean isIdRegistred(String id);

	
	
	public void addCensusToProfiles(List<String> idProfiles,String idCensus);
	public void removeCensusToProfiles(List<String>idProfiles,String idCensus);
	
}
