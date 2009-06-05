package org.integrallis.twitter;

import java.util.EnumSet;

public enum TwitterUserType {
	UNCLASSIFIED      (Double.MIN_VALUE, 0.0),
	TWITTER_CASTER    (0.0, 0.2), 
	NOTABLE           (0.2, 0.5), 
	SOCIALLY_HEALTY   (0.5, 1.0), 
	NEWBIE            (1.0, 2.0), 
	POTENTIAL_SPAMMER (2.0, Double.MAX_VALUE);  
	
	private Double low;
	private Double high;
	
	TwitterUserType(Double low, Double high) {
		this.low = low;
		this.high = high;
	}
	
	public static TwitterUserType getType(Double influenceRatio) {
		for (TwitterUserType userType : EnumSet.range(TWITTER_CASTER, POTENTIAL_SPAMMER)) {
			if ((influenceRatio > userType.low) && (influenceRatio <= userType.high)) {
				return userType;
			}
		}
		return UNCLASSIFIED;
	}
}
