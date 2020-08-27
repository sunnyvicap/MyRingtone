package com.sunny.myrigtoneapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation

class IndexActivity : AppCompatActivity() {


    var navCantroller : NavController ?= null;

     var doubleBackToExitPressedOnce :Boolean ? = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        navCantroller = Navigation.findNavController(this,R.id.mContainer);

        //navCantroller!!.addOnDestinationChangedListener(this)

    }

//    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
//
//        if(controller!!.currentDestination!!.id == R.id.loginFragment){
//
//
//        }
//    }

    override fun onBackPressed() {
        super.onBackPressed()

        if(navCantroller!!.currentDestination!!.id == R.id.loginFragment ){

            if(this!!.doubleBackToExitPressedOnce!!){

                finish()
                return
            }

            doubleBackToExitPressedOnce = true






        }
    }
}
