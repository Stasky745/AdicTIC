package com.adictic.client.ui.setting.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adictic.client.R;
import com.adictic.client.entity.NotificationInformation;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {
    private final static String ARG_LIST = "arg_list";
    private View root;

    private ArrayList<NotificationInformation> notifList;

    public NotificationFragment() { }

    public static NotificationFragment newInstance(List<NotificationInformation> list) {
        NotificationFragment notificationFragment = new NotificationFragment();

        Bundle args = new Bundle(1);
        args.putParcelableArrayList(ARG_LIST, new ArrayList<>(list));
        notificationFragment.setArguments(args);

        return notificationFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.notif_list_layout, container, false);

        assert getArguments() != null;
        notifList = getArguments().getParcelableArrayList(ARG_LIST);

        setRecyclerView(root);

        return root;
    }

    private void setRecyclerView(View root) {
        RecyclerView RV_notif_list = root.findViewById(R.id.RV_notif_list);
        NotificationRVadapter adapter = new NotificationRVadapter(requireContext(), notifList);

        RV_notif_list.setLayoutManager(new LinearLayoutManager(requireContext()));
        RV_notif_list.setAdapter(adapter);

        //https://www.geeksforgeeks.org/swipe-to-delete-and-undo-in-android-recyclerview/
        // on below line we are creating a method to create item touch helper
        // method for adding swipe to delete functionality.
        // in this we are specifying drag direction and position to right
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                // this method is called
                // when the item is moved.
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // below line is to get the position
                // of the item at that position.
                int position = viewHolder.getBindingAdapterPosition();

                // this method is called when we swipe our item to right direction.
                // on below line we are getting the item at a particular position.
                NotificationInformation deletedNotif = notifList.get(position);

                // this method is called when item is swiped.
                // below line is to remove item from our array list.
                notifList.remove(position);

                // below line is to notify our item is removed from adapter.
                adapter.deleteItem(position);

                // below line is to display our snackbar with action.
                Snackbar.make(RV_notif_list, deletedNotif.title, Snackbar.LENGTH_LONG).setAction(requireContext().getString(R.string.undo), v -> {
                    // adding on click listener to our action of snack bar.
                    // below line is to add our item to array list with a position.
                    notifList.add(position, deletedNotif);

                    // below line is to notify item is
                    // added to our adapter class.
                    adapter.insertItem(position);
                }).show();
            }
            // at last we are adding this
            // to our recycler view.
        }).attachToRecyclerView(RV_notif_list);
    }
}
