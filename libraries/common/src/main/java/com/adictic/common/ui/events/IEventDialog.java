package com.adictic.common.ui.events;

import com.adictic.common.entity.EventBlock;

public interface IEventDialog {
    void onSelectedData(EventBlock newEvent, boolean delete);
}
