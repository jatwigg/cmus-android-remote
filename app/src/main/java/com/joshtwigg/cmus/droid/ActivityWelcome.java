package com.joshtwigg.cmus.droid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

/**
 * Created by josh on 31/01/14.
 */
public class ActivityWelcome extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome);
        ((TextView)findViewById(R.id.body)).setMovementMethod(LinkMovementMethod.getInstance()); // applies the html tags
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
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
    }
}
