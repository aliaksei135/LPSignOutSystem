/*
 * com.aliakseipilko.signoutsystem.Activities.MainActivity was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 13/05/17 10:31
 */

package com.aliakseipilko.signoutsystem.Activities;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.util.store.DataStore;
import com.google.api.services.sheets.v4.SheetsScopes;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aliakseipilko.signoutsystem.DataHandlers.BiometricDataHandler;
import com.aliakseipilko.signoutsystem.DataHandlers.GoogleSheetsHandler;
import com.aliakseipilko.signoutsystem.DataHandlers.LocalRealmDBHandler;
import com.aliakseipilko.signoutsystem.DataHandlers.models.User;
import com.aliakseipilko.signoutsystem.Helpers.AdminReceiver;
import com.aliakseipilko.signoutsystem.Helpers.FingerprintUiHelper;
import com.aliakseipilko.signoutsystem.Helpers.IdleMonitor;
import com.aliakseipilko.signoutsystem.R;
import com.aliakseipilko.signoutsystem.Services.PersistService;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGAutoOnEventNotifier;
import SecuGen.FDxSDKPro.SGFDxErrorCode;
import SecuGen.FDxSDKPro.SGFingerPresentEvent;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@Keep
public class MainActivity extends AppCompatActivity implements SGFingerPresentEvent, GoogleApiClient.OnConnectionFailedListener, DialogInterface.OnDismissListener, IdleMonitor.IdleCallback {

    private static final int REQUEST_SELECTION = 530;
    private static final int REQUEST_FIRST_LAUNCH = 531;

    private static final String TAG = "MainActivity";

    private static final String NATIVE_HOUSE = "School";
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
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
    @BindView(R.id.manualSigningButton)
    Button manualSigningButton;
    @BindView(R.id.srButton)
    Button scannerRestartButton;
    @BindView(R.id.idleActTestButton)
    Button idleActTestButton;
    @BindView(R.id.flActTestButton)
    Button flActTestButton;
    @BindView(R.id.newUserTestButton)
    Button newUserTestButton;
    @BindView(R.id.disableDebugButton)
    Button disableDebugButton;
    @BindView(R.id.serviceModeButton)
    Button searchDbButton;

    private boolean isVerificationScan = false;
    private boolean isFirstRun;
    private IdleMonitor idleMonitor;
    private ConnectivityManager cm;
    private byte[] currentNewUserBiodata;
    private FingerprintUiHelper uiHelper;
    private Button confirmationButton;
    private JSGFPLib bioLib;
    private ProgressDialog mProgressDialog;
    private DevicePolicyManager mDpm;
    private BiometricDataHandler bioHandler;
    private SGAutoOnEventNotifier autoOn;
    private GoogleSheetsHandler sheetsHandler;
    private final BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (cm == null) {
                cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            }
            if (cm != null) {
                if (cm.getActiveNetworkInfo().isConnected()) {
                    sheetsHandler.dispatchCachedRequests();
                }
            }
        }
    };
    private DataStore<StoredCredential> credentialDataStore;
    private LocalRealmDBHandler dbHandler;

    //Null Constructor
    public MainActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        hideSysUI();
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ComponentName deviceAdmin = new ComponentName(this, AdminReceiver.class);
        mDpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (!mDpm.isAdminActive(deviceAdmin)) {
            Toast.makeText(this, "Not Device Admin!", Toast.LENGTH_SHORT).show();
        }

        if (mDpm.isDeviceOwnerApp(getPackageName())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mDpm.setLockTaskPackages(deviceAdmin, new String[]{getPackageName()});
            }
        }
        enableKioskMode(true);

        //Get rid of the unneeded action bar
        if (getActionBar() != null) {
            getActionBar().hide();
        }

        //Get rid of navbar
        hideSysUI();

        //Initialise helpers and handlers
        sheetsHandler = GoogleSheetsHandler.getInstance(this);
        dbHandler = new LocalRealmDBHandler();
        idleMonitor = IdleMonitor.getInstance();
        idleMonitor.registerIdleCallback(this);
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        idleMonitor.setTimer();
        startService(new Intent(this, PersistService.class));

        //Initialise biometrics
        initBio();
        autoOn = new SGAutoOnEventNotifier(bioLib, this);
        autoOn.start();

        ButterKnife.bind(this);

        manualSigningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idleMonitor.nullify();
                manualIdentify();
            }
        });

        // DEBUG
        scannerRestartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unregisterReceiver(mUsbReceiver);
                initBio();
                autoOn = new SGAutoOnEventNotifier(bioLib, null);
                autoOn.start();
            }
        });
        scannerRestartButton.setVisibility(View.GONE);
        idleActTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idleMonitor.nullify();
                startActivity(new Intent(MainActivity.this, IdleActivity.class));
            }
        });
        idleActTestButton.setVisibility(View.GONE);
        flActTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idleMonitor.nullify();
                startActivityForResult(new Intent(MainActivity.this, FirstLaunch.class), REQUEST_FIRST_LAUNCH);
            }
        });
        flActTestButton.setVisibility(View.GONE);
        newUserTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idleMonitor.nullify();
                makeNewUser(null);
            }
        });
        newUserTestButton.setVisibility(View.GONE);

        disableDebugButton.setVisibility(View.GONE);

        searchDbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idleMonitor.nullify();
                startActivity(new Intent(MainActivity.this, ManageSearchActivity.class));
            }
        });
        searchDbButton.setVisibility(View.GONE);
    }

    private void scheduleResetSignedIn() {

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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
        boolean scheduledTasksSet = sharedPreferences.getBoolean("scheduledTasksSet", false);

        if (isFirstRun) {
            //show start activity
            Toast.makeText(MainActivity.this, "First Run\nPlease sign in", Toast.LENGTH_LONG)
                    .show();
            startActivityForResult(new Intent(MainActivity.this, FirstLaunch.class), REQUEST_FIRST_LAUNCH);
        }

        if (!scheduledTasksSet) {
            scheduleResetSignedIn();
            sharedPreferences.edit().putBoolean("scheduledTasksSet", true).apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSysUI();
        if (!isFirstRun) {
            idleMonitor.setTimer();
        }
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
    protected void onPause() {
        super.onPause();
        idleMonitor.nullify();
        autoOn.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkStateReceiver);
        unregisterReceiver(mUsbReceiver);
        bioLib.CloseDevice();
        bioLib.Close();
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

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {

        //Cleanup after result activity
        System.gc();

        switch (requestCode) {
            case REQUEST_SELECTION:
                hideSysUI();
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {

                    new MakeLogEntryTask().execute(data.getExtras());

                } else if (resultCode == RESULT_CANCELED) {
                    Snackbar sb = Snackbar.make(findViewById(android.R.id.content), "Cancelled!", Snackbar.LENGTH_LONG);
                    View sbv = sb.getView();
                    TextView sbtv = (TextView) sbv.findViewById(android.support.design.R.id.snackbar_text);
                    sbtv.setTextSize(25f);
                    sbv.setBackgroundColor(getResources().getColor(R.color.warning_color));
                    sbv.setMinimumHeight(125);
                    sbv.setMinimumWidth(700);
                    sb.show();
                    onDismiss(null);
                }
                break;
            case REQUEST_FIRST_LAUNCH:
                if (resultCode == RESULT_OK && data != null) {
                    Toast.makeText(MainActivity.this, "All good!", Toast.LENGTH_SHORT).show();
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

    private void showLoadingLogResult() {
        Snackbar sb = Snackbar.make(findViewById(android.R.id.content), "Working on it...", Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout sbv = (Snackbar.SnackbarLayout) sb.getView();
        ProgressBar pb = new ProgressBar(this);
        pb.setIndeterminate(true);
        sbv.addView(pb);
        sbv.setBackgroundColor(getResources().getColor(R.color.neutral_color));
        sbv.setMinimumHeight(125);
        sbv.setMinimumWidth(700);
        sb.show();
    }


    private void showLogResult(boolean result, long id, String name, String location, int year) {
        if (result) {
            onDismiss(null);
            Snackbar sb = Snackbar.make(findViewById(android.R.id.content), "Goodbye " + name + "!", Snackbar.LENGTH_LONG);
            View sbv = sb.getView();
            TextView sbtv = (TextView) sbv.findViewById(android.support.design.R.id.snackbar_text);
            sbtv.setTextSize(25f);
            sbv.setBackgroundColor(getResources().getColor(R.color.success_color));
            sbv.setMinimumHeight(125);
            sbv.setMinimumWidth(700);
            sb.show();
            dbHandler.updateLocation(id, location);
        } else {
            Snackbar sb = Snackbar.make(findViewById(android.R.id.content), "That didn't work!", Snackbar.LENGTH_LONG);
            View sbv = sb.getView();
            TextView sbtv = (TextView) sbv.findViewById(android.support.design.R.id.snackbar_text);
            sbtv.setTextSize(25f);
            sbv.setBackgroundColor(getResources().getColor(R.color.warning_color));
            sbv.setMinimumHeight(125);
            sbv.setMinimumWidth(700);
            sb.show();
        }
        autoOn.start();
    }

    //First data input stage of user enrollment
    private void makeNewUser(final byte[] bioData) {

        Toast.makeText(this, "New User", Toast.LENGTH_LONG).show();

        if (bioData != null) {
            currentNewUserBiodata = bioData;
        }

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_new_user);

        final EditText nameInput = (EditText) dialog.findViewById(R.id.new_user_name_et);

        final Spinner houseSpinner = (Spinner) dialog.findViewById(R.id.new_user_house_sp);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.new_user_houses, R.layout.spinner_holder);
        adapter.setDropDownViewResource(R.layout.spinner_holder);
        houseSpinner.setAdapter(adapter);

        final Spinner yearSpinner = (Spinner) dialog.findViewById(R.id.new_user_year_sp);
        ArrayAdapter<CharSequence> adap = ArrayAdapter.createFromResource(this, R.array.new_user_years, R.layout.spinner_holder);
        adap.setDropDownViewResource(R.layout.spinner_holder);
        yearSpinner.setAdapter(adap);

        final EditText pinField = (EditText) dialog.findViewById(R.id.new_user_pin_et);

        final EditText pinConfirmField = (EditText) dialog.findViewById(R.id.new_user_pin_confirm_et);

        Button positiveButton = (Button) dialog.findViewById(R.id.new_user_yes_btn);
        Button negativeButton = (Button) dialog.findViewById(R.id.new_user_no_btn);

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        dialog.show();

        autoOn.stop();

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check empty fields
                if (!(nameInput.getText().toString().isEmpty())
                        || !(houseSpinner.getSelectedItem().equals("Select your House"))
                        || !(yearSpinner.getSelectedItem().equals("Select your Year"))
                        || !(pinField.getText().toString().isEmpty())
                        || !(pinConfirmField.getText().toString().isEmpty())) {

                    //Check Pin duplication match
                    if (pinField.getText().toString().equals(pinConfirmField.getText().toString())) {

                        String pin = pinConfirmField.getText().toString();

                        //Check PIN collisions in DB and debug access code
                        if (!(dbHandler.checkPINCollision(pin)) || pin.equals("87784190")) {

                            //Check pin sufficient length
                            if (!(pinField.getText().toString().length() <= 4)) {

                                //Check PIN doesnt begin with zero (the zero will end up stripped)
                                if (!(pinField.getText().toString().charAt(0) == '0')) {

                                    pushNewUser(nameInput.getText().toString(),
                                            houseSpinner.getSelectedItem().toString(),
                                            Integer.parseInt(yearSpinner.getSelectedItem().toString()),
                                            pinConfirmField.getText().toString());

                                    //Cancel without dismiss listener set to ensure autoOn remains stopped
                                    dialog.cancel();

                                } else {
                                    pinField.setError("First Digit cannot be zero");
                                }
                            } else {
                                pinField.setError("PIN must be at least 5 digits long");
                            }
                        } else {
                            pinField.setError("Choose another PIN");
                        }
                    } else {
                        pinConfirmField.setError("PINs don't match!");
                    }
                } else {
                    nameInput.setError("Enter ALL the required information");
                }
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set Dismiss listener and dismiss to ensure autoOn starts up
                dialog.setOnDismissListener(MainActivity.this);
                dialog.cancel();
            }
        });
    }

    //Second verification stage of user enrollment
    @SuppressWarnings("ResourceType")
    private void pushNewUser(final String name, final String house, final int year, final String pin) {

//        Toast.makeText(this, "New User Pushed", Toast.LENGTH_LONG).show();

        AlertDialog.Builder bldr = new AlertDialog.Builder(this);
        bldr.setTitle("Confirmation");
        RelativeLayout layout = new RelativeLayout(this);

        ImageView icon = new ImageView(this);
        icon.setId(2);
        icon.setMinimumWidth(80);
        icon.setMinimumHeight(80);
        icon.setPadding(24, 28, 16, 16);
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lp2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layout.addView(icon, lp2);

        TextView status = new TextView(this);
        status.setId(3);
        status.setPadding(16, 40, 24, 16);
        status.setText(getResources().getString(R.string.fingerprint_hint));
        status.setTextSize(24f);
        status.setTextColor(getResources().getColor(R.color.hint_color));
        RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp3.addRule(RelativeLayout.RIGHT_OF, icon.getId());
        layout.addView(status, lp3);

        bldr.setView(layout);

        bldr.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (currentNewUserBiodata == null) {
                    //Something happens
                } else {
                    dbHandler.addNewRecord(name, house, year, pin, currentNewUserBiodata, false);
                    currentNewUserBiodata = null;
                    handlePINID(pin, false);
                }
            }
        });
        bldr.setOnDismissListener(this);
        final AlertDialog verifyDialog = bldr.create();
        verifyDialog.show();

        uiHelper = new FingerprintUiHelper(icon, status);

        confirmationButton = verifyDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        confirmationButton.setPadding(8, 8, 8, 8);
        confirmationButton.setHeight(36);
        confirmationButton.setTextAppearance(this, android.R.style.TextAppearance_Holo_Large);
        confirmationButton.setVisibility(View.GONE);

        isVerificationScan = true;
        autoOn.start();
    }

    private void verifyNewUserScans(byte[] verifyData) {
        if (bioHandler.matchBioDataSets(currentNewUserBiodata, verifyData)) {
            uiHelper.showSuccess();
            confirmationButton.setVisibility(View.VISIBLE);
            autoOn.stop();
        } else {
            uiHelper.showError();
            isVerificationScan = true;
            autoOn.start();
        }
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
//                SGFingerPresentCallback();
                break;
            case "ResetRegistered":
                dbHandler.resetAllToSignedOut();
                break;
            default:
                //Do nothing
                break;
        }
    }

    @Override
    public void SGFingerPresentCallback() {

        autoOn.stop();

        if (isVerificationScan) {
            final byte[] verifyData = bioHandler.getProcessedBioData();
            isVerificationScan = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    verifyNewUserScans(verifyData);
                }
            });
        } else {
            final byte[] bioData = bioHandler.getProcessedBioData();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    idleMonitor.setTimer();
                    handleBioID(bioData);
                }
            });
        }
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
                    makeNewUser(bioData);
                } else {
                    makeNewUser(null);
                }
            }
        });

        builder.setNeutralButton("House Visitor", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                showVisitorSelectDialog(bioData, pin, isBio, modifyBio);
            }
        });

        AlertDialog dialog = builder.show();

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setPadding(150, 8, 8, 8);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextAppearance(this, android.R.style.TextAppearance_Holo_Large);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.neutral_color));
        hideSysUI();
    }

    private void showVisitorSelectDialog(final byte[] bioData, final String pin, final boolean isBio, final boolean modifyBio) {
        final int[] year = new int[1];
        final int[] house = new int[1];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Your House");
        builder.setOnDismissListener(this);
        builder.setItems(R.array.houses, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        //Grove visitor
                        year[0] = LocalRealmDBHandler.GROVE_VISITOR;
                        house[0] = 2;
                        break;
                    case 1:
                        //Fryer Visitor
                        year[0] = LocalRealmDBHandler.FRYER_VISITOR;
                        house[0] = 5;
                        break;
                    case 2:
                        //Reckitt visitor
                        year[0] = LocalRealmDBHandler.RECKITT_VISITOR;
                        house[0] = 4;
                        break;
                    case 3:
                        //Field visitor
                        year[0] = LocalRealmDBHandler.FIELD_VISITOR;
                        house[0] = 3;
                        break;
                    default:
                        year[0] = 13;
                        house[0] = 1;
                        break;
                }
                if (isBio) {
                    makeNewUser(bioData);
                } else {
                    makeNewUser(null);
                }
            }
        });

        builder.setNeutralButton("House Native", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                showYearSelectDialog(bioData, pin, isBio, modifyBio);
            }
        });

        AlertDialog dialog = builder.show();

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setPadding(150, 8, 8, 8);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextAppearance(this, android.R.style.TextAppearance_Holo_Large);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.neutral_color));
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
                    pinField.setError("Enter a PIN");
                } else {
                    handlePINID(pinField.getText().toString(), false);
                }
            }
        });
        bldr.setNegativeButton("Cancel", null);
        bldr.setNeutralButton("Change Fingerprint", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (pinField.getText() == null) {
                    pinField.setError("Enter a PIN");
                } else {
                    handlePINID(pinField.getText().toString(), true);
                }
            }
        });

        AlertDialog dialog = bldr.show();
        dialog.setOnDismissListener(this);

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setPadding(8, 8, 8, 8);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextAppearance(this, android.R.style.TextAppearance_Holo_Large);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.warning_color));

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setPadding(8, 8, 8, 8);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextAppearance(this, android.R.style.TextAppearance_Holo_Large);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.success_color));

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setPadding(8, 8, 8, 8);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextAppearance(this, android.R.style.TextAppearance_Holo_Large);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.neutral_color));

        hideSysUI();
    }

    private void handlePINID(String pin, boolean modifyBio) {
        showProgressDialog();
        autoOn.stop();
        boolean pinExists = dbHandler.checkPINCollision(pin);
        if (pin.equals("87784190")) {
            enableDebugMode();
        }
        if (pinExists) {
            User user = dbHandler.findByPin(pin);
            if (!modifyBio) {
                Intent selectionIntent = new Intent(this, SelectionActivity.class);
                selectionIntent.putExtra("name", user.getName());
                selectionIntent.putExtra("state", user.getWhereabouts());
                selectionIntent.putExtra("year", user.getYear());
                selectionIntent.putExtra("id", user.getId());
                selectionIntent.putExtra("type", "PIN");
                hideProgressDialog();
                idleMonitor.nullify();
                startActivityForResult(selectionIntent, REQUEST_SELECTION);
            } else {
                modifyBioData(user.getId());
            }
        } else {
            Snackbar sb = Snackbar.make(findViewById(android.R.id.content), "PIN incorrect/not found", Snackbar.LENGTH_LONG);
            sb.getView().setBackgroundColor(getResources().getColor(R.color.warning_color));
            sb.show();
            hideProgressDialog();
        }
    }

    private void enableDebugMode() {
        scannerRestartButton.setVisibility(View.VISIBLE);
        idleActTestButton.setVisibility(View.VISIBLE);
        flActTestButton.setVisibility(View.VISIBLE);
        newUserTestButton.setVisibility(View.VISIBLE);
        disableDebugButton.setVisibility(View.VISIBLE);
        searchDbButton.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.disableDebugButton)
    void disableDebugMode() {
        scannerRestartButton.setVisibility(View.GONE);
        idleActTestButton.setVisibility(View.GONE);
        flActTestButton.setVisibility(View.GONE);
        newUserTestButton.setVisibility(View.GONE);
        disableDebugButton.setVisibility(View.GONE);
        searchDbButton.setVisibility(View.GONE);
    }

    private void handleBioID(byte[] bioData) {
        showProgressDialog();
        autoOn.stop();
        long matchResult = bioHandler.matchBioData(bioData);
        if (matchResult == -1) {
            hideProgressDialog();
            makeNewUser(bioData);
        } else {
            Intent selectionIntent = new Intent(this, SelectionActivity.class);
            selectionIntent.putExtra("name", dbHandler.getName(matchResult));
            selectionIntent.putExtra("state", dbHandler.getWhereabouts(matchResult));
            selectionIntent.putExtra("year", dbHandler.getYear(matchResult));
            selectionIntent.putExtra("id", matchResult);
            selectionIntent.putExtra("type", "Fingerprint");
            hideProgressDialog();
            idleMonitor.nullify();
            startActivityForResult(selectionIntent, REQUEST_SELECTION);
        }

    }

    private void modifyBioData(final long matchResult) {

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
                dbHandler.updateID(matchResult, biodata);
                dialog.cancel();
//                Toast.makeText(MainActivity.this, dbHandler.getName(matchResult, year) + ", fingerprint modified", Toast.LENGTH_LONG).show();
                Snackbar sb = Snackbar.make(findViewById(android.R.id.content), dbHandler.getName(matchResult) + ", fingerprint modified", Snackbar.LENGTH_LONG);
                sb.getView().setBackgroundColor(getResources().getColor(R.color.success_color));
                sb.show();
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

    private void enableKioskMode(boolean enabled) {
        try {
            if (enabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (mDpm.isLockTaskPermitted(this.getPackageName())) {
                        startLockTask();
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    stopLockTask();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(MainActivity.this, "Cannot connect to Google...\nApplication will run in offline mode", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        hideSysUI();
        idleMonitor.setTimer();
        //Wait for 3 seconds after dialog dismiss to prevent duplicate finger scans
        Runnable startAutoOn = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                autoOn.start();
            }
        };
        startAutoOn.run();
    }

    @Override
    public void onDeviceStateIdle() {
        idleMonitor.nullify();
        startActivity(new Intent(MainActivity.this, IdleActivity.class));
    }


    private GoogleCredential getCredential() {

        AccountManager am = AccountManager.get(getBaseContext());
        Account[] accounts = am.getAccounts();
        AccountManagerFuture<Bundle> amf = am.getAuthToken(accounts[0], "oauth2:https://www.googleapis.com/auth/spreadsheets", null, true, null, null);
        try {
            return new GetCredentialTask().execute(amf).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class GetCredentialTask extends AsyncTask<AccountManagerFuture<Bundle>, Void, GoogleCredential> {

        @SafeVarargs
        @Override
        protected final GoogleCredential doInBackground(AccountManagerFuture<Bundle>... params) {
            try {
                Bundle result = params[0].getResult();
                GoogleCredential credential = new GoogleCredential();
                credential.setAccessToken(result.getString(AccountManager.KEY_AUTHTOKEN));
                return credential;
            } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private class MakeLogEntryTask extends AsyncTask<Bundle, Integer, Void> {

        final String[] info = new String[3];
        boolean result;
        int year;
        long id;
        String name, location, type;
        GoogleCredential credential;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            credential = getCredential();
            showLoadingLogResult();
        }

        @Override
        protected Void doInBackground(Bundle... params) {
            Bundle extras = params[0];
            name = extras.getString("name");
            location = extras.getString("location");
            type = extras.getString("type");
            year = extras.getInt("year");
            id = extras.getLong("id");
            info[0] = name;
            info[1] = location;
            info[2] = type;
            result = sheetsHandler.makeNewLogEntrySync(info, credential);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            showLogResult(result, id, name, location, year);
        }
    }
}
