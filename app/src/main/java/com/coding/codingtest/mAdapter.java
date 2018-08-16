package com.coding.codingtest;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class mAdapter extends RecyclerView.Adapter<mHolder>{

    Context context;
    ArrayList<CountryData> cdataList;

    public mAdapter(Activity context, ArrayList<CountryData> cdataList) {
        this.context = context;
        this.cdataList=cdataList;

    }

    @Override
    public mHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row,parent,false);
        mHolder holder=new mHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull mHolder holder, final int position) {
        holder.country.setText(cdataList.get(position).getName());
        holder.currency.setText(cdataList.get(position).getCurrency());
        holder.language.setText(cdataList.get(position).getLanguage());
    }

    @Override
    public int getItemCount() {
        return cdataList.size();
    }

    public void dismissRow(int pos){
        cdataList.remove(pos);
        this.notifyItemRemoved(pos);
    }
}
