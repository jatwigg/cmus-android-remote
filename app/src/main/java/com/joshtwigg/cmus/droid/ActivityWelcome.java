package com.joshtwigg.cmus.droid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by josh on 31/01/14.
 */
public class ActivityWelcome extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Storage.markWelcomeRead(this);
    }

    public void onClick(View view) {
        Storage.markWelcomeRead(this);
        finish();
    }

    public static void showIfFirstTime(Context context) {
        if (Storage.hasReadWelcome(context)) return;
        Intent intent = new Intent(context, ActivityWelcome.class);
        context.startActivity(intent);
    }
}
