package io.github.core55.joinup;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

/**
 * Created by simone on 2017-05-09.
 */

public class NavigationDrawer {


    public static Drawer buildDrawer(Activity activity, Context context) {
        SharedPreferences sharedPref;


        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("Hello");
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withName("goodbye");

        // Retrieve current user from shared preferences
        sharedPref = activity.getSharedPreferences("io.github.core55.joinup.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        String username = sharedPref.getString("USER_USERNAME", "Unknown User");
        String nickname = sharedPref.getString("USER_NICKNAME", "Unknown User");

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(R.drawable.side_nav_bar)
                .addProfiles(
                        new ProfileDrawerItem().withName(nickname).withEmail(username).withIcon(context.getResources().getDrawable(R.drawable.drawer_pic_placeholder))
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();


        Drawer result = new DrawerBuilder()
                .withActivity(activity)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        new SecondaryDrawerItem().withName("Pew")
                )
                .withOnDrawerItemClickListener(new com.mikepenz.materialdrawer.Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D

                        if (position == 0) {

                        }
                        Log.d("PEW", "HEre");
                        return true;
                    }
                })
                .build();

        return result;
    }
}
