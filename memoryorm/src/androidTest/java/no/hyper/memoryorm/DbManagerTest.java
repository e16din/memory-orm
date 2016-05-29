package no.hyper.memoryorm;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Created by Jean on 5/29/2016.
 */

@RunWith(AndroidJUnit4.class)
public class DbManagerTest {

    private static final String DB_NAME = "DbTest";
    private Context context;
    private DbManager manager;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getContext();
        context.deleteDatabase(DB_NAME);
        manager = new DbManager(context, DB_NAME, null, 1);
    }

    @Test
    public void shouldOpenDb() throws Exception {
        manager.openDb();
        Assert.assertEquals(true, manager.isDbOpen());
    }

    @Test
    public void shouldCloseDb() throws Exception {
        manager.close();
        Assert.assertEquals(false, manager.isDbOpen());
    }

}
