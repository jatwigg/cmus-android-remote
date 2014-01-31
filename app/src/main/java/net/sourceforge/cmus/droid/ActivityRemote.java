package net.sourceforge.cmus.droid;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * Created by josh on 31/01/14.
 */
public class ActivityRemote extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
    }

    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnsettings :
                ActivitySettings.Show(this);
                break;
            case R.id.btnmute :
                break;
            case R.id.btnvoldown :
                break;
            case R.id.btnvolup :
                break;
            case R.id.btnshuffle :
                break;
            case R.id.btnrepeat :
                break;
            case R.id.btnrepeatall :
                break;
            case R.id.btnback :
                break;
            case R.id.btnstop :
                break;
            case R.id.btnplay :
                break;
            case R.id.btnforward :
                break;
        }
    }
}
