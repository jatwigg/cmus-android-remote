package net.sourceforge.cmus.droid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * This code is so great, it hurts!!
 * Please clean me!!
 * 
 * @author bboudreau
 * 
 */
public class CmusDroidRemoteActivity extends Activity {
	public enum CmusCommand {
		REPEAT("Repeat", "toggle repeat"),
		SHUFFLE("Shuffle", "toggle shuffle"),
		STOP("Stop", "player-stop"),
		NEXT("Next", "player-next"),
		PREV("Previous", "player-prev"),
		PLAY("Play", "player-play"),
		PAUSE("Pause", "player-pause"),
		// FILE("player-play %s");
		// VOLUME("vol %s"),
		VOLUME_MUTE("Mute", "vol -100%"),
		VOLUME_UP("Volume +", "vol +10%"),
		VOLUME_DOWN("Volume -", "vol -10%"),
		// SEEK("seek %s"),
		STATUS("Status", "status");

		private final String label;
		private final String command;

		private CmusCommand(String label, String command) {
			this.label = label;
			this.command = command;
		}

		public String getCommand() {
			return command;
		}

		public String getLabel() {
			return label;
		}

		@Override
		public String toString() {
			return getLabel();
		}
	}

    public static final String TAG = "CmusDroidRemoteActivity";

	private AutoCompleteTextView mHostText;
	private EditText mPortText;
	private EditText mPasswordText;
	private Spinner mCommandSpinner;
	private Button mSendCommandButton;
	ArrayAdapter<String> hostAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Obtain handles to UI objects
		mHostText = (AutoCompleteTextView) findViewById(R.id.hostText);
		mPortText = (EditText) findViewById(R.id.portText);
		mPasswordText = (EditText) findViewById(R.id.passwordText);
		mCommandSpinner = (Spinner) findViewById(R.id.commandSpinner);
		mSendCommandButton = (Button) findViewById(R.id.sendCommandButton);

		mPortText.setText("3000");

		hostAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line,
				new ArrayList<String>());

		mHostText.setAdapter(hostAdapter);

		//runSearchHosts();

		mCommandSpinner.setAdapter(new ArrayAdapter<CmusCommand>(this,
				android.R.layout.simple_spinner_item, CmusCommand.values()));

		mSendCommandButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onSendCommandClicked();
			}
		});
	}

	private void onSendCommandClicked() {
		Log.v(TAG, "Save button clicked");
		if (validate()) {
			if (1==1){//isUsingWifi()) {
				sendCommand(mHostText.getText().toString(),
						Integer.parseInt(mPortText.getText().toString()),
						mPasswordText.getText().toString(),
						(CmusCommand) mCommandSpinner.getSelectedItem());
			} else {
				alert("Could not send command", "Not sending command: not on Wifi.");
			}
		}
		// finish();
	}

	private void alert(String title, String message) {
		Log.v(TAG, message);
		new AlertDialog.Builder(this)
				.setMessage(message)
				.setTitle(title).show();
	}

	private boolean validate() {
		boolean valid = true;

		if (!Util.validateString(mHostText.getText().toString())) {
			valid = false;
			mHostText.setError("the hostname is not valid");
		} else {
			mHostText.setError(null);
		}

		if (!Util.validateInteger(mPortText.getText().toString())) {
			valid = false;
			mPortText.setError("the port is not valid");
		} else {
			mPortText.setError(null);
		}

		if (!Util.validateString(mPasswordText.getText().toString())) {
			valid = false;
			mPasswordText.setError("the password is not valid");
		} else {
			mPasswordText.setError(null);
		}

		if (!valid) {
			alert("Could not send command", "Not sending command, some parameters are invalid.");
		}

		return valid;
	}

	private void addTagOrSetting(CmusStatus cmusStatus, String line) {
		int firstSpace = line.indexOf(' ');
		int secondSpace = line.indexOf(' ', firstSpace + 1);
		String type = line.substring(0, firstSpace);
		String key = line.substring(firstSpace + 1, secondSpace);
		String value = line.substring(secondSpace + 1);
		if (type.equals("set")) {
			cmusStatus.setSetting(key, value);
		} else if (type.equals("tag")) {
			cmusStatus.setTag(key, value);
		} else {
			Log.e(TAG, "Unknown type in status: " + line);
		}
	}

	private void handleStatus(String status) {

		CmusStatus cmusStatus = new CmusStatus();

		String[] strs = status.split("\n");

		for (String str : strs) {
			if (str.startsWith("set") || str.startsWith("tag")) {
				addTagOrSetting(cmusStatus, str);
			} else {
				int firstSpace = str.indexOf(' ');
				String type = str.substring(0, firstSpace);
				String value = str.substring(firstSpace + 1);
				if (type.equals("status")) {
					cmusStatus.setStatus(value);
				} else if (type.equals("file")) {
					cmusStatus.setFile(value);
				} else if (type.equals("duration")) {
					cmusStatus.setDuration(value);
				} else if (type.equals("position")) {
					cmusStatus.setPosition(value);
				}
			}
		}

		alert("Received Status", cmusStatus.toSimpleString());
	}

	private void sendCommand(final String host, final int port,
			final String password, final CmusCommand command) {

		new Thread(new Runnable() {
			private String readAnswer(BufferedReader in) throws IOException {
				StringBuilder answerBuilder = new StringBuilder();

				String line;
				while ((line = in.readLine()) != null && line.length() != 0) {
					answerBuilder.append(line).append("\n");
				}

				return answerBuilder.toString();
			}

			private void handleCmdAnswer(BufferedReader in, final CmusCommand command) throws Exception {
				final String cmdAnswer = readAnswer(in);
				if (cmdAnswer != null && cmdAnswer.trim().length() != 0) {
					Log.v(TAG, "Received answer to " + command.getLabel() + ": "
							+ cmdAnswer.replaceAll("\n", "\n\t").replaceFirst("\n\t", "\n"));
					CmusDroidRemoteActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							if (command.equals(CmusCommand.STATUS)) {
								handleStatus(cmdAnswer);
							} else {
								alert("Message from Cmus", "Received message: " + cmdAnswer);
							}
						}
					});
				}
			}

			private void validAuth(BufferedReader in) throws Exception {
				String passAnswer = readAnswer(in);
				if (passAnswer != null && passAnswer.trim().length() != 0) {
					throw new Exception("Could not login: " + passAnswer);
				}
			}

			public void run() {
				Socket socket = null;
				BufferedReader in = null;
				PrintWriter out = null;
				try {
					socket = new Socket(host, port);
					Log.v(TAG, "Connected to " + host + ":" + port);
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()), Character.SIZE);
					out = new PrintWriter(socket.getOutputStream(), true);

					out.println("passwd " + password);
					validAuth(in);
					out.println(command.getCommand());
					handleCmdAnswer(in, command);
				} catch (final Exception e) {
					Log.e(TAG, "Could not send the command", e);
					CmusDroidRemoteActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							alert("Could not send command", "Could not send the command: "
									+ e.getLocalizedMessage());
						}
					});
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (Exception e1) {
						}
						in = null;
					}
					if (out != null) {
						try {
							out.close();
						} catch (Exception e1) {
						}
						out = null;
					}
					if (socket != null) {
						try {
							socket.close();
						} catch (Exception e) {
						}
						socket = null;
					}
				}
			}
		}).start();
	}

}