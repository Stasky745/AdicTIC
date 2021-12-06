package com.adictic.client.ui.setting.notifications;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.adictic.client.R;
import com.adictic.client.entity.NotificationInformation;
import com.adictic.client.util.Funcions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    private TabLayout TL_notif_tabs;
    private ViewPager2 VP_notif_content;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notif_tab_layout);

        TL_notif_tabs = findViewById(R.id.TL_notif_tabs);
        VP_notif_content = findViewById(R.id.VP_notif_content);
        TextView TV_notif_empty = findViewById(R.id.TV_notif_empty);

        ArrayList<NotificationInformation> notifList = Funcions.getNotificationList(NotificationActivity.this);

        if(notifList == null || notifList.isEmpty()){
            TL_notif_tabs.setVisibility(View.GONE);
            VP_notif_content.setVisibility(View.GONE);
            TV_notif_empty.setVisibility(View.VISIBLE);
        }
        else {
            TL_notif_tabs.setVisibility(View.VISIBLE);
            VP_notif_content.setVisibility(View.VISIBLE);
            TV_notif_empty.setVisibility(View.GONE);

            createAdapter(notifList);
        }
    }

    private void createAdapter(ArrayList<NotificationInformation> notifList) {
        NotificationAdapter adapter = new NotificationAdapter(getSupportFragmentManager(), getLifecycle(), NotificationActivity.this, notifList);

        VP_notif_content.setAdapter(adapter);

        new TabLayoutMediator(TL_notif_tabs, VP_notif_content,
                (tab, position) -> tab.setText(adapter.getPageTitle(position))
        ).attach();
    }
}
