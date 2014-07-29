package com.pfc.ballots.entities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Census {

	private String id;
	private String idOwner;
	private String email;
	private String censusName;
	private List<String> usersCounted;

	public Census()
	{
		
	}
	/**
	 * Hace una copia del censo (usersCounted copiada elemento a elemento, no la referencia)
	 * @param old
	 */
	public Census(Census old)
	{
		setId(old.getId());
		setIdOwner(old.getIdOwner());
		setEmail(old.getEmail());
		setCensusName(old.getCensusName());
		for(String current:old.getUsersCounted())
		{
			this.addIdToUsersCounted(current);
		}
	}
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getIdOwner() {
		return idOwner;
	}
	public void setIdOwner(String idOwner) {
		this.idOwner = idOwner;
	}
	public String getCensusName() {
		return censusName;
	}
	public void setCensusName(String censusName) {
		this.censusName = censusName;
	}
	public List<String> getUsersCounted() {
		return usersCounted;
	}
	public void setUsersCounted(List<String> usersCounted) {
		this.usersCounted = usersCounted;
	}
	
	/**
	 * Añade un id de usuario a la lista de usuarios censados
	 * @param id
	 */
	public void addIdToUsersCounted(String id)
	{
		if(usersCounted==null)
		{
			usersCounted=new LinkedList<String>();
		}
		usersCounted.add(id);
	}
	/**
	 * Vacia la lista de usuarios censados
	 */
	public void emptyUsersCounted()
	{
		usersCounted.clear();
	}
	
	/**
	 * Elimina el id del usuario (en caso de que este) en la lista de usuarios censados
	 * @param id
	 */
	public void removeIdOfUsersCounted(String id)
	{
		if(usersCounted!=null)
		{
			for(String temp:usersCounted)
			{
				if(temp.equals(id))
				{
					usersCounted.remove(temp);
				}
			}
		}
	}
	
	
	public int getNumberOfCensed()
	{
		return usersCounted.size();
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 *	Compare this census with the @Param census and
	 *	returns the users @Param added and @Param removed of the census
	 */
	public void calcDifference(Census updated,List<String>added,List<String>removed)
	{
		Map<String,String> map=new HashMap<String,String>();
		
		if(updated.getUsersCounted()!=null)
		{
			for(String current:this.getUsersCounted())
			{
				map.put(current, "Removed");
			}
			for(String current:updated.getUsersCounted())
			{
				if(map.get(current)!=null)
				{
					map.put(current,"Current");
				}
				else
				{
					added.add(current);//add the new user
				}
			}
			for(String current:this.getUsersCounted())
			{
				if(map.get(current).equals("Removed"))
				{
					removed.add(current);
				}
			}
			
		}
	}

	
}
