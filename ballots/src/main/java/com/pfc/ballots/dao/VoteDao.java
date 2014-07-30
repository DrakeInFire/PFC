package com.pfc.ballots.dao;

import java.util.List;

import com.pfc.ballots.entities.Vote;

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
	
}