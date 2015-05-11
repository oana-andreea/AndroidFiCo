package ro.moa.financial.consultant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ro.moa.financial.consultant.contract.FinancialContract;
import ro.moa.financial.consultant.service.CAENIntentService;

import static android.widget.Toast.makeText;


@SuppressWarnings("ALL")
public class FreelancerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    // Identifies a particular Loader being used in this component
    private static final int LOCATION_LOADER = 0;
    private static final int CODE_LOADER = 1;

    private ArrayAdapter<String> locationAdapter;
    private ArrayAdapter<String> codesAdapter;
    private String selectedLocation;

    private AutoCompleteTextView selectedCode;
    private EditText income;
    private EditText age;
    private CheckBox employee;
    private Switch gender;
    private Spinner locationSpinner;
    private Button calculateSalary;
    private final Map<String, Double> normByCode = new HashMap<>();
    private final Map<String, String> idByLocation = new TreeMap<>();
    private StatusReceiver receiver;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FreelancerFragment newInstance(int sectionNumber) {
        FreelancerFragment fragment = new FreelancerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);

        return fragment;
    }

    public FreelancerFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_freelancer, container, false);

        codesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        codesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectedCode = (AutoCompleteTextView) rootView.findViewById(R.id.codeId);
        selectedCode.setThreshold(1);
        selectedCode.setAdapter(codesAdapter);

        //SalaryFragment salaryFragment = (SalaryFragment)getSupportFragmentManager().findFragmentById(R.id.salary);
        income = (EditText) rootView.findViewById(R.id.income);
        income.setText(Utils.getPreferredStringValue(getActivity(), R.string.pref_key_salary, R.string.pref_default_salary));
        income.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString(getActivity().getString(R.string.pref_key_salary), s.toString()).commit();
            }
        });

        age = (EditText) rootView.findViewById(R.id.ageAmount);
        age.setText(Utils.getPreferredStringValue(getActivity(), R.string.pref_key_age, R.string.pref_default_age));
        age.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString(getActivity().getString(R.string.pref_key_age), s.toString()).commit();
            }
        });
        employee = (CheckBox) rootView.findViewById(R.id.employee);
        gender = (Switch) rootView.findViewById(R.id.switchGender);
        gender.setChecked(Utils.getPreferredBooleanValue(getActivity(), R.string.pref_key_gender, false));
        gender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(getString(R.string.pref_key_gender), isChecked);
            }
        });

        //Create an ArrayAdapter using the string array and a default spinner layout
        locationAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner = (Spinner) rootView.findViewById(R.id.spinner);
        locationSpinner.setAdapter(locationAdapter);
        locationSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        final String selectedLocation = String.valueOf(locationAdapter.getItem(position));
                        if (selectedLocation.equals(FreelancerFragment.this.selectedLocation)) {
                            return;
                        }
                        FreelancerFragment.this.selectedLocation = selectedLocation;
                        Log.e("SelectedItem", selectedLocation);
                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString(getActivity().getString(R.string.pref_key_location), selectedLocation).commit();
                        calculateSalary.setEnabled(false);
                        Utils.notifyUser(getActivity(), String.format("Fetching data for %s, please wait...", selectedLocation));
                        unregisterReceiver();
                        receiver = new StatusReceiver();
                        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter(CAENIntentService.STATUS));

                        Intent intent = new Intent(getActivity(), CAENIntentService.class);
                        intent.putExtra(CAENIntentService.LOCATION, selectedLocation);
                        getActivity().startService(intent);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );
        calculateSalary = (Button) rootView.findViewById(R.id.CalculateSalary);
        calculateSalary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Double norm = normByCode.get(selectedCode.getText().toString());
                if (norm != null && norm != 0) {
                    Intent detail = new Intent(getActivity(), DetailActivity.class);
                    detail.putExtra(DetailActivity.TYPE, DetailActivity.FREELANCER);
                    detail.putExtra(DetailFreelancerFragment.NORM, norm);
                    detail.putExtra(DetailFreelancerFragment.INCOME, Utils.getValueOrDefault(income.getText().toString(), 0d));
                    detail.putExtra(DetailFreelancerFragment.GENDER, gender.isChecked() ? gender.getTextOn() : gender.getTextOff());
                    detail.putExtra(DetailFreelancerFragment.AGE, age.getText().toString());
                    detail.putExtra(DetailFreelancerFragment.EMPLOYEE, employee.isChecked());
                    startActivity(detail);

                } else {
                    Utils.notifyUser(getActivity(), "The selected location and code are incorrect. Please verify the entered data");
                }
            }
        });

        addInfoClickListener(rootView);
        return rootView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOCATION_LOADER) {
            return new CursorLoader(
                    getActivity(),
                    FinancialContract.LocationEntry.CONTENT_URI,
                    FinancialContract.LocationEntry.COLUMNS_LOCATION_ENTRY,
                    null,
                    null,
                    FinancialContract.LocationEntry.COLUMN_CITY_NAME + " ASC");
        } else if (id == CODE_LOADER) {
            if (selectedLocation == null) {
                Utils.notifyUser(getActivity(), "Network not available or server could be down. Please try again later !");
                return null;
            }
            return new CursorLoader(
                    getActivity(),
                    FinancialContract.CodeEntry.CONTENT_URI,
                    FinancialContract.CodeEntry.COLUMNS_CODE_ENTRY,
                    FinancialContract.CodeEntry.COLUMN_LOC_KEY + " = ?",
                    new String[]{idByLocation.get(selectedLocation)},
                    FinancialContract.CodeEntry.COLUMN_CODE + " ASC");
        }
        return null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().restartLoader(LOCATION_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null) {
            if (loader.getId() == LOCATION_LOADER) {
                Log.i("test", "Location loader complete");
                while (data.moveToNext()) {
                    idByLocation.put(data.getString(1), data.getInt(0) + "");
                }
                final List<String> locations = new ArrayList<>(idByLocation.keySet());
                locationAdapter.clear();
                locationAdapter.addAll(locations);
                String selectedLocation = Utils.getPreferredStringValue(getActivity(), R.string.pref_key_location, R.string.pref_default_location);
                final int preferredLocation = locations.indexOf(selectedLocation);
                if (preferredLocation > 0) {
                    locationSpinner.setSelection(preferredLocation, true);
                    this.selectedLocation = selectedLocation;
                } else {
                    selectedLocation = locations.isEmpty() ? "" : locations.get(0);
                    Utils.notifyUser(getActivity(), String.format("Unknown location set in preferences, using %s", selectedLocation));
                }
                if (!selectedLocation.equals(this.selectedLocation) || normByCode.isEmpty()) {
                    if (calculateSalary != null) {
                        calculateSalary.setEnabled(false);
                        Utils.notifyUser(getActivity(), String.format("Fetching data for %s, please wait...", selectedLocation));
                    }
                    Intent intent = new Intent(getActivity(), CAENIntentService.class);
                    intent.putExtra(CAENIntentService.LOCATION, selectedLocation);
                    getActivity().startService(intent);
                }


            } else if (loader.getId() == CODE_LOADER) {
                normByCode.clear();
                while (data.moveToNext()) {
                    normByCode.put(data.getString(3), Utils.getValueOrDefault(data.getString(4), 0d));
                }
                codesAdapter.clear();
                codesAdapter.addAll(new ArrayList<>(normByCode.keySet()));
                if (calculateSalary != null) {
                    calculateSalary.setEnabled(true);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == CODE_LOADER) {
            codesAdapter.clear();
        } else if (loader.getId() == LOCATION_LOADER) {
            locationAdapter.clear();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        unregisterReceiver();
        receiver = new StatusReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter(CAENIntentService.STATUS));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver();
    }

    private void unregisterReceiver() {
        if (receiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        }
    }

    private class StatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CAENIntentService.STATUS)) {
                Log.i("test", "Restarted code loader");
                getLoaderManager().restartLoader(CODE_LOADER, null, FreelancerFragment.this);
            }
        }
    }


    private void addInfoClickListener(View rootView) {
        addListener(rootView, R.id.imageLocationFreelancer, R.string.domicileDefinition);
        addListener(rootView, R.id.imageCodeFreelancer, R.string.codeDefinition);
        addListener(rootView, R.id.imageIncomeFreelancer, R.string.incomeDefinition);
        addListener(rootView, R.id.imageEmployeeFreelancer, R.string.employeeDefinition);
        addListener(rootView, R.id.imageAgeFreelancer, R.string.genderAgeInfo);
        addListener(rootView, R.id.imageGenderFreelancer, R.string.genderAgeInfo);

    }

    private void addListener(View rootView, int image, int definition) {
        final int def = definition;
        ImageView view = (ImageView) rootView.findViewById(image);
        if (view != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showInfoDialog(def);
                }
            });
        }
    }

    private void showInfoDialog(int messageId) {
        new AlertDialog.Builder(getActivity())
                .setMessage(getString(messageId))
                .setCancelable(true)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).create().show();

    }


}


