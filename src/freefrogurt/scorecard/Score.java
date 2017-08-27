package freefrogurt.scorecard;

import android.os.Parcel;
import android.os.Parcelable;

public class Score implements Parcelable {
	private int[] teamScore;

	public Score(int... scores) {
		teamScore = scores.clone();
	}

	public int getScoreForTeam(int teamId) {
		return teamScore[teamId];
	}

	public Score addPoints(int... points) {
		int[] newTeamScores = new int[teamScore.length];
		for (int i = 0; i < points.length; i++) {
			newTeamScores[i] = teamScore[i] + points[i];
		}
		return new Score(newTeamScores);
	}

	@Override
	public String toString() {
		return toString(false);
	}

	public String toShortString() {
		return toString(true);
	}

	private String toString(boolean compact) {
		StringBuffer buffer = new StringBuffer();
		boolean first = true;
		for (int teamId = 0; teamId < teamScore.length; teamId++) {
			if (!first) {
				buffer.append(compact ? " : " : ", ");
			}
			if (!compact) {
				buffer.append("Team " + teamId + ": ");
			}
			buffer.append(Integer.toString(teamScore[teamId]));
			first = false;
		}
		return buffer.toString();

	}

	public static final Parcelable.Creator<Score> CREATOR = new Parcelable.Creator<Score>() {
		public Score createFromParcel(Parcel source) {
			return new Score(source);
		}

		public Score[] newArray(int size) {
			return new Score[size];
		}

	};

	private Score(Parcel source) {
		teamScore = source.createIntArray();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeIntArray(teamScore);
	}
}
