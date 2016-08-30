package no.hyper.memoryorm.broadcastReceiver;

/**
 * Created by jean on 25.08.2016.
 */
public interface FetchListener {
    void onFetched(String className, boolean isList, Object object);
}
