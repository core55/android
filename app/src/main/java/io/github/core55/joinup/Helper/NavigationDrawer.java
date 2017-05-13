/*
  Authors: S. Stefani
 */

package io.github.core55.joinup.Helper;

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
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.List;

import io.github.core55.joinup.Entity.User;
import io.github.core55.joinup.Model.DataHolder;
import io.github.core55.joinup.R;
import io.github.core55.joinup.Activity.LoginActivity;
import io.github.core55.joinup.Activity.WelcomeActivity;

public class NavigationDrawer {

    public static Drawer buildDrawer(final Activity activity, Boolean withUsers) {
        final Context context = activity.getApplicationContext();
        final SharedPreferences sharedPref = activity.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        // Retrieve current user from shared preferences with default
        DataHolder store = DataHolder.getInstance();

        // Create profile header
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(R.drawable.side_nav_bar)
                .build();

        Drawer result = new DrawerBuilder()
                .withActivity(activity)
                .withAccountHeader(headerResult)
                .withOnDrawerItemClickListener(new com.mikepenz.materialdrawer.Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        if (drawerItem.getIdentifier() == 0) {
                            sharedPref.edit().remove(context.getString(R.string.auth_user)).commit();
                            sharedPref.edit().remove(context.getString(R.string.anonymous_user)).commit();
                            sharedPref.edit().remove(context.getString(R.string.jwt_string)).commit();
                            AuthenticationHelper.syncDataHolder(activity);

                            Intent intent = new Intent(activity, WelcomeActivity.class);
                            context.startActivity(intent);
                        } else if (drawerItem.getIdentifier() == 1) {
                            Intent intent = new Intent(activity, LoginActivity.class);
                            context.startActivity(intent);
                        }

                        return true;
                    }
                })
                .withSelectedItem(-1)
                .build();

        // If authenticated add profile item
        if (store.isAuthenticated() || store.isAnonymous()) {
            result.addItem(new PrimaryDrawerItem().withIdentifier(2).withName(store.getUser().getNickname()));
        }

        // Add directions item
        result.addItem(new PrimaryDrawerItem().withIdentifier(3).withName("Directions").withIcon(GoogleMaterial.Icon.gmd_directions));

        // If the drawer is in a meetup then add the list of users
        // Index starting from ten to facilitate assignments of listeners. Indexes 0 to 9 are
        // reserved for other items (logout, login, directions)
        if (withUsers && DataHolder.getInstance().getUserList() != null) {
            result.addItem(new DividerDrawerItem());
            List<User> users = DataHolder.getInstance().getUserList();

            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                result.addItem(new SecondaryDrawerItem().withName(user.getNickname()).withIdentifier(i + 10));
            }
        }

        // If the user is authenticated show logout button otherwise show login link
        if (store.isAuthenticated()) {
            result.addStickyFooterItem(new PrimaryDrawerItem().withIdentifier(0).withName("Logout").withIcon(GoogleMaterial.Icon.gmd_exit_to_app));
        } else {
            result.addStickyFooterItem(new PrimaryDrawerItem().withIdentifier(1).withName("Login").withIcon(GoogleMaterial.Icon.gmd_account_circle));
        }

        return result;
    }
}
