package io.github.core55.joinup.Helper;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lucasurbas.listitemview.ListItemView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.github.core55.joinup.Entity.User;
import io.github.core55.joinup.R;

/**
 * Created by juanl on 04/05/2017.
 */

public class UserAdapter extends ArrayAdapter<User> { //This class is the adapter for the user list in the navigation drawer view

    List userList;
    ArrayList<ListItemView> listItemViews = new ArrayList<ListItemView>();
    ListItemView listItemView;
    int[] profilePictures = {R.drawable.emoji_1, R.drawable.emoji_2, R.drawable.emoji_3, R.drawable.emoji_4, R.drawable.emoji_1, R.drawable.emoji_2};


    public UserAdapter(@NonNull Context context, @LayoutRes int resource, ArrayList userList) {
        super(context, 0, userList);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        User u = getItem(position);

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.listitem, null);
        }


        if (u != null) {
            TextView nicknameTV = (TextView) v.findViewById(R.id.nickname);
            TextView statusTV = (TextView) v.findViewById(R.id.status);
            ImageView profilePicIV = (ImageView) v.findViewById(R.id.profilePicture);

            if (nicknameTV != null && u.getNickname() != null) {
                nicknameTV.setText(u.getNickname());
            }

            if (statusTV != null && u.getStatus() != null) {
                statusTV.setText(u.getStatus());
            }
            if (profilePicIV != null) {
                if (u.getProfilePicture().equals("emoji")) {
                    profilePicIV.setImageResource(R.drawable.emoji_2);
                    Log.e("pew", "emoji");
                } else {
                    Picasso.with(getContext())
                            .load(u.getProfilePicture())
                            .transform(new CircleTransform())
                            .into(profilePicIV);
                }

            }

        }

        return v;
    }
}

