package com.khotapps.stringflowlivewallpaper;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

public class AdvSettingActivity extends AppCompatActivity implements ColorPickerDialogListener {

    private static final String PREFS_NAME    = "WavePrefs";
    private static final String KEY_STYLE     = "wave_style";
    private static final String KEY_SPEED     = "wave_speed";
    private static final String KEY_LINES     = "wave_lines";
    private static final String KEY_THICKNESS = "wave_thickness";
    private static final String KEY_PADDING   = "wave_padding";
    private static final String KEY_COLOR     = "wave_color";

    private static final int COLOR_PICKER_ID = 1;

    private WavePreviewView wavePreview;
    private Button btnColorPicker;
    private View viewColorPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adv_setting);

        wavePreview     = findViewById(R.id.wavePreview);
        RadioGroup radioGroup      = findViewById(R.id.radioGroupStyle);
        SeekBar    seekSpeed       = findViewById(R.id.seekSpeed);
        TextView   txtSpeedValue   = findViewById(R.id.txtSpeedValue);
        SeekBar    seekLines       = findViewById(R.id.seekLines);
        TextView   txtLinesValue   = findViewById(R.id.txtLinesValue);
        SeekBar    seekThickness   = findViewById(R.id.seekThickness);
        TextView   txtThicknessValue = findViewById(R.id.txtThicknessValue);
        SeekBar    seekPadding     = findViewById(R.id.seekPadding);
        TextView   txtPaddingValue = findViewById(R.id.txtPaddingValue);
        btnColorPicker = findViewById(R.id.btnColorPicker);
        viewColorPreview = findViewById(R.id.viewColorPreview);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load saved values
        int savedStyle     = prefs.getInt(KEY_STYLE, 0);     // 0 = None
        int savedSpeed     = prefs.getInt(KEY_SPEED, 50);
        int savedLines     = prefs.getInt(KEY_LINES, 1);
        int savedThickness = prefs.getInt(KEY_THICKNESS, 4);
        int savedPadding   = prefs.getInt(KEY_PADDING, 15);
        int savedColor     = prefs.getInt(KEY_COLOR, Color.CYAN);

        // Apply
        wavePreview.setStyle(savedStyle);
        wavePreview.setSpeed(savedSpeed / 50f);
        wavePreview.setNumLines(savedLines);
        wavePreview.setLineThickness(savedThickness);
        wavePreview.setLinePadding(savedPadding);
        wavePreview.setCustomColor(savedColor);

        // UI
        seekSpeed.setProgress(savedSpeed);
        txtSpeedValue.setText("Speed: " + savedSpeed);

        seekLines.setProgress(savedLines - 1);
        txtLinesValue.setText("Lines: " + savedLines);

        seekThickness.setProgress(savedThickness - 2);
        txtThicknessValue.setText("Thickness: " + savedThickness + "dp");

        seekPadding.setProgress(savedPadding);
        txtPaddingValue.setText("Spacing: " + savedPadding + "dp");

        viewColorPreview.setBackgroundColor(savedColor);
        updateColorPickerState(savedStyle == 0);  // FIXED: Correct method name

        // Set radio
        int checkedId = getRadioIdForStyle(savedStyle);
        radioGroup.check(checkedId);

        // Style listener
        radioGroup.setOnCheckedChangeListener((group, id) -> {
            int style = getStyleForRadioId(id);
            wavePreview.setStyle(style);
            prefs.edit().putInt(KEY_STYLE, style).apply();
            updateColorPickerState(style == 0);  // FIXED
        });

        // Speed
        seekSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean u) {
                if (u) {
                    txtSpeedValue.setText("Speed: " + p);
                    wavePreview.setSpeed(p / 50f);
                    prefs.edit().putInt(KEY_SPEED, p).apply();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        // Lines
        seekLines.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean u) {
                if (u) {
                    int lines = p + 1;
                    txtLinesValue.setText("Lines: " + lines);
                    wavePreview.setNumLines(lines);
                    prefs.edit().putInt(KEY_LINES, lines).apply();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        // Thickness
        seekThickness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean u) {
                if (u) {
                    int thick = p + 2;
                    txtThicknessValue.setText("Thickness: " + thick + "dp");
                    wavePreview.setLineThickness(thick);
                    prefs.edit().putInt(KEY_THICKNESS, thick).apply();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        // Padding
        seekPadding.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean u) {
                if (u) {
                    txtPaddingValue.setText("Spacing: " + p + "dp");
                    wavePreview.setLinePadding(p);
                    prefs.edit().putInt(KEY_PADDING, p).apply();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        // Color Picker
        btnColorPicker.setOnClickListener(v -> {
            if (btnColorPicker.isEnabled()) {
                ColorPickerDialog.newBuilder()
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setAllowPresets(true)
                        .setDialogId(COLOR_PICKER_ID)
                        .setColor(savedColor)
                        .show(this);
            }
        });
    }

    private void updateColorPickerState(boolean enable) {
        btnColorPicker.setEnabled(enable);
        btnColorPicker.setAlpha(enable ? 1.0f : 0.5f);
    }

    private int getRadioIdForStyle(int style) {
        if (style == 0) return R.id.radio_none;
        if (style == 1) return R.id.radio_k1;
        if (style == 2) return R.id.radio_k2;
        if (style == 3) return R.id.radio_k3;
        return R.id.radio_k4;
    }

    private int getStyleForRadioId(int id) {
        if (id == R.id.radio_none) return 0;
        if (id == R.id.radio_k1) return 1;
        if (id == R.id.radio_k2) return 2;
        if (id == R.id.radio_k3) return 3;
        return 4;
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        if (dialogId == COLOR_PICKER_ID) {
            viewColorPreview.setBackgroundColor(color);
            wavePreview.setCustomColor(color);
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putInt(KEY_COLOR, color)
                    .apply();
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {}
}