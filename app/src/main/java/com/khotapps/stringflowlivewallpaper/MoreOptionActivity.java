package com.khotapps.stringflowlivewallpaper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MoreOptionActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ThemePrefs";
    private static final String KEY_THEME = "theme_mode";
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // INITIALIZE sp FIRST
        sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // NOW APPLY SAVED THEME (sp is ready!)
        applySavedTheme();

        setContentView(R.layout.activity_more);

        // --- UI Elements ---
        Button btnThemes = findViewById(R.id.btnThemes);
        Button btnShare = findViewById(R.id.btnShare);
        Button btnAbout = findViewById(R.id.btnBackAbout);
        Button btnFeedback = findViewById(R.id.btnFeedback);

        ImageView linkFacebook = findViewById(R.id.linkFacebook);
        ImageView linkInstagram = findViewById(R.id.linkInstagram);
        ImageView linkTwitter = findViewById(R.id.linkTwitter);
        ImageView linkGithub = findViewById(R.id.linkGithub);

        // --- Listeners ---
        btnThemes.setOnClickListener(v -> showThemeDialog());
        btnAbout.setOnClickListener(v -> startActivity(new Intent(this, AboutActivity.class)));

        btnShare.setOnClickListener(v -> shareApp());
        linkFacebook.setOnClickListener(v -> openLink("https://facebook.com/khotapps"));
        linkInstagram.setOnClickListener(v -> openLink("https://instagram.com/khotapps"));
        linkTwitter.setOnClickListener(v -> openLink("https://twitter.com/khotapps"));
        linkGithub.setOnClickListener(v -> openLink("https://github.com/kavingam/StringFlowLiveWallpaper"));

        btnFeedback.setOnClickListener(v -> sendFeedback());
    }

    // APPLY SAVED THEME (uses 'sp' which is now initialized)
    private void applySavedTheme() {
        int savedMode = sp.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedMode);
    }

    // SAVE & APPLY NEW THEME
    private void setThemeAndRecreate(int mode) {
        if (AppCompatDelegate.getDefaultNightMode() == mode) {
            return;
        }

        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(KEY_THEME, mode);
        editor.apply();

        recreate(); // Smooth restart
    }

    // THEME DIALOG
    private void showThemeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_theme_selector, null));

        AlertDialog dialog = builder.create();
        dialog.show();

        RadioGroup group = dialog.findViewById(R.id.themeGroup);
        RadioButton system = dialog.findViewById(R.id.themeSystem);
        RadioButton dark = dialog.findViewById(R.id.themeDark);
        RadioButton light = dialog.findViewById(R.id.themeLight);

        // Restore current selection
        int current = AppCompatDelegate.getDefaultNightMode();
        if (current == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            system.setChecked(true);
        } else if (current == AppCompatDelegate.MODE_NIGHT_YES) {
            dark.setChecked(true);
        } else {
            light.setChecked(true);
        }

        group.setOnCheckedChangeListener((g, checkedId) -> {
            int newMode;
            if (checkedId == system.getId()) {
                newMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            } else if (checkedId == dark.getId()) {
                newMode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                newMode = AppCompatDelegate.MODE_NIGHT_NO;
            }
            setThemeAndRecreate(newMode);
            dialog.dismiss();
        });
    }

    private void shareApp() {
        String appLink = "https://play.google.com/store/apps/details?id=" + getPackageName();
        Intent intent = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, "Check out String Flow Live Wallpaper:\n" + appLink);
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    private void openLink(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open link", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO)
                .setData(Uri.parse("mailto:"))
                .putExtra(Intent.EXTRA_EMAIL, new String[]{"darksicker71@gmail.com"})
                .putExtra(Intent.EXTRA_SUBJECT, "String Flow - Feedback");
        try {
            startActivity(Intent.createChooser(intent, "Send Email"));
        } catch (Exception e) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }
}