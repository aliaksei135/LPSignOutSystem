package com.apilko.signoutsystem.Helpers;

import android.support.annotation.Keep;

import org.jasypt.util.binary.StrongBinaryEncryptor;

@Keep
public class strongBinaryEncryptor {

    private static StrongBinaryEncryptor ourInstance;

    private strongBinaryEncryptor() {
    }

    public static StrongBinaryEncryptor getInstance() {

        if (ourInstance == null) {
            ourInstance = new StrongBinaryEncryptor();
            //256 byte password seed
            ourInstance.setPassword("m3-r|u$6xq?Tb5h#QCvKp8#Ls$S%U#^VheyInW31S^37d&I$9TSEl+iL_0F#ev7F-jdmjx+EPmW%_6CcJHclETDmdKGeoh7WIMUh*A6jgySoyLX#Y1Y1COYuX4FFJv8pkldqWIX0zFIy_72sOI#zj7y|g$B4@gjPTLV^lRVGj8tPJA?KWS95q8WiCjfe2a?mEe=y|aEEU-c4*$=2AM=uW|y0HZ%Ot#HUMv7!qLJ+42+F!w0o!vn8hR-&F1eO7yF^");
            return ourInstance;
        } else {
            return ourInstance;
        }
    }
}
