package com.apilko.signoutsystem.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.apilko.signoutsystem.Fragments.stateAtGreenFragment;
import com.apilko.signoutsystem.Fragments.stateSignedInFragment;
import com.apilko.signoutsystem.Fragments.stateSignedOutFragment;
import com.apilko.signoutsystem.Fragments.stateStudyPeriodFragment;
import com.apilko.signoutsystem.Fragments.stateVisitHouseFragment;
import com.apilko.signoutsystem.R;

public class SelectionActivity extends AppCompatActivity implements
        stateSignedInFragment.OnFragmentInteractionListener,
        stateSignedOutFragment.OnFragmentInteractionListener,
        stateAtGreenFragment.OnFragmentInteractionListener,
        stateStudyPeriodFragment.OnFragmentInteractionListener,
        stateVisitHouseFragment.OnFragmentInteractionListener {

    private String name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        Intent intent = getIntent();
        String state = intent.getStringExtra("state");
        name = intent.getStringExtra("name");
        String serverAuthCode = intent.getStringExtra("serverAuthCode");

        TextView currentStateTextView = (TextView) findViewById(R.id.currentStateTextView);
        if (currentStateTextView != null) {
            currentStateTextView.setText(state);
        }

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            switch (state) {
                case "SIGN_IN":
                    stateSignedInFragment stateSignedInFrag = stateSignedInFragment.newInstance();
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateSignedInFrag).commit();
                    break;
                case "Signed In":
                    stateSignedInFrag = stateSignedInFragment.newInstance();
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateSignedInFrag).commit();
                    break;
                case "SIGN_OUT":
                    stateSignedOutFragment stateSignedOutFrag = stateSignedOutFragment.newInstance();
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateSignedOutFrag).commit();
                    break;
                case "Signed Out":
                    stateSignedOutFrag = stateSignedOutFragment.newInstance();
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateSignedOutFrag).commit();
                    break;
                case "AT_GREEN":
                    stateAtGreenFragment stateAtGreenFrag = stateAtGreenFragment.newInstance();
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateAtGreenFrag).commit();
                    break;
                case "Gone to Green":
                    stateAtGreenFrag = stateAtGreenFragment.newInstance();
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateAtGreenFrag).commit();
                    break;
                case "STUDY_PERIOD":
                    stateStudyPeriodFragment stateStudyPeriodFrag = stateStudyPeriodFragment.newInstance();
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateStudyPeriodFrag).commit();
                    break;
                case "Study Period":
                    stateStudyPeriodFrag = stateStudyPeriodFragment.newInstance();
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateStudyPeriodFrag).commit();
                    break;
                case "VISIT_HOUSE_FIELD":
                    stateVisitHouseFragment stateVisitFieldHouseFrag = stateVisitHouseFragment.newInstance("FIELD");
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateVisitFieldHouseFrag).commit();
                    break;
                case "Visiting Field":
                    stateVisitFieldHouseFrag = stateVisitHouseFragment.newInstance("FIELD");
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateVisitFieldHouseFrag).commit();
                    break;
                case "VISIT_HOUSE_GROVE":
                    stateVisitHouseFragment stateVisitGroveHouseFrag = stateVisitHouseFragment.newInstance("GROVE");
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateVisitGroveHouseFrag).commit();
                    break;
                case "Visiting Grove":
                    stateVisitGroveHouseFrag = stateVisitHouseFragment.newInstance("GROVE");
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateVisitGroveHouseFrag).commit();
                    break;
                case "VISIT_HOUSE_RECKITT":
                    stateVisitHouseFragment stateVisitReckittHouseFrag = stateVisitHouseFragment.newInstance("RECKITT");
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateVisitReckittHouseFrag).commit();
                    break;
                case "Visiting Reckitt":
                    stateVisitReckittHouseFrag = stateVisitHouseFragment.newInstance("RECKITT");
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateVisitReckittHouseFrag).commit();
                    break;
                case "VISIT_HOUSE_FRYER":
                    stateVisitHouseFragment stateVisitFryerHouseFrag = stateVisitHouseFragment.newInstance("FRYER");
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateVisitFryerHouseFrag).commit();
                    break;
                case "Visiting Fryer":
                    stateVisitFryerHouseFrag = stateVisitHouseFragment.newInstance("FRYER");
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateVisitFryerHouseFrag).commit();
                    break;
                default:
                    //Display all possible states fragment
                    //TODO Handle this
//                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, currentFrag).commit();
                    break;
            }
        }
    }

    @Override
    //Fragment Listener interface implementation
    public void onFragmentInteraction(String type) {
        Intent result = new Intent();
        switch (type) {
            case "CANCEL":
                setResult(RESULT_CANCELED);
                finish();
                break;
            case "SIGN_OUT":
                result.putExtra("name", name);
                result.putExtra("location", "Signed Out");
                //Not functional for now, added for compatibility with other methods in future eg NFC
                result.putExtra("type", "Fingerprint");
                setResult(RESULT_OK, result);
                finish();
                break;
            case "GREEN":
                result.putExtra("name", name);
                result.putExtra("location", "Gone to Green");
                //Not functional for now, added for compatibility with other methods in future eg NFC
                result.putExtra("type", "Fingerprint");
                setResult(RESULT_OK, result);
                finish();
                break;
            case "STUDY_PERIOD":
                result.putExtra("name", name);
                result.putExtra("location", "Study Period");
                //Not functional for now, added for compatibility with other methods in future eg NFC
                result.putExtra("type", "Fingerprint");
                setResult(RESULT_OK, result);
                finish();
                break;
            case "VISIT_HOUSE_FIELD":
                result.putExtra("name", name);
                result.putExtra("location", "Visiting Field");
                //Not functional for now, added for compatibility with other methods in future eg NFC
                result.putExtra("type", "Fingerprint");
                setResult(RESULT_OK, result);
                finish();
                break;
            case "VISIT_HOUSE_GROVE":
                result.putExtra("name", name);
                result.putExtra("location", "Visiting Grove");
                //Not functional for now, added for compatibility with other methods in future eg NFC
                result.putExtra("type", "Fingerprint");
                setResult(RESULT_OK, result);
                finish();
                break;
            case "VISIT_HOUSE_RECKITT":
                result.putExtra("name", name);
                result.putExtra("location", "Visiting Reckitt");
                //Not functional for now, added for compatibility with other methods in future eg NFC
                result.putExtra("type", "Fingerprint");
                setResult(RESULT_OK, result);
                finish();
                break;
            case "VISIT_HOUSE_FRYER":
                result.putExtra("name", name);
                result.putExtra("location", "Visiting Fryer");
                //Not functional for now, added for compatibility with other methods in future eg NFC
                result.putExtra("type", "Fingerprint");
                setResult(RESULT_OK, result);
                finish();
                break;
            case "SIGN_IN":
                result.putExtra("name", name);
                result.putExtra("location", "Signed In");
                //Not functional for now, added for compatibility with other methods in future eg NFC
                result.putExtra("type", "Fingerprint");
                setResult(RESULT_OK, result);
                finish();
                break;
            default:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }
}
