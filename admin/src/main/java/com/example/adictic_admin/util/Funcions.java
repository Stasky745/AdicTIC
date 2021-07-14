package com.example.adictic_admin.util;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.adictic.common.util.Global;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.adictic_admin.BuildConfig;

public class Funcions extends com.adictic.common.util.Funcions {
    private final static String TAG = "Funcions";

    public static void setAdminPhoto(Context ctx, Long idAdmin, final ImageView d){
        if(idAdmin==-1)
            return;

        if(AdminApp.getAdminPic() != null) {
            d.setImageDrawable(AdminApp.getAdminPic());
            return;
        }

        String URL = Global.BASE_URL_RELEASE;
        if(BuildConfig.DEBUG)
            URL = Global.BASE_URL_DEBUG;

        Uri imageUri = Uri.parse(URL).buildUpon()
                .appendPath("admins")
                .appendPath("pictures")
                .appendPath(idAdmin.toString())
                .build();

        Glide.with(ctx)
                .load(imageUri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(d);

        AdminApp.setAdminPic(d.getDrawable());
    }


}
