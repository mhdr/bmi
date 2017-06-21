package ir.mhdr.bmi;

import android.content.Context;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;


import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ir.hamsaa.persiandatepicker.Listener;
import ir.hamsaa.persiandatepicker.PersianDatePickerDialog;
import ir.hamsaa.persiandatepicker.util.PersianCalendar;
import ir.mhdr.bmi.bl.HistoryBL;
import ir.mhdr.bmi.bl.UserBL;
import ir.mhdr.bmi.lib.FirebaseUtils;
import ir.mhdr.bmi.lib.Gender;
import ir.mhdr.bmi.lib.Resources;
import ir.mhdr.bmi.model.History;
import ir.mhdr.bmi.model.User;
import ir.pupli.jalalicalendarlib.JCalendar;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NewEditProfileActivity extends AppCompatActivity {

    Toolbar toolbarNewProfile;
    List<String> genderList;
    ArrayAdapter<String> spinnerAdapter;
    AppCompatEditText editTextNewProfileName;
    AppCompatSpinner spinnerNewProfileGender;
    AppCompatEditText editTextNewProfileBirthdate;
    AppCompatEditText editTextNewProfileHeight;
    AppCompatEditText editTextNewProfileWeight;
    AppCompatTextView textViewNewProfileName;
    AppCompatButton buttonNewProfileSave;

    boolean editMode = false;
    User userToEdit;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_edit_profile);

        if (FirebaseUtils.checkPlayServices(this)) {
            // Obtain the FirebaseAnalytics instance.
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        }

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            if (bundle.containsKey("userId")) {
                editMode = true;
                UserBL userBL = new UserBL(NewEditProfileActivity.this);
                userToEdit = userBL.getUser(bundle.getLong("userId"));
            }
        }

        toolbarNewProfile = (Toolbar) findViewById(R.id.toolbarNewProfile);
        setSupportActionBar(toolbarNewProfile);

        if (editMode) {
            getSupportActionBar().setTitle(R.string.edit_profile);
        } else {
            getSupportActionBar().setTitle(R.string.new_profile);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        textViewNewProfileName = (AppCompatTextView) findViewById(R.id.textViewNewProfileName);
        textViewNewProfileName.requestFocusFromTouch();

        editTextNewProfileName = (AppCompatEditText) findViewById(R.id.editTextNewProfileName);
        spinnerNewProfileGender = (AppCompatSpinner) findViewById(R.id.spinnerNewProfileGender);
        editTextNewProfileBirthdate = (AppCompatEditText) findViewById(R.id.editTextNewProfileBirthdate);
        editTextNewProfileHeight = (AppCompatEditText) findViewById(R.id.editTextNewProfileHeight);
        editTextNewProfileWeight = (AppCompatEditText) findViewById(R.id.editTextNewProfileWeight);
        buttonNewProfileSave = (AppCompatButton) findViewById(R.id.buttonNewProfileSave);

        Resources resources = new Resources(NewEditProfileActivity.this);
        genderList = resources.getGenderList();

        spinnerAdapter = new ArrayAdapter<String>(NewEditProfileActivity.this, R.layout.simple_spinner_dropdown_item_rtl, genderList);
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_rtl);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNewProfileGender.setAdapter(spinnerAdapter);

        buttonNewProfileSave.setOnClickListener(buttonNewProfileSave_OnClickListener);

        editTextNewProfileBirthdate.setOnClickListener(editTextNewProfileBirthdate_OnClickListener);
        editTextNewProfileBirthdate.setOnFocusChangeListener(editTextNewProfileBirthdate_OnFocusChangeListener);

        editTextNewProfileHeight.setOnFocusChangeListener(editTextNewProfileHeight_OnFocusChangeListener);
        editTextNewProfileHeight.setOnClickListener(editTextNewProfileHeight_OnClickListener);

        editTextNewProfileWeight.setOnFocusChangeListener(editTextNewProfileWeight_OnFocusChangeListener);
        editTextNewProfileWeight.setOnClickListener(editTextNewProfileWeight_OnClickListener);

        if (editMode) {
            // load data

            editTextNewProfileName.setText(userToEdit.getName());

            if (userToEdit.getGenderX() == Gender.Male) {
                spinnerNewProfileGender.setSelection(0);
            } else if (userToEdit.getGenderX() == Gender.Female) {
                spinnerNewProfileGender.setSelection(1);
            }

            String pattern = "yyyy-MM-dd";

            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            Date birth = null;

            try {
                birth = dateFormat.parse(userToEdit.getBirthdate());
            } catch (ParseException e) {
                e.printStackTrace();
                FirebaseCrash.report(e);
            }

            DateTime dateTime = new DateTime(birth);

            JCalendar jCalendar = new JCalendar(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), true);
            String dateStr = jCalendar.toString();

            editTextNewProfileBirthdate.setText(dateStr);
            editTextNewProfileHeight.setText(userToEdit.getLatestHeight());
            editTextNewProfileWeight.setText(userToEdit.getLatestWeight());
        }
    }

    private void openHeightDialog() {
        String valueStr = editTextNewProfileHeight.getText().toString();

        HeightFragment heightFragment = new HeightFragment();

        if (valueStr.length() > 0) {
            heightFragment.setHeightValue(Integer.parseInt(valueStr));
        }

        heightFragment.setOnSaveListener(new HeightFragment.OnSaveListener() {
            @Override
            public void onSave(int value) {
                editTextNewProfileHeight.setText(String.valueOf(value));
            }
        });

        heightFragment.show(getSupportFragmentManager(), "height");
    }

    private void openWeightDialog() {
        String valueStr = editTextNewProfileWeight.getText().toString();

        WeightFragment weightFragment = new WeightFragment();

        if (valueStr.length() > 0) {
            weightFragment.setWeightValue(Double.parseDouble(valueStr));
        }

        weightFragment.setOnSaveListener(new WeightFragment.OnSaveListener() {
            @Override
            public void onSave(double value) {
                editTextNewProfileWeight.setText(String.valueOf(value));
            }
        });
        weightFragment.show(getSupportFragmentManager(), "weight");
    }

    private void openPersianDatePickerDialog() {

        String previousStr = editTextNewProfileBirthdate.getText().toString();
        PersianCalendar initDate = null;

        if (previousStr.length() > 0) {
            String[] previous = previousStr.split("/");
            PersianCalendar previousBirthDate = new PersianCalendar();

            int year = Integer.parseInt(previous[0]);
            int month = Integer.parseInt(previous[1]);
            int day = Integer.parseInt(previous[2]);

            previousBirthDate.setPersianDate(year, month, day);
            initDate = previousBirthDate;
        } else {
            initDate = new PersianCalendar();
            initDate.setPersianDate(1364, 3, 1);
        }

        PersianDatePickerDialog dialog = new PersianDatePickerDialog(NewEditProfileActivity.this)
                .setPositiveButtonString("تائید")
                .setNegativeButton("انصراف")
                .setTodayButton("امروز")
                .setTodayButtonVisible(false)
                .setInitDate(initDate)
                .setMaxYear(1450)
                .setMinYear(1300)
                .setListener(new Listener() {
                    @Override
                    public void onDateSelected(PersianCalendar persianCalendar) {
                        int year = persianCalendar.getPersianYear();
                        int month = persianCalendar.getPersianMonth();
                        int day = persianCalendar.getPersianDay();

                        String output = String.format(Locale.US, "%s/%s/%s", year, month, day);
                        editTextNewProfileBirthdate.setText(output);
                    }

                    @Override
                    public void onDisimised() {

                    }
                });

        dialog.show();
    }

    View.OnClickListener buttonNewProfileSave_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            int validationError = 0;

            String name = editTextNewProfileName.getText().toString();

            if (name.length() == 0) {
                validationError++;
            }

            String genderStr = (String) spinnerNewProfileGender.getSelectedItem();

            if (genderStr.length() == 0) {
                validationError++;
            }

            String birthdateStr = editTextNewProfileBirthdate.getText().toString();

            if (birthdateStr.length() == 0) {
                validationError++;
            }

            String height = editTextNewProfileHeight.getText().toString();

            if (height.length() == 0) {
                validationError++;
            }

            String weight = editTextNewProfileWeight.getText().toString();

            if (weight.length() == 0) {
                validationError++;
            }

            if (validationError > 0) {
                Toast.makeText(getApplicationContext(), R.string.new_profile_validation_msg, Toast.LENGTH_LONG).show();

                return;
            }

            Gender gender = Gender.Male;

            switch (genderStr) {
                case "مرد":
                    gender = Gender.Male;
                    break;
                case "زن":
                    gender = Gender.Female;
                    break;
            }

            String[] birthdateArray = birthdateStr.split("/");
            int year = Integer.parseInt(birthdateArray[0]);
            int month = Integer.parseInt(birthdateArray[1]);
            int day = Integer.parseInt(birthdateArray[2]);

            JCalendar jCalendar=new JCalendar(year, month, day);
            DateTime birthdate = new DateTime(jCalendar.toGregorianDate());

            if (editMode) {
                UserBL userBL = new UserBL(NewEditProfileActivity.this);

                User user = userToEdit;
                user.setName(name);
                user.setGenderX(gender);
                user.setBirthdate(birthdate.toString());
                user.setLatestHeight(height);
                user.setLatestWeight(weight);

                userBL.update(user);

                HistoryBL historyBL = new HistoryBL(NewEditProfileActivity.this);
                History history = historyBL.getLastHistory(user);
                history.setValue(weight);

                historyBL.update(history);

                Toast.makeText(getApplicationContext(), R.string.profile_edit_successful, Toast.LENGTH_LONG).show();

                NavUtils.navigateUpFromSameTask(NewEditProfileActivity.this); // return to parent activity

            } else {
                User user = new User();
                user.setName(name);
                user.setGenderX(gender);
                user.setBirthdate(birthdate.toString());
                user.setLatestHeight(height);
                user.setLatestWeight(weight);
                user.setIsActiveX(false);

                UserBL userBL = new UserBL(NewEditProfileActivity.this);
                long id = userBL.insert(user);

                // assign the created id for using this instance of User class later
                user.setId(id);

                if (id > 0) {

                    HistoryBL historyBL = new HistoryBL(NewEditProfileActivity.this);

                    DateTime current = new DateTime();

                    History history = new History();
                    history.setUserId(id);
                    history.setValue(weight);
                    history.setDatetime(current.toString());

                    long historyId = historyBL.insert(history);

                    if (historyId > 0) {
                        Toast.makeText(getApplicationContext(), R.string.profile_created_successful_msg, Toast.LENGTH_LONG).show();

                        userBL.SwitchActiveUser(user);

                        NavUtils.navigateUpFromSameTask(NewEditProfileActivity.this); // return to parent activity
                    }
                }
            }
        }
    };

    View.OnFocusChangeListener editTextNewProfileBirthdate_OnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                openPersianDatePickerDialog();
            }

        }
    };

    View.OnClickListener editTextNewProfileBirthdate_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openPersianDatePickerDialog();
        }
    };

    View.OnFocusChangeListener editTextNewProfileHeight_OnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                openHeightDialog();
            }

        }
    };

    View.OnClickListener editTextNewProfileHeight_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openHeightDialog();
        }
    };

    View.OnFocusChangeListener editTextNewProfileWeight_OnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                openWeightDialog();
            }
        }
    };

    View.OnClickListener editTextNewProfileWeight_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openWeightDialog();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                //Intent intent=new Intent(ProfileActivity.this,MainActivity.class);
                //NavUtils.navigateUpTo(ProfileActivity.this,intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        NavUtils.navigateUpFromSameTask(this);

        super.onBackPressed();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
