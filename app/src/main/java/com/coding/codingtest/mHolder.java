package com.coding.codingtest;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coding.codingtest.R;

public class mHolder extends RecyclerView.ViewHolder {

    TextView country ;
    TextView currency ;
    TextView language;
    ImageView bombBtn;
    RelativeLayout viewBack;
    LinearLayout viewFront;

    public mHolder(View itemView) {
        super(itemView);

         country = (TextView) itemView.findViewById(R.id.country);
         currency = (TextView) itemView.findViewById(R.id.currency);
         language = (TextView) itemView.findViewById(R.id.lang);
         bombBtn=(ImageView) itemView.findViewById(R.id.bomb);
         viewBack=(RelativeLayout)itemView.findViewById(R.id.viewBack);
         viewFront=(LinearLayout)itemView.findViewById(R.id.viewFront);
    }
}
