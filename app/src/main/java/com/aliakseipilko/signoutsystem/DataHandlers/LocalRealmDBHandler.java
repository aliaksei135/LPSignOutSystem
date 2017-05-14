/*
 * com.aliakseipilko.signoutsystem.DataHandlers.LocalRealmDBHandler was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 23/04/17 20:43
 */

package com.aliakseipilko.signoutsystem.DataHandlers;


import com.aliakseipilko.signoutsystem.DataHandlers.models.User;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class LocalRealmDBHandler {

    public static final int SCHOOL_VISITOR = 4;
    public static final int GROVE_VISITOR = 1;
    public static final int FIELD_VISITOR = 2;
    public static final int RECKITT_VISITOR = 3;
    public static final int FRYER_VISITOR = 5;

    Realm realm;

    public LocalRealmDBHandler() {
        realm = Realm.getDefaultInstance();
    }

    byte[] getBioImage(long id) {

        return realm.where(User.class).equalTo("id", id).findFirst().getBioImage();
    }

    public long getPin(long id) {
        return Long.parseLong(realm.where(User.class).equalTo("id", id).findFirst().getPin());
    }

    public long getRecordNum() {
        Number num = realm.where(User.class).max("id");
        if (num == null || num.equals(0)) {
            return 0;
        }
        return num.longValue();
    }

    public String getName(long id) {
        return realm.where(User.class).equalTo("id", id).findFirst().getName();
    }

    public int getYear(long id) {
        return realm.where(User.class).equalTo("id", id).findFirst().getYear();
    }

    public String getWhereabouts(long id) {
        return realm.where(User.class).equalTo("id", id).findFirst().getWhereabouts();
    }

    public User findByPin(String pin) {
        return realm.where(User.class).equalTo("pin", pin).findFirst();
    }

    public void addNewRecord(String name, String house, int year, String pin, byte[] ID, boolean isNFC) {
        User newUser = new User();
        newUser.setId(getNextID());
        newUser.setName(name);
        newUser.setNativeHouse(house);
        newUser.setYear(year);
        newUser.setPin(pin);
        newUser.setBioImage(ID);
        //They must be in the house if they are creating a new user
        newUser.setWhereabouts("Signed In");

        realm.beginTransaction();
        realm.copyToRealm(newUser);
        realm.commitTransaction();
    }

    public boolean checkPINCollision(String pin) {
        RealmResults<User> results = realm.where(User.class).equalTo("pin", pin).findAll();

        return !results.isEmpty();
    }

    public void updateID(long id, byte[] newID) {
        User user = realm.where(User.class).equalTo("id", id).findFirst();

        realm.beginTransaction();
        user.setBioImage(newID);
        realm.copyToRealmOrUpdate(user);
        realm.commitTransaction();
    }

    public void updatePIN(long id, String newPin) {
        User user = realm.where(User.class).equalTo("id", id).findFirst();

        realm.beginTransaction();
        user.setPin(newPin);
        realm.copyToRealmOrUpdate(user);
        realm.commitTransaction();
    }

    public void updateLocation(long id, String location) {
        User user = realm.where(User.class).equalTo("id", id).findFirst();

        realm.beginTransaction();
        user.setWhereabouts(location);
        realm.copyToRealmOrUpdate(user);
        realm.commitTransaction();
    }

    public void resetAllToSignedOut() {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                //RealmResults is autoupdating, however updates will only remove already changed objects :)
                RealmResults<User> results = realm.where(User.class).not().equalTo("whereabouts", "Signed Out").findAll();
                for (User u : results) {
                    u.setWhereabouts("Signed Out");
                    realm.copyToRealm(u);
                }
            }
        });
    }

    public RealmResults<User> searchRecords(String searchKey) {
        return realm.where(User.class)
                .contains("name", searchKey, Case.INSENSITIVE)
                .or()
                .contains("year", searchKey)
                .findAll()
                .sort("name", Sort.DESCENDING);
    }

    public User getRecordById(long id) {
        return realm.where(User.class).equalTo("id", id).findFirst();
    }

    private long getNextID() {
        Number idNum = realm.where(User.class).max("id");
        if (idNum == null) {
            return 1;
        } else {
            long id = idNum.longValue();
            return id + 1;
        }
    }
}
