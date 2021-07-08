package com.example.adictic_admin.ui.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.adictic.common.entity.WebLink;
import com.example.adictic_admin.R;
import com.google.android.material.textfield.TextInputEditText;

public class SubmitWeblinkFragment extends DialogFragment {
    private TextInputEditText TIET_titol, TIET_url;

    private Button BT_accept, BT_cancel;

    private String name, url;

    private int posicio = -1;

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    public SubmitWeblinkFragment(WebLink wl, int pos) {
        name = wl.name;
        url = wl.url;
        posicio = pos;
    }

    public SubmitWeblinkFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.weblink_submit, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TIET_titol = (TextInputEditText) view.findViewById(R.id.TIET_title);
        TIET_url = (TextInputEditText) view.findViewById(R.id.TIET_url);

        if(posicio > -1){
            TIET_titol.setText(name);
            TIET_url.setText(url);
        }

        BT_accept = (Button) view.findViewById(R.id.BT_accept);
        BT_cancel = (Button) view.findViewById(R.id.BT_cancel);

        setButtons();

        getDialog().setTitle(getString(R.string.new_weblink));
    }

    public void setButtons() {
        BT_accept.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("name",TIET_titol.getText().toString());
            bundle.putString("url",TIET_url.getText().toString());
            bundle.putInt("posicio",posicio);

            Intent intent = new Intent().putExtras(bundle);

            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK,intent);

            dismiss();
        });

        BT_cancel.setOnClickListener(v -> dismiss());
    }
}
