package com.sunny.myrigtoneapp.BottomView.home.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.sunny.myrigtoneapp.BottomView.home.Model.RingtoneModel
import com.sunny.myrigtoneapp.R
import com.sunny.myrigtoneapp.RecyclerInterface.IRecyclerItemClickListener

import java.util.ArrayList

class RingToneListAdapter(private val context: Context, private val ringtoneModelArrayList: ArrayList<RingtoneModel>?, private val iRecyclerItemClickListener: IRecyclerItemClickListener) : RecyclerView.Adapter<RingToneListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RingToneListAdapter.ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.rigtone_list_child, parent, false)

        return ViewHolder(view, iRecyclerItemClickListener)
    }

    override fun onBindViewHolder(holder: RingToneListAdapter.ViewHolder, position: Int) {

        if (ringtoneModelArrayList != null && ringtoneModelArrayList.size > 0) {

            holder.mRingtoneTitle.text = ringtoneModelArrayList[position].title


        }

    }

    override fun getItemCount(): Int {
        return ringtoneModelArrayList!!.size
    }


    inner class ViewHolder(itemView: View, internal var iRecyclerItemClickListener: IRecyclerItemClickListener) : RecyclerView.ViewHolder(itemView) {

        internal var mRingtoneTitle: TextView
        internal var mPLAYRingtone: ImageView

        private val mProgressBar: ProgressBar

        init {
            mRingtoneTitle = itemView.findViewById(R.id.mTXTRingtoneTitle)
            mPLAYRingtone = itemView.findViewById(R.id.mPLAYRingtone)
            mProgressBar = itemView.findViewById(R.id.mProgressBarView)
            mPLAYRingtone.setOnClickListener { v -> iRecyclerItemClickListener.onRecyclerItemClick(v, adapterPosition, mProgressBar) }
        }
    }
}
