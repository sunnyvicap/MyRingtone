package com.sunny.myrigtoneapp.splash

import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.sunny.myrigtoneapp.Home.HomeActivity
import com.sunny.myrigtoneapp.IndexActivity
import com.sunny.myrigtoneapp.R
import com.sunny.myrigtoneapp.Utilities.LocalPrefrances

import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {


    private var user: FirebaseUser? = null

    private var mAuth: FirebaseAuth? = null

    private lateinit var rotate: Animation

    private var mLocalPrefrances: LocalPrefrances? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        mLocalPrefrances = LocalPrefrances(this)
        rotate = AnimationUtils.loadAnimation(this, R.anim.rotate)

        mSplashLOGO!!.startAnimation(rotate)

        mAuth = FirebaseAuth.getInstance()


        user = mAuth!!.currentUser

        rotate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onAnimationStart(animation: Animation?) {

                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }


            override fun onAnimationEnd(animation: Animation) {

                if (user != null && !mLocalPrefrances!!.logout) {

                    startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(this@SplashActivity, IndexActivity::class.java))
                    finish()
                }


            }


        })


    }
}
