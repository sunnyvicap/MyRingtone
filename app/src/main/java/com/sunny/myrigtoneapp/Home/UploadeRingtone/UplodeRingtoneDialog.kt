package com.sunny.myrigtoneapp.Home.UploadeRingtone

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.loader.content.CursorLoader

import android.os.Handler
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.*

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.sunny.myrigtoneapp.R
import com.sunny.myrigtoneapp.Utilities.ConstantsRequestCodes

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.HashMap

import androidx.constraintlayout.widget.Constraints.TAG
import kotlinx.android.synthetic.main.fragment_uplode_ringtone_dialog.*
import kotlinx.android.synthetic.main.fragment_uplode_ringtone_dialog.view.*

import java.lang.Exception


class UplodeRingtoneDialog : DialogFragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    internal var player: MediaPlayer? = null

    private var window: Window? = null

    internal var audioFileUri: Uri? = null

    private var mCurrentAudioPath: String? = null

    internal lateinit var mHandler: Handler

    internal var duration = 0

    internal var amoungToupdate = 0L

    private val isMusicStarted = false

    internal var db: FirebaseFirestore? = null
    private var firebaseUser: FirebaseUser? = null
    private var mAuth: FirebaseAuth? = null

    private var uploadTask: UploadTask? = null
    private var storageReference: StorageReference? = null
    internal var storage: FirebaseStorage? = null
    private var metadata: StorageMetadata? = null
    private var mProgressDialog: ProgressDialog? = null

    private var mCategory: String? = null
    private var file: Uri? = null

    private val uploadedUri: Uri? = null

    internal lateinit var mRingtoneTitle: String

    private var mapRingtone: MutableMap<String, Any>? = null


    lateinit var customView: View;

    override fun onStart() {
        super.onStart()

        window = dialog!!.window

        assert(window != null)

        val windowParams = window!!.attributes
        windowParams.dimAmount = 0.75f
        windowParams.flags = windowParams.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window!!.attributes = windowParams

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        customView = activity!!.layoutInflater.inflate(R.layout.fragment_uplode_ringtone_dialog, LinearLayout(activity), false)

        init();

        val builder = Dialog(activity!!)
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
        builder.window!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.colorPrimaryDark)))

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(builder.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        builder.setCanceledOnTouchOutside(false)
        builder.show()
        builder.window!!.attributes = lp
        builder.setContentView(customView)

        //set to adjust screen height automatically, when soft keyboard appears on screen
        builder.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        return builder
    }


    private fun init() {


        player = MediaPlayer()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            player!!.setAudioAttributes(AudioAttributes
                    .Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
        }
        mHandler = Handler()
        mapRingtone = HashMap()

        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference
        db = FirebaseFirestore.getInstance()

        mAuth = FirebaseAuth.getInstance()

        firebaseUser = mAuth!!.currentUser


        mProgressDialog = ProgressDialog(activity)
        mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        mProgressDialog!!.setMessage("Uploading Ringtone, please wait.")
        mProgressDialog!!.max = 100


        customView.mENTR!!.setOnCheckedChangeListener(this)
        customView.mLove!!.setOnCheckedChangeListener(this)
        customView.mFUN!!.setOnClickListener(this)
        customView.mFabSelectRingtone!!.setOnClickListener(this)
        customView.mMusigcLogo!!.setOnClickListener(this)
        customView.mSubmit!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.mFabSelectRingtone ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                        requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), ConstantsRequestCodes.REQUEST_STORAGE)
                    else
                        selectRingtone()

                } else
                    selectRingtone()

            R.id.mMusigcLogo ->

                playRingTone()


            R.id.mSubmit ->

                uploadeRingtone()
        }
    }

    private fun uploadeRingtone() {


        mRingtoneTitle = mEditRingtoneTitle!!.text.toString()

        if (TextUtils.isEmpty(mCurrentAudioPath)) {

            Toast.makeText(activity, "Please Select Ringtone to uploade", Toast.LENGTH_SHORT).show()

        } else if (TextUtils.isEmpty(mRingtoneTitle)) {

            Toast.makeText(activity, "Please Enter Title", Toast.LENGTH_SHORT).show()

        } else if (TextUtils.isEmpty(mCategory)) {
            Toast.makeText(activity, "Please Select Ringtone category", Toast.LENGTH_SHORT).show()

        } else {
            file = Uri.fromFile(File(mCurrentAudioPath))

            mProgressDialog!!.show()
            mProgressDialog!!.setCanceledOnTouchOutside(false)
            mProgressDialog!!.setCancelable(false)

            metadata = StorageMetadata.Builder()
                    .setContentType("audio/mp3")
                    .build()

            uploadTask = storageReference!!.child(mCategory + "/" + file!!.lastPathSegment).putFile(file!!, metadata!!)


            uploadTask!!.addOnProgressListener { taskSnapshot ->
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                mProgressDialog!!.progress = progress.toInt()
                println("Upload is $progress% done")
            }.addOnPausedListener { println("Upload is paused") }.addOnFailureListener { exception -> exception.printStackTrace() }.addOnSuccessListener { taskSnapshot ->
                Log.d(" Uploade Sucessfull", "" + taskSnapshot.storage.downloadUrl)


                setRingtoneOnFireStore()
            }

        }


    }

    private fun setRingtoneOnFireStore() {


        mapRingtone!!["user_id"] = firebaseUser!!.uid
        mapRingtone!!["ringtone_title"] = mRingtoneTitle
        mapRingtone!!["ringtone_child"] = mCategory + "/" + file!!.lastPathSegment
        mapRingtone!!["ringtone_category"] = mCategory!!


        db!!.collection("Ringtone")
                .add(mapRingtone!!)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.id)
                    mProgressDialog!!.dismiss()
                    onDismiss(dialog!!)
                }.addOnFailureListener { e ->
                    mProgressDialog!!.dismiss()
                    Log.w(TAG, "Error adding document", e)
                }
    }

    private fun playRingTone() {

        if (player!!.isPlaying) {

            customView!!.mMusigcLogo!!.setImageResource(R.drawable.play_button_filled)


            player!!.pause()

        } else {
            customView!!.mMusigcLogo!!.setImageResource(R.drawable.pause_audio_filled)

            player!!.start()

            //            duration = player.getDuration();
            //            amoungToupdate = duration / 100;
            //
            //
            //            getActivity().runOnUiThread(new Runnable() {
            //
            //                @Override
            //                public void run() {
            //                    if (player != null) {
            //                        int p;
            //                        if (!(amoungToupdate * mPlayerProgress.getProgress() >= duration)) {
            //                            p = mPlayerProgress.getProgress();
            //                            p += 1;
            //
            //                            setProgressAnimate(mPlayerProgress, p);
            //                        }
            //                    }
            //                    mHandler.postDelayed(this, amoungToupdate);
            //                }
            //            });

            duration = player!!.duration;
            amoungToupdate = (duration / 100).toLong();

            activity!!.runOnUiThread(Runnable {


                mHandler.postDelayed(Runnable {


                    if (player != null) {
                        var p = 0;

                        if ((amoungToupdate * customView.mPlayerProgress!!.progress) >= duration) {

                            p = customView.mPlayerProgress!!.progress;

                            p += 1;

                            setProgressAnimate(customView.mPlayerProgress!!, p)
                        }


                    }


                }, amoungToupdate)


            })


        }
    }

    private fun setProgressAnimate(pb: ProgressBar, progressTo: Int) {
        val animation = ObjectAnimator.ofInt(pb, "progress", progressTo, progressTo)
        animation.duration = amoungToupdate.toLong()
        animation.interpolator = DecelerateInterpolator()
        animation.start()
    }

    private fun selectRingtone() {
        val audioIntent = Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(audioIntent, "Select Audio"), ConstantsRequestCodes.REQ_CODE_PICK_SOUNDFILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ConstantsRequestCodes.REQ_CODE_PICK_SOUNDFILE && resultCode == RESULT_OK) {

            if (data != null && data.data != null) {

                audioFileUri = data.data
                customView.mMusigcLogo!!.setImageResource(R.drawable.play_button_filled)

                mCurrentAudioPath = getAudioPath(audioFileUri!!)

                val mFileName = File(mCurrentAudioPath)
                customView.mRingtoneName!!.text = mFileName.name
                customView.mEditRingtoneTitle!!.setText(mFileName.name)

                Log.e("Music File Name", mCurrentAudioPath)

                prepareRingtone()


                // Now you can use that Uri to get the file path, or upload it, ...


            }
        }
    }


    private fun prepareRingtone() {

        try {
            player!!.setDataSource(FileInputStream(File(mCurrentAudioPath)).fd)


            player!!.setOnPreparedListener { customView!!.mMusigcLogo!!.setImageResource(R.drawable.play_button_filled) }


            player!!.setOnCompletionListener { customView!!.mMusigcLogo!!.setImageResource(R.drawable.play_button_filled) }


            player!!.prepareAsync()

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()


    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        if (player != null && player!!.isPlaying) {
            player!!.stop()
            player!!.release()
            player = null
        }
    }

    private fun getAudioPath(uri: Uri): String {
        val data = arrayOf(MediaStore.Audio.Media.DATA)
        val loader = CursorLoader(activity!!, uri, data, null, null, null)
        val cursor = loader.loadInBackground()

        val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)



        cursor.moveToFirst()

        return cursor.getString(columnIndex)


    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {

            R.id.mENTR -> {
                mCategory = "Entertainment"
                Log.e("Category", mCategory)
            }

            R.id.mFUN -> {
                mCategory = "Fun"
                Log.e("Category", mCategory)
            }

            R.id.mLove -> {
                mCategory = "Love"
                Log.e("Category", mCategory)
            }
        }
    }
}
