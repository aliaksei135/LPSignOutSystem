package com.apilko.signoutsystem.Activities;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
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

import com.apilko.signoutsystem.DataHandlers.BiometricDataHandler;
import com.apilko.signoutsystem.DataHandlers.GoogleSheetsHandler;
import com.apilko.signoutsystem.DataHandlers.LocalDatabaseHandler;
import com.apilko.signoutsystem.R;

import java.util.Calendar;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGAutoOnEventNotifier;
import SecuGen.FDxSDKPro.SGFingerPresentEvent;

@Keep
public class MainActivity extends AppCompatActivity implements SGFingerPresentEvent, GoogleApiClient.OnConnectionFailedListener, DialogInterface.OnDismissListener {

    private static final int REQUEST_SELECTION = 530;
    private static final int REQUEST_FIRST_LAUNCH = 531;

    private static final byte[] debugBytes = hexStringToByteArray("098316f697ab78cbf64f");

    private static final String TAG = "MainActivity";

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static String serverAuthCode;
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
    private GoogleAccountCredential mCredential;
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

    private static byte[] hexStringToByteArray(String s) {

        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get rid of the unneeded action bar
        if (getActionBar() != null) {
            getActionBar().hide();
        }

        Button manualSigningButton = (Button) findViewById(R.id.manualSigningButton);

        Button test1Button = (Button) findViewById(R.id.test1button);
        Button idleActTestButton = (Button) findViewById(R.id.idleActTestButton);
        Button flActTestButton = (Button) findViewById(R.id.flActTestButton);
        Button newUserTestButton = (Button) findViewById(R.id.newUserTestButton);


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

        sheetsHandler = GoogleSheetsHandler.getInstance(this);
        dbHandler = LocalDatabaseHandler.getInstance(this);


        assert manualSigningButton != null;
        manualSigningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                manualIdentify();
            }
        });

        assert test1Button != null;
        test1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                debugMethod();
            }
        });
        assert idleActTestButton != null;
        idleActTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, IdleActivity.class));
            }
        });
        assert flActTestButton != null;
        flActTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivityForResult(new Intent(MainActivity.this, FirstLaunch.class), REQUEST_FIRST_LAUNCH);
            }
        });
        assert newUserTestButton != null;
        newUserTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeNewUser();
            }
        });


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

            autoOn = new SGAutoOnEventNotifier(bioLib, this);
            autoOn.start();

        }

        if (!scheduledTasksSet) {
            scheduleResetRegistered();
            sharedPreferences.edit().putBoolean("scheduledTasksSet", true).commit();
        }

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
        bioLib.CloseDevice();
        bioLib.Close();
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
                    showYearSelectDialog(null, pinField.getText().toString(), false);
                }
            }
        });
        bldr.setNegativeButton("Cancel", null);

        bldr.create().show();
    }

    private void handlePINID(String text, int year) {
        long pin = Long.parseLong(text);
        showProgressDialog();
        boolean pinExists = dbHandler.checkPINCollision(year, pin);
        if (pinExists) {
            int matchResult = 0;
            long numRecords = dbHandler.getRecordNum(year);
            if (numRecords <= 0) {
                matchResult = -1;
            } else {
                for (int i = 0; i < numRecords; i++) {
                    long dbPin = dbHandler.getPin(i, year);
                    if (dbPin == pin) {
                        matchResult = i;
                        break;
                    }
                    matchResult++;
                }
            }
            if (matchResult == -1) {
                hideProgressDialog();
                makeNewUser();
            } else {

                Intent selectionIntent = new Intent(this, SelectionActivity.class);
                selectionIntent.putExtra("name", dbHandler.getName((long) matchResult, year));
                selectionIntent.putExtra("state", dbHandler.getWhereabouts((long) matchResult, year));
                selectionIntent.putExtra("year", year);
                hideProgressDialog();
                startActivityForResult(selectionIntent, REQUEST_SELECTION);
            }
        }
    }

    private void debugMethod() {
        //Create db record first
        dbHandler.addNewRecord("Steve", "School", 9, 7700737, debugBytes, false);
        //Send off to selection activity
        Intent selectionIntent = new Intent(this, SelectionActivity.class);
        selectionIntent.putExtra("name", "Steve");
        selectionIntent.putExtra("state", dbHandler.getWhereabouts("Steve", 13));
        startActivityForResult(selectionIntent, REQUEST_SELECTION);
    }

    private void showProgressDialog() {

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
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
                    int year = data.getIntExtra("year", 13);

                    if (sheetsHandler.makeNewLogEntry(info, serverAuthCode)) {
                        dbHandler.updateLocation(name, location, year);
                        Toast.makeText(this, "All done!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "That didn't work! Try again!", Toast.LENGTH_SHORT).show();
                    }
                    onDismiss(null);
                    //Display confirmation
                    Toast.makeText(this, "Goodbye " + name + "!", Toast.LENGTH_LONG).show();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    onDismiss(null);
                }
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
                        mCredential.setSelectedAccountName(accountName);
                    }

                    autoOn.start();

                } else {
                    if (data != null) {
                        Toast.makeText(MainActivity.this, data.getStringExtra("result"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "That didn't work", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void makeNewUser() {

        Toast.makeText(this, "New User", Toast.LENGTH_LONG).show();

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Name and House");

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView nameTitle = new TextView(this);
        nameTitle.setText("Full Name");
        nameTitle.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Medium);
        layout.addView(nameTitle);

        final EditText nameInput = new EditText(this);
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(nameInput);

        final TextView houseTitle = new TextView(this);
        houseTitle.setText("House");
        houseTitle.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Medium);
        layout.addView(houseTitle);

        final Spinner houseSpinner = new Spinner(this);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Select your House", "School", "Grove", "Field", "Reckitt", "Fryer"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        houseSpinner.setAdapter(adapter);
        layout.addView(houseSpinner);

        final TextView yearTitle = new TextView(this);
        houseTitle.setText("Year");
        houseTitle.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Medium);
        layout.addView(yearTitle);

        final Spinner yearSpinner = new Spinner(this);
        final ArrayAdapter<String> adap = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Select your year", "7", "8", "9", "10", "11", "12", "13"});
        adap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(adap);
        layout.addView(yearSpinner);

        final TextView pinTitle = new TextView(this);
        pinTitle.setText("Enter a PIN as a backup");
        pinTitle.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Medium);
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

        builder.setView(layout);
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();
        dialog.show();

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

                                pushNewUser(nameInput.getText().toString(),
                                        houseSpinner.getSelectedItem().toString(),
                                        Integer.parseInt(yearSpinner.getSelectedItem().toString()),
                                        Integer.parseInt(pinConfirmField.getText().toString()));

                                dialog.cancel();
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
                showYearSelectDialog(bioData, null, true);
            }
        });
    }

    private void showYearSelectDialog(final byte[] bioData, final String pin, final boolean isBio) {
        final int[] year = new int[1];

        hideProgressDialog();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose your Year");
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
                    handlePINID(pin, year[0]);
                }
            }
        });

        builder.setNeutralButton("House Visitor", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showVisitorSelectDialog(bioData);
            }
        });
        builder.create().show();
    }

    private void showVisitorSelectDialog(final byte[] bioData) {
        final int[] year = new int[1];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Your House");
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
        builder.create().show();
    }

    private void handleBioID(int year, byte[] bioData) {
        showProgressDialog();
        int matchResult = bioHandler.matchBioData(bioData, year);
        if (matchResult == -1) {
            hideProgressDialog();
            makeNewUser();
        } else {

            Intent selectionIntent = new Intent(this, SelectionActivity.class);
            selectionIntent.putExtra("name", dbHandler.getName((long) matchResult, year));
            selectionIntent.putExtra("state", dbHandler.getWhereabouts((long) matchResult, year));
            selectionIntent.putExtra("year", year);
            hideProgressDialog();
            startActivityForResult(selectionIntent, REQUEST_SELECTION);
        }

    }

    private void pushNewUser(final String name, final String house, final int year, final int pin) {

        Toast.makeText(this, "New User Pushed", Toast.LENGTH_LONG).show();

        AlertDialog.Builder bldr = new AlertDialog.Builder(this);
        bldr.setTitle("New User Enrollment");
        bldr.setMessage("Place same finger on scanner and press OK to enroll");
        bldr.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                byte[] bioData = bioHandler.getProcessedBioData();
                dbHandler.addNewRecord(name, house, year, pin, bioData, false);
                Toast.makeText(MainActivity.this, name + ", you are now enrolled", Toast.LENGTH_LONG).show();
            }
        });
        bldr.setOnDismissListener(this);
        bldr.show();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(MainActivity.this, "Cannot connect to Google...\nApplication will not be able to access Google Sheets", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        try {
            Thread.sleep(1500);
            autoOn.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
