package com.pfc.ballots.dao;

import java.util.List;

import com.pfc.ballots.entities.UserLoged;

public interface UserLogedDao {
	
	public void store(UserLoged userLoged);
	public UserLoged getUserLoged(String idSession);
	public void updateUserLoged(UserLoged userLoged);
	public List<UserLoged> retrieveAll();
	public void clearSessions(long timeOfSession);
	
	public void delete(String idSession);
	public void deleteByEmail(String email);
	public boolean isLogedIn(String idSession);

}
