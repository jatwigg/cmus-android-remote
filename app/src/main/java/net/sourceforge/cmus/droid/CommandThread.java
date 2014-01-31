package net.sourceforge.cmus.droid;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by josh on 31/01/14.
 */
public class CommandThread extends Thread {
    private final Host _host;
    private final CmusCommand _command;
    private final ICallback _callback;

    public CommandThread(final Host host, final CmusCommand command, ICallback callback) {
        _host = host;
        _command = command;
        _callback = callback;
    }

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
            _callback.onAnswer(command, cmdAnswer);
        }
        else
        {
            _callback.onError(new Exception("Empty response from cmus."));
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
            socket = new Socket(_host.host, _host.port);
            Log.v(getClass().getSimpleName(), "Connected to " + _host.host + ":" + _host.port + ".");
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()), Character.SIZE);
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("passwd " + _host.password);
            validAuth(in);
            out.println(_command.getCommand());
            handleCmdAnswer(in, _command);
        } catch (final Exception e) {
            Log.e(getClass().getSimpleName(), "Could not send the command", e);
            _callback.onError(e);
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
}
