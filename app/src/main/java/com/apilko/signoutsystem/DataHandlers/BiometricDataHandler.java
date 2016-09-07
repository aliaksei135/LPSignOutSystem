package com.apilko.signoutsystem.DataHandlers;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Keep;
import android.widget.Toast;

import com.apilko.signoutsystem.Helpers.strongBinaryEncryptor;

import org.jasypt.util.binary.StrongBinaryEncryptor;

import java.nio.ByteBuffer;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGDeviceInfoParam;
import SecuGen.FDxSDKPro.SGFDxDeviceName;
import SecuGen.FDxSDKPro.SGFDxErrorCode;
import SecuGen.FDxSDKPro.SGFDxSecurityLevel;
import SecuGen.FDxSDKPro.SGFingerInfo;
import SecuGen.FDxSDKPro.SGFingerPosition;
import SecuGen.FDxSDKPro.SGImpressionType;

@Keep
public class BiometricDataHandler {

    private static BiometricDataHandler ourInstance;

    private final Context context;
    //Encryptor singleton ensures same hash, makes decryption possible
    private final StrongBinaryEncryptor encryptor = strongBinaryEncryptor.getInstance();
    private final JSGFPLib bioLib;
    private SGDeviceInfoParam deviceParams;
    private int bioImageHeight;
    private int bioImageWidth;
    private LocalDatabaseHandler dbHandler;

    private BiometricDataHandler(JSGFPLib bioLib, Context context) throws Resources.NotFoundException {
        this.context = context;
        this.bioLib = bioLib;
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

        return strongEncryptBioData(extractBioData(captureImage()));
    }

    private byte[] extractBioData(byte[] imageBuffer) {

        int[] maxTemplateSize = new int[2];
        bioLib.GetMaxTemplateSize(maxTemplateSize);
        byte[] minBuffer = new byte[maxTemplateSize[0]];
        //Set finger info
        SGFingerInfo fingerInfo = new SGFingerInfo();
        //Hardcode Right thumb for now
        fingerInfo.FingerNumber = SGFingerPosition.SG_FINGPOS_RT;
        fingerInfo.ImageQuality = getImageQuality(imageBuffer);
        fingerInfo.ImpressionType = SGImpressionType.SG_IMPTYPE_LP;
        fingerInfo.ViewNumber = 1;
        //Create Minutiae template
        bioLib.CreateTemplate(fingerInfo, imageBuffer, minBuffer);
        return minBuffer;
    }

    private int matchBioData(byte[] toVerifyData) {
        //encrypt data first for higher efficiency db search
        //toVerifyData = encryptor.encrypt(toVerifyData);
        //Get number of records in db
        long recordNum = dbHandler.getRecordNum();
        //Iterate through local db to find fingerprint
        byte[] storedData;
        boolean[] match = new boolean[1];
        for (int i = 0; i < recordNum; i++) {
            match[0] = false;
            storedData = strongDecryptBioData(dbHandler.getBioImage(i));
            bioLib.MatchTemplate(storedData, toVerifyData, SGFDxSecurityLevel.SL_NORMAL, match);
            if (match[0]) {
                return i;
            }
        }
        return -1;
    }

    public int getAndMatchBioData() {

        return matchBioData(extractBioData(captureImage()));
    }

    public int getAndMatchBioDataTest(byte[] imgBuffer) {

        return matchBioData(extractBioData(imgBuffer));
    }

    private byte[] captureImage() {

        final byte[] imageBuffer = new byte[bioImageHeight * bioImageWidth];
        if (bioLib.GetImage(imageBuffer) == SGFDxErrorCode.SGFDX_ERROR_NONE) {
            if (isGoodQuality(imageBuffer)) {
                return imageBuffer;
            } else {
                Toast.makeText(context, "Scan finger again", Toast.LENGTH_LONG).show();
                try {
                    //Wait for 2 seconds before retrying
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                captureImage();
            }
        }
        return null;
    }

    private boolean isGoodQuality(byte[] imageBuffer) {

        int[] quality = new int[2];
        bioLib.GetImageQuality(bioImageWidth, bioImageHeight, imageBuffer, quality);
        return quality[0] > 80;
    }

    private int getImageQuality(byte[] imageBuffer) {

        int[] quality = new int[2];
        bioLib.GetImageQuality(bioImageWidth, bioImageHeight, imageBuffer, quality);
        return quality[0];
    }

    private byte[] strongEncryptBioData(byte[] rawBioData) {
        //Strong Encrypt the Minutiae data
        return encryptor.encrypt(rawBioData);
    }

    private byte[] strongDecryptBioData(byte[] encryptedBioData) {
        //Decrypt the minutiae data
        return encryptor.decrypt(encryptedBioData);
    }
}
