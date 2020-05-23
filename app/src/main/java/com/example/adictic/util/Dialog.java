package com.example.adictic.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.ResultReceiver;

import com.example.adictic.entity.APIError;

// This class contains several static function to show standard dialogs that are used
// through the application
public class Dialog {

    // Show a standard error dialog
    public static void onError(String title, Activity a, APIError e, final ResultReceiver rr) {
        AlertDialog alertDialog = new AlertDialog.Builder(a).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(e.msg);

        // Setting OK Button
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                rr.send(0, null);
            }
        });
        alertDialog.show();
    }

}