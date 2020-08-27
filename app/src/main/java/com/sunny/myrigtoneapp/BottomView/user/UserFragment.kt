package com.sunny.myrigtoneapp.BottomView.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

import com.google.firebase.auth.FirebaseAuth
import com.sunny.myrigtoneapp.IndexActivity
import com.sunny.myrigtoneapp.R
import com.sunny.myrigtoneapp.Utilities.LocalPrefrances
import kotlinx.android.synthetic.main.fragment_user.*

class UserFragment : Fragment() {

    private var localPrefrances: LocalPrefrances? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? { // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        localPrefrances = LocalPrefrances(activity!!)
        mButtonSignOut!!.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            localPrefrances!!.reset(activity!!)
            startActivity(Intent(activity, IndexActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            activity!!.finish()
        }
    }

    override fun onDetach() {
        super.onDetach()
    }
}