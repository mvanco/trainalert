package cz.intesys.trainalert.activity;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.intesys.trainalert.BuildConfig;
import cz.intesys.trainalert.R;
import cz.intesys.trainalert.TaConfig;
import cz.intesys.trainalert.adapter.NavAdapter;
import cz.intesys.trainalert.databinding.ActivityMainBinding;
import cz.intesys.trainalert.entity.Poi;
import cz.intesys.trainalert.entity.TaCallback;
import cz.intesys.trainalert.entity.realm.Profile;
import cz.intesys.trainalert.fragment.MainFragment;
import cz.intesys.trainalert.fragment.ManualFragment;
import cz.intesys.trainalert.fragment.PasswordDialogFragment;
import cz.intesys.trainalert.fragment.ProfileFragment;
import cz.intesys.trainalert.fragment.TripFragment;
import cz.intesys.trainalert.fragment.TripIdDialogFragment;
import cz.intesys.trainalert.fragment.TripIdManuallyDialogFragment;
import cz.intesys.trainalert.repository.DataHelper;
import cz.intesys.trainalert.utility.Utility;
import cz.intesys.trainalert.viewmodel.MainActivityViewModel;

import static android.support.v4.widget.DrawerLayout.STATE_IDLE;
import static android.support.v4.widget.DrawerLayout.STATE_SETTLING;
import static cz.intesys.trainalert.TaConfig.TRIP_FRAGMENT_NEXT_STOP_COUNT;
import static cz.intesys.trainalert.TaConfig.TRIP_FRAGMENT_PREVIOUS_STOP_COUNT;
import static cz.intesys.trainalert.repository.DataHelper.TRIP_NO_TRIP;

public class MainActivity extends AppCompatActivity implements TripIdDialogFragment.OnFragmentInteractionListener,
        TripFragment.OnFragmentInteractionListener,
        PasswordDialogFragment.OnFragmentInteractionListener,
        MainFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener {

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    private static final String MAIN_FRAGMENT_TAG = "cz.intesys.trainAlert.mainActivity.mainFragmentTag";
    private static final String TRIP_ID_DIALOG_FRAGMENT_TAG = "cz.intesys.trainAlert.mainActivity.tripIdTag";
    private static final String TRIP_ID_MANUALLY_DIALOG_FRAGMENT_TAG = "cz.intesys.trainAlert.mainActivity.tripIdManuallyTag";
    private static final String PASSWORD_DIALOG_FRAGMENT_TAG = "cz.intesys.trainAlert.mainActivity.passwordTag";
    private static final String TRIP_FRAGMENT_TAG = "cz.intesys.trainAlert.mainActivity.sideFragmentTag";
    private static final String PROFILE_DIALOG_FRAGMENT_TAG = "cz.intesys.trainAlert.mainActivity.ProfileDialogFragmentTag";

    private ActivityMainBinding mBinding;
    private ActionBarDrawerToggle mToggle;
    private TripIdDialogFragment mTripDialogFragment;
    private Menu mMenu;
    private MainActivityViewModel mViewModel;
    private TextView mClockTextView;
    private boolean mShouldShowPasswordDialog = true;
    private AnimatorSet mAnimSet;
    private boolean mTripSelectionIconEnabled = true;

    @Override
    public void onFabClick() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Název");

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton(getResources().getString(R.string.button_save),
                (dialog, which) -> {
                    String title = input.getText().toString();
                    mViewModel.addProfile(MainActivity.this, title);
                    ProfileFragment fragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag(PROFILE_DIALOG_FRAGMENT_TAG);
                    fragment.reload();
                });

        alertDialog.setNegativeButton(getResources().getString(R.string.button_cancel),
                (dialog, which) -> dialog.cancel());

        alertDialog.show();
    }

    @Override
    public void onProfileDeleted(Profile profile) {
        if (profile.getName().equals("Výchozí profil")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialog_message_def_profile_deletion_title);
            builder.setMessage(R.string.dialog_message_def_profile_deletion);
            builder.setPositiveButton(getResources().getString(R.string.button_delete), (dialog, which) -> {
                dialog.dismiss();
                mViewModel.deleteProfile(profile);
                ProfileFragment fragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag(PROFILE_DIALOG_FRAGMENT_TAG);
                if (fragment != null) {
                    fragment.reload();
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.button_cancel), (dialog, which) -> {
                dialog.dismiss();
                ProfileFragment fragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag(PROFILE_DIALOG_FRAGMENT_TAG);
                if (fragment != null) {
                    fragment.reload();
                }
            });
            builder.show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_message_profile_deletion_title);
        builder.setMessage(R.string.dialog_message_profile_deletion);
        builder.setPositiveButton(getResources().getString(R.string.button_delete), (dialog, which) -> {
            dialog.dismiss();
            mViewModel.deleteProfile(profile);
            ProfileFragment fragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag(PROFILE_DIALOG_FRAGMENT_TAG);
            if (fragment != null) {
                fragment.reload();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.button_cancel), (dialog, which) -> {
            dialog.dismiss();
            ProfileFragment fragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag(PROFILE_DIALOG_FRAGMENT_TAG);
            if (fragment != null) {
                fragment.reload();
            }
        });
        builder.show();
    }

    @Override
    public void onProfileClicked(String profileName) {
        mViewModel.loadProfile(this, profileName);
        Toast.makeText(this, R.string.message_profil_loaded, Toast.LENGTH_SHORT).show();
        MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.initIcons();
        }
    }

    @Override
    public void onProfileUpdated(String profileName) {
        mViewModel.deleteProfile(profileName);
        mViewModel.addProfile(this, profileName);
        Toast.makeText(this, R.string.message_profil_updated, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                    if (fragment != null) {
                        transaction.remove(fragment);
                    }

                    transaction.add(R.id.activityMain_fragmentContainer, MainFragment.newInstance(), MAIN_FRAGMENT_TAG).commit();

                    getSupportFragmentManager().executePendingTransactions();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.message_application_termination);
                    builder.setPositiveButton(R.string.button_confirm, (dialog, which) -> {
                        this.finish();
                    });
                    builder.create().show();
                }
                return;
        }
    }

    @Override
    public void onTripSelected(String tripId) { // Returned from TripIdDialogFragment or TripIdManuallyDialogFragment
        showTripIdSelectionIconLoader();
        DataHelper.getInstance().setTrip(tripId, new TaCallback<Void>() {
            @Override
            public void onResponse(Void response) {
                showSideBar();
                hideTripIdSelectionIconLoader();
                setSideBarHandleVisibility();  // Make handle visible.
                mViewModel.reloadPois();
                Log.d("reloadingPois", ".");
            }

            @Override
            public void onFailure(Throwable t) {
                hideTripIdSelectionIconLoader();
            }
        });
    }

    @Override
    public void onTripFinished() {
        DataHelper.getInstance().unregisterTrip();
        initTripIdSelectionIconLoader();  // Disable animation.
        setSideBarHandleVisibility();  // Sets to invisible
        hideSideBar();
    }

    @Override
    public void onBusinessTripSelected(String tripId) {
        showTripIdSelectionIconLoader();
        DataHelper.getInstance().setTrip(tripId, new TaCallback<Void>() {
            @Override
            public void onResponse(Void response) {
                Toast.makeText(MainActivity.this, R.string.message_business_trip_set, Toast.LENGTH_SHORT).show();
                hideTripIdSelectionIconLoader();
            }

            @Override
            public void onFailure(Throwable t) {
                hideTripIdSelectionIconLoader();
            }
        });


        mBinding.activityMainInclude.activityMainSideContainer.setVisibility(View.GONE);
    }

    @Override
    public void onTripManuallySelected() {
        DialogFragment tripIdDialogFragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(TRIP_ID_DIALOG_FRAGMENT_TAG);

        if (tripIdDialogFragment != null && tripIdDialogFragment.getDialog() != null && tripIdDialogFragment.getDialog().isShowing()) {
            tripIdDialogFragment.dismiss();
        }
        TripIdManuallyDialogFragment.newInstance().show(getSupportFragmentManager(), TRIP_ID_MANUALLY_DIALOG_FRAGMENT_TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        MenuItem tripSelectionItem = menu.findItem(R.id.menu_trip_selection);
        ImageView iv = (ImageView) inflater.inflate(R.layout.custom_action_view, null);
        iv.setOnClickListener(v -> MainActivity.this.onOptionsItemSelected(tripSelectionItem));
        tripSelectionItem.setActionView(iv);

        MenuItem soundItem = menu.findItem(R.id.menu_sound);
        ImageView iv2 = (ImageView) inflater.inflate(R.layout.custom_action_view, null);
        iv2.setOnClickListener(v -> MainActivity.this.onOptionsItemSelected(soundItem));
        soundItem.setActionView(iv2);

        MenuItem clockItem = menu.findItem(R.id.menu_clock);
        mClockTextView = (TextView) clockItem.getActionView();
        mClockTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.activityMain_clock_textSize));

        MenuItem notificationItem = menu.findItem(R.id.menu_notification);
        ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.notification_action_view, null);
        notificationItem.setActionView(cl);

        mMenu = menu;
        hideTripIdSelectionIconLoader();
        setupVolume();
        return true;
    }

    /**
     * Prerequisities: must be set field with type {@link Menu}
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_trip_selection:
                if (mTripSelectionIconEnabled) {
                    showTripIdDialogFragment();
                }
                return true;
            case R.id.menu_profile_selection:
                ProfileFragment.newInstance().show(getSupportFragmentManager(), PROFILE_DIALOG_FRAGMENT_TAG);
                return true;
            case R.id.menu_sound:
                if (mViewModel.isVolumeUp(this)) {
                    mViewModel.setVolumeUp(this, false);
                } else {
                    mViewModel.setVolumeUp(this, true);
                }
                setupVolume();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setupVolume() {
        if (mMenu == null) {
            return;
        }
        MenuItem item = mMenu.findItem(R.id.menu_sound);
        AudioManager audioManager = (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
        if (mViewModel.isVolumeUp(this)) {
            ((ImageView) item.getActionView()).setImageResource(R.drawable.ic_volume_up);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0);
        } else {
            ((ImageView) item.getActionView()).setImageResource(R.drawable.ic_volume_off);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
        }
    }

    @Override
    public void onPasswordEntered(int password) {
        DataHelper.getInstance().registerSideBar(password, new TaCallback<Void>() {
            @Override
            public void onResponse(Void response) {
                mBinding.activityMainDrawerLayout.openDrawer(Gravity.LEFT);
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(MainActivity.this, R.string.error_wrong_password, Toast.LENGTH_SHORT).show();
            }
        });
        mShouldShowPasswordDialog = true;
    }

    @Override
    public void onDialogCanceled() {
        mShouldShowPasswordDialog = true;
    }

    @Override
    public void onFinishAnimationStarted() {
        MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.setAnimating(false);
        }
    }

    @Override
    public void onBackPressed() {
        // Suppress shutting the application down using back button.
    }

    @Override
    public void onActionBarNotificationShow(String alarmMessage) {
        if (mMenu == null) {
            return;
        }
        MenuItem notificationItem = mMenu.findItem(R.id.menu_notification);
        TextView tv = ((ConstraintLayout) notificationItem.getActionView()).findViewById(R.id.notification_text);
        tv.setText(alarmMessage);
    }

    @Override
    public void onPassedPoi(Poi poi) {
        if (mMenu == null) {
            return;
        }
        MenuItem notificationItem = mMenu.findItem(R.id.menu_notification);
        TextView tv = ((ConstraintLayout) notificationItem.getActionView()).findViewById(R.id.notification_text);
        tv.setText("");
    }

    @Override
    public void onToggleSideBar() {
        Fragment tripFragment = getSupportFragmentManager().findFragmentByTag(TRIP_FRAGMENT_TAG);
        if (tripFragment == null) {  // Not shown side bar.
            if (DataHelper.getInstance().getRegisteredTrip() != TRIP_NO_TRIP) {
                showSideBar();
            }
        } else {
            hideSideBar();
        }
    }

    public void onTimeChanged(Date time) {
        if (mClockTextView != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(time);
            int hours = cal.get(Calendar.HOUR_OF_DAY);
            int minutes = cal.get(Calendar.MINUTE);
            mClockTextView.setText(Utility.getTwoDigitString(hours) + ":" + Utility.getTwoDigitString(minutes));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!BuildConfig.USE_OFFLINE_MAPS
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        getLifecycle().addObserver(mViewModel);

        if (BuildConfig.USE_OFFLINE_MAPS
                || (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)) {
            getSupportFragmentManager().beginTransaction().add(R.id.activityMain_fragmentContainer, MainFragment.newInstance(), MAIN_FRAGMENT_TAG).commitNow();
            getSupportFragmentManager().executePendingTransactions();
        }

        setupActionBar();
        setupNavigationBar();
        mViewModel.getLocationLiveData().observe(this, (location) -> onTimeChanged(location.getTime()));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mAnimSet = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.ic_trip_selection_animator);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DataHelper.getInstance().isFirstRun()) {
            DataHelper.getInstance().getTrainId(new TaCallback<String>() { // Load train id to SharedPreferences.
                @Override
                public void onResponse(String response) {
                }

                @Override
                public void onFailure(Throwable t) {
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        DialogFragment tripIdDialogFragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(TRIP_ID_DIALOG_FRAGMENT_TAG);

        if (tripIdDialogFragment != null && tripIdDialogFragment.getDialog() != null && tripIdDialogFragment.getDialog().isShowing()) {
            tripIdDialogFragment.dismiss();
        }

        DialogFragment tripIdManuallyDialogFragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(TRIP_ID_MANUALLY_DIALOG_FRAGMENT_TAG);

        if (tripIdManuallyDialogFragment != null && tripIdManuallyDialogFragment.getDialog() != null && tripIdManuallyDialogFragment.getDialog().isShowing()) {
            tripIdManuallyDialogFragment.dismiss();
        }

        if (!mViewModel.getAutoRegisterLiveData().hasActiveObservers()) {
            mViewModel.getAutoRegisterLiveData().observe(this, (firstTime) -> {
                autoRegister(firstTime);
            });
        }
        setIconsVisibility();
        setupVolume();

    }

    private void setIconsVisibility() {

    }

    private void setupNavigationBar() {
        RecyclerView recyclerView = mBinding.activityMainNavigationView.findViewById(R.id.activityMain_recyclerView);
        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new NavAdapter((id) -> onNavigationItemSelected(id)));
    }

    /**
     * There must be already set train id
     */
    synchronized private void showTripIdDialogFragment() {
        showTripIdSelectionIconLoader();
        DataHelper.getInstance().getTrips(new TaCallback<List<String>>() {
            @Override
            public void onResponse(List<String> response) {

                DialogFragment tripIdDialogFragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(TRIP_ID_DIALOG_FRAGMENT_TAG);

                if (tripIdDialogFragment != null && tripIdDialogFragment.getDialog() != null && tripIdDialogFragment.getDialog().isShowing()) {
                    return;
                }

                DialogFragment tripIdManuallyDialogFragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(TRIP_ID_MANUALLY_DIALOG_FRAGMENT_TAG);

                if (tripIdManuallyDialogFragment != null && tripIdManuallyDialogFragment.getDialog() != null && tripIdManuallyDialogFragment.getDialog().isShowing()) {
                    return;
                }

                mTripDialogFragment = TripIdDialogFragment.newInstance(response, DataHelper.getInstance().getRegisteredTrip());
                if (getSupportFragmentManager().isStateSaved() == false) {
                    mTripDialogFragment.show(getSupportFragmentManager(), TRIP_ID_DIALOG_FRAGMENT_TAG);
                }
                hideTripIdSelectionIconLoader();
            }

            @Override
            public void onFailure(Throwable t) {
                hideTripIdSelectionIconLoader();
            }
        });
    }

    /**
     * Animates button for trip selection.
     */
    private void showTripIdSelectionIconLoader() {
        if (mMenu == null) {
            return;
        }
        MenuItem item = mMenu.findItem(R.id.menu_trip_selection);
        mAnimSet.setTarget(item.getActionView());
        mAnimSet.start();
        mTripSelectionIconEnabled = false; // Suppress functionality.
        item.setEnabled(false); // Suppress click animation.
    }

    /**
     * Stop animation of button for trip selection.
     */
    private void hideTripIdSelectionIconLoader() {
        initTripIdSelectionIconLoader();
    }

    /**
     * Init trip id selection icon loader according to current trip id registration.
     */
    private void initTripIdSelectionIconLoader() {
        if (mMenu == null) {
            return;
        }
        MenuItem item = mMenu.findItem(R.id.menu_trip_selection);

        // SET DEFAULT VALUES
        mAnimSet.end();
        item.getActionView().setRotation(0f);
        item.getActionView().setAlpha(1f);

        if (TRIP_NO_TRIP.equals(DataHelper.getInstance().getRegisteredTrip())) {
            ((ImageView) item.getActionView()).setImageResource(R.drawable.ic_trip_selection_red);

            //Toast.makeText(this, R.string.activity_main_unsuccessful_trip_selection, Toast.LENGTH_SHORT).show();
        } else {
            ((ImageView) item.getActionView()).setImageResource(R.drawable.ic_trip_selection);
        }
        mTripSelectionIconEnabled = true;
        item.setEnabled(true);
    }

    private void setSideBarHandleVisibility() {
        MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
        if (TRIP_NO_TRIP.equals(DataHelper.getInstance().getRegisteredTrip())) {
            fragment.showSideBarHandle(false);
        } else {
            fragment.showSideBarHandle(true);
        }
    }

    private void onNavigationItemSelected(@StringRes int id) {
        mBinding.activityMainDrawerLayout.closeDrawer(Gravity.LEFT, false);
        if (id == R.string.nav_pois) { // POIs
            startActivity(PoiActivity.newIntent(this));
        } else if (id == R.string.nav_categories) {
            startActivity(CategoryActivity.newIntent(this));
        } else if (id == R.string.nav_settings) {
            startActivity(SettingActivity.newIntent(this));
        } else if (id == R.string.nav_profiles) {
            ProfileFragment.newInstance().show(getSupportFragmentManager(), PROFILE_DIALOG_FRAGMENT_TAG);
        } else if (id == R.string.nav_manual) {
//            startActivity(ManualActivity.newIntent(this));
            try {
                openPdf(TaConfig.MANUAL_PAGE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (id == R.string.nav_logout) {
            Toast.makeText(this, R.string.message_successful_logout, Toast.LENGTH_SHORT).show();
            DataHelper.getInstance().unregisterSideBar();
        }
    }

    public void openPdf(String url) throws IOException {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void setupActionBar() {
        setSupportActionBar(mBinding.activityMainToolbar);

        mToggle = new ActionBarDrawerToggle(
                this, mBinding.activityMainDrawerLayout, mBinding.activityMainToolbar, R.string.activity_main_navigation_drawer_open, R.string.activity_main_navigation_drawer_close);
        mBinding.activityMainDrawerLayout.addDrawerListener(mToggle);
        mBinding.activityMainDrawerLayout.setScrimColor(Color.TRANSPARENT);
        mToggle.syncState();

        mBinding.activityMainDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                if (!DataHelper.getInstance().isRegisteredSidebar()) {
                    mBinding.activityMainDrawerLayout.closeDrawer(Gravity.LEFT);
                    if (mShouldShowPasswordDialog) {
                        PasswordDialogFragment.newInstance().show(getSupportFragmentManager(), PASSWORD_DIALOG_FRAGMENT_TAG);
                        mShouldShowPasswordDialog = false;
                    }
                }
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {
                MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
                if (newState == STATE_SETTLING) { // Animation is in progress.
                    fragment.setAnimating(false); // Stop animation in fragment to allow finer animation of drawer.
                } else if (newState == STATE_IDLE) { // Animation is not in progress.
                    fragment.setAnimating(true);
                }
            }
        });

        //setDrawerState(false);
    }

    private void setDrawerState(boolean enabled) {
        if (enabled) {
            mBinding.activityMainDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mToggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_UNLOCKED);
            mToggle.syncState();

        } else {
            mBinding.activityMainDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mToggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mToggle.syncState();
        }
    }

    /**
     * Show right side bar with trip list.
     */
    private void showSideBar() {
        MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
        mainFragment.setAnimating(false);

        Fragment fragment = TripFragment.newInstance(TRIP_FRAGMENT_PREVIOUS_STOP_COUNT, TRIP_FRAGMENT_NEXT_STOP_COUNT);
        getSupportFragmentManager().beginTransaction().replace(R.id.activityMain_sideContainer, fragment, TRIP_FRAGMENT_TAG).commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
        mBinding.activityMainInclude.activityMainSideContainer.setVisibility(View.VISIBLE);
        AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.side_bar_animator);
        set.setTarget(mBinding.activityMainInclude.activityMainSideContainer);
        set.setInterpolator(new FastOutLinearInInterpolator());
        set.start();

        new Handler().postDelayed(() -> {
            mBinding.activityMainInclude.activityMainSideContainerSpace.setVisibility(View.VISIBLE);
            mainFragment.setAnimating(true);
        }, set.getChildAnimations().get(0).getDuration());
    }

    /**
     * Hide right side bar with trip list.
     */
    private void hideSideBar() {
        mBinding.activityMainInclude.activityMainSideContainerSpace.setVisibility(View.GONE);

        MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
        mainFragment.setAnimating(false);

        AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.side_bar_animator_reversed);
        set.setTarget(mBinding.activityMainInclude.activityMainSideContainer);
        set.setInterpolator(new LinearOutSlowInInterpolator());
        set.start();

        new Handler().postDelayed(() -> {
            Fragment tripFragment = getSupportFragmentManager().findFragmentByTag(TRIP_FRAGMENT_TAG);
            getSupportFragmentManager().beginTransaction().remove(tripFragment).commitAllowingStateLoss();
            getSupportFragmentManager().executePendingTransactions();
            mBinding.activityMainInclude.activityMainSideContainer.setVisibility(View.GONE);
            mBinding.activityMainInclude.activityMainSideContainerSpace.setVisibility(View.GONE);
            mainFragment.setAnimating(true);
        }, set.getChildAnimations().get(0).getDuration());
    }

    /**
     * Auto-register in case there is active trip. In other case do nothing.
     */
    private void autoRegister(boolean firstTime) {
        if (getSupportFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG) == null) {  // Fragment is not attached due to insufficient permissions.
            return;
        }

        DataHelper.getInstance().getActiveTrip(new TaCallback<String>() {
            @Override
            public void onResponse(String trip) {
                boolean tripChanged = trip != null && TRIP_NO_TRIP.equals(DataHelper.getInstance().getRegisteredTrip());
                boolean tripChanged2 = trip == null && !TRIP_NO_TRIP.equals(DataHelper.getInstance().getRegisteredTrip());

                // Register every time during first run in order to show sidebar when active trip, in other case only when registration is changed.
                if (firstTime || tripChanged || tripChanged2 || (trip != null && !trip.equals(DataHelper.getInstance().getRegisteredTrip()))) {
                    String previousTrip = DataHelper.getInstance().getRegisteredTrip();
                    if (trip == null) {
                        DataHelper.getInstance().unregisterTrip();
                    } else {
                        DataHelper.getInstance().registerTrip(trip);
                        showSideBar();  // This is default state after application start.
                    }
                    initTripIdSelectionIconLoader();  // Set correct color of icon.
                    setSideBarHandleVisibility();  // Set handle properly.
                    mViewModel.reloadPois();
                    if (trip != null && previousTrip != null && !trip.equals(previousTrip)) {
                        Toast.makeText(MainActivity.this, "Jízda byla změnena kvůli synchronizaci s dalším zařízením", Toast.LENGTH_LONG).show();
                    }
                    Log.d("reloadingPois", ".");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // Nothing to do here.
            }
        });
    }
}
