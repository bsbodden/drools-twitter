package org.integrallis.twitter;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public final class TwitterUtils {
	
	private static Logger logger = Logger.getLogger(TwitterUtils.class);
	
    public final static Double getTwitterInfluenceRatio(Twitter twitter, String username) throws TwitterException {
        User user = twitter.getUserDetail(username);
        return getTwitterInfluenceRatio(user);
    }
    
    public final static Double getTwitterInfluenceRatio(User user) throws TwitterException {
        double followers = user.getFollowersCount();
        double following = user.getFriendsCount();
        logger.info("User " + user.getScreenName() + " has " + followers + " followers and is following " + following);
        return  following / followers;
    }

    public final static Boolean hasSetProfileImage(User user) throws TwitterException {
        URL url = user.getProfileImageURL();
        logger.info("image url is => " + url);
		return !url.toString().equals("http://static.twitter.com/images/default_profile_normal.png");
    }
    
    public final static Double averageTweetsPerDay(User user) throws TwitterException {
    	int totalTweets = user.getStatusesCount();
    	logger.info("Total tweets => " + totalTweets);
    	DateTime startDate = new DateTime(user.getCreatedAt());
    	logger.info("Member since => " + startDate.toString(DateTimeFormat.forPattern("MM-dd-yyyy")));
    	DateTime endDate = new DateTime();
    	double days = Days.daysBetween(startDate, endDate).getDays();
    	logger.info("days => " + days);
    	
		return totalTweets / days;
    }
    
    public final static Boolean inactiveForTheLast(User user, Integer days) {
    	DateTime lastUpdate = new DateTime(user.getStatusCreatedAt());
    	Period period = new Period().withDays(days).withHours(12);
    	
    	return lastUpdate.plus(period).isBeforeNow();
    }
    
    public final static Integer userHasRepliedTo(Twitter twitter, String target) throws TwitterException {
    	int directMessagesFromTarget = 0;
    	List<DirectMessage> directMessages = twitter.getDirectMessages();
    	for (DirectMessage directMessage : directMessages) {
    		if (directMessage.getSender().getScreenName().equals(target)) directMessagesFromTarget++;
		}
    	
    	int mentionsFromTarget = 0;
    	List<Status> replies = twitter.getMentions();
    	for (Status status : replies) {
    		if (status.getUser().getScreenName().equals(target)) mentionsFromTarget++;
    	}
    	
    	return directMessagesFromTarget + mentionsFromTarget;
    }
    
    public final static Integer followersInCommon(Twitter twitter, User target) throws TwitterException {
    	return followersInCommon(twitter, target.getScreenName());
    }
    
    public final static Integer followersInCommon(Twitter twitter, String target) throws TwitterException {
    	List<User> targetFollowers = twitter.getFollowers(target);
        List<User> followers = twitter.getFollowers();
        
        Set<User> union = new HashSet<User>(targetFollowers);
        union.addAll(new HashSet<User>(followers));
        
    	return new ArrayList<User>(union).size();
    }
    
    public final static Integer followingInCommon(Twitter twitter, User target) throws TwitterException {
    	return followingInCommon(twitter, target.getScreenName());
    }
    
    public final static Integer followingInCommon(Twitter twitter, String target) throws TwitterException {
    	List<User> targetFollowing = twitter.getFriends(target);
        List<User> following = twitter.getFriends();
        
        Set<User> union = new HashSet<User>(targetFollowing);
        union.addAll(new HashSet<User>(following));
        
    	return new ArrayList<User>(union).size();
    }
    
    public final static Boolean isFollowing(Twitter twitter, String target) throws TwitterException {
    	return twitter.existsFriendship(twitter.getUserId(), target);
    }
    
    public final static Boolean isFollowing(Twitter twitter, User target) {
    	boolean result = false;
    	try {
			result = twitter.existsFriendship(twitter.getUserId(), target.getScreenName());
		} catch (TwitterException e) {
			// ignore and move on
		}  	
		return result;
    }
    
}
