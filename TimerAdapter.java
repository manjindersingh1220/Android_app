package com.example.caipsgcms;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

public class TimerAdapter extends RecyclerView.Adapter<TimerAdapter.ViewHolder> {
    private Context mContext;
    private List<Timer> mTimerList;

    public TimerAdapter(Context mContext,List<Timer> mTimerList){
        this.mContext=mContext;
        this.mTimerList= mTimerList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.timerlayout,parent,false);
        return new TimerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(mTimerList.size()>0) {
            Log.d("timer lenght",String.valueOf(mTimerList.size()));
            final Timer timer = mTimerList.get(position);
            if(TextUtils.equals(timer.getTime(),"")){
                holder.textView.setVisibility(View.GONE);
            }
            else
            {
                holder.textView.setVisibility(View.VISIBLE);
            }
            holder.textView.setText(timer.getTime());


            Log.d("timeqq", timer.getTime());
            //Toast.makeText(mContext,timer.getTime(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return mTimerList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewTimer);
            //webView.requestFocus();
            //webView.getSettings().setJavaScriptEnabled(true);
           //webView.getSettings().setGeolocationEnabled(true);
        }
    }

}
