/*
  Authors: S. Stefani
 */

package io.github.core55.joinup.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import io.github.core55.joinup.R;
import io.github.core55.joinup.activities.LoginActivity;

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
                        new PrimaryDrawerItem().withIdentifier(1).withName("Hello").withIcon(GoogleMaterial.Icon.gmd_wb_sunny),
                        new PrimaryDrawerItem().withIdentifier(2).withName("Goodbye").withIcon(GoogleMaterial.Icon.gmd_wb_cloudy),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withIdentifier(3).withName("Logout").withIcon(GoogleMaterial.Icon.gmd_exit_to_app)

                )
                .withOnDrawerItemClickListener(new com.mikepenz.materialdrawer.Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        if (position == 4) {
                            sharedPref.edit().remove("username").commit();
                            sharedPref.edit().remove("nickname").commit();

                            Intent intent = new Intent(activity, LoginActivity.class);
                            context.startActivity(intent);

                        }

                        return true;
                    }
                })
                .withSelectedItem(-1)
                .build();

        return result;
    }
}
