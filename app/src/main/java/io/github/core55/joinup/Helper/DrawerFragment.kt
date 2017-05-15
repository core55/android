package io.github.core55.joinup.Helper

/**
 * Created by juanl on 15/05/2017.
 */

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.zsmb.materialdrawerkt.R.attr.background
import co.zsmb.materialdrawerkt.builders.accountHeader
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badge
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.badgeable.secondaryItem
import co.zsmb.materialdrawerkt.draweritems.expandable.expandableBadgeItem
import co.zsmb.materialdrawerkt.draweritems.profile.profile
import co.zsmb.materialdrawerkt.draweritems.profile.profileSetting
import co.zsmb.materialdrawerkt.draweritems.sectionHeader
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.Drawer
import com.squareup.picasso.Picasso
import io.github.core55.joinup.Entity.User
import io.github.core55.joinup.Model.DataHolder
import io.github.core55.joinup.R
import kotlinx.android.synthetic.main.fragment_navdrawer.view.*

class DrawerFragment : Fragment() {

    private lateinit var result: Drawer
    private lateinit var headerResult: AccountHeader
    lateinit var store : DataHolder
    // Retrieve current user from shared preferences with default



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.e("store",store.toString())
        Log.e("store_user",store.user.nickname)
        Log.e("store_userList",store.userList.toString())
        val view = inflater.inflate(R.layout.fragment_navdrawer, container, false)


        result = drawer {
            savedInstance = savedInstanceState
            displayBelowStatusBar = true

            if (store.isAuthenticated) {
                headerResult = accountHeader {
                    background = R.drawable.side_nav_bar
                    translucentStatusBar = true
                    compactStyle = true
                    savedInstance = savedInstanceState
                    profile(store.user.nickname, store.user.nickname) {
                        //iconBitmap = profilePicture(store.user)
                        icon = R.drawable.emoji_4
                    }
                    profileSetting("Log out") {
                        iicon = GoogleMaterial.Icon.gmd_exit_to_app
                        identifier = 100_001
                    }

                }

            }

            expandableBadgeItem("People") {
                iicon = GoogleMaterial.Icon.gmd_people
                identifier = 1000
                selectable = true
                badge(store.userList.size.toString()) {
                    textColor = Color.WHITE.toLong()
                    colorRes = R.color.md_red_700
                }
                for (user in store.userList){
                    primaryItem(user.nickname,user.status) {
                        level = 2
                        selectable = true
                        iicon = GoogleMaterial.Icon.gmd_people
                        //ico =  "https://www.gravatar.com/avatar/576e391f3f68e7597fa7be5435ca5e73" //profilePicture(user)
                        identifier = user.id + 10
                    }

                }

            }
            primaryItem("Directions"){
                iicon = GoogleMaterial.Icon.gmd_directions
            }
            primaryItem("Settings & Privacy") {
                iicon = GoogleMaterial.Icon.gmd_settings
            }

        }

        view.title.text = arguments.getString(KEY_TITLE)

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
        if (user.profilePicture == null){
            Picasso.with(context).load(R.drawable.emoji_2).transform(CircleTransform()).into(IconTargetHelper.getTarget())
        } else {
            Picasso.with(context).load(user.profilePicture).transform(CircleTransform()).into(IconTargetHelper.getTarget())
        }
        return IconTargetHelper.iconProfile
    }

    companion object {
        private const val KEY_TITLE = "title"

        fun newInstance(title: String, dataHolder: DataHolder) =
                DrawerFragment().apply {
                    val args = Bundle()
                    args.putString(KEY_TITLE, title)
                    arguments = args
                    store = dataHolder

                }

    }

}
