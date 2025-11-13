package com.khotapps.stringflowlivewallpaper;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button btnAdvSetting;
    private Button btnMore;
    private Button btnSetLiveWallpaper;
    private AdView adView;
    private InterstitialAd mInterstitialAd;

    // TEST AD UNIT IDs (GUARANTEED TO SHOW)
    private static final String BANNER_TEST_ID = "ca-app-pub-3940256099942544/6300978111";
    private static final String INTERSTITIAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupAdMob();
        setupButtons();
    }

    private void setupAdMob() {
        // FORCE TEST ADS
        RequestConfiguration config = new RequestConfiguration.Builder()
                .setTestDeviceIds(Collections.singletonList(AdRequest.DEVICE_ID_EMULATOR))
                .build();
        MobileAds.setRequestConfiguration(config);

        // Initialize
        MobileAds.initialize(this, initializationStatus -> {
            Log.d(TAG, "AdMob Initialized");
            loadBannerAd();
            loadInterstitialAd();
        });
    }

    private void loadBannerAd() {
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, INTERSTITIAL_TEST_ID, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.d(TAG, "TEST INTERSTITIAL LOADED");
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        mInterstitialAd = null;
                        Log.e(TAG, "TEST AD FAILED: " + error.getMessage());
                    }
                });
    }

    private void setupButtons() {
        btnAdvSetting = findViewById(R.id.advSettingButton);
        btnMore = findViewById(R.id.moreOptionButton);
        btnSetLiveWallpaper = findViewById(R.id.btnSetLiveWallpaper);

        btnAdvSetting.setOnClickListener(v -> showAdThen(() -> startActivity(new Intent(this, AdvSettingActivity.class))));
        btnMore.setOnClickListener(v -> showAdThen(() -> startActivity(new Intent(this, MoreOptionActivity.class))));
        btnSetLiveWallpaper.setOnClickListener(v -> showAdThen(this::setLiveWallpaper));
    }

    private void showAdThen(Runnable action) {
        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    action.run();
                    mInterstitialAd = null;
                    loadInterstitialAd();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                    action.run();
                    mInterstitialAd = null;
                    loadInterstitialAd();
                }
            });
            mInterstitialAd.show(this);
        } else {
            action.run();
            loadInterstitialAd(); // Try again
        }
    }

    private void setLiveWallpaper() {
        try {
            Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    new ComponentName(this, WaveLiveWallpaperService.class));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to open wallpaper settings", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        if (adView != null) adView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) adView.resume();
    }

    @Override
    protected void onDestroy() {
        if (adView != null) adView.destroy();
        super.onDestroy();
    }
}