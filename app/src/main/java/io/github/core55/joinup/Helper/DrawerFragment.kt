package io.github.core55.joinup.Helper

/**
 * Created by juanl on 15/05/2017.
 */

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.zsmb.materialdrawerkt.builders.accountHeader
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.builders.footer
import co.zsmb.materialdrawerkt.draweritems.badge
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.badgeable.secondaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import co.zsmb.materialdrawerkt.draweritems.expandable.expandableBadgeItem
import co.zsmb.materialdrawerkt.draweritems.profile.profile
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.Drawer
import com.squareup.picasso.Picasso
import io.github.core55.joinup.Activity.LoginActivity
import io.github.core55.joinup.Activity.MapActivity
import io.github.core55.joinup.Activity.RegisterActivity
import io.github.core55.joinup.Entity.User
import io.github.core55.joinup.Model.DataHolder
import io.github.core55.joinup.R
import kotlinx.android.synthetic.main.fragment_navdrawer.view.*

class DrawerFragment : Fragment() {

    private lateinit var result: Drawer
    private lateinit var headerResult: AccountHeader
    lateinit var store: DataHolder
    // Retrieve current user from shared preferences with default


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_navdrawer, container, false)


        result = drawer {

            savedInstance = savedInstanceState
            displayBelowStatusBar = true

            if (store.isAnonymous || store.isAuthenticated) {
                headerResult = accountHeader {
                    background = R.drawable.material_drawer_shadow_top
                    translucentStatusBar = true
                    compactStyle = false
                    savedInstance = savedInstanceState
                    profile(store.user.nickname) {
                        if(store.user.status != null)
                            email= store.user.status
                        //iconBitmap = profilePicture(store.user)
                        icon = R.drawable.emoji_4
                    }
                }
            }

            expandableBadgeItem("People") {
                iicon = GoogleMaterial.Icon.gmd_people
                identifier = 1000
                selectable = true


                if (store.userList != null) {
                    badge(store.userList.size.toString()) {
                        textColor = Color.WHITE.toLong()
                        colorRes = R.color.md_red_700
                    }

                    for (user in store.userList) {
                        try {
                            primaryItem(user.nickname, user.status) {
                                level = 2
                                selectable = true
                                icon = R.drawable.emoji_3 //Icon.createWithResource(context,R.drawable.emoji_3)//GoogleMaterial.Icon.gmd_people
                                identifier = user.id + 10
                                onClick { view, position, drawerItem ->
                                    var act: MapActivity = getActivity() as MapActivity
                                    act.centerMapOnMarker(user.id)
                                    false
                                }
                            }

                        }
                        catch (e : IllegalStateException ){
                            Log.e("pew","User nickname &/or status = null")
                        }


                    }
                }

            }
            primaryItem("Directions") {
                iicon = GoogleMaterial.Icon.gmd_directions
            }
            primaryItem("Settings & Privacy") {
                iicon = GoogleMaterial.Icon.gmd_settings
            }
            divider { }
            primaryItem("Leave Meetup") {
                iicon = GoogleMaterial.Icon.gmd_time_to_leave
            }
            footer {
                if (store.isAuthenticated) {
                    secondaryItem("Log out") {
                        iicon = GoogleMaterial.Icon.gmd_exit_to_app
                        onClick { _ ->
                            startActivity(Intent(context, LoginActivity::class.java))
                            false
                        }
                    }
                } else {
                    secondaryItem("Log in") {
                        iicon = GoogleMaterial.Icon.gmd_radio
                        onClick { _ ->
                            startActivity(Intent(context, LoginActivity::class.java))
                            false
                        }
                    }
                    secondaryItem("Register") {
                        iicon = GoogleMaterial.Icon.gmd_favorite
                        onClick { _ ->
                            startActivity(Intent(context, RegisterActivity::class.java))
                            false
                        }
                    }
                }

            }

        }


        view.title.text = null //arguments.getString(KEY_TITLE)

        result.drawerLayout.fitsSystemWindows = true
        result.slider.fitsSystemWindows = true

        return view
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        result.saveInstanceState(outState)
        headerResult.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    fun profilePicture(user: User): Bitmap {
        if (user.profilePicture == null) {
            Picasso.with(context).load(R.drawable.emoji_2).transform(CircleTransform()).into(IconTargetHelper.getTarget())
        } else {
            Picasso.with(context).load(user.profilePicture).transform(CircleTransform()).into(IconTargetHelper.getTarget())
        }
        return IconTargetHelper.iconProfile
    }


    companion object {
        private const val KEY_TITLE = "title"

        fun newInstance(title: String, dataHolder: DataHolder, activity: MapActivity) =
                DrawerFragment().apply {
                    val args = Bundle()
                    args.putString(KEY_TITLE, title)
                    arguments = args
                    store = dataHolder

                }

    }

}
