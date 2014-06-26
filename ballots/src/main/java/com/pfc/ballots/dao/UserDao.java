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
	public String getEmailById(String Id);
	public String getIdByEmail(String email);
	public void setOwner(String idNewOwner);
	public Profile getOwner();
	
	public void UpdateByEmail(String Email,Profile updatedProfile);
	public void UpdateByEmail(Profile updatedProfile);
	public void UpdateById(Profile updatedProfile);
	public void deleteByEmail(String Email);
	public void deleteById(String id);
	public boolean isProfileRegistred(String Email);
}
