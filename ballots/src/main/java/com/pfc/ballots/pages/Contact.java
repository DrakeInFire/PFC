package com.pfc.ballots.pages;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.beaneditor.Width;

import com.pfc.ballots.dao.EmailAccountDao;
import com.pfc.ballots.dao.FactoryDao;
import com.pfc.ballots.entities.EmailAccount;
import com.pfc.ballots.util.Mail2;

public class Contact
{
	
	//////////////////////////////////////////////////////////// DAO ///////////////////////////////////////////////////////////
	
	FactoryDao DB4O =FactoryDao.getFactory(FactoryDao.DB4O_FACTORY);
	EmailAccountDao emailAccountDao=null;
	
	
	
	////////////////////////////////////////////////////////// INTIALIZE //////////////////////////////////////////////////////////////
	
	public void setupRender()
	{
		emailAccountDao=DB4O.getEmailAccountDao();
		emailAccount=emailAccountDao.getAccount();
		if(Mail2.checkAccount(emailAccount))
		{
			showEmailProblem=false;
		}
		
	}
	
	@Property
	@Persist()
	EmailAccount emailAccount;
	
	@Property
	private boolean showEmailProblem;

	@Property
	@Validate("required,regexp=^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
	private String remitente;
	
	@Width(value = 75)
	@Property
	@Validate("minLength=20")
	private String text;
	
	public Object onSuccessFromContact()
	{
		String asunto="Contact: "+remitente;
		String asuntoRemit="NoReply-copia: "+emailAccount.getEmail();
		if(Mail2.sentMail(emailAccount.getEmail(), emailAccount.getPassword(), emailAccount.getEmail(), asunto, text))
		{
			Mail2.sentMail(emailAccount.getEmail(), emailAccount.getPassword(), remitente, asuntoRemit, text);
			System.out.println("ENVIADO");
		}
		else
		{
			System.out.println("ERROR AL ENVIAR");
		}
		return Index.class;
		
	}
	
	
	
}
