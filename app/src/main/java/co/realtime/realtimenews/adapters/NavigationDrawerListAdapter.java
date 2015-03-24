package co.realtime.realtimenews.adapters;


import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import co.realtime.realtimenews.R;
import co.realtime.realtimenews.domains.NavigationChildItem;

public class NavigationDrawerListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader; // header titles
    private HashMap<String, List<NavigationChildItem>> _listDataChild;

    public NavigationDrawerListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<NavigationChildItem>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this.set_listDataChild(listChildData);
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.get_listDataChild().get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final NavigationChildItem item = (NavigationChildItem) getChild(groupPosition, childPosition);

        String childText = item.getContent();
        String childCounter = String.valueOf(item.getCounter());

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.navigatin_child_row, null);
        }


        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.text1);

        txtListChild.setText(childText);

        TextView txtListChildCounter = (TextView) convertView.findViewById(R.id.counter);

        //Counter
        if (item.getCounter() > 0){
            txtListChildCounter.setVisibility(View.VISIBLE);
            txtListChildCounter.setText(""+ childCounter);
        }else{
            //Hide counter if == 0
            txtListChildCounter.setVisibility(View.INVISIBLE);
        }


        txtListChildCounter.setText(childCounter);

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.get_listDataChild().get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(android.R.layout.simple_list_item_1, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(android.R.id.text1);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);



        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    public HashMap<String, List<NavigationChildItem>> get_listDataChild() {
        return _listDataChild;
    }

    public void set_listDataChild(HashMap<String, List<NavigationChildItem>> _listDataChild) {
        this._listDataChild = _listDataChild;
    }
}
