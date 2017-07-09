package com.github.andrejnazarov.notesrealm.manager;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * @author Nazarov on 09.07.17.
 */

public class RealmManager {
    private Realm mRealm;

    public RealmManager(Context context) {
        mRealm = Realm.getInstance(
                new RealmConfiguration.Builder(context)
                .name("notesRealm.realm")
                .build());
    }

    public Realm getRealm() {
        return mRealm;
    }
}
