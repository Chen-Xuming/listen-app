package com.example.xiong.myapplication.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xiong.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends ArrayAdapter {

    private Context context;
    private List<String> titles;
    private int resourceId;
    private SearchView searchView;

    public HistoryAdapter(Context context, int resourceId, List<String> titles, SearchView searchView) {
        super(context, resourceId, titles);
        this.context=context;
        this.titles=titles;
        this.resourceId=resourceId;
        this.searchView=searchView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater mInflater= LayoutInflater.from(context);
            convertView = mInflater.inflate(R.layout.item_search_history, null);
            viewHolder.historyInfo =  convertView.findViewById(R.id.tv_item_search_history);
            viewHolder.delete = convertView.findViewById(R.id.iv_item_search_delete);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.historyInfo.setText(titles.get(position));

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchHistoryDB searchHistoryDB = new SearchHistoryDB(context, SearchHistoryDB.DB_NAME, null, 1);
                searchHistoryDB.deleteHistory(titles.get(position));
                titles.remove(position);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    @Override
    public int getCount() {
        return titles.size();
    }

    @Override
    public String getItem(int position) {
        return titles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder
    {
        TextView historyInfo;
        ImageView delete;
    }


    private List<String> mOriginalValues;

    @NonNull
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                titles = (List<String>) results.values; // 得到筛选后的列表结果
                notifyDataSetChanged();  // 刷新数据
            }


            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<String> filteredArrList = new ArrayList<String>();
                if (mOriginalValues == null) {

                    //保存一份未筛选前的完整数据
                    mOriginalValues = new ArrayList<String>(titles);
                }

                //如果接收到的文字为空，则不作比较，直接返回未筛选前的完整数据
                if (constraint == null || constraint.length() == 0) {
                    results.count = mOriginalValues.size();
                    results.values = mOriginalValues;
                } else {

                    //遍历原始数据，与接收到的文字作比较，得到筛选结果

                    for (String string : mOriginalValues) {
                        if (string.contains(constraint)) {
                            filteredArrList.add(string);
                        }
                    }

                    //返回得到的筛选列表
                    results.count = filteredArrList.size();
                    results.values = filteredArrList;
                }
                return results;
            }
        };
        return filter;
    }

}
