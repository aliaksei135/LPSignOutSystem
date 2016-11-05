package com.apilko.signoutsystem.DataHandlers;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Keep;
import android.util.Log;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGDeviceInfoParam;
import SecuGen.FDxSDKPro.SGFDxDeviceName;
import SecuGen.FDxSDKPro.SGFDxErrorCode;
import SecuGen.FDxSDKPro.SGFDxSecurityLevel;

@Keep
public class BiometricDataHandler {

    private static BiometricDataHandler ourInstance;

    private final JSGFPLib bioLib;
    private final SGDeviceInfoParam deviceParams = new SGDeviceInfoParam();
    private final LocalDatabaseHandler dbHandler;
    private int bioImageHeight;
    private int bioImageWidth;

    private BiometricDataHandler(JSGFPLib bioLib, Context context) throws Resources.NotFoundException {
        this.bioLib = bioLib;
        this.dbHandler = LocalDatabaseHandler.getInstance(context);
        initialiseLib();
    }

    public static BiometricDataHandler getInstance(JSGFPLib bioLib, Context context) {
        if (ourInstance == null) {
            ourInstance = new BiometricDataHandler(bioLib, context);
            return ourInstance;
        } else {
            return ourInstance;
        }
    }

    private void initialiseLib() throws Resources.NotFoundException {

        long libInitError = bioLib.Init(SGFDxDeviceName.SG_DEV_AUTO);
        if (libInitError == 0) {
            long deviceInitError = bioLib.OpenDevice(0);
            if (deviceInitError == 0) {
                initialiseDevice();
            } else {
                throw new Resources.NotFoundException("Device not opened");
            }
        } else {
            throw new Resources.NotFoundException("Device not found");
        }
    }

    private void initialiseDevice() {

        long result = bioLib.GetDeviceInfo(deviceParams);

        if (result == SGFDxErrorCode.SGFDX_ERROR_NONE) {
            bioImageHeight = deviceParams.imageHeight;
            bioImageWidth = deviceParams.imageWidth;
        }
    }

    public byte[] getProcessedBioData() {

        byte[] image = captureImage();
        if (image != null) {
            return extractBioData(image);
        } else {
            return null;
        }
    }

    private byte[] extractBioData(byte[] imageBuffer) {

        int[] maxTemplateSize = new int[1];
        bioLib.GetMaxTemplateSize(maxTemplateSize);
        byte[] minBuffer = new byte[maxTemplateSize[0]];
        //Create Minutiae template
        bioLib.CreateTemplate(null, imageBuffer, minBuffer);
        return minBuffer;
    }

    public int matchBioData(byte[] toVerifyData, int year) {
        //Get number of records in db
        long recordNum = dbHandler.getRecordNum(year);
        if (recordNum <= 0) {
            return -1;
        }
        //Iterate through local db to find fingerprint
        byte[] storedData = null;
        boolean[] match = new boolean[1];
        int i;
        boolean found = false;
        for (i = 1; i <= recordNum; i++) {
            match[0] = false;
            storedData = dbHandler.getBioImage(i, year);
            bioLib.MatchTemplate(storedData, toVerifyData, SGFDxSecurityLevel.SL_NORMAL, match);
            if (match[0]) {
                found = true;
                break;
            }
        }

        int[] score = new int[1];
        bioLib.GetMatchingScore(storedData, toVerifyData, score);
        Log.d("BioHandler", "Verification matching score: " + score[0]);

        if (found) {
            return i;
        } else {
            return -1;
        }
    }

    public boolean matchBioDataSets(byte[] set1, byte[] set2) {
        boolean[] result = new boolean[1];
        bioLib.MatchTemplate(set1, set2, SGFDxSecurityLevel.SL_NORMAL, result);
        int[] score = new int[1];
        bioLib.GetMatchingScore(set1, set2, score);
        Log.d("BioHandler", "Modification matching score: " + score[0]);
        return result[0];
    }

    private byte[] captureImage() {

        final byte[] imageBuffer = new byte[bioImageHeight * bioImageWidth];
        bioLib.GetImage(imageBuffer);
        return imageBuffer;

    }
}
