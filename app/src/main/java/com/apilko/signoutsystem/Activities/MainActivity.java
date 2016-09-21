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
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGAutoOnEventNotifier;
import SecuGen.FDxSDKPro.SGFingerPresentEvent;

@Keep
public class MainActivity extends AppCompatActivity implements SGFingerPresentEvent, GoogleApiClient.OnConnectionFailedListener {


    ////////////////////////DEFINE CONSTANTS////////////////////////////////////
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


        Button manualSigningButton = (Button) findViewById(R.id.manualSigningButton);

        Button test1Button = (Button) findViewById(R.id.test1button);
        Button idleActTestButton = (Button) findViewById(R.id.idleActTestButton);
        Button flActTestButton = (Button) findViewById(R.id.flActTestButton);
        Button newUserTestButton = (Button) findViewById(R.id.newUserTestButton);
        Button matchBioTestButton = (Button) findViewById(R.id.bioTestButton);

        //USB Permissions
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);

//        bioLib = new JSGFPLib((UsbManager) getSystemService(Context.USB_SERVICE));
//        try {
//            bioHandler = biometricDataHandler.getInstance(bioLib, this);
//        } catch (Resources.NotFoundException e) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setCancelable(false);
//            builder.setTitle("Device not connected");
//            builder.setMessage("Restart app with device connected");
//            builder.show();
//        }

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

                showYearSelectDialog();
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
        assert matchBioTestButton != null;
        matchBioTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                int matchResult = bioHandler.getAndMatchBioDataTest(debugBytes, 13);
                if (matchResult == -1) {
                    hideProgressDialog();
                    makeNewUser();
                } else {
                    hideProgressDialog();
                    Intent selectionIntent = new Intent(MainActivity.this, SelectionActivity.class);
                    selectionIntent.putExtra("name", dbHandler.getName((long) matchResult, 13));
                    selectionIntent.putExtra("state", dbHandler.getWhereabouts((long) matchResult, 13));
                    startActivityForResult(selectionIntent, REQUEST_SELECTION);
                }
            }
        });

//        autoOn = new SGAutoOnEventNotifier(bioLib, this);
//        autoOn.start();

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

    @Override
    protected void onStart() {

        super.onStart();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);

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

                        MainActivity.serverAuthCode = googleSignInResult.getSignInAccount().getServerAuthCode();
                        hideProgressDialog();
                        Log.d(TAG, "handleSignInResult:" + googleSignInResult.isSuccess());
                    }
                });
            }
        }

        sharedPreferences.edit().putBoolean("isFirstRun", false).commit();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
//        bioLib.CloseDevice();
//        bioLib.Close();
    }


    private void manualIdentify() {
        //TODO Manual Identification
        Toast.makeText(MainActivity.this, "Not implemented yet!", Toast.LENGTH_SHORT).show();
    }

    private void debugMethod() {
        //Create db record first
        dbHandler.addNewRecord("Steve", "School", 13, debugBytes, false);
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
                    //Display confirmation
                    Toast.makeText(this, name + type, Toast.LENGTH_LONG).show();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
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

    @SuppressWarnings("deprecation")
    private void makeNewUser() {

        Toast.makeText(this, "New User", Toast.LENGTH_LONG).show();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Name and House");

        LinearLayout layout = new LinearLayout(this);
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

        final Spinner yearSpinner = new Spinner(this);
        final ArrayAdapter<String> adap = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new String[]{"Select your year", "7", "8", "9", "10", "11", "12", "13"});
        adap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(adap);
        layout.addView(yearSpinner);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (!(nameInput.getText() == null) || !(houseSpinner.getSelectedItem().equals("Select your House")) || !(yearSpinner.getSelectedItem().equals("Select your year"))) {
                    dialog.cancel();
                    dbHandler.addNewRecord(nameInput.getText().toString(), houseSpinner.getSelectedItem().toString(), Integer.parseInt(yearSpinner.getSelectedItem().toString()), getRescan(), false);
                }
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                    }
                });

        builder.setView(layout);
        builder.show();
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
            default:
                //Do nothing
                break;
        }
    }

    @Override
    public void SGFingerPresentCallback() {

        autoOn.stop();
        showYearSelectDialog();
    }

    private void showYearSelectDialog() {
        final int[] year = new int[1];

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

                handleBioID(year[0]);
            }
        });

        builder.setNeutralButton("House Visitor", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showVisitorSelectDialog();
            }
        });

        builder.create().show();
    }

    private void showVisitorSelectDialog() {
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
                handleBioID(year[0]);
            }
        });
        builder.create().show();
    }

    private void handleBioID(int year) {
        showProgressDialog();
        int matchResult = bioHandler.getAndMatchBioData(year);
        if (matchResult == -1) {
            hideProgressDialog();
            makeNewUser();
        } else {

            Intent selectionIntent = new Intent(this, SelectionActivity.class);
            selectionIntent.putExtra("name", dbHandler.getName((long) matchResult, year));
            selectionIntent.putExtra("state", dbHandler.getWhereabouts((long) matchResult, year));
            hideProgressDialog();
            startActivityForResult(selectionIntent, REQUEST_SELECTION);
        }

    }

    private byte[] getRescan() {

        Toast.makeText(this, "New User Registration\nScan same finger again", Toast.LENGTH_LONG).show();
        return null;
//        //Wait for 2.5 seconds for user interaction
//        try {
//            Thread.sleep(2500);
//
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        return bioHandler.getProcessedBioData();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(MainActivity.this, "Cannot connect to Google...\nApplication will now close", Toast.LENGTH_LONG).show();
//        finish();
    }

}
