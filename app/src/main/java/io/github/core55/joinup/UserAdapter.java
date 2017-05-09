package io.github.core55.joinup;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
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

/**
 * Created by juanl on 04/05/2017.
 */

public class UserAdapter extends ArrayAdapter<User> { //This class is the adapter for the user list in the navigation drawer view

    List userList;
    ArrayList<ListItemView> listItemViews = new ArrayList<ListItemView>();
    ListItemView listItemView;
    int [] profilePictures = {R.drawable.emoji_1,R.drawable.emoji_2,R.drawable.emoji_3,R.drawable.emoji_4,R.drawable.emoji_1,R.drawable.emoji_2};


    public UserAdapter(@NonNull Context context, @LayoutRes int resource, ArrayList userList) {
        super(context,0, userList);
        Log.d("userList",userList.toString());
        //Log.d("contexts","this.context:  "+  this.getContext() + "comingContext:  " + context);
        //Activity a = (Activity) userList.get(userList.size()-1);
        //listItemView = (ListItemView) a.findViewById(R.id.user_item); //new ListItemView(this.getContext());


        /*this.userList = userList;
        for (User user : userList){

        }*/


    }

/*
    @Override
    public int getCount() {
        return userList.size();
    }*/

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

            if (nicknameTV != null) {
                nicknameTV.setText(u.getNickname());
            }

            if (statusTV != null) {
                statusTV.setText(u.getStatus());
            }
            if (profilePicIV != null){
                profilePicIV.setImageResource(u.getProfilePicture());
            }

        }

        return v;

        /*listItemView = new ListItemView(this.getContext());

        listItemView.setTitle(u.getNickname());
        listItemView.setSubtitle(u.getStatus());
        //listItemView.setIconResId(-1);
        listItemView.findViewById(R.id.listitem);
        //Picasso.with(listItemView.getContext()).load(profilePictures[position]).transform(new CircleTransform()).into(listItemView.getAvatarView());
        listItemViews.add(listItemView);
        Log.d("getViewcalled",listItemView.toString());
        return listItemView;*/
    }
}


