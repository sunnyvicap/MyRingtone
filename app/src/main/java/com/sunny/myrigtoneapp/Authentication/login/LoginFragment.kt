package com.sunny.myrigtoneapp.Authentication.login

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import androidx.fragment.app.Fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.sunny.myrigtoneapp.Authentication.RegisterFragment
import com.sunny.myrigtoneapp.Home.HomeActivity
import com.sunny.myrigtoneapp.Utilities.toast;
import com.sunny.myrigtoneapp.R
import com.sunny.myrigtoneapp.Utilities.LocalPrefrances
import kotlinx.android.synthetic.main.fragment_login.*
import java.lang.NullPointerException
import java.util.zip.Inflater


class LoginFragment : Fragment(), View.OnClickListener {

    private var activity: Activity? = null

    private val TAG = LoginFragment::class.java.simpleName


    private var isPasswordVisible: Boolean = false


    private var mAuth: FirebaseAuth? = null

    private var mLocalPrefrances: LocalPrefrances? = null


    private var navCantroller: NavController ? = null







    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        return inflater.inflate(R.layout.fragment_login,container, false);

    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity = context as Activity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        mLocalPrefrances = LocalPrefrances(activity!!)

        mAuth = FirebaseAuth.getInstance()

        navCantroller = Navigation.findNavController(view)

        mShowPasswordLogin!!.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {
                isPasswordVisible = true
                mEditTextPassword!!.transformationMethod = HideReturnsTransformationMethod.getInstance()
                mEditTextPassword!!.setSelection(mEditTextPassword!!.text!!.length)


            } else {

                isPasswordVisible = false
                mEditTextPassword!!.transformationMethod = PasswordTransformationMethod.getInstance()
                mEditTextPassword!!.setSelection(mEditTextPassword!!.text!!.length)

            }
        }


        mButtonSignIn!!.setOnClickListener(this)
        mRegisterLL!!.setOnClickListener(this)

    }


    override fun onClick(v: View) {
        when (v.id) {

            R.id.mRegisterLL -> {

               navCantroller!!.navigate(R.id.action_loginFragment_to_registerFragment)
            }

            R.id.mButtonSignIn ->

                loginUser()
        }
    }


    private fun loginUser() {

        try {



            val email = mEditTextEmail!!.text!!.toString().trim { it <= ' ' }
            val password = mEditTextPassword!!.text!!.toString().trim { it <= ' ' }




            if (email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                mEditInputFieldEmail!!.requestFocus()
                mEditInputFieldEmail!!.error = "Invalid Email  !"

            } else if (password.isEmpty() && password.length < 8) {

                mEditInputFieldPassword!!.requestFocus()
                mEditInputFieldPassword!!.error = "Password Should Be Miniumum 8 Characters"

            } else {

                mLoginProgressBar.visibility = View.VISIBLE;

                mAuth!!.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                mLoginProgressBar.visibility = View.GONE;

                                mLocalPrefrances!!.logout = false
                                val intent = Intent(activity!!, HomeActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)
                                activity!!.finish()

                            }
                        }.addOnFailureListener {

                            mLoginProgressBar.visibility = View.GONE;
                            activity!!.toast("Authentication failed ");

                        }


            }
        } catch (e: Exception) {
            e.printStackTrace()

           activity!!.toast("Something Went Wrong")

            mLoginProgressBar.visibility = View.GONE;
        }

    }


}


