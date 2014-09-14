package com.pfc.ballots.dao;

import java.util.List;

import com.pfc.ballots.entities.Vote;
/**
 * Dao Interface to retrieves Vote entity
 * 
 * @author Mario Temprano Martin
 * @version 1.0 JUL-2014
 *
 */

public interface VoteDao {

	public void store(Vote vote);
	public void store(List<Vote> votes);
	public List<Vote> getBallotVotes(String idBallot);
	public Vote getVoteByIds(String idBallot,String idUser);
	public boolean isVoted(String idBallot,String idUser);
	public List<Vote> getVotesUser(String idUser);
	
	public List<String> getBallotsWithParticipation(String idUser);
	
	public void updateVote(Vote vote);
	public void deleteVoteOfBallot(String idBallot);
	public void deleteVotesNotDone(List<String> idUser,List<String> idBallot);
	public void addVotes(List<String> idUser,List<String> idBallot);
	
}
