package com.sunny.myrigtoneapp.Home

import android.content.Intent
import android.os.Bundle

import com.bumptech.glide.Glide
import com.google.android.gms.common.internal.Asserts
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction


import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.sunny.myrigtoneapp.BottomView.favorite.FavoriteFragment
import com.sunny.myrigtoneapp.BottomView.home.HomeFragment
import com.sunny.myrigtoneapp.BottomView.user.UserFragment
import com.sunny.myrigtoneapp.IndexActivity
import com.sunny.myrigtoneapp.R
import com.sunny.myrigtoneapp.Utilities.LocalPrefrances

import androidx.drawerlayout.widget.DrawerLayout

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import android.view.Menu
import android.widget.ImageView
import android.widget.TextView

import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.nav_header_home.*
import kotlinx.android.synthetic.main.nav_header_home.view.*

class HomeActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {


    private var toggle: ActionBarDrawerToggle? = null



    internal var firebaseUser: FirebaseUser? = null
    internal var mAuth: FirebaseAuth? = null

    private var localPrefrances: LocalPrefrances? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        localPrefrances = LocalPrefrances(this)
        mAuth = FirebaseAuth.getInstance()
        firebaseUser = mAuth!!.currentUser


        setToolbar()

        init()

    }

    private fun init() {
        bottom_navigation!!.setOnNavigationItemSelectedListener(this)
        bottom_navigation!!.selectedItemId = R.id.action_home


        addHome_Fragment()
    }

    private fun addHome_Fragment() {

        val ft = supportFragmentManager.beginTransaction()

        val prev = supportFragmentManager.findFragmentById(R.id.mContainer)


        if (prev != null) {
            if (prev is HomeFragment)

                Log.e("Fragement Added", "NO")
            else {
                ft.remove(prev)
                ft.add(R.id.mContainer, HomeFragment()).commitAllowingStateLoss()
            }
        } else
            ft.add(R.id.mContainer, HomeFragment()).commitAllowingStateLoss()

    }


    private fun setToolbar() {

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setTitle(R.string.app_name)

        toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)


        toggle!!.toolbarNavigationClickListener = View.OnClickListener { drawer_layout!!.openDrawer(Gravity.LEFT) }

        toggle!!.syncState()

        val headerView: View = nav_view.getHeaderView(0)

        if (firebaseUser != null) {

            if (firebaseUser!!.photoUrl != null) {
                Glide.with(this).load(firebaseUser!!.photoUrl).into(headerView.mNavUserImage!!)
                Log.e("PHOTO", "" + firebaseUser!!.photoUrl!!)
            }

            headerView.mNavUserEmail!!.text = firebaseUser!!.email
            headerView.mNavUserName!!.text = firebaseUser!!.displayName


        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        return true
    }


    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {

        val sc = supportFragmentManager.findFragmentById(R.id.mContainer)


        when (menuItem.itemId) {


            R.id.action_home ->
                if (sc is HomeFragment) {

                    //          Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();

                } else {

                    val ft = supportFragmentManager.beginTransaction()

                    val prev = supportFragmentManager.findFragmentById(R.id.mContainer)

                    if (prev != null) {
                        ft.remove(prev)
                    }

                    ft.replace(R.id.mContainer, HomeFragment(), "HOME FRAGMENT").commitAllowingStateLoss()

                }


            R.id.action_favorite ->

                if (sc is FavoriteFragment) {
                    //          Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
                } else {
                    val ft = supportFragmentManager.beginTransaction()

                    val prev = supportFragmentManager.findFragmentById(R.id.mContainer)


                    if (prev != null) {
                        ft.remove(prev)
                    }

                    ft.replace(R.id.mContainer, FavoriteFragment(), "FAVORITE FRAGMENT").commitAllowingStateLoss()

                }

            R.id.action_user ->
                if (sc is FavoriteFragment) {
                    //          Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
                } else {
                    val ft = supportFragmentManager.beginTransaction()

                    val prev = supportFragmentManager.findFragmentById(R.id.mContainer)


                    if (prev != null) {
                        ft.remove(prev)
                    }

                    ft.replace(R.id.mContainer, UserFragment(), "USER FRAGMENT").commitAllowingStateLoss()
                }
        }

        return true
    }


}

