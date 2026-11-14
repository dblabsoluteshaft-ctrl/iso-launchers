package com.isolauncher;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String[] LABELS = {
        "Any emulator (choose each time)",
        "PPSSPP (PSP)",
        "Dolphin (GameCube / Wii)",
        "Dolphin MMJR2 (GameCube / Wii)"
    };
    private static final String[] PKGS = {
        null,
        "org.ppsspp.ppsspp",
        "org.dolphinemu.dolphinemu",
        "org.dolphinemu.mmjr"
    };
    private static final int PICK = 42;

    private Uri gameUri;
    private TextView fileText;
    private Spinner spinner;
    private Button launch;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        int p = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(p, p * 2, p, p);
        root.setBackgroundColor(Color.parseColor("#0f1115"));

        TextView title = new TextView(this);
        title.setText("ISO Launcher");
        title.setTextSize(26);
        title.setTextColor(Color.WHITE);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Pick a game file of any size and open it in your emulator.");
        sub.setTextColor(Color.LTGRAY);
        sub.setPadding(0, p / 2, 0, p);
        root.addView(sub);

        Button pick = new Button(this);
        pick.setText("1. Choose game file (.iso, .bin, .rvz, ...)");
        pick.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(i, PICK);
        });
        root.addView(pick);

        fileText = new TextView(this);
        fileText.setText("No file selected");
        fileText.setTextColor(Color.GRAY);
        fileText.setPadding(0, p / 2, 0, p / 2);
        root.addView(fileText);

        TextView lbl = new TextView(this);
        lbl.setText("2. Emulator");
        lbl.setTextColor(Color.LTGRAY);
        root.addView(lbl);

        spinner = new Spinner(this);
        ArrayAdapter<String> ad = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, LABELS);
        spinner.setAdapter(ad);
        root.addView(spinner);

        launch = new Button(this);
        launch.setText("3. Play");
        launch.setEnabled(false);
        launch.setOnClickListener(v -> play());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = p;
        root.addView(launch, lp);

        TextView hint = new TextView(this);
        hint.setText("Controllers: connect via Bluetooth/USB. Navigate this screen with the "
                + "D-pad and press A. In-game controls are handled by the emulator.");
        hint.setTextColor(Color.GRAY);
        hint.setGravity(Gravity.START);
        hint.setPadding(0, p, 0, 0);
        root.addView(hint);

        setContentView(root);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK && res == RESULT_OK && data != null && data.getData() != null) {
            gameUri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(
                        gameUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) { }
            fileText.setText(nameOf(gameUri));
            fileText.setTextColor(Color.parseColor("#4f8cff"));
            launch.setEnabled(true);
        }
    }

    private String nameOf(Uri u) {
        try (Cursor c = getContentResolver().query(u, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (i >= 0) return c.getString(i);
            }
        } catch (Exception ignored) { }
        return u.getLastPathSegment();
    }

    private void play() {
        String pkg = PKGS[spinner.getSelectedItemPosition()];
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(gameUri, "application/octet-stream");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (pkg != null) i.setPackage(pkg);
        try {
            startActivity(pkg == null ? Intent.createChooser(i, "Open with") : i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, pkg == null
                    ? "No installed app can open this file."
                    : "That emulator isn't installed or doesn't accept this file. Try 'Any emulator'.",
                    Toast.LENGTH_LONG).show();
        }
    }
          }
