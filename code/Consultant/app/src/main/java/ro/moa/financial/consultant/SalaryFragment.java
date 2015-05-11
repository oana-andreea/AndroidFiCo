package ro.moa.financial.consultant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class SalaryFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SalaryFragment newInstance(int sectionNumber) {
        SalaryFragment fragment = new SalaryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public SalaryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_salary, container, false);
        addInfoClickListener(rootView);
        return rootView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    private void addInfoClickListener(View rootView)
    {   addListener(rootView,R.id.imageGrossSalary,R.string.grossSalaryDefinition);
        addListener(rootView,R.id.imageNetSalary,R.string.netSalaryDefinition);
        addListener(rootView,R.id.imageHandicappedSalary,R.string.handicappedDefinition);
        addListener(rootView,R.id.imageProgrammerSalary,R.string.handicappedDefinition);
        addListener(rootView,R.id.imageLunchSalary,R.string.lunchDefinition);
        addListener(rootView,R.id.imageDependantsSalary,R.string.dependantsDefinition);
        addListener(rootView,R.id.imageBaseFunctionSalary,R.string.baseFunctionDefinition);
        addListener(rootView,R.id.imageWorkConditionsSalary,R.string.workConditionsDefinition);

    }

    private void addListener(View rootView,int image, int definition){
        final int def= definition;
        final ImageView view = (ImageView) rootView.findViewById(image);
        /* only non-tablet mode will contain valid views */
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
