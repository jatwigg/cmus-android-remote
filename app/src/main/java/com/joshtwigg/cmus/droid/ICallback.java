package com.joshtwigg.cmus.droid;

/**
 * Created by josh on 31/01/14.
 */
public interface ICallback {
    void onAnswer(CmusCommand command, String answer);
    void onError(Exception e);
}
