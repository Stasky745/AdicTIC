package com.example.adictic.ui.events;

import com.example.adictic.entity.HorarisEvents;
import com.example.adictic.roomdb.EventBlock;

public interface IEventDialog {
    void onSelectedData(EventBlock newEvent);
}
