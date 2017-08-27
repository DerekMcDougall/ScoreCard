package freefrogurt.scorecard;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class HybridArrayAdapter<T> extends ArrayAdapter<T> {
	
	public HybridArrayAdapter(Context context, int resource, T[] objects) {
		super(context, resource, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView view = new TextView(super.getContext());
		view.setText(this.getItem(position).toString());
		return view;
	}

}
