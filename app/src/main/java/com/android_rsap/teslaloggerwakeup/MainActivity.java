//
//  TeslaLoggerWakeup
//
//  Copyright 2019 Martin Gerczuk
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package com.android_rsap.teslaloggerwakeup;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity
{
    public static final String CHANNEL_INFO = "default";
    public static final String CHANNEL_ERROR = "error";

    private int selected;
    private List<String> deviceNames;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        deviceNames = new ArrayList<String>();
        for (BluetoothDevice bt : pairedDevices)
            deviceNames.add(bt.getName());
        selected = deviceNames.indexOf(preferences.getString(AclReceiver.PREFKEY_DEVICE_NAME, AclReceiver.DEFAULT_DEVICE));

        Spinner spinner = findViewById(R.id.spinner_device);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, deviceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setSelection(selected);

        EditText edit = findViewById(R.id.edit_token);
        edit.setText(preferences.getString(AclReceiver.PREFKEY_TOKEN, AclReceiver.DEFAULT_TOKEN));

        findViewById(R.id.button_apply).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();

                Spinner spinner1 = findViewById(R.id.spinner_device);
                if (spinner1.getSelectedItem() != null)
                {
                    String newDevice = spinner1.getSelectedItem().toString();
                    editor.putString(AclReceiver.PREFKEY_DEVICE_NAME, newDevice);
                }

                EditText edit1 = findViewById(R.id.edit_token);
                CharSequence newToken = edit1.getText();
                editor.putString(AclReceiver.PREFKEY_TOKEN, newToken.toString());

                editor.apply();
            }
        });

        findViewById(R.id.button_wakeup).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String token = preferences.getString(AclReceiver.PREFKEY_TOKEN, AclReceiver.DEFAULT_TOKEN);
                AclReceiver.sendWakeup(MainActivity.this, token);
            }
        });
    }

    private void createNotificationChannel()
    {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            NotificationChannel channel = new NotificationChannel(CHANNEL_INFO, getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.channel_description));

            notificationManager.createNotificationChannel(channel);

            channel = new NotificationChannel(CHANNEL_ERROR, getString(R.string.error_channel_name), NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(getString(R.string.error_channel_description));
            channel.enableVibration(true);

            notificationManager.createNotificationChannel(channel);
        }
    }
}
