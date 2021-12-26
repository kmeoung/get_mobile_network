package com.kmeoung.getnetwork.base

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

open class BaseActivity : AppCompatActivity() {


    /**
     * Fragment replace
     */
    fun replaceFragment(view: View, fragment: Fragment, addToBack: Boolean) {
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(view.id, fragment)

        if (addToBack) fm.addToBackStack(null)

        fm.commit()
    }
}