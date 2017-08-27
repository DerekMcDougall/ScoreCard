package freefrogurt.scorecard;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * EditText subclass that can be paired with a second instance.
 * Changes in one instance will affect the value of the paired instance, unless the user had already modified it.
 * @author mcdougal
 *
 */
public class ComplementaryEditText extends EditText {

	public static void Pair(ComplementaryEditText offenseEdit, int offenseMinimum, ComplementaryEditText defenseEdit, int combinedTotal) {
		offenseEdit.complement = defenseEdit;
		defenseEdit.complement = offenseEdit;
		offenseEdit.minimumComplementaryValue = offenseMinimum;
		offenseEdit.substituteForUnderMinimum = -offenseMinimum;
		offenseEdit.combinedTotal = defenseEdit.combinedTotal = combinedTotal;
		offenseEdit.userModified = defenseEdit.userModified = false;
	}

	// Total number of points available for a hand, not counting special bids (8
	// for Bid Euchre)
	private int combinedTotal = 0;
	// minimum value that can be allowed that would complement the paired
	// EditText to add up to combinedTotal.
	// e.g. if the bid was 5, then 5 is the minimum for the offense
	private int minimumComplementaryValue = 0;
	// Value to use instead of complementing the paired EditText if it would be
	// below the minimum.
	// e.g. if the bid was 5, then -5 would be the substituted value for the
	// offense
	// This is always 0 for the defense, and that handles special bids where the
	// offense gets higher than combinedTotal.
	private int substituteForUnderMinimum = 0;

	// The paired EditText
	private ComplementaryEditText complement = null;

	// Indicates whether the user has modified this value
	private boolean userModified = false;
	private boolean nextChangeIsFromComplement = false;

	public ComplementaryEditText(Context context) {
		super(context);
		init();
	}

	public ComplementaryEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ComplementaryEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		this.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
	}

	@Override
	public synchronized void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
		if (nextChangeIsFromComplement) {
			// Ignore this one. But reset flag so we don't ignore next.
			// TODO: I feel like this shouldn't work...
			nextChangeIsFromComplement = false;
		} else {
			int value;
			try {
				value = Integer.parseInt(text.toString(), 10);
				userModified = true;
				complement.notifyOfPairedValue(value);
			}
			catch (NumberFormatException ex) {
				// Field does not contain a valid integer. It could be empty or just a minus sign.
				// Just ignore this change.
			}
		}
	}

	private int calculateComplement(int pairedValue) {
		int comp = this.combinedTotal - pairedValue;
		if (comp < this.minimumComplementaryValue) {
			comp = this.substituteForUnderMinimum;
		}
		return comp;
	}

	private void notifyOfPairedValue(int pairedValue) {
		if (this.userModified) {
			// Don't overwrite what a user already entered
			return;
		}
		if (pairedValue >= 0) {
			// We should be able to reliably know what score complements a non-negative value
			this.setTextWithoutDirty(Integer.toString(calculateComplement(pairedValue)));
		}
		else {
			// For negative values, assume the most common case is for the offense to miss their bid by 1.
			// So calculate the complement of 1 less than the paired EditText's minimum.
			this.setTextWithoutDirty(Integer.toString(calculateComplement(complement.minimumComplementaryValue-1)));
		}
	}

	/**
	 * Disable the event listener that detects when the text is updated, update
	 * the text, and re-enable the listener. In effect, this sets the text
	 * without changing the userModified flag and without notifying the paired
	 * EditText.
	 * 
	 * @param text
	 */
	private synchronized void setTextWithoutDirty(CharSequence text) {
		nextChangeIsFromComplement = true;
		this.setText(text);
	}

	public int getCombinedTotal() {
		return combinedTotal;
	}

	public ComplementaryEditText getComplement() {
		return complement;
	}

	public boolean isUserModified() {
		return userModified;
	}

}
