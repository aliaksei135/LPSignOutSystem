/*
 * de.adorsys.android.securestoragelibrary.KeystoreTool was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 25/04/17 20:56
 */

package de.adorsys.android.securestoragelibrary;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

import static android.os.Build.VERSION_CODES.M;

class KeystoreTool {
    private static final String KEY_ALIAS = "adorsysKeyPair";
    private static final String KEY_ENCRYPTION_ALGORITHM = "RSA";
    private static final String KEY_CHARSET = "UTF-8";
    private static final String KEY_KEYSTORE_NAME = "AndroidKeyStore";
    private static final String KEY_CIPHER_JELLYBEAN_PROVIDER = "AndroidOpenSSL";
    private static final String KEY_CIPHER_MARSHMALLOW_PROVIDER = "AndroidKeyStoreBCWorkaround";
    private static final String KEY_TRANSFORMATION_ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static final String KEY_X500PRINCIPAL = "CN=SecureDeviceStorage, O=Adorsys, C=Germany";

    @Nullable
    static String encryptMessage(@NonNull Context context, @NonNull String plainMessage) throws CryptoException {
        try {
            Cipher input;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                    && Build.VERSION.SDK_INT < M) {
                input = Cipher.getInstance(KEY_TRANSFORMATION_ALGORITHM, KEY_CIPHER_JELLYBEAN_PROVIDER);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                input = Cipher.getInstance(KEY_TRANSFORMATION_ALGORITHM, KEY_CIPHER_MARSHMALLOW_PROVIDER);
            } else {
                Log.e(KeystoreTool.class.getName(), context.getString(R.string.message_supported_api));
                throw new CryptoException(context.getString(R.string.message_supported_api), null);
            }
            input.init(Cipher.ENCRYPT_MODE, getPublicKey(context));

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    outputStream, input);
            cipherOutputStream.write(plainMessage.getBytes(KEY_CHARSET));
            cipherOutputStream.close();

            byte[] values = outputStream.toByteArray();
            return Base64.encodeToString(values, Base64.DEFAULT);

        } catch (NoSuchAlgorithmException
                | NoSuchProviderException
                | NoSuchPaddingException
                | InvalidKeyException
                | IOException e) {
            throw new CryptoException(e.getMessage(), e);
        }
    }

    @Nullable
    static String decryptMessage(@NonNull Context context, @NonNull String encryptedMessage) throws CryptoException {
        try {
            Cipher output;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                    && Build.VERSION.SDK_INT < M) {
                output = Cipher.getInstance(KEY_TRANSFORMATION_ALGORITHM, KEY_CIPHER_JELLYBEAN_PROVIDER);
            } else if (Build.VERSION.SDK_INT >= M) {
                output = Cipher.getInstance(KEY_TRANSFORMATION_ALGORITHM, KEY_CIPHER_MARSHMALLOW_PROVIDER);
            } else {
                Log.e(KeystoreTool.class.getName(), context.getString(R.string.message_supported_api));
                throw new CryptoException(context.getString(R.string.message_supported_api), null);
            }

            output.init(Cipher.DECRYPT_MODE, getPrivateKey(context));

            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(Base64.decode(encryptedMessage, Base64.DEFAULT)), output);
            List<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte) nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i);
            }

            return new String(bytes, 0, bytes.length, KEY_CHARSET);

        } catch (NoSuchAlgorithmException
                | NoSuchProviderException
                | NoSuchPaddingException
                | InvalidKeyException
                | IOException e) {
            throw new CryptoException(e.getMessage(), e);
        }
    }

    static boolean keyPairExists() {
        try {
            return getKeyStoreInstance().getKey(KEY_ALIAS, null) != null;
        } catch (Exception e) {
            return false;
        }
    }

    static void generateKeyPair(@NonNull Context context) throws CryptoException {
        // Create new key if needed
        if (!keyPairExists()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                generateAsymmetricKeyPair(context);
            } else {
                Log.e(KeystoreTool.class.getName(), context.getString(R.string.message_supported_api));
                throw new CryptoException(context.getString(R.string.message_supported_api), null);
            }
        } else {
            if (BuildConfig.DEBUG) {
                Log.e(KeystoreTool.class.getName(),
                        context.getString(R.string.message_keypair_already_exists));
            }
        }
    }

    static void deleteKeyPair(@NonNull Context context) throws CryptoException {
        // Delete Key from Keystore
        if (keyPairExists()) {
            try {
                getKeyStoreInstance().deleteEntry(KEY_ALIAS);
            } catch (KeyStoreException e) {
                throw new CryptoException(e.getMessage(), e);
            }
        } else {
            Log.e(KeystoreTool.class.getName(),
                    context.getString(R.string.message_keypair_does_not_exist));
        }
    }

    @Nullable
    private static PublicKey getPublicKey(@NonNull Context context) throws CryptoException {
        try {
            if (keyPairExists()) {
                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) getKeyStoreInstance().getEntry(KEY_ALIAS, null);
                return privateKeyEntry.getCertificate().getPublicKey();
            } else {
                if (BuildConfig.DEBUG) {
                    Log.e(KeystoreTool.class.getName(), context.getString(R.string.message_keypair_does_not_exist));
                }
                throw new CryptoException(context.getString(R.string.message_keypair_does_not_exist), null);
            }
        } catch (NoSuchAlgorithmException
                | UnrecoverableEntryException
                | KeyStoreException e) {
            throw new CryptoException(e.getMessage(), e);
        }
    }

    @Nullable
    private static PrivateKey getPrivateKey(@NonNull Context context) throws CryptoException {
        try {
            if (keyPairExists()) {
                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) getKeyStoreInstance().getEntry(KEY_ALIAS, null);
                return privateKeyEntry.getPrivateKey();
            } else {
                if (BuildConfig.DEBUG) {
                    Log.e(KeystoreTool.class.getName(), context.getString(R.string.message_keypair_does_not_exist));
                }
                throw new CryptoException(context.getString(R.string.message_keypair_does_not_exist), null);
            }
        } catch (NoSuchAlgorithmException
                | UnrecoverableEntryException
                | KeyStoreException e) {
            throw new CryptoException(e.getMessage(), e);
        }
    }

    private static boolean isRTL(@NonNull Context context) {
        Configuration config = context.getResources().getConfiguration();
        return config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    private static void generateAsymmetricKeyPair(@NonNull Context context) throws CryptoException {
        try {
            if (isRTL(context)) {
                Locale.setDefault(Locale.US);
            }

            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 99);

            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(KEY_ALIAS)
                    .setSubject(new X500Principal(KEY_X500PRINCIPAL))
                    .setSerialNumber(BigInteger.TEN)
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();

            KeyPairGenerator generator
                    = KeyPairGenerator.getInstance(KEY_ENCRYPTION_ALGORITHM, KEY_KEYSTORE_NAME);
            generator.initialize(spec);
            generator.generateKeyPair();
        } catch (NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException e) {
            throw new CryptoException(e.getMessage(), e);
        }
    }

    @NonNull
    private static KeyStore getKeyStoreInstance() throws CryptoException {
        try {
            // Get the AndroidKeyStore instance
            KeyStore keyStore = KeyStore.getInstance(KEY_KEYSTORE_NAME);

            // Relict of the JCA API - you have to call load even
            // if you do not have an input stream you want to load or it'll crash
            keyStore.load(null);

            return keyStore;
        } catch (CertificateException
                | NoSuchAlgorithmException
                | KeyStoreException
                | IOException e) {
            throw new CryptoException(e.getMessage(), e);
        }
    }
}