package io.github.core55.joinup.Helper;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by juanl on 15/05/2017.
 */

public  class IconTargetHelper {
    public static Bitmap iconProfile;

    public static Target getTarget(){
        return new Target(){

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                //This gets called when your application has the requested resource in the bitmap object
                iconProfile = bitmap;
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                //This gets called if the library failed to load the resource
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                //This gets called when the request is started
            }
        };
    }
}
