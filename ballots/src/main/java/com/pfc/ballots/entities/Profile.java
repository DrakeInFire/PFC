package com.pfc.ballots.entities;




import java.util.Date;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.beaneditor.Validate;

import com.pfc.ballots.data.Sex;


/**
 * Profile Entity that contains the data associated to an user
 * 
 * @version 1.0, JUN-2013
 * @author Irene Grande Hernández
 * @version 2.0 FEB-2014
 * @author Mario Temprano Martin
 */

public class Profile {

	
	public Profile()
	{
		
	}
	public Profile(String email)
	{
		this.email=email;
	}
	public Profile(Profile old)
	{
		this.admin=old.isAdmin();
		this.maker=old.isMaker();
		this.owner=old.isOwner();
		this.id=old.getId();
		this.email=old.email;
		this.password=old.getPassword();
		this.sex=old.getSex();
		this.firstName=old.getFirstName();
		this.lastName=old.getLastName();
		this.university=old.getUniversity();
		this.city=old.getCity();
		this.country=old.getCountry();
		this.regDate=old.getRegDate();
	}
	
	/*********************************************** Profile fields **************************************************************/

	
	//It may be interesting to change the name of this field to any random word, for security reasons. I leave it like this
	//by now so the next developer don't go crazy and try to kill me. Whenever you need to create a new administrator from scratch,
	//just comment the line "NonVisual". But don't forget to change it back!
	
	private boolean admin;
	
	private boolean maker;
	
	@NonVisual
	private String id;
	//@NonVIsual
	private boolean owner;
	//this will also be the username
	@Validate("required,regexp=^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
	private String email;
	//Encrypted password of the user
	@Validate("required")
	private String password;
	
	
	private String plainPass;

	//Other stuff of the user
	private Sex sex;
	@Validate("required")
	private String firstName;
	@Validate("required")
	private String lastName;
	private String university;
	private String city;
	private String country;
	
	private Date regDate;
	
	private Date lastLog;
	
	public String getId()
	{
		return id;
	}
	public void setId(String id)
	{
		this.id=id;
	}
	public boolean isAdmin(){
		
		return admin;
	}
	public void setAdmin(boolean admin){
		
		this.admin = admin;
	}
	public boolean isMaker() {
		return maker;
	}
	public void setMaker(boolean maker) {
		this.maker = maker;
	}
	public String getEmail(){
			
		return email;
	}
	public void setEmail(String email){
			
		this.email = email.toLowerCase();
	}
	
	public String getPassword(){
		
		return password;
	}
	public void setPassword(String password){
		
		this.password = password;
	}
		

	public Sex getSex(){
		
		return sex;
	}
	public void setSex(Sex sex){
		
		this.sex = sex;
	}

	
	public String getFirstName(){
		
		return firstName;
	}
	public void setFirstName(String firstName){
		
		this.firstName = firstName;
	}


	public String getLastName(){
		
		return lastName;
	}
	public void setLastName(String lastName){
		
		this.lastName = lastName;
	}

	
	
	public String getUniversity(){
		
		return university;
	}
	public void setUniversity(String university){
		
		this.university = university;
	}

	
	public String getCity(){
		
		return city;
	}
	public void setCity(String city){
		
		this.city = city;
	}
	

	public String getCountry(){
		
		return country;
	}
	public void setCountry(String country){
		
		this.country = country;
	}
	public Date getRegDate()
	{
		return regDate;
	}
	public void setRegDatetoActual()
	{
		//SimpleDateFormat format= new SimpleDateFormat("dd/MM/yyyy");
		//RegDate=format.format(new Date());
		
		regDate=new Date();
		System.out.println(regDate);
	}
	public Date getLastLog() {
		return lastLog;
	}
	public void setLogtoactual() {
		lastLog =new Date();
	}
	public String getPlainPass() {
		return plainPass;
	}
	public void setPlain(String plainPass) {
		this.plainPass = plainPass;
	}
	public void copy(Profile old)
	{
		this.admin=old.isAdmin();
		this.maker=old.isMaker();
		this.id=old.getId();
		this.email=old.email;
		this.password=old.getPassword();
		this.sex=old.getSex();
		this.firstName=old.getFirstName();
		this.lastName=old.getLastName();
		this.university=old.getUniversity();
		this.city=old.getCity();
		this.country=old.getCountry();
		this.regDate=old.getRegDate();
	}
	public boolean isOwner() {
		return owner;
	}
	public void setOwner(boolean owner) {
		this.owner = owner;
	}
	/**
	 * Requiered fields must be filled(no compare password) 
	 */
	
	public boolean equals(Profile profile)
	{
		if (profile==null)
		{
			return false;
		}
		if(!this.getId().equals(profile.getId()))
		{
			return false;
		}
		
		if(this.isAdmin()!=profile.isAdmin())
		{
			return false;
		}
		
		if(this.isMaker()!=profile.isMaker())
		{
			return false;
		}
		
		if(!this.getEmail().equals(profile.getEmail()))
		{
			return false;
		}
		
		if(!this.getFirstName().equals(profile.getFirstName()))
		{
			return false;
		}
		if(!this.getLastName().equals(profile.getLastName()))
		{
			return false;
		}
		
		//University Check
		if(this.getUniversity()==null && profile.getUniversity()==null)
		{
			//Must be empty, its a check to avoid not null exception
		}			
		else if(this.getUniversity()==null && profile.getUniversity()!=null)
		{
			return false;
		}
		else if(this.getUniversity()!=null && profile.getUniversity()==null)
		{
			return false;
		}
		else if(!this.getUniversity().equals(profile.getUniversity()))
		{
			return false;
		}
		
		//City Check
		
		if(this.getCity()==null && profile.getCity()==null)
		{
			//Must be empty, its a check to avoid not null exception
		}			
		else if(this.getCity()==null && profile.getCity()!=null)
		{
			return false;
		}
		else if(this.getCity()!=null && profile.getCity()==null)
		{
			return false;
		}
		else if(!this.getCity().equals(profile.getCity()))
		{
			return false;
		}

		//Country check
		if(this.getCountry()==null && profile.getCountry()==null)
		{
			//Must be empty, its a check to avoid not null exception
		}			
		else if(this.getCountry()==null && profile.getCountry()!=null)
		{
			return false;
		}
		else if(this.getCountry()!=null && profile.getCountry()==null)
		{
			return false;
		}
		else if(!this.getCountry().equals(profile.getCountry()))
		{
			return false;
		}
		
		return true;
	}
}
