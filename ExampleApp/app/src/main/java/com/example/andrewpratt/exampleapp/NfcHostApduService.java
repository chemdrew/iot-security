package com.example.andrewpratt.exampleapp;

import android.nfc.cardemulation.HostApduService;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;

/**
 * Created by andrewpratt on 11/5/16.
 */

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class NfcHostApduService extends HostApduService {
    private final String TAG = "NfcHostApduService";

    private int messageCounter = 0;

    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
        if (selectAidApdu(apdu)) {
            Log.i(TAG, "Application selected");
            return getWelcomeMessage();
        }
        else {
            Log.i(TAG, "Received: " + new String(apdu));
            return getNextMessage();
        }
    }

    private byte[] getWelcomeMessage() {
        return "Hello Desktop!".getBytes();
    }

    private byte[] getNextMessage() {
        return ("Message from android: " + messageCounter++).getBytes();
    }

    private boolean selectAidApdu(byte[] apdu) {
        return apdu.length >= 2 && apdu[0] == (byte)0 && apdu[1] == (byte)0xa4;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.i(TAG, "Deactivated: " + reason);
    }
}
