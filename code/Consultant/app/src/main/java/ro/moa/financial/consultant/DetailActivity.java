package ro.moa.financial.consultant;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;


public class DetailActivity extends ActionBarActivity {

    public static final String FREELANCER ="freelancer";
    public static final String TYPE = "type";
    public static final String SALARY = "salary";
    private ShareActionProvider mShareActionProvider;
    private Intent shareIntent=new Intent(Intent.ACTION_SEND);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        final FragmentManager fragmentManager = getSupportFragmentManager();
        Bundle bundle = getIntent().getExtras();
        String type = bundle.getString(TYPE);
        shareIntent.setType("text/plain");

        //String norm = bundle.getString(FREELANCER);
        if (type.equals(FREELANCER)) {
            DetailFreelancerFragment detailFreelancerFragment  = new DetailFreelancerFragment();
            detailFreelancerFragment.setArguments(bundle);

            fragmentManager.beginTransaction()
                    .replace(R.id.container_detail, detailFreelancerFragment)
                            //.addToBackStack(null)
                    .commit();
        }
        else if (type.equals(SALARY)) {
            DetailSalaryFragment detailSalaryFragment = new DetailSalaryFragment();
            detailSalaryFragment.setArguments(bundle);

            fragmentManager.beginTransaction()
                    .replace(R.id.container_detail, detailSalaryFragment)
                            //.addToBackStack(null)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        MenuItem item= menu.findItem(R.id.menu_item_share2);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        String text ="Share the knowledge! Take advantage of a financial consultant!";
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        mShareActionProvider.setShareIntent(shareIntent);
        return true;
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

}
