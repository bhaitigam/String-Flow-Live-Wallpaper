package com.khotapps.stringflowlivewallpaper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply global theme (Dark/Light/System)
        ThemeHelper.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Initialize views
        ImageView btnBack = findViewById(R.id.btnBackAbout);
        TextView linkPrivacy = findViewById(R.id.linkPrivacy);
        TextView linkRate = findViewById(R.id.linkRate);

        // Back button action
        btnBack.setOnClickListener(v -> finish());

        // Open Privacy Policy link
        linkPrivacy.setOnClickListener(v -> {
            String url = "https://your-privacy-policy-url.com"; // <-- Replace with your real privacy policy link
            openLink(url);
        });

        // Open Google Play Store (app rating)
        linkRate.setOnClickListener(v -> {
            String packageName = getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + packageName)));
            } catch (android.content.ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
            }
        });
    }

    /** Helper function to open URLs in browser */
    private void openLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
