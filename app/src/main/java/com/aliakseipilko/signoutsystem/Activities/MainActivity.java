/*
 * com.aliakseipilko.signoutsystem.Activities.MainActivity was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2016.  All Rights Reserved.
 *
 * Last modified 11/11/16 20:11
 */

package com.aliakseipilko.signoutsystem.Activities;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.sheets.v4.SheetsScopes;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aliakseipilko.signoutsystem.DataHandlers.BiometricDataHandler;
import com.aliakseipilko.signoutsystem.DataHandlers.GoogleSheetsHandler;
import com.aliakseipilko.signoutsystem.DataHandlers.LocalDatabaseHandler;
import com.aliakseipilko.signoutsystem.Helpers.IdleMonitor;
import com.aliakseipilko.signoutsystem.R;

import java.util.Calendar;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGAutoOnEventNotifier;
import SecuGen.FDxSDKPro.SGFDxErrorCode;
import SecuGen.FDxSDKPro.SGFingerPresentEvent;

@Keep
public class MainActivity extends AppCompatActivity implements SGFingerPresentEvent, GoogleApiClient.OnConnectionFailedListener, DialogInterface.OnDismissListener, IdleMonitor.IdleCallback {

    private static final int REQUEST_SELECTION = 530;
    private static final int REQUEST_FIRST_LAUNCH = 531;

    private static final String TAG = "MainActivity";

    private static final String NATIVE_HOUSE = "School";

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static String serverAuthCode;
    private static IdleMonitor idleMonitor;
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(
                            UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            Log.d(TAG, "Vendor ID : " + device.getVendorId() + "\n");
                            Log.d(TAG, "Product ID: " + device.getProductId() + "\n");
                        } else
                            Log.e(TAG, "mUsbReceiver.onReceive() Device is null");
                    } else
                        Log.e(TAG, "mUsbReceiver.onReceive() permission denied for device "
                                + device);
                }
            }
        }
    };
    private JSGFPLib bioLib;
    private ProgressDialog mProgressDialog;
    private GoogleApiClient mGoogleApiClient;
    private BiometricDataHandler bioHandler;
    private SGAutoOnEventNotifier autoOn;
    private GoogleSheetsHandler sheetsHandler;
    private LocalDatabaseHandler dbHandler;

    //Null Constructor
    public MainActivity() {
    }

    //TODOLIST
    //TODO Work out Access and Refresh token flow, specifically how to obtain refresh token from current flow
    //TODO Convert layout weights to percentages using com.android.support.percent (PercentRelativeLayout)
    //TODO Put weather and calendar fetches on a background IntentService and preload in background OR ContentProvider?
    //TODO More aesthetic loading dialogs?
    //TODO Fix visitor db configs


    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        hideSysUI();
        setContentView(R.layout.activity_main);

        //Get rid of the unneeded action bar
        if (getActionBar() != null) {
            getActionBar().hide();
        }

        //Get rid of navbar
        hideSysUI();

        //Initialise helpers and handlers
        sheetsHandler = GoogleSheetsHandler.getInstance(this);
        dbHandler = LocalDatabaseHandler.getInstance(this);
        idleMonitor = IdleMonitor.getInstance();
        idleMonitor.registerIdleCallback(this);

        //Initialise biometrics
        initBio();
        autoOn = new SGAutoOnEventNotifier(bioLib, this);
        autoOn.start();

        //UI
        Button manualSigningButton = (Button) findViewById(R.id.manualSigningButton);

        //DEBUG BUTTONS
        Button scannerRestartButton = (Button) findViewById(R.id.srButton);
        Button idleActTestButton = (Button) findViewById(R.id.idleActTestButton);
        Button flActTestButton = (Button) findViewById(R.id.flActTestButton);
        Button newUserTestButton = (Button) findViewById(R.id.newUserTestButton);

        assert manualSigningButton != null;
        manualSigningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idleMonitor.nullify();
                manualIdentify();
            }
        });

        assert scannerRestartButton != null;
        scannerRestartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initBio();
                autoOn = new SGAutoOnEventNotifier(bioLib, null);
                autoOn.start();
            }
        });
        assert idleActTestButton != null;
        idleActTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idleMonitor.nullify();
                startActivity(new Intent(MainActivity.this, IdleActivity.class));
            }
        });
        assert flActTestButton != null;
        flActTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idleMonitor.nullify();
                startActivityForResult(new Intent(MainActivity.this, FirstLaunch.class), REQUEST_FIRST_LAUNCH);
            }
        });
        assert newUserTestButton != null;
        newUserTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idleMonitor.nullify();
                makeNewUser(0, null);
            }
        });


        //Initialise google apis
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(SheetsScopes.SPREADSHEETS))
                .requestServerAuthCode(getResources().getString(R.string.server_client_id))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addScope(new Scope(SheetsScopes.SPREADSHEETS))
                .build();
    }

    public String getServerAuthCode() {
        return serverAuthCode;
    }

    private void scheduleResetRegistered() {

        //Schedule reset to registered state task
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent resetIntent = new Intent(this, MainActivity.class);
        resetIntent.putExtra("type", "ResetRegistered");
        PendingIntent taskIntent = PendingIntent.getBroadcast(this, 0, resetIntent, 0);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 1);
        cal.set(Calendar.MINUTE, 30);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, taskIntent);
    }

    @Override
    protected void onStart() {

        super.onStart();

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
        boolean scheduledTasksSet = sharedPreferences.getBoolean("scheduledTasksSet", false);

        if (isFirstRun) {
            //show start activity
            Toast.makeText(MainActivity.this, "First Run\nPlease sign in", Toast.LENGTH_LONG)
                    .show();
            startActivityForResult(new Intent(MainActivity.this, FirstLaunch.class), REQUEST_FIRST_LAUNCH);
        } else {
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
            if (opr.isDone()) {
                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                Log.d(TAG, "Got cached sign-in");
                GoogleSignInResult result = opr.get();
                MainActivity.serverAuthCode = result.getSignInAccount().getServerAuthCode();
                Log.d(TAG, "handleSignInResult:" + result.isSuccess());
            } else {
                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
                showProgressDialog();
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                        if (googleSignInResult.getSignInAccount() != null) {
                            MainActivity.serverAuthCode = googleSignInResult.getSignInAccount().getServerAuthCode();
                        } else {
                            startActivityForResult(new Intent(MainActivity.this, FirstLaunch.class), REQUEST_FIRST_LAUNCH);
                        }
                        hideProgressDialog();
                        Log.d(TAG, "handleSignInResult:" + googleSignInResult.isSuccess());
                        sharedPreferences.edit().putBoolean("isFirstRun", false).commit();
                    }
                });
            }
        }

        if (!scheduledTasksSet) {
            scheduleResetRegistered();
            sharedPreferences.edit().putBoolean("scheduledTasksSet", true).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        idleMonitor.setTimer();
        hideSysUI();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (bioLib == null) {
            initBio();
        } else if (!(bioLib.ReadSerialNumber(new byte[20]) == SGFDxErrorCode.SGFDX_ERROR_NONE)) {
            initBio();
        }

        if (autoOn == null) {
            autoOn = new SGAutoOnEventNotifier(bioLib, this);
        }
        autoOn.start();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        idleMonitor.nullify();
//        unregisterReceiver(mUsbReceiver);
//        autoOn.stop();
//        bioLib.CloseDevice();
//        bioLib.Close();
    }

    private void initBio() {
        //USB Permissions
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);

        bioLib = new JSGFPLib((UsbManager) getSystemService(Context.USB_SERVICE));
        bioLib.AutoOnEnabled();
        bioLib.OpenDevice(0);
        try {
            bioHandler = BiometricDataHandler.getInstance(bioLib, this);
        } catch (Resources.NotFoundException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle("Device not connected");
            builder.setMessage("Restart app with device connected");
            builder.show();
        }
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_SELECTION:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String name = data.getStringExtra("name");
                    String location = data.getStringExtra("location");
                    String type = data.getStringExtra("type");
                    String[] info = {name, location, type};
                    //Defaults to year 13, however this shouldn't happen as year is already validated before selection activity
                    int year = data.getIntExtra("year", 13);

                    if (sheetsHandler.makeNewLogEntry(info, serverAuthCode)) {
                        dbHandler.updateLocation(name, location, year);
                        onDismiss(null);
                        Toast.makeText(this, "Goodbye " + name + "!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "That didn't work! Try again!", Toast.LENGTH_SHORT).show();
                    }

                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    onDismiss(null);
                }
                hideSysUI();
                break;
            case REQUEST_FIRST_LAUNCH:
                if (resultCode == RESULT_OK && data != null) {
                    Toast.makeText(MainActivity.this, data.getStringExtra("result"), Toast.LENGTH_SHORT).show();
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    MainActivity.serverAuthCode = data.getStringExtra("authCode");

                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("accountName", accountName);
                        editor.apply();
                    }

                } else {
                    if (data != null) {
                        Toast.makeText(MainActivity.this, data.getStringExtra("result"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "That didn't work", Toast.LENGTH_SHORT).show();
                    }
                }
                hideSysUI();
                break;
        }
    }

    //First data input stage of user enrollment
    private void makeNewUser(int year, final byte[] bioData) {

        Toast.makeText(this, "New User", Toast.LENGTH_LONG).show();

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Name and House");

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView nameTitle = new TextView(this);
        nameTitle.setText("Full Name");
        nameTitle.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Medium);
        nameTitle.setPadding(20, 1, 20, 1);
        layout.addView(nameTitle);

        final EditText nameInput = new EditText(this);
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(nameInput);

        final TextView houseTitle = new TextView(this);
        houseTitle.setText("House");
        houseTitle.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Medium);
        houseTitle.setPadding(20, 15, 20, 1);
        layout.addView(houseTitle);

        final Spinner houseSpinner = new Spinner(this);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Select your House", "School", "Grove", "Field", "Reckitt", "Fryer"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        houseSpinner.setAdapter(adapter);
        layout.addView(houseSpinner);

        final TextView yearTitle = new TextView(this);
        yearTitle.setText("Year");
        yearTitle.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Medium);
        yearTitle.setPadding(20, 15, 20, 1);
        layout.addView(yearTitle);

        final Spinner yearSpinner = new Spinner(this);
        final ArrayAdapter<String> adap = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Select your year", "7", "8", "9", "10", "11", "12", "13"});
        adap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(adap);
        layout.addView(yearSpinner);

        final TextView pinTitle = new TextView(this);
        pinTitle.setText("Enter a PIN as a backup");
        pinTitle.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Medium);
        pinTitle.setPadding(20, 15, 20, 1);
        layout.addView(pinTitle);

        final EditText pinField = new EditText(this);
        pinField.setHint("Enter a PIN");
        pinField.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(pinField);

        final EditText pinConfirmField = new EditText(this);
        pinConfirmField.setHint("Confirm your PIN");
        pinConfirmField.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(pinConfirmField);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //This is overridden after the dialog shows anyway
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //This is overridden after the dialog shows anyway
            }
        });

        switch (year) {
            case 7:
                //Zeroth position is placeholder text
                yearSpinner.setSelection(1);
                break;
            case 8:
                yearSpinner.setSelection(2);
                break;
            case 9:
                yearSpinner.setSelection(3);
                break;
            case 10:
                yearSpinner.setSelection(4);
                break;
            case 11:
                yearSpinner.setSelection(5);
                break;
            case 12:
                yearSpinner.setSelection(6);
                break;
            case 13:
                yearSpinner.setSelection(7);
                break;
            default:
                yearSpinner.setSelection(0);
                break;
        }

        builder.setView(layout);
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();
        dialog.show();

        autoOn.stop();

        //Set larger text on buttons
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(nameInput.getText().toString().isEmpty())
                        || !(houseSpinner.getSelectedItem().equals("Select your House"))
                        || !(yearSpinner.getSelectedItem().equals("Select your year"))
                        || !(pinField.getText().toString().isEmpty())
                        || !(pinConfirmField.getText().toString().isEmpty())) {

                    if (pinField.getText().toString().equals(pinConfirmField.getText().toString())) {

                        int year = Integer.parseInt(yearSpinner.getSelectedItem().toString());
                        int pin = Integer.parseInt(pinConfirmField.getText().toString());

                        if (!(dbHandler.checkPINCollision(year, pin))) {

                            if (!(pinField.getText().toString().length() <= 4)) {

                                if (!(pinField.getText().toString().charAt(0) == '0')) {

                                    pushNewUser(nameInput.getText().toString(),
                                            houseSpinner.getSelectedItem().toString(),
                                            Integer.parseInt(yearSpinner.getSelectedItem().toString()),
                                            pinConfirmField.getText().toString(), bioData);

                                    dialog.cancel();

                                } else {
                                    Toast.makeText(MainActivity.this, "First Digit cannot be zero", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "PIN must be at least 5 digits long", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "PIN Collision!\nPlease enter a new PIN", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "PINs don't match", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Enter ALL the information", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.setOnDismissListener(MainActivity.this);
                dialog.cancel();
            }
        });
    }

    //Second verification stage of user enrollment
    private void pushNewUser(final String name, final String house, final int year, final String pin, final byte[] biodata1) {

//        Toast.makeText(this, "New User Pushed", Toast.LENGTH_LONG).show();

        autoOn.stop();

        AlertDialog.Builder bldr = new AlertDialog.Builder(this);
        bldr.setTitle("New User Enrollment");
        bldr.setMessage("Place the same finger on scanner and press OK simultaneously to enroll");
        bldr.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //This is overridden after dialog shows
            }
        });
        bldr.setOnDismissListener(this);
        final AlertDialog dialog = bldr.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] bioData2 = bioHandler.getProcessedBioData();
                if (bioHandler.matchBioDataSets(biodata1, bioData2)) {
                    dbHandler.addNewRecord(name, house, year, pin, biodata1, false);
                    dialog.cancel();
                    Toast.makeText(MainActivity.this, name + ", you are now enrolled", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Scans don't match, try again!", Toast.LENGTH_LONG).show();
                }
            }
        });
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        switch (intent.getStringExtra("type")) {
            case "touch":
                //Do nothing
                break;
            case "fingerprint":
                //Fingerprint present
                SGFingerPresentCallback();
                break;
            case "ResetRegistered":
                dbHandler.resetAllToRegistered();
                break;
            default:
                //Do nothing
                break;
        }
    }

    @Override
    public void SGFingerPresentCallback() {

        autoOn.stop();
        final byte[] bioData = bioHandler.getProcessedBioData();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                idleMonitor.setTimer();
                showYearSelectDialog(bioData, null, true, false);
            }
        });
    }

    private void showYearSelectDialog(final byte[] bioData, final String pin, final boolean isBio, final boolean modifyBio) {
        final int[] year = new int[1];

        hideProgressDialog();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose your Year");
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                autoOn.start();
            }
        });
        builder.setItems(R.array.year_groups, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        year[0] = 7;
                        break;
                    case 1:
                        year[0] = 8;
                        break;
                    case 2:
                        year[0] = 9;
                        break;
                    case 3:
                        year[0] = 10;
                        break;
                    case 4:
                        year[0] = 11;
                        break;
                    case 5:
                        year[0] = 12;
                        break;
                    case 6:
                        year[0] = 13;
                        break;
                    default:
                        year[0] = 13;
                        break;
                }

                if (isBio) {
                    handleBioID(year[0], bioData);
                } else {
                    handlePINID(pin, year[0], modifyBio);
                }
            }
        });

        builder.setNeutralButton("House Visitor", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showVisitorSelectDialog(bioData);
            }
        });

        AlertDialog dialog = builder.show();

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);
        hideSysUI();
    }

    private void showVisitorSelectDialog(final byte[] bioData) {
        final int[] year = new int[1];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Your House");
        builder.setOnDismissListener(this);
        builder.setItems(R.array.houses, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        //Grove visitor
                        year[0] = 1;
                        break;
                    case 1:
                        //Fryer Visitor
                        year[0] = 5;
                        break;
                    case 2:
                        //Reckitt visitor
                        year[0] = 3;
                        break;
                    case 3:
                        //Field visitor
                        year[0] = 2;
                        break;
                    default:
                        year[0] = 13;
                        break;
                }
                handleBioID(year[0], bioData);
            }
        });

        AlertDialog dialog = builder.show();

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);
    }

    private void manualIdentify() {
        final AlertDialog.Builder bldr = new AlertDialog.Builder(this);
        bldr.setTitle("Manual Identification");
        final EditText pinField = new EditText(this);
        pinField.setHint("Enter your PIN");
        pinField.setInputType(InputType.TYPE_CLASS_NUMBER);
        bldr.setView(pinField);
        bldr.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (pinField.getText() == null) {
                    Toast.makeText(MainActivity.this, "No PIN entered", Toast.LENGTH_SHORT).show();
                    bldr.show();
                } else {
                    showYearSelectDialog(null, pinField.getText().toString(), false, false);
                }
            }
        });
        bldr.setNegativeButton("Cancel", null);
        bldr.setNeutralButton("Change Fingerprint", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (pinField.getText() == null) {
                    Toast.makeText(MainActivity.this, "No PIN entered", Toast.LENGTH_SHORT).show();
                    bldr.show();
                } else {
                    showYearSelectDialog(null, pinField.getText().toString(), false, true);
                }
            }
        });

        AlertDialog dialog = bldr.show();

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);

        hideSysUI();
    }

    private void handlePINID(String text, int year, boolean modifyBio) {
        long pin = Long.parseLong(text);
        showProgressDialog();
        autoOn.stop();
        boolean pinExists = dbHandler.checkPINCollision(year, pin);
        if (pinExists) {
            long matchResult = 0;
            long numRecords = dbHandler.getRecordNum(year);
            if (numRecords <= 0) {
                matchResult = -1;
            } else {
                for (int i = 1; i <= numRecords; i++) {
                    long dbPin = dbHandler.getPin(i, year);
                    if (dbPin == pin) {
                        matchResult = i;
                        break;
                    }
                    matchResult++;
                }
            }
            if (!modifyBio) {
                if (matchResult == -1) {
                    hideProgressDialog();
                    makeNewUser(year, null);
                } else {

                    Intent selectionIntent = new Intent(this, SelectionActivity.class);
                    selectionIntent.putExtra("name", dbHandler.getName(matchResult, year));
                    selectionIntent.putExtra("state", dbHandler.getWhereabouts(matchResult, year));
                    selectionIntent.putExtra("year", year);
                    hideProgressDialog();
                    idleMonitor.nullify();
                    startActivityForResult(selectionIntent, REQUEST_SELECTION);
                }
            } else {
                modifyBioData(matchResult, year);
            }
        } else {
            Toast.makeText(this, "PIN incorrect/not found", Toast.LENGTH_SHORT).show();
            hideProgressDialog();
        }
    }

    private void handleBioID(int year, byte[] bioData) {
        showProgressDialog();
        autoOn.stop();
        long matchResult = bioHandler.matchBioData(bioData, year);
        if (matchResult == -1) {
            hideProgressDialog();
            makeNewUser(year, bioData);
        } else {

            Intent selectionIntent = new Intent(this, SelectionActivity.class);
            selectionIntent.putExtra("name", dbHandler.getName(matchResult, year));
            selectionIntent.putExtra("state", dbHandler.getWhereabouts(matchResult, year));
            selectionIntent.putExtra("year", year);
            hideProgressDialog();
            idleMonitor.nullify();
            startActivityForResult(selectionIntent, REQUEST_SELECTION);
        }

    }

    private void modifyBioData(final long matchResult, final int year) {

        autoOn.stop();

        AlertDialog.Builder bldr = new AlertDialog.Builder(this);
        bldr.setTitle("Fingerprint Verification");
        bldr.setMessage("Place the same finger on scanner and press OK simultaneously to enroll");
        bldr.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //This is overridden after dialog shows
            }
        });
        bldr.setOnDismissListener(this);
        final AlertDialog dialog = bldr.create();
        dialog.show();
        dialog.setOnDismissListener(this);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] biodata = bioHandler.getProcessedBioData();
                dbHandler.updateID(matchResult, biodata, false, year);
                dialog.cancel();
                Toast.makeText(MainActivity.this, dbHandler.getName(matchResult, year) + ", fingerprint modified", Toast.LENGTH_LONG).show();
            }
        });
        hideProgressDialog();
    }

    private void showProgressDialog() {

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }

        mProgressDialog.show();
        hideSysUI();
    }

    private void hideProgressDialog() {

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
        hideSysUI();
    }

    private void showSysUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void hideSysUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(MainActivity.this, "Cannot connect to Google...\nApplication will not be able to access Google Sheets", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        try {
            hideSysUI();
            idleMonitor.setTimer();
            //Wait for 3 seconds after dialog dismiss to prevent duplicate finger scans
            Thread.sleep(3000);
            autoOn.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeviceStateIdle() {
        idleMonitor.nullify();
        startActivity(new Intent(MainActivity.this, IdleActivity.class));
    }
}
