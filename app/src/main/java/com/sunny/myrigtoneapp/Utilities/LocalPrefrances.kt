package com.sunny.myrigtoneapp.Utilities

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

class LocalPrefrances(context: Context) {
    private var appSharedPrefs: SharedPreferences? = null
    private var prefsEditor: SharedPreferences.Editor? = null


    private val Email = "EMAIL"
    private val Name = "NAME"
    private val Phone = "Phone"

    private val isUserVerified = "USER VERIFIED"

    private val isPhoneNumberVirified = "USER PHONE VERIFIED"


    private val isLoggedOut = "LOG OUT"

    private val FireStoreToken = "FIRESTORE TOKEN"

    private val FireAuthToken = "FIREAUTH TOKEN"

    var email: String?
        get() = appSharedPrefs!!.getString(Email, "")

        set(email) {
            prefsEditor!!.putString(Email, email).commit()
        }

    var name: String?
        get() = appSharedPrefs!!.getString(Name, "")
        set(name) {
            prefsEditor!!.putString(Name, name).commit()
        }

    var phone: String?
        get() = appSharedPrefs!!.getString(Phone, "")
        set(email) {
            prefsEditor!!.putString(Phone, email).commit()
        }

    var logout: Boolean
        get() = appSharedPrefs!!.getBoolean(isLoggedOut, true)
        set(logout) {

            prefsEditor!!.putBoolean(isLoggedOut, logout).commit()
        }

    var userVerified: Boolean
        get() = appSharedPrefs!!.getBoolean(isUserVerified, true)
        set(isuserVerified) {

            prefsEditor!!.putBoolean(isUserVerified, isuserVerified).commit()
        }

    val phoneNumberVerified: Boolean
        get() = appSharedPrefs!!.getBoolean(isLoggedOut, true)

    var fireStoreToken: String?
        get() = appSharedPrefs!!.getString(FireStoreToken, "")
        set(token) {
            prefsEditor!!.putString(FireStoreToken, token).commit()

        }

    var fireAuthToken: String?
        get() = appSharedPrefs!!.getString(FireAuthToken, "")
        set(token) {
            prefsEditor!!.putString(FireAuthToken, token).commit()

        }


    init {
        this.appSharedPrefs = context.getSharedPreferences(USER_PREFS, Activity.MODE_PRIVATE)
        this.prefsEditor = appSharedPrefs!!.edit()
    }


    fun reset(context: Context) {

        this.appSharedPrefs = context.getSharedPreferences(USER_PREFS, Activity.MODE_PRIVATE)
        this.prefsEditor = appSharedPrefs!!.edit().clear()
    }


    fun setPhoneNumberVirified(numberVirified: Boolean) {

        prefsEditor!!.putBoolean(isPhoneNumberVirified, numberVirified).commit()
    }

    companion object {
        private val USER_PREFS = "LOCKATED_PREFS"
    }

}