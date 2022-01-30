package com.adictic.admin.util.hilt;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.ImageView;

import com.adictic.admin.BuildConfig;
import com.adictic.admin.rest.AdminApi;
import com.adictic.admin.util.AdminApp;
import com.adictic.common.dao.BlockedAppDao;
import com.adictic.common.dao.EventBlockDao;
import com.adictic.common.dao.HorarisNitDao;
import com.adictic.common.util.Global;
import com.adictic.common.util.hilt.Repository;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import javax.inject.Inject;

public class AdminRepository extends Repository {
    private final RequestManager glideRequestManager;
    private final AdminApi api;

    @Inject
    public AdminRepository(Application application, AdminApi api, BlockedAppDao blockedAppDao, EventBlockDao eventBlockDao, HorarisNitDao horarisNitDao, RequestManager glideRequestManager, SharedPreferences sharedPreferences) {
        super(application, api, blockedAppDao, eventBlockDao, horarisNitDao, glideRequestManager, sharedPreferences);

        this.glideRequestManager = glideRequestManager;
        this.api = api;
    }

    public AdminApi getApi() { return api; }

    public void setAdminPhoto(Long idAdmin, final ImageView d){
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

        glideRequestManager
                .load(imageUri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(d);

        AdminApp.setAdminPic(d.getDrawable());
    }
}
