package io.github.core55.joinup.Helper

/**
 * Created by juanl on 15/05/2017.
 */

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.badgeable.secondaryItem
import co.zsmb.materialdrawerkt.draweritems.sectionHeader

import com.mikepenz.materialdrawer.Drawer
import io.github.core55.joinup.R
import kotlinx.android.synthetic.main.fragment_navdrawer.view.*

class DrawerFragment : Fragment() {

    lateinit var result: Drawer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_navdrawer, container, false)

        result = drawer {
            savedInstance = savedInstanceState
            //rootView = view.root
            displayBelowStatusBar = false

            primaryItem { "Hola"; "Adios" }
            primaryItem { "Hola"; "Adios" }
            primaryItem { "Hola"; "Adios" }
            sectionHeader { "Section header" }
            secondaryItem { "user1";"Pepe" }
            secondaryItem { "user1";"Pepe" }
            secondaryItem { "user1";"Pepe" }

        }

        view.title.text = arguments.getString(KEY_TITLE)

        result.drawerLayout.fitsSystemWindows = false
        result.slider.fitsSystemWindows = false

        return view
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        result.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val KEY_TITLE = "title"

        fun newInstance(title: String) =
                DrawerFragment().apply {
                    val args = Bundle()
                    args.putString(KEY_TITLE, title)
                    arguments = args
                }
    }

}
