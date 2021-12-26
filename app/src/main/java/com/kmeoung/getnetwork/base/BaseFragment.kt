package com.kmeoung.getnetwork.base

import android.view.View
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {

    /**
     * Fragment replace
     */
    fun replaceFragment(view: View, fragment: Fragment, addToBack: Boolean) {
        val fm = parentFragmentManager
        val ft = fm.beginTransaction()

        ft.add(view.id, fragment)

        if (addToBack) ft.addToBackStack(null)

        ft.commit()

    }

}