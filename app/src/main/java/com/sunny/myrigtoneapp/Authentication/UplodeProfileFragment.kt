package com.sunny.myrigtoneapp.Authentication

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.sunny.myrigtoneapp.Home.HomeActivity
import com.sunny.myrigtoneapp.R
import com.sunny.myrigtoneapp.Utilities.FilePath
import com.sunny.myrigtoneapp.Utilities.ImagePicker
import com.sunny.myrigtoneapp.Utilities.LocalPrefrances

import java.io.File
import java.io.IOException
import java.util.HashMap
import java.util.Random

import android.app.Activity.RESULT_OK
import androidx.constraintlayout.widget.Constraints.TAG
import com.sunny.myrigtoneapp.Utilities.ConstantsRequestCodes.CHOOSE_IMAGE_REQUST
import com.sunny.myrigtoneapp.Utilities.ConstantsRequestCodes.REQUEST_CAMERA
import com.sunny.myrigtoneapp.Utilities.ConstantsRequestCodes.REQUEST_STORAGE
import com.sunny.myrigtoneapp.Utilities.ConstantsRequestCodes.REQUEST_TAKE_PHOTO
import com.sunny.myrigtoneapp.Utilities.ConstantsRequestCodes.RESULT_CROP
import kotlinx.android.synthetic.main.fragment_uplode_profile.*


class UplodeProfileFragment : Fragment(), View.OnClickListener {



    internal var isImageSet = false


    private var rand: Random? = null
    private var user: Map<String, Any>? = null

    private var db: FirebaseFirestore? = null
    private var firebaseUser: FirebaseUser? = null

    private var localPrefrances: LocalPrefrances? = null


    private var mProgressDialog: ProgressDialog? = null


    private var mAuth: FirebaseAuth? = null

    internal var documentReference: DocumentReference ? =null ;



    private var activity: Activity? = null

    private var alertDialog: AlertDialog? = null

    private var mCurrentPhotoPath: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_uplode_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mProgressDialog = ProgressDialog(activity)
        mProgressDialog!!.setMessage("Please Wait")

        localPrefrances = LocalPrefrances(activity!!)


        rand = Random()
        user = HashMap()
        mAuth = FirebaseAuth.getInstance()
        firebaseUser = mAuth!!.currentUser
        db = FirebaseFirestore.getInstance()

        documentReference = db!!.collection("user").document(localPrefrances!!.fireStoreToken!!)
        if (localPrefrances != null) {

            mTXTEMail!!.text = localPrefrances!!.email
            mTXTUserName!!.text = localPrefrances!!.name

        }

        profile_image!!.setOnClickListener(this)
        mButtonUploadeIn!!.setOnClickListener(this)
        mTxtSkip!!.setOnClickListener(this)



    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity = context as Activity

    }

    override fun onDetach() {
        super.onDetach()

    }


    override fun onClick(v: View) {
        when (v.id) {

            R.id.profile_image -> if (!isImageSet) {

                selectImage()

            } else {
                selectImageAction()
            }


            R.id.mButtonUploadeIn ->

                updateProfile()


            R.id.mTxtSkip -> {

                val intent = Intent(getActivity(), HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                getActivity()!!.finish()
            }
        }
    }

    private fun updateProfile() {
        try {


        mProgressDialog!!.show()


            if(!mCurrentPhotoPath.isNullOrEmpty()) {

                val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(localPrefrances!!.name)
                        .setPhotoUri(Uri.parse(mCurrentPhotoPath))
                        .build()


                firebaseUser!!.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                updateUi()

                                Log.d(TAG, "User profile updated.")
                            }
                        }

            }else{

                val intent = Intent(getActivity(), HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                getActivity()!!.finish()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    private fun updateUi() {

        mProgressDialog!!.dismiss()

        documentReference!!.update("PhotoPath", mCurrentPhotoPath)
                .addOnSuccessListener {
                    mProgressDialog!!.dismiss()
                    Log.d(TAG, "DocumentSnapshot successfully updated!")

                    val intent = Intent(getActivity(), HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    getActivity()!!.finish()
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }


    }

    private fun selectImageAction() {
        val options = arrayOf<CharSequence>("Change Image", "Remove Image")

        val builder = AlertDialog.Builder(activity!!, R.style.DialogeTheme)

        builder.setItems(options) { dialog, item ->
            if (options[item] == "Change Image") {
                selectImage()

            } else {

                profile_image!!.setImageResource(R.drawable.add_user_logo)
                isImageSet = false
            }
        }

        builder.show()
    }

    private fun selectImage() {

        val layoutInflater = LayoutInflater.from(activity)
        val alertview = layoutInflater.inflate(R.layout.attachment_dialog, null)

        val builder = AlertDialog.Builder(activity!!, R.style.DialogeTheme)
        builder.setView(alertview)
        val camera = alertview.findViewById<TextView>(R.id.openCamera)
        val gallery = alertview.findViewById<TextView>(R.id.openGallery)

        camera.setOnClickListener { checkCameraPermission() }
        gallery.setOnClickListener { checkStoragePermission() }

        alertDialog = builder.show()


    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_STORAGE)
            else
                onGalleryClicked()
        } else
            onGalleryClicked()
    }

    private fun onGalleryClicked() {
        alertDialog!!.dismiss()
        ImagePicker.pickImage(this, "Pic Image From Gallery", CHOOSE_IMAGE_REQUST, true, false)


    }

    private fun checkCameraPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CAMERA)
            else
                onCameraClicked()
        } else
            onCameraClicked()
    }

    private fun onCameraClicked() {

        alertDialog!!.dismiss()
        ImagePicker.pickImage(this, "Pic Image From Camera", REQUEST_TAKE_PHOTO, false, false)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try {
            if (resultCode == RESULT_OK) {

                if (requestCode == REQUEST_TAKE_PHOTO) {

                    mCurrentPhotoPath = ImagePicker.getImagePath( requestCode)


                    profile_image!!.setImageBitmap(ImagePicker.handleSamplingAndRotationBitmap(activity!!, FilePath.getUri(File(mCurrentPhotoPath))!!))

                    performCrop(mCurrentPhotoPath)

                    Log.e("PHoto path", mCurrentPhotoPath)
                } else if (requestCode == RESULT_CROP) {

                    val extras = data!!.extras
                    var selectedBitmap: Bitmap? = null

                    if (extras != null) {
                        selectedBitmap = extras.getParcelable("data")
                    }

                    profile_image!!.setImageBitmap(selectedBitmap)

                    isImageSet = true


                } else {

                    val uri = data!!.data

                    profile_image!!.setImageBitmap(ImagePicker.handleSamplingAndRotationBitmap(activity!!, uri!!))

                    mCurrentPhotoPath = FilePath.getPath(activity!!, uri)
                    performCrop(mCurrentPhotoPath)
                    Log.e("PHoto path", FilePath.getPath(activity!!, uri))
                }


            }

        } catch (e: IOException) {
            e.printStackTrace()
        }


    }


    private fun performCrop(picUri: String?) {
        try {
            //Start Crop Activity
            val cropIntent = Intent("com.lockated.camera.action.CROP")
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // indicate image type and Uri
            val f = File(picUri)
            //Uri contentUri = Uri.fromFile(f);
            val fileUri = FileProvider.getUriForFile(activity!!, activity!!.applicationContext.packageName + ImagePicker.AUTHORITY, f)

            /* cropIntent.setDataAndType(contentUri, "image*//*");*/
            cropIntent.setDataAndType(fileUri, "image/*")
            // set crop properties
            cropIntent.putExtra("crop", "true")
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1)
            cropIntent.putExtra("aspectY", 1)
            // indicate output X and Y
            cropIntent.putExtra("outputX", 280)
            cropIntent.putExtra("outputY", 280)
            // retrieve data on return
            cropIntent.putExtra("return-data", true)
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, RESULT_CROP)
        } catch (anfe: ActivityNotFoundException) {
            // display an error message
            val errorMessage = "your device doesn't support the crop action!"
            val toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT)
            toast.show()
        }
        // respond to users whose devices do not support the crop action

    }
}
