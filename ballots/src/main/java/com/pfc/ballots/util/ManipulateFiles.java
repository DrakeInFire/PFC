package com.pfc.ballots.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.pfc.ballots.entities.Profile;
/**
 * Reads the users in a xml file
 * 
 * @author Mario Temprano Martin
 * @version 1.0 FEB-2014
 *
 */
public class ManipulateFiles {

	public static List<Profile> getProfilesFromFile(String path)
	{
		String[] temp=path.split("\\.");
		if(temp[temp.length-1].toLowerCase().equals("xml"))
		{
			return getXML(path);
		}
		/*else if(temp[temp.length-1].toLowerCase().equals("txt"))
		{
			return getTXT(path);
		}*/
		return null;
	}
	
	private static List<Profile> getXML(String path)
	{
		List<Profile> list=null;
		try
		{
			File fxml=new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fxml);
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("user");
			
			list=new LinkedList<Profile>();
			
			for (int x = 0; x < nList.getLength();x++) 
			{
				Node nNode = nList.item(x);
				Profile temp=new Profile();
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) 
				{	
					Element eElement = (Element) nNode;
					Integer xt=x+1;//un id real sera introducido cuando se añada a la base de datos
					temp.setId(xt.toString());
					if(eElement.getElementsByTagName("firstName").getLength()!=0 && eElement.getElementsByTagName("lastName").getLength()!=0)
					{
						temp.setFirstName(eElement.getElementsByTagName("firstName").item(0).getTextContent());
						temp.setLastName(eElement.getElementsByTagName("lastName").item(0).getTextContent());
						temp.setPassword(PasswordGenerator.getPassword());
						if(eElement.getElementsByTagName("email").getLength()!=0)
							{
								temp.setEmail(eElement.getElementsByTagName("email").item(0).getTextContent());
							}
						else 
						{
								temp.setEmail(removerAcentos(eElement.getElementsByTagName("firstName").item(0).getTextContent().trim().replaceAll(" ", ""))+"0@nomail.com");
								
						}
						if(eElement.getElementsByTagName("university").getLength()!=0)
							{temp.setUniversity(eElement.getElementsByTagName("university").item(0).getTextContent());}
						if(eElement.getElementsByTagName("city").getLength()!=0)
							{temp.setCity(eElement.getElementsByTagName("city").item(0).getTextContent());}
						if(eElement.getElementsByTagName("country").getLength()!=0)
							{temp.setCountry(eElement.getElementsByTagName("country").item(0).getTextContent());}
						if(eElement.getElementsByTagName("address").getLength()!=0)
						{
							temp.setAddress(eElement.getElementsByTagName("address").item(0).getTextContent());
						}
						if(eElement.getElementsByTagName("phone").getLength()!=0)
						{
							temp.setPhone(eElement.getElementsByTagName("phone").item(0).getTextContent());
						}
						if(eElement.getElementsByTagName("center").getLength()!=0)
						{
							temp.setCentro(eElement.getElementsByTagName("center").item(0).getTextContent());
						}
						if(eElement.getElementsByTagName("studies").getLength()!=0)
						{
							temp.setCarrera(eElement.getElementsByTagName("studies").item(0).getTextContent());
						}
						if(eElement.getElementsByTagName("dni").getLength()!=0)
						{
							temp.setDNI(eElement.getElementsByTagName("dni").item(0).getTextContent());
						}
						if(eElement.getElementsByTagName("Student").getLength()!=0)
						{
							String t=eElement.getElementsByTagName("address").item(0).getTextContent();
							if(t.toLowerCase().equals("true") || t.toLowerCase().equals("si") || t.toLowerCase().equals("sí")|| t.toLowerCase().equals("yes"))
							{
								temp.setStudent(true);
							}
							else if(t.toLowerCase().equals("false") || t.toLowerCase().equals("no"))
							{
								temp.setStudent(false);
							}
						}
						
						
						list.add(temp);
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return list;
	}
	/*private static List<Profile> getTXT(String path)
	{

		File file=null;
		FileReader fr=null;
		BufferedReader br=null;
		
		List<Profile> list=null;
		try
		{
			file=new File(path);
			fr=new FileReader(file);
			br=new BufferedReader(fr);
			
			String line;
			list=new LinkedList<Profile>();
			Integer id=0;
			while((line=br.readLine())!=null)
			{
				
				if(line.equals(""))
				{
					break;
				}
				System.out.println(line);
				String[] sub=line.split(";");
				Profile temp=new Profile();
				if(sub.length>0)
					{temp.setEmail(sub[0]);}
				if(sub.length>1)
					{temp.setFirstName(sub[1]);}
				if(sub.length>2)
					{temp.setLastName(sub[2]);}
				if(sub.length>3)
					{temp.setPassword(sub[3]);}
				else if(sub.length>0)
					{temp.setPassword(sub[0]);}
				if(sub.length>4)
					{temp.setUniversity(sub[4]);}
				if(sub.length>5)
					{temp.setCountry(sub[5]);}
				if(sub.length>6)
					{temp.setCity(sub[6]);}
				
				temp.setId(id.toString());
				id++;
				list.add(temp);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return list;
	}*/
	
	/**
	 * Deletes a file 
	 * @param path of the file to delete
	 */
	public static void deletefile(String path)
	{
		try
		{
			File file=new File(path);
			file.delete();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
			
		
	}
	 	private static String conAcentos = "áéíóúÁÉÍÓÚ";
	    private static String sinAcentos = "aeiouAEIOU";

	    private static String removerAcentos(String s) {
	        for (int i = 0; i < conAcentos.length(); i++)
	            s = s.replace(conAcentos.charAt(i), sinAcentos.charAt(i));
	        return s;
	    }
}
