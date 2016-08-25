package no.hyper.memoryorm.broadcastReceiver;

import android.os.Parcelable;

/**
 * Created by jean on 25.08.2016.
 */
public interface FetchListener {
    void onFetched(String className, Parcelable parcel);
}
