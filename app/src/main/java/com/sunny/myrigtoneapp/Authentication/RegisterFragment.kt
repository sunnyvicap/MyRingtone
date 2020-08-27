package com.sunny.myrigtoneapp.Authentication

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.sunny.myrigtoneapp.R
import com.sunny.myrigtoneapp.Utilities.LocalPrefrances
import kotlinx.android.synthetic.main.fragment_register.*
import java.util.*


class RegisterFragment : Fragment(), View.OnClickListener {


    private val TAG = RegisterFragment::class.java.simpleName

    private var activity: Activity? = null

    private var isPasswordVisible: Boolean = false

    private var rand: Random? = null
    private var user: MutableMap<String, Any>? = null

    private var db: FirebaseFirestore? = null
    private var firebaseUser: FirebaseUser? = null

    private var localPrefrances: LocalPrefrances? = null


    private var mAuth: FirebaseAuth? = null

    private var navController:NavController ? = null;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register, container, false)



        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        localPrefrances = LocalPrefrances(activity!!)

        navController = Navigation.findNavController(view);

        rand = Random()
        user = HashMap()
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        mShowPassword!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                isPasswordVisible = true
                mEditTextPassword!!.transformationMethod = HideReturnsTransformationMethod.getInstance()
                mEditTextPassword!!.setSelection(mEditTextPassword!!.text!!.length)

                mEditTextConfirmPassword!!.transformationMethod = HideReturnsTransformationMethod.getInstance()
                mEditTextConfirmPassword!!.setSelection(mEditTextConfirmPassword!!.text!!.length)


            } else {

                isPasswordVisible = false
                mEditTextPassword!!.transformationMethod = PasswordTransformationMethod.getInstance()
                mEditTextPassword!!.setSelection(mEditTextPassword!!.text!!.length)

                mEditTextConfirmPassword!!.transformationMethod = PasswordTransformationMethod.getInstance()
                mEditTextConfirmPassword!!.setSelection(mEditTextConfirmPassword!!.text!!.length)
            }
        }

        mButtonSignUp!!.setOnClickListener(this)
        mLoginLL!!.setOnClickListener(this)


    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity

    }

    override fun onDetach() {
        super.onDetach()

    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onClick(v: View) {
        when (v.id) {

            R.id.mLoginLL -> {

                navController!!.navigateUp()

            }

            R.id.mButtonSignUp ->

                registerUser()
        }
    }

    private fun registerUser() {

        try {


            val fullName = mEditUserName!!.text!!.toString().trim { it <= ' ' }
            val email = mEditTextEmail!!.text!!.toString().trim { it <= ' ' }
            val password = mEditTextPassword!!.text!!.toString().trim { it <= ' ' }
            val confirmPassword = mEditTextConfirmPassword!!.text!!.toString().trim { it <= ' ' }
            //val id = rand!!.nextInt(50) + 1

            if (TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mEditTextEmail!!.requestFocus()
                mEditTextEmail!!.error = "Invalid Email !"

            } else if (TextUtils.isEmpty(fullName)) {
                mEditUserName!!.requestFocus()
                mEditUserName!!.error = "Please Enter Your Name"

            } else if (TextUtils.isEmpty(password) && password.length >= 8) {
                mEditTextPassword!!.requestFocus()
                mEditTextPassword!!.error = "Password Should Be Miniumum 8 Characters"


            } else if (TextUtils.isEmpty(confirmPassword) && confirmPassword != password) {
                mEditTextConfirmPassword!!.requestFocus()
                mEditTextConfirmPassword!!.error = "Password Missmatch"

            } else {

                mRegisterProgressBar.visibility = View.VISIBLE;

                authenticateUser(email, password, fullName)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun authenticateUser(email: String, password: String, fullName: String) {

        mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        mRegisterProgressBar.visibility = View.GONE


                        firebaseUser = FirebaseAuth.getInstance().currentUser

                        assert(firebaseUser != null)

                        localPrefrances!!.fireAuthToken = firebaseUser!!.uid
                        addDataInFireStore(email, password, fullName)

                    } else {
                        // If sign in fails, display a message to the user.
                        mRegisterProgressBar.visibility = View.GONE

                        Log.w(TAG, "signInWithEmail:failure", task.exception)

                        Toast.makeText(getActivity(), "Authentication failed  Or User Already Exists",
                                Toast.LENGTH_SHORT).show()

                    }
                }
    }

    private fun addDataInFireStore(email: String, password: String, fullName: String) {

        user!!["id"] = firebaseUser!!.uid
        user!!["user_name"] = fullName
        user!!["email"] = email
        user!!["password"] = password
        user!!["phone"] = ""

        db!!.collection("user")
                .add(user!!)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.id)

                    mRegisterProgressBar.visibility = View.VISIBLE

                    localPrefrances!!.email = email
                    localPrefrances!!.name = fullName
                    localPrefrances!!.fireStoreToken = documentReference.id

                    updateUI()
                }.addOnFailureListener { e ->
                    mRegisterProgressBar.visibility = View.GONE
                    Log.w(TAG, "Error adding document", e)
                }

    }

    private fun updateUI() {

        navController!!.navigate(R.id.action_registerFragment_to_uplodeProfileFragment)

    }
}
