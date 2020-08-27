package com.sunny.myrigtoneapp.RecyclerInterface;

import android.view.View;
import android.widget.ProgressBar;

public interface IRecyclerItemClickListener
{
    void onRecyclerItemClick(View view, int position, ProgressBar progressBar);
}
