package com.apilko.signoutsystem.DataHandlers;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Keep;

import com.apilko.signoutsystem.Helpers.strongBinaryEncryptor;

import org.jasypt.util.binary.StrongBinaryEncryptor;

import java.nio.ByteBuffer;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGDeviceInfoParam;
import SecuGen.FDxSDKPro.SGFDxDeviceName;
import SecuGen.FDxSDKPro.SGFDxErrorCode;
import SecuGen.FDxSDKPro.SGFDxSecurityLevel;

@Keep
public class BiometricDataHandler {

    private static BiometricDataHandler ourInstance;

    private final Context context;
    //Encryptor singleton ensures same hash, makes decryption possible
    private final StrongBinaryEncryptor encryptor = strongBinaryEncryptor.getInstance();
    private final JSGFPLib bioLib;
    private SGDeviceInfoParam deviceParams = new SGDeviceInfoParam();
    private int bioImageHeight;
    private int bioImageWidth;
    private LocalDatabaseHandler dbHandler;

    private BiometricDataHandler(JSGFPLib bioLib, Context context) throws Resources.NotFoundException {
        this.context = context;
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

    public static byte[] toByteArray(double value) {

        byte[] bytes = new byte[JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES * JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
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
        //Set finger info
//        SGFingerInfo fingerInfo = new SGFingerInfo();
//        //Hardcode Right thumb for now
//        fingerInfo.FingerNumber = SGFingerPosition.SG_FINGPOS_RT;
//        fingerInfo.ImageQuality = getImageQuality(imageBuffer);
//        fingerInfo.ImpressionType = SGImpressionType.SG_IMPTYPE_LP;
//        fingerInfo.ViewNumber = 1;
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
        byte[] storedData;
        boolean[] match = new boolean[1];
        int i;
        for (i = 1; i < recordNum; i++) {
            match[0] = false;
            storedData = dbHandler.getBioImage(i, year);
            bioLib.MatchTemplate(storedData, toVerifyData, SGFDxSecurityLevel.SL_NORMAL, match);
            if (match[0]) {
                break;
            }
        }
        //Return for new user or unable to find entry
        return match[0] ? i : -1;
    }

    public int getAndMatchBioData(int year) {

        return matchBioData(extractBioData(captureImage()), year);
    }

    public int getAndMatchBioDataTest(byte[] imgBuffer, int year) {

        return matchBioData(extractBioData(imgBuffer), year);
    }

    private byte[] captureImage() {

        final byte[] imageBuffer = new byte[bioImageHeight * bioImageWidth];
        bioLib.GetImage(imageBuffer);
        return imageBuffer;

    }

    private int getImageQuality(byte[] imageBuffer) {

        int[] quality = new int[2];
        bioLib.GetImageQuality(bioImageWidth, bioImageHeight, imageBuffer, quality);
        return quality[0];
    }
}
