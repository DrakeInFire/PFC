package com.pfc.ballots.pages.profile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.services.Response;

import com.pfc.ballots.dao.EmailAccountDao;
import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.dao.ProfileCensedInDao;
import com.pfc.ballots.dao.UserDao;
import com.pfc.ballots.data.DataSession;
import com.pfc.ballots.entities.EmailAccount;
import com.pfc.ballots.entities.Profile;
import com.pfc.ballots.entities.ProfileCensedIn;
import com.pfc.ballots.pages.Index;
import com.pfc.ballots.pages.SessionExpired;
import com.pfc.ballots.pages.UnauthorizedAttempt;
import com.pfc.ballots.pages.admin.AdminMail;
import com.pfc.ballots.util.Encryption;
import com.pfc.ballots.util.Mail;
import com.pfc.ballots.util.ManipulateFiles;
import com.pfc.ballots.util.UUID;








/**
 * 
 * CreateProfile class is the controller for the CreateProfile page that
 * allows to create a new user
 * 
 * @author Mario Temprano Martin
 * @version 1.0 FEB-2014
 */


public class ProfileByFile {
	 	
	String sep=System.getProperty("file.separator");
	String path=System.getProperty("user.home")+sep+"BallotsFiles"+sep+"uploadfiles"+sep;
	
	@SessionState
	private DataSession datasession;
	@Inject
	private Request request;
	
	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;
	@Inject
    private ComponentResources componentResources;
	
	/////////////////////////////////////////// DAO ///////////////////////////////////////////////
	FactoryDao DB4O =FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
	UserDao userDao=null;
	ProfileCensedInDao censedInDao=null;
	EmailAccountDao emailDao=null;
	
	
	
	
	@Persist
	String access;
	@Persist
	String lastaccess;
	
	@Persist
	String finalpath;
	
	@Property
	@Persist
	private boolean fileupload;
	@Persist
	private boolean fileLoaded;
	@Property
	@Persist
	private boolean editing;
	@Property
	@Persist
	private boolean badFile;
	@Persist
	private boolean newuser;
	
	
	
		
	private enum Actions{
		SAVE,CANCEL
	};
	private Actions action;
	
	
	/**
	 * Set the name of the DB
	 * @param dname
	 */
	public void setup(String dname){
		access=dname;
	}
	/**
	 * Initialize variables
	 */
	void setupRender()
	{
		badFile=false;
		if(!(lastaccess==null && access==null))
		{
			if(lastaccess==null && access!=null)
			{
				fileupload=false;
				fileLoaded=false;
				
				lastaccess=access;
			}
			else if(lastaccess!=null && access==null)
			{
				fileupload=false;
				fileLoaded=false;
				
				lastaccess=access;
			}
			else if(!lastaccess.equals(access))
			{
				fileupload=false;
				fileLoaded=false;
				
				lastaccess=access;
			}
		}
			
		if(fileupload)
		{
			newuser=false;
			editing=false;
			if(!finalpath.toLowerCase().endsWith(".xml"))
			{
				badFile=true;
				ManipulateFiles.deletefile(finalpath);
				fileupload=false;				
			}
			if(!fileLoaded)
			{
				fileLoaded=true;
				persons=ManipulateFiles.getProfilesFromFile(finalpath);
			}
		}
		else
		{
			currentId=null;
		}
		
	}
	
	/**
	 * Return a view of an example xml file
	 * 
	 * @return the view of the example file
	 */
	  StreamResponse onReturnStreamResponse() {
	        return new StreamResponse() {
	            InputStream inputStream;

	            //@Override
	            public void prepareResponse(Response response) {
	                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	                
	                inputStream = classLoader.getResourceAsStream("com/pfc/ballots/util/examples/example.xml");
	                try {
	                    response.setHeader("Content-Length", "" + inputStream.available());
	                }
	                catch (IOException e) {
	                    // Ignore the exception in this simple example.
	                }
	            }

	            //@Override
	            public String getContentType() {
	                return "text/plain";
	            }

	            //@Override
	            public InputStream getStream() throws IOException {
	                return inputStream;
	            }
	        };
	    }
	
	
	@Property
	@Persist
	private List<Profile> persons;
	
	private List<Profile> added;
	
	
	private Profile person;
	
	
	
	
	public Profile getPerson()
	{
		userDao=DB4O.getUsuarioDao(access);
		if(userDao.isNoMailRegistred(person.getEmail()))
		{
			boolean isOk=false;
			int i=1;
			while(isOk==false)
			{
				person.setEmail(person.getFirstName()+i+"@nomail.com");
				//person.setPassword(person.getFirstName()+i+"@nomail.com");

				i++;
				if(!userDao.isNoMailRegistred(person.getEmail()))
				{
					isOk=true;
				}
			}
		}
		return person;
	}
	public void setPerson (Profile person)
	{
		this.person=person;
	}
	
	@Persist
	@Property
	private Profile profile;
	
	
	
	@Persist
	private String currentId;
	
	@Property
	private UploadedFile file;
	
	@InjectComponent
	private Zone gridZone;
	@InjectComponent
	private Zone editZone;
	@InjectComponent
	private Form editForm;
	
	
	
	
	
	/**
	 * Upload the file
	 */
	public void onSuccessFromUploadForm()
	{
		System.out.println("SUCCESSS");
		String[] namef=file.getFileName().split("\\.");
		if(datasession.isMainAdmin())
		{
			finalpath=path+datasession.getEmail()+"."+namef[namef.length-1];
		}
		else
		{
			finalpath=path+datasession.getEmail()+"."+datasession.getDBName()+"."+namef[namef.length-1];
		}
		File copied= new File(finalpath);
		fileupload=true;
		fileLoaded=false;
		file.write(copied);
	}
	/**
	 * Shows an edit form for a user
	 * @param id
	 */
	public void onActionFromEditBut(String id)
	{
		
		System.out.println("EDIT->"+id);
		currentId=id;
		person=lookforid(id);
		profile=new Profile(person);
		editing=true;
		if(request.isXHR())
		{
			ajaxResponseRenderer.addRender(editZone).addRender(gridZone);
		}
	}
	/**
	 * Deletes the uploaded file
	 * @return
	 */
	public Object onActionFromEndBut()
	{
		fileupload=false;
		ManipulateFiles.deletefile(finalpath);
		persons=null;
		fileLoaded=false;
		componentResources.discardPersistentFieldChanges();
		return Index.class;
		
	}
	/**
	 * Shows a form to add a new user row
	 */
	public void onActionFromAddRowBut()
	{
		profile=new Profile();
		profile.setId(nextId());
		newuser=true;
		editing=true;
		if(request.isXHR())
		{
			ajaxResponseRenderer.addRender(editZone).addRender(gridZone);
		}
		
	}
	
	public void onSelectedFromSave()
	{
		action=Actions.SAVE;
	}
	public void onSelectedFromCancel()
	{
		action=Actions.CANCEL;
	}
	/**
	 * Stores the selected user
	 */
	public void onSuccessFromEditForm()
	{
		System.out.println("SUCCESS");
		if(action==Actions.SAVE)
		{
			if(newuser)
			{
				persons.add(profile);
			}
			else
			{
				person=lookforid(currentId);
				person.copy(profile);
			}
		}
		editing=false;
		if(request.isXHR())
		{
			ajaxResponseRenderer.addRender(editZone).addRender(gridZone);
		}
	}
	public void onFailureFromEditForm()
	{
		if(action==Actions.CANCEL)
		{
			editing=false;
			newuser=false;
			if(request.isXHR())
			{
				ajaxResponseRenderer.addRender(editZone).addRender(gridZone);
			}
		}
		editForm.clearErrors();
	}
	public boolean isAvalible()
	{
		userDao=DB4O.getUsuarioDao(access);
		if(person.getEmail()==null)
			return false;
		return !(userDao.isProfileRegistred(person.getEmail().toLowerCase()));
		
	}
	public boolean isComplete()
	{
		if(person.getEmail()==null || person.getPassword()==null || person.getFirstName()==null || person.getLastName()==null)
		{
			return false;
		}
		if(person.getEmail().trim().length()==0 || person.getPassword().trim().length()<=5 || person.getFirstName().trim().length()==0 || person.getLastName().trim().length()==0)
		{
			return false;
		} 
		 Pattern pat = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
	     Matcher mat = pat.matcher(person.getEmail());
	     if(mat.find())
	     {
	    	 return true;
	     }
	     return false;
		
	}
	public boolean isOkemail()
	{
		if(person.getEmail()==null)
			return false;
		 Pattern pat = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
	     Matcher mat = pat.matcher(person.getEmail());
	     if(mat.find())
	     {
	    	 return true;
	     }
	     return false;
		
	}
	public boolean isOkpass()
	{
		if(person.getPassword()==null)
			return false;
		if(person.getPassword().trim().length()<=5)
		{
			return false;
		}
		return true;
	}
	public boolean isOkfirstname()
	{
		if(person.getFirstName()==null)
			return false;
		if(person.getFirstName().trim().length()==0)
			return false;
					
		return true;
	}
	public boolean isOklastname()
	{
		if(person.getLastName()==null)
			return false;
		if(person.getLastName().trim().length()==0)
			return false;
		
		return true;
	}
	/**
	 * Stores the available users
	 */
	public void onActionFromAddavaliblesbut()
	{
		userDao=DB4O.getUsuarioDao(access);
		censedInDao=DB4O.getProfileCensedInDao(access);
		
		
		added=new LinkedList<Profile>();
		for(int i=0;i<persons.size();i++)
		{
			System.out.println("ITERACION-->"+i);
			person=persons.get(i);
			if(isAvalible())
			{
				Profile temp=new Profile(person);
				added.add(person);
				temp.setId(UUID.generate());
				temp.setRegDatetoActual();
				temp.setPassword(Encryption.getStringMessageDigest(person.getPassword(), Encryption.SHA1));
				
				ProfileCensedIn censedIn=new ProfileCensedIn(temp.getId());
				
				censedInDao.store(censedIn);
				userDao.store(temp);
				sendMail(temp.getId());
			}
			else
			{
				System.out.println(person.getId()+"->NO");
			}
		}
		for(int i=0;i<added.size();i++)
		{
			persons.remove(added.get(i));
		}
		added.clear();
		if(request.isXHR())
		{
			ajaxResponseRenderer.addRender(editZone).addRender(gridZone);
		}
	}
	public void onActionFromAddbut(String id)
	{
		userDao=DB4O.getUsuarioDao(access);
		censedInDao=DB4O.getProfileCensedInDao(access);
		
		person=lookforid(id);
		
		
		Profile temp=new Profile(person);
		persons.remove(person);
		temp.setId(UUID.generate());
		temp.setRegDatetoActual();
		
		temp.setPassword(Encryption.getStringMessageDigest(person.getPassword(), Encryption.SHA1));
		
		ProfileCensedIn censedIn=new ProfileCensedIn(temp.getId());
		censedInDao.store(censedIn);
		userDao.store(temp);
		sendMail(temp.getId());
		if(request.isXHR())
		{
			ajaxResponseRenderer.addRender(editZone).addRender(gridZone);
		}
	}

	/**
	 * Report a users that are able to vote in the created ballot
	 * @param censo
	 * @param ballotMail
	 */
	private void sendMail (String idUser)
	{
		userDao=DB4O.getUsuarioDao(datasession.getDBName());
		emailDao=DB4O.getEmailAccountDao();
		String emailDestino=userDao.getEmailById(idUser);
		Profile nuevoUser=userDao.getProfileById(idUser);
		EmailAccount account=emailDao.getAccount();
		
		
		
		String subject;
		String txt;
		
		if(access==null)
		{
			subject="Votaciones Usal alta";
			txt="Usted ha sido dado de alta en la pagina web Votaciones usal<br/> Su nombre de usuario es "+emailDestino+"<br/>"
					+ "Para obtener su contraseña, solicitela en ¿Olvidó contraseña? y se la mandaremos a su correo electrónico"; 
		}
		else
		{
			subject="Votaciones Usal alta";
			txt="Usted ha sido dado de alta en la pagina web Votaciones usal<br/> Su nombre de usuario es "+emailDestino+"<br/>"
					+ "Para obtener su contraseña, solicitela en ¿Olvidó contraseña? y se la mandaremos a su correo electrónico";
		}
		

		
		Mail.sendMail(account.getEmail(), account.getPassword(), emailDestino, subject, txt);
	}
	
	private Profile lookforid(String id)
	{
		for(int i=0;i<=persons.size();i++)
		{
			if(persons.get(i).getId().equals(id))
			{
				return persons.get(i);
			}
		}
		return null;
		
	}
	private String nextId()
	{
		Integer temp;
		temp =persons.size();
		return temp.toString();
	}
	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 /////////////////////////////////////////////////////// ON ACTIVATE //////////////////////////////////////////////////////// 
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
