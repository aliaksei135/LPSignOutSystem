/*
 * com.aliakseipilko.signoutsystem.DataHandlers.models.User was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 14/02/17 16:19
 */

package com.aliakseipilko.signoutsystem.DataHandlers.models;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;


public class User extends RealmObject {

    @PrimaryKey
    public long id;

    @Required
    public String name;

    public int year;

    public String nativeHouse;

    public boolean state;

    public String whereabouts;

    @Required
    public byte[] bioImage;

    @Index
    public String pin;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getNativeHouse() {
        return nativeHouse;
    }

    public void setNativeHouse(String nativeHouse) {
        this.nativeHouse = nativeHouse;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String getWhereabouts() {
        return whereabouts;
    }

    public void setWhereabouts(String whereabouts) {
        this.whereabouts = whereabouts;
    }

    public byte[] getBioImage() {
        return bioImage;
    }

    public void setBioImage(byte[] bioImage) {
        this.bioImage = bioImage;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
