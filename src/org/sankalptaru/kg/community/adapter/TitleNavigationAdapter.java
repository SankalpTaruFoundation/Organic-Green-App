package org.sankalptaru.kg.community.adapter;

import java.util.ArrayList;

import org.sankalptaru.kg.community.R;
import org.sankalptaru.kg.community.model.SpinnerNavItem;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TitleNavigationAdapter extends BaseAdapter {

	private ImageView imgIcon;
	private TextView txtTitle,txtCount;
	private ArrayList<SpinnerNavItem> spinnerNavItem;
	private Context context;
	private ArrayList<Integer> eachCategoryThreadCount;

	public TitleNavigationAdapter(Context context,
			ArrayList<SpinnerNavItem> spinnerNavItem, ArrayList<Integer> eachCategoryThreadCount) {
		this.spinnerNavItem = spinnerNavItem;
		this.context = context;
		this.eachCategoryThreadCount=eachCategoryThreadCount;
	}

	@Override
	public int getCount() {
		return spinnerNavItem.size();
	}

	@Override
	public Object getItem(int index) {
		return spinnerNavItem.get(index);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) { 
		if (convertView == null) {
			LayoutInflater mInflater = (LayoutInflater)
					context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.list_item_title_navigation, null);
		}

		imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
		txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
		txtCount = (TextView) convertView.findViewById(R.id.txtThreadCount);

		imgIcon.setImageResource(spinnerNavItem.get(position).getIcon());
		imgIcon.setVisibility(View.GONE);
		
		
		txtCount.setVisibility(View.GONE);
		
		txtTitle.setText(spinnerNavItem.get(position).getTitle());
		return convertView;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater mInflater = (LayoutInflater)
					context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.list_item_title_navigation, null);
		}

		imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
		txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
		txtCount = (TextView) convertView.findViewById(R.id.txtThreadCount);

		imgIcon.setImageResource(spinnerNavItem.get(position).getIcon());        
		txtTitle.setText(spinnerNavItem.get(position).getTitle());
		
		txtCount.setText(""+eachCategoryThreadCount.get(position)+" Posts");
		
		return convertView;
	}

}
