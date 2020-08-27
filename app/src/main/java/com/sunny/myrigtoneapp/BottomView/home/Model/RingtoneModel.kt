package com.sunny.myrigtoneapp.BottomView.home.Model

import android.net.Uri

class RingtoneModel(var category: String?, var title: String?, private var child: String?) {

    fun getmUri(): String? {
        return child
    }

    fun setmUri(child: String) {
        this.child = child
    }
}
