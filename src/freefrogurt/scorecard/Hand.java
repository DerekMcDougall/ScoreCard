package freefrogurt.scorecard;

import android.os.Parcel;
import android.os.Parcelable;
import freefrogurt.scorecard.Game.Suit;

public class Hand implements Parcelable {

	// Score at the end of the hand
	private Score resultingScore;
	private int dealerId;
	private int bid;
	private int bidderId;
	private Suit trump;
	// ID of partner for 5-player games
	private int partnerId = -1;

	private boolean biddingDone = false;
	private boolean handDone = false;

	public Hand(int dealerId) {
		this.dealerId = dealerId;
	}

	public int getDealerId() {
		return dealerId;
	}

	public Score getResultingScore() {
		return resultingScore;
	}

	public int getBid() {
		return bid;
	}

	public int getBidderId() {
		return bidderId;
	}

	public Suit getTrump() {
		return trump;
	}

	public void setBidResult(int bidderId, int bid, Game.Suit trump) {
		this.bidderId = bidderId;
		this.bid = bid;
		this.trump = trump;
		biddingDone = true;
	}
	
	public void setPartner(int partnerId) {
		this.partnerId = partnerId;
	}

	public void setResultingScore(Score resultingScore)
			throws GameStateException {
		if (!biddingDone) {
			throw new GameStateException(
					"Resulting score for hand cannot be set before bid result");
		}
		this.resultingScore = resultingScore;
		handDone = true;
	}

	public boolean isBiddingDone() {
		return biddingDone;
	}

	public boolean isHandDone() {
		return handDone;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.dealerId);
		dest.writeInt(biddingDone ? 1 : 0);
		if (!biddingDone) {
			// Nothing else to persist
			return;
		}
		dest.writeInt(bidderId);
		dest.writeInt(bid);
		dest.writeInt(trump.ordinal());
		dest.writeInt(handDone ? 1 : 0);
		if (!handDone) {
			// no score yet
			return;
		}
		dest.writeInt(partnerId);
		dest.writeParcelable(resultingScore, 0);
	}
	
	public static final Parcelable.Creator<Hand> CREATOR = new Parcelable.Creator<Hand>() {

		public Hand createFromParcel(Parcel source) {
			return new Hand(source);
		}

		public Hand[] newArray(int size) {
			return new Hand[size];
		}
	};
	
	private Hand(Parcel source) {
		dealerId = source.readInt();
		biddingDone = (source.readInt() == 1);
		if (!biddingDone) {
			// Nothing else to restore
			return;
		}
		bidderId = source.readInt();
		bid = source.readInt();
		trump = Suit.values()[source.readInt()];
		handDone = (source.readInt() == 1);
		if (!handDone) {
			// no score yet
			return;
		}
		partnerId = source.readInt();
		resultingScore = source.readParcelable(null);
	}
}
