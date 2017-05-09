/*
  Authors: S. Stefani
 */

package io.github.core55.joinup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

public class NavigationDrawer {


    public static Drawer buildDrawer(final Activity activity) {
        final Context context = activity.getApplicationContext();

        // Retrieve current user from shared preferences
        final SharedPreferences sharedPref = activity.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String username = sharedPref.getString(context.getString(R.string.user_username), "Unknown User");
        String nickname = sharedPref.getString(context.getString(R.string.user_nickname), "Unknown User");

        // Create profile header
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
                        new PrimaryDrawerItem().withIdentifier(1).withName("Hello"),
                        new PrimaryDrawerItem().withIdentifier(2).withName("Goodbye"),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("Pew")
                )
                .withOnDrawerItemClickListener(new com.mikepenz.materialdrawer.Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D

                        if (position == 0) {
                            Log.d("PEW", "HEre");

                        } else if (position == 1){
                            Log.d("PEW", "1");

                            sharedPref.edit().remove("username").commit();
                            sharedPref.edit().remove("nickname").commit();

                            Intent intent = new Intent(activity, LoginActivity.class);
                            context.startActivity(intent);
                        } else if (position == 2){
                            Log.d("PEW", "2");
                        }

                        return true;
                    }
                })
                .withSelectedItem(-1)
                .build();

        return result;
    }
}
