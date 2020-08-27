package com.sunny.myrigtoneapp.BottomView.home

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sunny.myrigtoneapp.BottomView.home.Adapter.RingToneListAdapter
import com.sunny.myrigtoneapp.BottomView.home.Model.RingtoneModel
import com.sunny.myrigtoneapp.Home.UploadeRingtone.UplodeRingtoneDialog
import com.sunny.myrigtoneapp.R
import com.sunny.myrigtoneapp.RecyclerInterface.IRecyclerItemClickListener

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.ArrayList

import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_uplode_ringtone_dialog.*
import kotlinx.android.synthetic.main.progress_bar_view.*


class HomeFragment : Fragment(), View.OnClickListener, IRecyclerItemClickListener {


    internal lateinit var player: MediaPlayer

    private val window: Window? = null

    internal lateinit var audioFileUri: Uri

    private var mCurrentAudioPath: String? = null

    internal var mHandler: Handler? = null

    internal var duration = 0

    internal var amoungToupdate = 0


    private var linearLayoutManager: LinearLayoutManager? = null

    internal var mAuth: FirebaseAuth ?= null
    internal var firebaseUser: FirebaseUser? = null
    internal var db: FirebaseFirestore ?= null
    internal var documentReference: DocumentReference? = null

    internal var mRingToneListAdapter: RingToneListAdapter? = null

    private var ringtoneModelArrayList: ArrayList<RingtoneModel>? = null

    internal var httpsReference: StorageReference ?= null
    internal var storage: FirebaseStorage ?= null


    private val TAG = HomeFragment::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)



        player = MediaPlayer()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            player.setAudioAttributes(AudioAttributes
                   .Builder()
                   .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                   .build())
        };

        db = FirebaseFirestore.getInstance()

        mAuth = FirebaseAuth.getInstance()

        firebaseUser = mAuth!!.currentUser

        ringtoneModelArrayList = ArrayList()

        storage = FirebaseStorage.getInstance()

       // val storageRef = storage!!.reference





        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager!!.orientation = RecyclerView.VERTICAL

        my_ringtone_list!!.layoutManager = linearLayoutManager

        mRingToneListAdapter = RingToneListAdapter(activity!!, ringtoneModelArrayList, this)

        my_ringtone_list!!.adapter = mRingToneListAdapter


        getAllRingtonesCollection()

        mUploadeFab!!.setOnClickListener(this)


    }
    private fun getAllRingtonesCollection() {

        try {

            if(mProgressBarView.visibility == GONE){

                mProgressBarView!!.visibility = VISIBLE

            }

            db!!.collection("Ringtone")
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (document in task.result!!) {

                                if(mProgressBarView.visibility == VISIBLE){

                                    mProgressBarView!!.visibility = GONE

                                }

                                val model = RingtoneModel(
                                        document.data["ringtone_category"]!!.toString(),
                                        document.data["ringtone_title"]!!.toString(),
                                        document.data["ringtone_child"]!!.toString())

                                ringtoneModelArrayList!!.add(model)


                            }

                            mRingToneListAdapter!!.notifyDataSetChanged()
                        } else {

                            Log.d(TAG, "Error getting documents: ", task.exception)
                        }
                    }

        } catch (e: Exception) {

            e.printStackTrace()

        }

    }


    override fun onDestroyView() {
        super.onDestroyView()

    }

    override fun onClick(v: View) {
        when (v.id) {

            R.id.mUploadeFab -> {


                val uploade = UplodeRingtoneDialog()

                uploade.show(childFragmentManager, "Show")
            }
        }
    }

    override fun onRecyclerItemClick(view: View, position: Int, mProgressBar: ProgressBar) {

        when (view.id) {

            R.id.mPLAYRingtone -> {



                if (!player.isPlaying) {

                    mCurrentAudioPath = ringtoneModelArrayList!![position].getmUri()

                    httpsReference = storage!!.getReference(mCurrentAudioPath!!)


                    if (mProgressBar.visibility == GONE) {
                        mProgressBar.visibility = VISIBLE

                    }
                    view.visibility = GONE

                    httpsReference!!.downloadUrl.addOnSuccessListener {

                        uri -> prepareRingtone(view, uri, mProgressBar)
                    }

                }else{

                    player.pause();

                }
            }
        }
    }

    private fun prepareRingtone(view: View, uri: Uri, mProgressBar: ProgressBar) {
        val imageView = view.findViewById<ImageView>(R.id.mPLAYRingtone)


        audioFileUri = uri

        try {
            player.setDataSource(activity!!, audioFileUri)




            player.setOnPreparedListener {
                if (mProgressBar.visibility == VISIBLE) {
                    mProgressBar.visibility = GONE

                }
                imageView.visibility = VISIBLE


                imageView.setImageResource(R.drawable.pause_audio_filled)

                player.start()
            }


            player.setOnCompletionListener { imageView.setImageResource(R.drawable.play_button_filled) }


            player.prepareAsync()

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
