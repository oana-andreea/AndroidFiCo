package ro.moa.financial.consultant;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;

import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.CheckBox;
import android.widget.EditText;


import java.util.ArrayList;

import ro.moa.financial.consultant.service.LocationIntentService;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks{
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private ShareActionProvider mShareActionProvider;
    private Intent shareIntent=new Intent(Intent.ACTION_SEND);

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private ArrayList<CharSequence> titles = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        shareIntent.setType("text/plain");

        /* refresh locations if needed */
        startService(new Intent(this, LocationIntentService.class));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        final FragmentManager fragmentManager = getSupportFragmentManager();
        titles.add(mTitle);

        if (position < 0)
            fragmentManager.beginTransaction()
                    .replace(R.id.container, SalaryFragment.newInstance(1))
                    .commit();
        else {

            if (position == 0)
                fragmentManager.beginTransaction()
                        .replace(R.id.container, SalaryFragment.newInstance(position + 1))
                        .addToBackStack(null)
                        .commit();
            else if (position == 1)
                fragmentManager.beginTransaction()
                        .replace(R.id.container, FreelancerFragment.newInstance(position + 1))
                        .addToBackStack(null)
                        .commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
      //  int size = getSupportFragmentManager().getBackStackEntryCount();
        mTitle = titles.remove(Math.min(titles.size() - 1, getSupportFragmentManager().getBackStackEntryCount() + 1));
        restoreActionBar();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            MenuItem item= menu.findItem(R.id.menu_item_share);
            mShareActionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(item);
            String text ="Share the knowledge! Take advantage of a financial consultant!";
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
            mShareActionProvider.setShareIntent(shareIntent);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCalculateSalaryClick(View view) {
        //SalaryFragment salaryFragment = (SalaryFragment)getSupportFragmentManager().findFragmentById(R.id.salary);
        EditText grossText = (EditText) findViewById(R.id.grossSalaryAmount);
        EditText netText = (EditText) findViewById(R.id.netSalaryAmount);
        CheckBox handicapped = (CheckBox) findViewById(R.id.handicapped);
        CheckBox programmer = (CheckBox) findViewById(R.id.programmer);
        final boolean freeTaxes = handicapped.isChecked() || programmer.isChecked();
        final boolean baseFunction = ((CheckBox) findViewById(R.id.baseFunction)).isChecked();
        EditText dependantsText = (EditText) findViewById(R.id.dependants);
        EditText lunchTicketAmountText = (EditText) findViewById(R.id.lunchTicketAmount);
        EditText riskPercentText = (EditText) findViewById(R.id.riskPercent);


        Intent detail = new Intent(this, DetailActivity.class);
        detail.putExtra(DetailActivity.TYPE, DetailActivity.SALARY);
        detail.putExtra(DetailSalaryFragment.BASE, baseFunction);
        detail.putExtra(DetailSalaryFragment.DEPENDANTS, Utils.getValueOrDefault(dependantsText, 0));
        detail.putExtra(DetailSalaryFragment.GROSS_SALARY, Utils.getValueOrDefault(grossText, 0d));
        detail.putExtra(DetailSalaryFragment.NET_SALARY, Utils.getValueOrDefault(netText, 0d));
        detail.putExtra(DetailSalaryFragment.LUNCH, Utils.getValueOrDefault(lunchTicketAmountText, 0));
        detail.putExtra(DetailSalaryFragment.REDUCTION, freeTaxes);
        detail.putExtra(DetailSalaryFragment.WORK, Utils.getValueOrDefault(riskPercentText, 0d));
        startActivity(detail);

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

    }

}
