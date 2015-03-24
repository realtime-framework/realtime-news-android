package co.realtime.realtimenews.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import co.realtime.realtimenews.R;
import co.realtime.realtimenews.domains.ContentResponse;
import co.realtime.realtimenews.networking.DownloadNewsTask;
import co.realtime.realtimenews.util.ProgressWheel;

public class CustomListAdapter extends BaseAdapter implements Filterable{

    private Activity activity;
    private List<ContentResponse> contentItems;
    private ItemFilter mFilter = new ItemFilter();

    static class ViewHolderItem{
        ImageView thumbnail;
        TextView title;
        TextView description;
        TextView timestamp;
        TextView tag;
        TextView state;
        ImageView saveBtn;
        ProgressWheel progressWheel;

    }

    public CustomListAdapter(Activity activity, List<ContentResponse> contentItems) {
        this.activity = activity;
        this.contentItems = contentItems;
    }

    @Override
    public int getCount() {
        return contentItems.size();
    }

    @Override
    public Object getItem(int location) {
        return contentItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolderItem viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(activity).inflate(R.layout.list_row, parent, false);

            viewHolder = new ViewHolderItem();
            viewHolder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            viewHolder.description = (TextView) convertView.findViewById(R.id.description);
            viewHolder.timestamp = (TextView) convertView.findViewById(R.id.date);
            viewHolder.tag = (TextView) convertView.findViewById(R.id.tag);
            viewHolder.state = (TextView) convertView.findViewById(R.id.state);
            viewHolder.saveBtn = (ImageView) convertView.findViewById(R.id.saveBtn);
            viewHolder.progressWheel = (ProgressWheel) convertView.findViewById(R.id.pw_spinner);

            convertView.setTag(viewHolder);

        } else{
            viewHolder = (ViewHolderItem)convertView.getTag();
        }

        ContentResponse c = contentItems.get(position);

        viewHolder.thumbnail.setTag(c.getTimestamp());

        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(activity).load(c.getImg()).into(viewHolder.thumbnail);

        // title
        viewHolder.title.setText(c.getTitle());

        // description
        viewHolder.description.setText(c.getDescription());

        // date
        viewHolder.timestamp.setText(c.getTimestampText());

        // tag
        viewHolder.tag.setText(c.getTag());

        //state
        viewHolder.state.setVisibility(c.getStateVisibility());
        viewHolder.state.setText(c.getStateText());

        viewHolder.saveBtn.setImageResource(c.getSaveBtnResource());

        viewHolder.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentResponse c = (ContentResponse) v.getTag();
                DownloadNewsTask downloadNewsTask = new DownloadNewsTask(activity, viewHolder.progressWheel, viewHolder.saveBtn);
                downloadNewsTask.execute(c,v);
            }
        });

        viewHolder.saveBtn.setClickable(c.isClickable());

        viewHolder.saveBtn.setTag(c);

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }


    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();
            // We implement here the filter logic
            if (constraint == null || constraint.length() == 0) {
                // No filter implemented we return all the list
                results.values = contentItems;
                results.count = contentItems.size();
            }
            else {
                // We perform filtering operation
                List<ContentResponse> nContentList = new ArrayList<ContentResponse>();

                for (ContentResponse p : contentItems) {
                    if (p.getTag().equalsIgnoreCase(constraint.toString()) || p.getType().equalsIgnoreCase(constraint.toString()))
                        nContentList.add(p);
                }

                results.values = nContentList;
                results.count = nContentList.size();

            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Now we have to inform the adapter about the new list filtered
            if (results.count == 0)
                notifyDataSetInvalidated();
            else {
                contentItems = (List<ContentResponse>) results.values;
                notifyDataSetChanged();
            }
        }

    }
}
