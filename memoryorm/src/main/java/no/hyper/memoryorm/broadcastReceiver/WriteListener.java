package no.hyper.memoryorm.broadcastReceiver;

/**
 * Created by jean on 30.08.2016.
 */
public interface WriteListener {
    void onWrote(String className, boolean isList, Object object);
}
