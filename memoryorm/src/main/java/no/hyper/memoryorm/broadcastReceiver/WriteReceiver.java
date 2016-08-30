package no.hyper.memoryorm.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import no.hyper.memoryorm.service.FetchInBackground;
import no.hyper.memoryorm.service.WriteInBackground;

/**
 * Created by jean on 30.08.2016.
 */
public class WriteReceiver extends BroadcastReceiver {

    private WriteListener listener;

    public WriteReceiver(WriteListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String className = intent.getStringExtra(FetchInBackground.CLASS_NAME);
        boolean isList = intent.getBooleanExtra(FetchInBackground.IS_LIST, false);
        Object result;

        if (isList) {
            result = intent.getLongArrayExtra(WriteInBackground.ROW_ID);
        } else {
            result = intent.getLongExtra(WriteInBackground.ROW_ID, -1);
        }
        listener.onWrote(className, isList, result);
    }

}
