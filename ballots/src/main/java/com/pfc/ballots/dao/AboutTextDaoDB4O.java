package com.pfc.ballots.dao;

import java.util.Calendar;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.query.Query;
import com.pfc.ballots.entities.AboutText;
import com.pfc.ballots.entities.Ballot;
/**
 * Implementation of the interface AboutTextDao for the DB4O database
 * 
 * @author Mario Temprano Martin
 * @version 2.0 JUL-2014
 *
 */
public class AboutTextDaoDB4O implements AboutTextDao{
	String sep=System.getProperty("file.separator");
	String PATH;
	String ruta=System.getProperty("user.home")+sep+"BallotsFiles"+sep;
	EmbeddedConfiguration config = null;
	ObjectContainer DB=null;
	
	public AboutTextDaoDB4O()
	{
			PATH=ruta+"DB4Obbdd.dat";
	}


	/**
	 * Stores the about text
	 * @param about class with the text info
	 */
	public void store(AboutText about)
	{
		open();
		try
		{
			DB.store(about);
			System.out.println("[DB4O]Ballot was Stored");
			
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
	/**
	 * Returns the about us text
	 * @return
	 */
	public AboutText getAboutText()
	{
		open();
		try
		{
			Query query=DB.query();
			query.constrain(AboutText.class);
			ObjectSet result=query.execute();
			if(result.hasNext())
			{
				return (AboutText)result.next();
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
		return null;
	}
	/**
	 * deletes the about us text
	 */
	public void deleteAboutText()
	{
		open();
		try
		{
			Query query=DB.query();
			query.constrain(AboutText.class);
			ObjectSet result=query.execute();
			if(result.hasNext())
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
	 * Updates the about us text
	 * @param about
	 */
	public void updateAboutText(AboutText about)
	{
		open();
		try
		{
			Query query=DB.query();
			query.constrain(AboutText.class);
			ObjectSet result=query.execute();
			if(result.hasNext())
			{
				DB.delete(result.next());
				
			}
			DB.store(about);
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
	 * Opens the database
	 */
	private void open()
	{
		config=Db4oEmbedded.newConfiguration();
		config.common().objectClass(AboutText.class).cascadeOnUpdate(true);
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
