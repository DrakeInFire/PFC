package com.pfc.ballots.dao;

import com.pfc.ballots.entities.AboutText;

public interface AboutTextDao {
	public void store(AboutText about);
	public AboutText getAboutText();
	public void deleteAboutText();
	public void updateAboutText(AboutText about);

}
