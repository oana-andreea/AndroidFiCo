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
 * {@link DetailFreelancerFragment} interface
 * to handle interaction events.
 * Use the {@link DetailFreelancerFragment} factory method to
 * create an instance of this fragment.
 */
@SuppressWarnings("ALL")
public class DetailFreelancerFragment extends Fragment {
    public static final String NORM = "norm";
    public static final String INCOME = "income";
    public static final String GENDER = "gender";
    public static final String AGE = "age";
    public static final String EMPLOYEE = "employee";

    private EditText incomeAmount;
    private EditText normAmount;
    private EditText taxAmount;
    private EditText pensionAmount;
    private EditText healthCareAmount;
    private EditText reductionAmount;
    private EditText incomeAfterTaxesAmount;

    private double mNorm;
    private double mIncome;
    private int mAge;
    private String mGender;
    private boolean mEmployee;
    double incomeAfterTaxes = 0;
    double health = 0;
    double pension = 0;
    double taxes = 0;
    double reduction = 0;
    double norm = 0;


    public DetailFreelancerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        if (getArguments() != null) {
            mNorm = args.getDouble(NORM);
            mIncome = args.getDouble(INCOME);
            mAge = Utils.getValueOrDefault(args.getString(AGE), 0);
            mEmployee = args.getBoolean(EMPLOYEE);
            mGender = args.getString(GENDER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail_freelancer, container, false);

        calculateNorm();
        setValues(rootView);
        setFocusable(false);
        addInfoClickListener(rootView);
        ((Button) rootView.findViewById(R.id.button)).setVisibility(View.INVISIBLE);
        return rootView;
    }

    private void calculateNorm() {
        if (mAge > 50) {
            if (mGender.equals(getString(R.string.pref_gender_female))) {
                if (mAge < 55) {
                    reduction = 0.30;
                } else if (mAge < 60) {
                    reduction = 0.40;
                } else {
                    reduction = 0.50;
                }
            } else {
                if (mAge > 55)
                    if (mGender.equals(getString(R.string.pref_gender_male))) {
                        if (mAge < 60) {
                            reduction = 0.30;
                        } else if (mAge < 65) {
                            reduction = 0.40;
                        } else {
                            reduction = 0.50;
                        }
                    }
            }
        }

        if (mEmployee) {
            reduction = 0.50;
        }
        norm = mNorm * (1 - reduction);
        reduction = mNorm * reduction;
        if (norm != 0)

        {
            taxes = norm * 0.16;
            health = norm * 0.055;
            if (!mEmployee)
                pension = norm * 0.263;
            incomeAfterTaxes = mIncome - health - pension - taxes;
        }
    }

    private void addInfoClickListener(View rootView) {
        addListener(rootView, R.id.imageViewIncome, R.string.incomeInfoDefinition);
        addListener(rootView, R.id.imageViewNorm, R.string.normInfoDefinition);
        addListener(rootView, R.id.imageViewTax, R.string.taxInfoDefinition);
        addListener(rootView, R.id.imageViewPension, R.string.pensionInfoDefinition);
        addListener(rootView, R.id.imageViewHealthCare, R.string.healthCareInfoDefinition);
        addListener(rootView, R.id.imageViewReduction, R.string.reductionInfoDefinition);
        addListener(rootView, R.id.imageViewAfterTaxes, R.string.incomeAfterTaxesInfoDefinition);

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

    private void setFocusable(boolean focus) {
        incomeAmount.setFocusable(focus);
        reductionAmount.setFocusable(focus);
        incomeAfterTaxesAmount.setFocusable(focus);
        taxAmount.setFocusable(focus);
        pensionAmount.setFocusable(focus);
        healthCareAmount.setFocusable(focus);
        normAmount.setFocusable(focus);
    }

    public void setValues(View rootView) {
        incomeAmount = (EditText) rootView.findViewById(R.id.incomeAmount2);
        incomeAmount.setText(Math.round(mIncome) + "");

        normAmount = (EditText) rootView.findViewById(R.id.normAmount2);
        normAmount.setText(Math.round(mNorm) + "");

        healthCareAmount = (EditText) rootView.findViewById(R.id.healthCareAmount2);
        healthCareAmount.setText(Math.round(health) + "");

        pensionAmount = (EditText) rootView.findViewById(R.id.pensionAmount2);
        pensionAmount.setText(Math.round(pension) + "");

        taxAmount = (EditText) rootView.findViewById(R.id.taxAmount2);
        taxAmount.setText(Math.round(taxes) + "");

        incomeAfterTaxesAmount = (EditText) rootView.findViewById(R.id.incomeAfterTaxesAmount2);
        incomeAfterTaxesAmount.setText(Math.round(incomeAfterTaxes) + "");

        reductionAmount = (EditText) rootView.findViewById(R.id.reductionAmount2);
        reductionAmount.setText(Math.round(reduction) + "");
    }
}
