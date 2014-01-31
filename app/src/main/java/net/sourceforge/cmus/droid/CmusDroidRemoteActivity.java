//package net.sourceforge.cmus.droid;
//
//import java.util.ArrayList;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.ArrayAdapter;
//import android.widget.AutoCompleteTextView;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Spinner;
//
///**
// * This code is so great, it hurts!!
// * Please clean me!!
// *
// * @author bboudreau
// *
// */
//public class CmusDroidRemoteActivity extends Activity {
//
//    public static final String TAG = "CmusDroidRemoteActivity";
//
//	private AutoCompleteTextView mHostText;
//	private EditText mPortText;
//	private EditText mPasswordText;
//	private Spinner mCommandSpinner;
//	private Button mSendCommandButton;
//	ArrayAdapter<String> hostAdapter;
//
//	/** Called when the activity is first created. */
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.main);
//
//		// Obtain handles to UI objects
//		mHostText = (AutoCompleteTextView) findViewById(R.id.hostText);
//		mPortText = (EditText) findViewById(R.id.portText);
//		mPasswordText = (EditText) findViewById(R.id.passwordText);
//		mCommandSpinner = (Spinner) findViewById(R.id.commandSpinner);
//		mSendCommandButton = (Button) findViewById(R.id.sendCommandButton);
//
//		mPortText.setText("3000");
//
//		hostAdapter = new ArrayAdapter<String>(this,
//				android.R.layout.simple_dropdown_item_1line,
//				new ArrayList<String>());
//
//		mHostText.setAdapter(hostAdapter);
//
//		//runSearchHosts();
//
//		mCommandSpinner.setAdapter(new ArrayAdapter<CmusCommand>(this,
//				android.R.layout.simple_spinner_item, CmusCommand.values()));
//
//		mSendCommandButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				onSendCommandClicked();
//			}
//		});
//	}
//
//	private void onSendCommandClicked() {
//		Log.v(TAG, "Save button clicked");
//		if (validate()) {
//			if (1==1){//isUsingWifi()) {
//				sendCommand(mHostText.getText().toString(),
//						Integer.parseInt(mPortText.getText().toString()),
//						mPasswordText.getText().toString(),
//						(CmusCommand) mCommandSpinner.getSelectedItem());
//			} else {
//				alert("Could not send command", "Not sending command: not on Wifi.");
//			}
//		}
//		// finish();
//	}
//
//	private void alert(String title, String message) {
//		Log.v(TAG, message);
//		new AlertDialog.Builder(this)
//				.setMessage(message)
//				.setTitle(title).show();
//	}
//
//	private boolean validate() {
//		boolean valid = true;
//
//		if (!Util.validateString(mHostText.getText().toString())) {
//			valid = false;
//			mHostText.setError("the hostname is not valid");
//		} else {
//			mHostText.setError(null);
//		}
//
//		if (!Util.validateInteger(mPortText.getText().toString())) {
//			valid = false;
//			mPortText.setError("the port is not valid");
//		} else {
//			mPortText.setError(null);
//		}
//
//		if (!Util.validateString(mPasswordText.getText().toString())) {
//			valid = false;
//			mPasswordText.setError("the password is not valid");
//		} else {
//			mPasswordText.setError(null);
//		}
//
//		if (!valid) {
//			alert("Could not send command", "Not sending command, some parameters are invalid.");
//		}
//
//		return valid;
//	}
//
//}