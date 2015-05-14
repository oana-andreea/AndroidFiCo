package ro.moa.financial.consultant;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetailSalaryFragment} interface
 * to handle interaction events.
 * Use the {@link DetailSalaryFragment} factory method to
 * create an instance of this fragment.
 */
@SuppressWarnings("ALL")
public class DetailSalaryFragment extends Fragment {
    //    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String GROSS_SALARY = "grossSalary";
    public static final String NET_SALARY = "netSalary";
    public static final String LUNCH = "lunch";
    public static final String WORK = "workConditions";
    public static final String BASE = "baseFunction";
    public static final String DEPENDANTS = "dependants";
    public static final String REDUCTION = "reductions";


    private double mGrossSalary;
    private double mNetSalary;
    private double mLunch;
    private boolean mReduction;
    private double mDependants;
    private double mWork;
    private boolean mBaseFunction;

    private double health = 0;
    private double pension = 0;
    private double taxes = 0;
    private double unemployment = 0;
    private double taxDeductible = 0;

    private EditText grossSalaryAmount;
    private EditText netSalaryAmount;
    private EditText healthCareAmount;
    private EditText pensionAmount;
    private EditText taxAmount;
    private EditText unemploymentAmount;
    private EditText lunchAmount;
    private EditText taxDeductibleAmount;
    private Button change;

    public DetailSalaryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        Log.d("DetailSalaryFragment", args.size() + "");

        if (getArguments() != null) {
            mGrossSalary = args.getDouble(GROSS_SALARY);
            mNetSalary = args.getDouble(NET_SALARY);
            mBaseFunction = args.getBoolean(BASE);
            mLunch = args.getInt(LUNCH);
            mWork = args.getDouble(WORK);
            mReduction = args.getBoolean(REDUCTION);
            mDependants = args.getInt(DEPENDANTS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_detail_salary, container, false);
        if (mGrossSalary != 0) {
            calculateNetSalary();
        } else calculateGrossSalary();

        setValues(rootView);
        addInfoClickListener(rootView);

        ((Button) rootView.findViewById(R.id.Change)).setVisibility(View.INVISIBLE);
        /*change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyChange(rootView);
            }
        });*/
        return rootView;
    }

    private void setValues(View rootView) {
        grossSalaryAmount = (EditText) rootView.findViewById(R.id.grossSalaryResults);
        grossSalaryAmount.setText(Math.round(mGrossSalary) + "");
        grossSalaryAmount.setFocusable(false);

        netSalaryAmount = (EditText) rootView.findViewById(R.id.netSalaryResults);
        netSalaryAmount.setText(Math.round(mNetSalary) + "");
        netSalaryAmount.setFocusable(false);
        healthCareAmount = (EditText) rootView.findViewById(R.id.CASSAmount);
        healthCareAmount.setText(Math.round(health) + "");
        healthCareAmount.setFocusable(false);
        pensionAmount = (EditText) rootView.findViewById(R.id.CASAmount);
        pensionAmount.setText(Math.round(pension) + "");
        pensionAmount.setFocusable(false);
        taxAmount = (EditText) rootView.findViewById(R.id.taxesAmountSalary);
        taxAmount.setText(Math.round(taxes) + "");
        taxAmount.setFocusable(false);
        unemploymentAmount = (EditText) rootView.findViewById(R.id.UnemploymentAmount);
        unemploymentAmount.setText(Math.round(unemployment) + "");
        unemploymentAmount.setFocusable(false);
        taxDeductibleAmount = (EditText) rootView.findViewById(R.id.taxDeductibleAmount);
        taxDeductibleAmount.setText(Math.round(taxDeductible) + "");
        taxDeductibleAmount.setFocusable(false);
        lunchAmount = (EditText) rootView.findViewById(R.id.lunchAmount);
        lunchAmount.setText(Math.round(mLunch) + "");
        lunchAmount.setFocusable(false);
    }

    public void applyChange(View rootView) {


        if ((change.getText()).equals("Change Salary")) {
            grossSalaryAmount.setFocusable(true);
            //grossSalaryAmount.setEnabled(true);
            //grossSalaryAmount.setSelected(true);
            grossSalaryAmount.setFocusableInTouchMode(true);
            //grossSalaryAmount.setClickable(true);
            netSalaryAmount.setFocusable(true);
            //netSalaryAmount.setEnabled(true);
            //netSalaryAmount.setSelected(true);
            netSalaryAmount.setFocusableInTouchMode(true);

            change.setText(getString(R.string.recalculateSalary));
        } else if ((change.getText()).equals(getString(R.string.recalculateSalary))) {
            grossSalaryAmount.setFocusable(false);
            netSalaryAmount.setFocusable(false);
            change.setText(getString(R.string.changeSalary));

            final int grossSalaryValue = Utils.getValueOrDefault(grossSalaryAmount, 0);
            if (grossSalaryValue != 0) {
                mGrossSalary = grossSalaryValue;
                calculateNetSalary();
            }
            final int netSalaryValue = Utils.getValueOrDefault(netSalaryAmount, 0);
            if (netSalaryValue != 0) {
                mNetSalary = netSalaryValue;
                calculateNetSalary();
            }
            setValues(rootView);


        }
    }

    public void calculateNetSalary() {
        health = mGrossSalary * 0.055;
        pension = mGrossSalary * 0.105;
        unemployment = mGrossSalary * 0.005;
        if (mBaseFunction) {
            if (mDependants > 4) mDependants = 4;
            taxDeductible = 250 + mDependants * 100;
            if (mGrossSalary > 1000 && mGrossSalary < 3000)
                taxDeductible *= (1 - (mGrossSalary - 1000) / 2000);
            else if (mGrossSalary >= 3000) taxDeductible = 0;
        } else taxDeductible = 0;

        if (mReduction) taxes = 0;
        else
            taxes = (mGrossSalary - taxDeductible) * 0.16;

        mNetSalary = mGrossSalary - health - pension - taxes;
    }

    public void calculateGrossSalary() {
        health = mNetSalary * 1.055;
        pension = mNetSalary * 1.105;
        unemployment = mNetSalary * 1.005;
        if (mBaseFunction) {
            if (mDependants > 4) mDependants = 4;
            taxDeductible = 250 + mDependants * 100;
            if (mNetSalary > 1000 && mNetSalary < 3000)
                taxDeductible *= (1 - (mNetSalary - 1000) / 2000);
            else if (mNetSalary >= 3000) taxDeductible = 0;
        } else taxDeductible = 0;

        if (mReduction) taxes = 0;
        else
            taxes = (mNetSalary + taxDeductible) * 0.16;

        mGrossSalary = mNetSalary + health + pension + taxes;
    }

    private void addInfoClickListener(View rootView) {
        addListener(rootView, R.id.imageGrossSalaryDetail, R.string.grossSalaryDefinition);
        addListener(rootView, R.id.imageNetSalaryDetail, R.string.netSalaryDefinition);
        addListener(rootView, R.id.imageHealthCareSalaryDetail, R.string.healthCareInfoDefinition);
        addListener(rootView, R.id.imageLunchSalaryDetail, R.string.lunchDefinition);
        addListener(rootView, R.id.imagePensionSalaryDetail, R.string.pensionInfoDefinition);
        addListener(rootView, R.id.imageTaxDeductibleSalaryDetail, R.string.deductibleInfoDefinition);
        addListener(rootView, R.id.imageTaxesSalaryDetail, R.string.totalTaxes);
        addListener(rootView, R.id.imageUnemploymentSalaryDetail, R.string.unemploymentDescription);

    }


    private void addListener(View rootView, int image, int definition) {
        final int def = definition;
        final ImageView view = (ImageView) rootView.findViewById(image);
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
