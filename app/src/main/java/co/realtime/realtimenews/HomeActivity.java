package co.realtime.realtimenews;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import co.realtime.realtimenews.adapters.CustomListAdapter;
import co.realtime.realtimenews.config.Config;
import co.realtime.realtimenews.domains.ContentResponse;
import co.realtime.realtimenews.domains.TagResponse;
import co.realtime.realtimenews.interfaces.OnMoreDataLoaded;
import co.realtime.realtimenews.listeners.InfiniteScrollListener;
import co.realtime.realtimenews.networking.ConnectionDetector;
import co.realtime.realtimenews.rest.AuthTokenTask;
import co.realtime.realtimenews.storage.StorageManager;

public class HomeActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private CharSequence mTag;

    private ProgressBar storageConnectionProgressBar;

    private ProgressBar mProgressDialog;



    private List<ContentResponse> contentsList = new ArrayList<ContentResponse>();
    private ListView listView;
    private CustomListAdapter adapter;

    private InfiniteScrollListener scrollListener = new InfiniteScrollListener(0) {
        @Override
        public void loadMore(int page, int totalItemsCount) {
            ContentResponse lastItem = (ContentResponse) adapter.getItem(adapter.getCount() - 1);
            String lastMonthYear = lastItem.getMonthYear();
            String lastTimestamp = lastItem.getTimestamp();
            mProgressDialog.setVisibility(View.VISIBLE);
            StorageManager.getInstance(HomeActivity.this).loadMoreData(lastMonthYear, lastTimestamp, new OnMoreDataLoaded() {
                @Override
                public void onMoreDataLoaded(final ArrayList<ContentResponse> newContent) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            contentsList = StorageManager.getInstance(HomeActivity.this).getContentsList();
                            contentsList.addAll(newContent);
                            adapter.notifyDataSetChanged();
                            mProgressDialog.setVisibility(View.GONE);
                        }
                    });
                }
            });
        }
    };

    private static boolean mIsInForegroundMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter != null){
                    ContentResponse c = (ContentResponse) adapter.getItem(position);
                    if (StorageManager.getInstance(HomeActivity.this).isConnected()){

                        String url = c.getUrl();
                        String body = c.getBody();
                        String title = c.getTitle();
                        Intent i = new Intent(HomeActivity.this,WebViewActivity.class);
                        if (url != null) {
                            i.putExtra("url", url);
                        }

                        if (body != null){
                            i.putExtra("body",body);
                        }

                        i.putExtra("title",title);
                        startActivity(i);
                    } else{
                        String body = c.getBody();
                        String title = c.getTitle();
                        if(body != null){
                            Intent i = new Intent(HomeActivity.this,WebViewActivity.class);
                            i.putExtra("body",body);
                            i.putExtra("title",title);
                            startActivity(i);
                        }else {
                            Toast.makeText(HomeActivity.this, "Please check Internet Connection!", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });


        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();


        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        startUpdateReceiver();

        storageConnectionProgressBar = (ProgressBar)findViewById(R.id.progressBar1finite);
        storageConnectionProgressBar.getIndeterminateDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);

        mProgressDialog = (ProgressBar) findViewById(R.id.dialog);

        boolean isConnected = ConnectionDetector.hasInternetConnection(this);

        if (getIntent().getExtras().getBoolean("doAuthentication") && isConnected){
            AuthTokenTask authTokenTask = new AuthTokenTask(this);
            authTokenTask.execute();
        }else{
            StorageManager.getInstance(this).init();
        }

    }

    private void setListViewLoadMoreListener(boolean setListener){
        if (setListener) {
            listView.setOnScrollListener(scrollListener);
        }else{
            listView.setOnScrollListener(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsInForegroundMode = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsInForegroundMode = false;
    }

    public static boolean isInForeground() {
        return mIsInForegroundMode;
    }

    private void loadData(){

        contentsList = StorageManager.getInstance(this).getContentsList();
        adapter = new CustomListAdapter(this, contentsList);
        listView.setAdapter(adapter);

        if (mTitle != null) {
            adapter.getFilter().filter(mTitle);
            if (mTitle != getString(R.string.title_section1)){
                setListViewLoadMoreListener(false);
            }
            else{
                setListViewLoadMoreListener(true);
            }
        }


        if (mTag != null){
            adapter.getFilter().filter(mTag);
        }

        if(mProgressDialog != null){
            mProgressDialog.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopUpdateReceiver();
    }

    private void startUpdateReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(Config.BROADCAST_EVENT));
    }

    private void stopUpdateReceiver(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(Config.BROADCAST_MESSAGE)) {
                Log.d("receiver", "Got message: " + intent.getExtras().toString());
                int type = intent.getIntExtra(Config.BROADCAST_MESSAGE,-1);
                switch (type){
                    case Config.STORAGE_CONNECTED:
                        if (storageConnectionProgressBar != null){
                            storageConnectionProgressBar.getIndeterminateDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    mProgressDialog.setVisibility(View.VISIBLE);
                                    storageConnectionProgressBar.setVisibility(View.GONE);
                                }
                            }, 500);

                        }
                        break;
                    case Config.STORAGE_INIT:
                        String data = intent.getStringExtra("tagsList");
                        Gson gson = new Gson();
                        Type typeToken = new TypeToken<ArrayList<TagResponse>>(){}.getType();
                        ArrayList<TagResponse> tagResponseArrayList = gson.fromJson(data, typeToken);
                        mNavigationDrawerFragment.updateNavigationDrawer(tagResponseArrayList);

                        loadData();

                        String newsType = getIntent().getStringExtra("type");
                        String timestamp = getIntent().getStringExtra("timestamp");

                        if(newsType != null && timestamp != null){
                            listView.performItemClick(
                                    listView.getAdapter().getView(0, null, null),
                                    0,
                                    listView.getAdapter().getItemId(0));
                        }

                        break;
                    case Config.STORAGE_DELETE:
                        String type2 = intent.getStringExtra("type");
                        String tag2 = intent.getStringExtra("tag");
                        mNavigationDrawerFragment.removeCounterNavigationDrawer(type2,tag2);
                        mNavigationDrawerFragment.updateGlobalCounter();
                        //mTitle = getString(R.string.title_section1);
                        loadData();
                        break;
                    case Config.STORAGE_UPDATE:
                        String type1 = intent.getStringExtra("type");
                        String tag = intent.getStringExtra("tag");
                        mNavigationDrawerFragment.updateCounterNavigationDrawer(type1,tag);
                        mNavigationDrawerFragment.updateGlobalCounter();
                        //loadData();
                        break;
                    case Config.STORAGE_REFRESH:
                        loadData();
                        break;
                    case Config.STORAGE_REFRESH_RECONNECTED:
                        mNavigationDrawerFragment.selectFirstItem();
                        mTitle = getString(R.string.title_section1);
                        mTag = null;
                        scrollListener.resetFields();
                        loadData();
                        break;
                    case Config.STORAGE_RECONNECTING:
                        if (storageConnectionProgressBar != null){
                            storageConnectionProgressBar.getIndeterminateDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                            storageConnectionProgressBar.setVisibility(View.VISIBLE);
                        }
                        break;
                    case Config.STORAGE_RECONNECTED:
                        if (storageConnectionProgressBar != null){
                            storageConnectionProgressBar.getIndeterminateDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    storageConnectionProgressBar.setVisibility(View.GONE);
                                }
                            }, 500);
                        }
                        mProgressDialog.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }

            }

        }
    };


    @Override
    public void onNavigationDrawerItemSelected(int position, String tag) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
        mTag = tag;
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
        if (adapter != null) {
            loadData();
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.navigation, menu);
            final Menu m = menu;
            final MenuItem item = menu.findItem(R.id.badge);
            item.getActionView().setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    m.performIdentifierAction(item.getItemId(), 0);
                }
            });
            final MenuItem item1 = menu.findItem(R.id.clearFilters);
            item1.getActionView().setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    m.performIdentifierAction(item1.getItemId(), 0);
                }
            });
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_home, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((HomeActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
