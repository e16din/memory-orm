package no.hyper.memoryorm.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import no.hyper.memoryorm.service.FetchInBackground;

/**
 * Created by jean on 25.08.2016.
 */
public class FetchReceiver extends BroadcastReceiver {

    private FetchListener listener;

    public FetchReceiver(FetchListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String className = intent.getStringExtra(FetchInBackground.CLASS_NAME);
        Parcelable parcelable = intent.getParcelableExtra(FetchInBackground.PARCELABLE);
        listener.onFetched(className, parcelable);
    }

}
