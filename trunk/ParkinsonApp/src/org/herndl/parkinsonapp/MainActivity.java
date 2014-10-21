package org.herndl.parkinsonapp;

import org.herndl.parkinsonapp.maps.MapFragment;
import org.herndl.parkinsonapp.med.MedReminderFragment;
import org.herndl.parkinsonapp.track.TrackerFragment;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/*
 * this is the main activity which is called when opening the app
 * the main layout initializations are done here
 */
@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity implements
		ActionBar.TabListener {

	// pager which holds the main fragments
	AppSectionsPagerAdapter fragmentPager;
	// viewpager for swipe navigation
	private ViewPager viewPager;
	// taskhandler binds to the notification service to set and unset alarms
	private TaskHandler taskHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Log.d("MainActivity", "onCreate");
		taskHandler = new TaskHandler(this);
		taskHandler.doBindService();

		// stop currently showing reminder notifications if there are some
		TaskNotifyService.stopNotification();

		// adapter which returns the main fragments
		fragmentPager = new AppSectionsPagerAdapter(this,
				getSupportFragmentManager());

		// action bar setup
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// attach the fragment adapter to the view pager and listen for changes
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(fragmentPager);
		// change tab according to swipe action in viewer
		viewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// fill up the actionbar with the fragment titles
		for (int i = 0; i < fragmentPager.getCount(); i++) {
			actionBar.addTab(actionBar.newTab()
					.setText(fragmentPager.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		taskHandler.doUnbindService();
	};

	// change fragment according to tab selection
	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		viewPager.setCurrentItem(tab.getPosition());
	}

	// fragment pager adapter which returns the fragments and their titles
	public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

		private Context context;

		public AppSectionsPagerAdapter(Context context, FragmentManager fm) {
			super(fm);
			this.context = context;
		}

		public Fragment getItem(int i) {
			switch (i) {
			case 0:
				return new MedReminderFragment();
			case 1:
				return new MapFragment();
			case 2:
				return new TrackerFragment();
			default:
				return new Fragment();
			}
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int pos) {
			switch (pos) {
			case 0:
				return context.getString(R.string.fragment_reminder);
			case 1:
				return context.getString(R.string.fragment_maps);
			case 2:
				return context.getString(R.string.fragment_tracker);
			default:
				return context.getString(R.string.error);
			}
		}
	}

	// build menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// helper function to show a simple about dialog
	private void showAbout() {
		View messageView = getLayoutInflater().inflate(R.layout.about,
				(ViewGroup) findViewById(R.id.about_layout), false);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle(R.string.app_name);
		builder.setView(messageView);
		builder.create();
		builder.show();
	}

	// handle option select actions
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_about)
			showAbout();
		return super.onOptionsItemSelected(item);
	}

	// unnused methodes
	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
	}
}
