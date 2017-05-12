/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jikexueyuan.jikecalculator;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.javia.arity.SyntaxException;

import java.util.Vector;

class RecyclerHistoryAdapter extends RecyclerView.Adapter<RecyclerHistoryAdapter.ViewHolder> {
    private Vector<HistoryEntry> mEntries;
    private LayoutInflater mInflater;
    private Logic mEval;

    //保存History的实例到全局变量中
    private final History mHistory;

    RecyclerHistoryAdapter(Context context, History history, Logic evaluator) {
        mEntries = history.mEntries;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mEval = evaluator;
        mHistory = history;
    }


    // @Override
    public Object getItem(int position) {
        return mEntries.elementAt(position);
    }

    // @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RecyclerHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = mInflater.inflate(R.layout.history_item, viewGroup, false);
        return new RecyclerHistoryAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerHistoryAdapter.ViewHolder viewHolder, int i) {
        HistoryEntry entry = mEntries.elementAt(i);
        String base = entry.getBase();
        viewHolder.expr.setText(entry.getBase());

        try {
            String res = mEval.evaluate(base);
            viewHolder.result.setText("" + res); // remove "="
        } catch (SyntaxException e) {
            viewHolder.result.setText("");
        }

        if(getItemCount()==1){
            //只有一条记录，则选择单表盘，
            viewHolder.bg.setBackgroundResource(R.drawable.history_item_bg_single);
        }else if(i == getItemCount()-1){
            //如果多余1条记录，且最后一条记录，则将具有向上时间线的表盘作为背景。
            viewHolder.bg.setBackgroundResource(R.drawable.history_item_bg_bottom);
        }else if(i == 0){
            //如果多余1条记录，且当前是第一条记录，则将具有向下时间线的表盘作为背景
            viewHolder.bg.setBackgroundResource(R.drawable.history_item_bg_top);
        }else {
            //其他情况显示将具有向上和向下时间线的表盘，作为背景。
            viewHolder.bg.setBackgroundResource(R.drawable.history_item_bg_middle);
        }
    }


    @Override
    public int getItemCount() {
        return mEntries.size() - 1;
    }

    // @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.history_item, parent, false);
        } else {
            view = convertView;
        }

        //时间轴概念的背景。
        View bg = view.findViewById(R.id.bg_holder);
        TextView expr   = (TextView) view.findViewById(R.id.historyExpr);
        TextView result = (TextView) view.findViewById(R.id.historyResult);

        HistoryEntry entry = mEntries.elementAt(position);
        String base = entry.getBase();
        expr.setText(entry.getBase());

        try {
            String res = mEval.evaluate(base);
            result.setText("" + res); // remove "="
        } catch (SyntaxException e) {
            result.setText("");
        }

        if(getItemCount()==1){
            //只有一条记录，则选择单表盘，
            bg.setBackgroundResource(R.drawable.history_item_bg_single);
        }else if(position == getItemCount()-1){
            //如果多余1条记录，且最后一条记录，则将具有向上时间线的表盘作为背景。
            bg.setBackgroundResource(R.drawable.history_item_bg_bottom);
        }else if(position == 0){
            //如果多余1条记录，且当前是第一条记录，则将具有向下时间线的表盘作为背景
            bg.setBackgroundResource(R.drawable.history_item_bg_top);
        }else {
            //其他情况显示将具有向上和向下时间线的表盘，作为背景。
            bg.setBackgroundResource(R.drawable.history_item_bg_middle);
        }

        return view;
    }

    //滑动删除后撤销，需要用到。
    public void insert(int pos ,HistoryEntry he){
        mHistory.insert(pos, he);
    }

    //滑动删除，需要用到。
    public void remove(int pos){
        mHistory.remove(pos);
    }

    public void removeAll() {
        mHistory.clear();
    }

    //合计功能会用到
    public void addAll() {
        StringBuilder sb = new StringBuilder();
        for(HistoryEntry entry :mEntries){
            if(entry.getEdited().equals("")){
                break;
            }
            sb.append(entry.getEdited()).append("+");
        }
        String strToEvalute = sb.toString();
        while(strToEvalute.endsWith("+")){
            strToEvalute = strToEvalute.substring(0,sb.length()-1);
        }
        Log.d("TCL", "strToEvalute :" + strToEvalute);
        mEval.evaluateAndShowResult(strToEvalute, CalculatorDisplay.Scroll.UP);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{

        //时间轴概念的背景。
        View bg ;
        TextView expr ;
        TextView result;

        public ViewHolder(View itemView) {
            super(itemView);
            //时间轴概念的背景。
            bg = itemView.findViewById(R.id.bg_holder);
            expr   = (TextView) itemView.findViewById(R.id.historyExpr);
            result = (TextView) itemView.findViewById(R.id.historyResult);
        }
    }
}

