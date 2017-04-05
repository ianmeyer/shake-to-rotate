package com.iantmeyer.shaketorotate.fragment.applist;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iantmeyer.shaketorotate.activity.MainActivity;
import com.iantmeyer.shaketorotate.R;
import com.iantmeyer.shaketorotate.data.AppItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AppListFragment extends Fragment implements AppListMvp.View,
        AppsRecyclerViewAdapter.OnItemClickListener {

    private AppsRecyclerViewAdapter mAdapter;

    @BindView(R.id.recycler_view)
    protected RecyclerView mRecyclerView;

    private AppListMvp.Presenter mPresenter;

    public AppListFragment() {
    }

    @SuppressWarnings("unused")
    public static AppListFragment newInstance(int columnCount) {
        AppListFragment fragment = new AppListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_list, container, false);
        ButterKnife.bind(this, view);

        mAdapter = new AppsRecyclerViewAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.setAdapter(mAdapter);

        mPresenter = new AppListPresenter(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        String activityTitle = getResources().getString(R.string.title_activity_app_list);
        MainActivity settingsActivity = (MainActivity) getActivity();
        settingsActivity.getSupportActionBar().setTitle(activityTitle);
        settingsActivity.showBackButton();
    }

    @Override
    public void onItemClick(AppItem item) {
        mPresenter.onAppItemClick(item);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void setAppList(List<AppItem> appList) {
        mAdapter.updateData(appList);
    }
}