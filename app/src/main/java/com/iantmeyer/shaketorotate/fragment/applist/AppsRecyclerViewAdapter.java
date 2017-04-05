package com.iantmeyer.shaketorotate.fragment.applist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.iantmeyer.shaketorotate.data.AppItem;
import com.iantmeyer.shaketorotate.R;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link AppItem} and makes a call to the
 * specified {@link OnItemClickListener}.
 */
class AppsRecyclerViewAdapter extends RecyclerView.Adapter<AppsRecyclerViewAdapter.ViewHolder> {

    private List<AppItem> mAppItems;
    private final OnItemClickListener mListener;

    AppsRecyclerViewAdapter(OnItemClickListener listener) {
        mListener = listener;
    }

    void updateData(List<AppItem> items) {
        mAppItems = items;
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mIconView.setImageDrawable(mAppItems.get(position).mDrawable);
        holder.mTitleView.setText(mAppItems.get(position).mTitle);
        holder.mCheckbox.setChecked(mAppItems.get(position).mChecked);
        holder.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    AppItem appItem = mAppItems.get(holder.getAdapterPosition());
                    appItem.toggleChecked();
                    mListener.onItemClick(appItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if(mAppItems == null) {
            return 0;
        }
        return mAppItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final View mItemView;
        final ImageView mIconView;
        final TextView mTitleView;
        final CheckBox mCheckbox;

        ViewHolder(View view) {
            super(view);
            mItemView = view;
            mIconView = (ImageView) view.findViewById(R.id.icon);
            mTitleView = (TextView) view.findViewById(R.id.title);
            mCheckbox = (CheckBox) view.findViewById(R.id.checkbox);
        }
    }

    interface OnItemClickListener {
        void onItemClick(AppItem item);
    }
}