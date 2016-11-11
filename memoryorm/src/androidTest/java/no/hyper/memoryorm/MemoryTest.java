package no.hyper.memoryorm;

import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import no.hyper.memoryorm.model.Profile;

/**
 * Created by jean on 25.07.2016.
 */
public class MemoryTest {

    private Context context;
    private Memory memory;

    @Before
    public void start() throws Exception {
        context = InstrumentationRegistry.getContext();
        memory = new Memory(context);
        memory.deleteDatabase();
        memory.createDatabase();
    }

    @Test
    public void shouldSave() throws Exception {
        memory.save(new Profile("id", "test", 23, true));

        memory.openDb();
        Cursor cursor = memory.rawQuery("SELECT * FROM Profile", null);
        Assert.assertEquals(1, cursor.getCount());
        memory.closeDb();
    }

    @Test
    public void shouldSaveList() throws Exception {
        saveList();
    }

    @Test
    public void shouldFetchAll() throws Exception {
        List<Profile> profiles = saveList();

        List<Profile> fetchedProfiles = memory.fetchAll(Profile.class);
        for (int i = 0; i < profiles.size(); i++) {
            Assert.assertEquals(profiles.get(i), fetchedProfiles.get(i));
        }
    }

    @Test
    public void shouldFetchFirst() throws Exception {
        Profile profile = new Profile("id", "name", 20, true);
        memory.save(profile);

        Profile fetched = memory.fetchFirst(Profile.class);
        Assert.assertEquals(profile, fetched);
    }

    @Test
    public void shouldFetchById() throws Exception {
        Profile profile = new Profile("id", "name", 20, true);
        memory.save(profile);

        Profile fetched = memory.fetchById(Profile.class, "id");
        Assert.assertEquals(profile, fetched);
    }

    @Test
    public void shouldUpdateEntity() throws Exception {
        Profile profile = new Profile("id", "name", 20, true);
        memory.save(profile);

        profile.setName("changed name");
        memory.update(profile);

        Profile fetched = memory.fetchFirst(Profile.class);
        Assert.assertEquals(profile, fetched);
    }

    @Test
    public void shouldUpdateListEntity() throws Exception {
        List<Profile> profiles = saveList();

        for (Profile profile : profiles){
            profile.setName("changed name");
        }

        List<Profile> fetchedProfiles = memory.fetchAll(Profile.class);
        Assert.assertEquals(profiles, fetchedProfiles);
        for (int i = 0; i < profiles.size(); i++) {
            Assert.assertEquals(profiles.get(i), fetchedProfiles.get(i));
        }
    }

    @Test
    public void shouldSaveOrUpdateEntity() throws Exception {
        Profile profile = new Profile("id", "name", 20, true);
        memory.saveOrUpdate(profile);

        Profile fetched = memory.fetchFirst(Profile.class);
        Assert.assertEquals(profile, fetched);

        profile.setName("changed name");
        memory.saveOrUpdate(profile);

        fetched = memory.fetchFirst(Profile.class);
        Assert.assertEquals(profile, fetched);
    }

    @Test
    public void shouldSaveOrUpdateList() throws Exception {
        List<Profile> profiles = saveList();
        List<Profile> fetched = memory.fetchAll(Profile.class);
        for (int i = 0; i < profiles.size(); i++) {
            Assert.assertEquals(profiles.get(i), fetched.get(i));
        }

        for (Profile profile : profiles) {
            profile.setName("changed name");
        }

        memory.saveOrUpdate(profiles);
        for (int i = 0; i < profiles.size(); i++) {
            Assert.assertEquals(profiles.get(i), fetched.get(i));
        }
    }

    @Test
    public void shouldGetTableCount() throws Exception {
        List<Profile> profiles = saveList();
        Integer rowCount = memory.getTableCount(Profile.class.getSimpleName());
        Assert.assertEquals(profiles.size(), rowCount.intValue());
    }

    @Test
    public void shouldEmptyDatabase() throws Exception {
        List<Profile> profiles = saveList();
        Integer rowCount = memory.getTableCount(Profile.class.getSimpleName());
        Assert.assertEquals(profiles.size(), rowCount.intValue());

        memory.emptyDatabase();

        rowCount = memory.getTableCount(Profile.class.getSimpleName());
        Assert.assertEquals(0, rowCount.intValue());
    }

    @Test
    public void shouldEmptyTable() throws Exception {
        List<Profile> profiles = saveList();
        Integer rowCount = memory.getTableCount(Profile.class.getSimpleName());
        Assert.assertEquals(profiles.size(), rowCount.intValue());

        memory.emptyTable(Profile.class.getSimpleName(), null);

        rowCount = memory.getTableCount(Profile.class.getSimpleName());
        Assert.assertEquals(0, rowCount.intValue());
    }

    private List<Profile> getProfileList() {
        List<Profile> profiles = new ArrayList<>();
        profiles.add(new Profile("id1", "name", 20, true));
        profiles.add(new Profile("id2", "name", 20, true));
        profiles.add(new Profile("id3", "name", 20, true));

        return profiles;
    }

    private List<Profile> saveList() {
        List<Profile> profiles = getProfileList();
        memory.save(profiles);

        memory.openDb();
        Cursor cursor = memory.rawQuery("SELECT * FROM Profile", null);
        Assert.assertEquals(3, cursor.getCount());
        memory.closeDb();

        return profiles;
    }

}
