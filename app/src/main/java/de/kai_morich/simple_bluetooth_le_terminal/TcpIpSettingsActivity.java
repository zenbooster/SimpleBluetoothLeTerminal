package de.kai_morich.simple_bluetooth_le_terminal;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference;

import android.widget.EditText;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.Editable;

public class TcpIpSettingsActivity extends AppCompatActivity {

    protected void showIP() {
        TextView tvIp = findViewById(R.id.tvIp);
        tvIp.setText(MainActivity.instance.getIPAddress());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity_tcpip);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        showIP();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference edit_Pref = (EditTextPreference)
                    getPreferenceScreen().findPreference("listen_port");
            /*edit_Pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // put validation here..
                    int v = Integer.parseInt(newValue.toString());

                    if(v >= 1024 && v <= 65535){
                        return true;
                    }else{
                        return false;
                    }
                }
            });*/
            edit_Pref.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            String validationError;
                            try {
                                // ... insert your validation logic here, throw on failure ...
                                int port = Integer.parseInt(editable.toString());
                                if(port < 1024 | port > 65535) {
                                    throw new Exception("Port number must be in the range from 1024 to 65535!");
                                }
                                validationError = null; // All OK!
                            } catch (Exception e) {
                                validationError = e.getMessage();
                            }
                            editText.setError(validationError);
                            editText.getRootView().findViewById(android.R.id.button1)
                                    .setEnabled(validationError == null);
                        }
                    });
                }
            });
        }
    }

    public void onbtnUpdateClick(View view)
    {
        showIP();
    }
}