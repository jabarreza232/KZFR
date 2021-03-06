package creek.fm.doublea.kzfr.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import creek.fm.doublea.kzfr.R;
import creek.fm.doublea.kzfr.adapters.SchedulePagerAdapter;
import creek.fm.doublea.kzfr.api.ApiClient;
import creek.fm.doublea.kzfr.api.KZFRRetrofitCallback;
import creek.fm.doublea.kzfr.fragments.ScheduleDayFragment;
import creek.fm.doublea.kzfr.models.Day;
import creek.fm.doublea.kzfr.models.Show;
import retrofit.client.Response;

/**
 * An activity that displays the schedule for each week.
 */
public class ScheduleActivity extends MainActivity implements ScheduleDayFragment.GetDataInterface {
    private static final String TAG = ScheduleActivity.class.getSimpleName();
    private static final String SCHEDULE_DATA_KEY = TAG + ".schedule_data";

    private SchedulePagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View content = inflater.inflate(R.layout.activity_schedule, mContentView, true);
        mViewPager = (ViewPager) content.findViewById(R.id.schedule_pager);
        mViewPager.setAdapter(getSchedulePagerAdapter());
        if (savedInstanceState != null) {
            @SuppressWarnings("unchecked")
            HashMap<String, Day> scheduleData = (HashMap<String, Day>) savedInstanceState.getSerializable(SCHEDULE_DATA_KEY);
            addDataToPager(scheduleData);
        } else {
            showProgressBar(true);
            executeApiCall();
        }
    }

    private void addDataToPager(HashMap<String, Day> data) {
        Log.d("ScheduleActivity", "Start Adding data to pager");
        if (data != null && !data.isEmpty()) {
            Log.d("ScheduleActivity", "data available");
            mPagerAdapter.setData(data);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPagerAdapter.notifyDataSetChanged();
                    mViewPager.setCurrentItem(getDayOfWeek());
                    showProgressBar(false);
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPagerAdapter != null && !mPagerAdapter.isEmpty()) {
            outState.putSerializable(SCHEDULE_DATA_KEY, mPagerAdapter.getAllData());
        }
    }

    private SchedulePagerAdapter getSchedulePagerAdapter() {
        Log.d("SchudeleActivity", "setupPagerAdapter");
        if (mPagerAdapter == null) {
            mPagerAdapter = new SchedulePagerAdapter(getSupportFragmentManager());
        }
        return mPagerAdapter;
    }

    private void executeApiCall() {
        ApiClient.getKZFRApiClient(this).getSchedule(new KZFRRetrofitCallback<HashMap<String, Day>>() {

            @Override
            public void success(HashMap<String, Day> days, Response response) {
                super.success(days, response);
                addDataToPager(days);
            }
        });
    }

    private int getDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        return ((calendar.get(Calendar.DAY_OF_WEEK) - 2) % 7);
    }

    @Override
    public List<Show> getShowListData(int position) {
        if (mPagerAdapter != null && !mPagerAdapter.isEmpty()) {
            return mPagerAdapter.getData(position);
        }
        return null;
    }
}
