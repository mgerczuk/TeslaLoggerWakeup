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

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class AclReceiver extends BroadcastReceiver
{
    public static final String PREFKEY_DEVICE_NAME = "DeviceName";
    public static final String PREFKEY_TOKEN = "Token";

    public static final String DEFAULT_DEVICE = "";
    public static final String DEFAULT_TOKEN = "";

    static void sendWakeup(final Context context, String token)
    {
        if (TextUtils.isEmpty(token))
        {
            Toast.makeText(context, R.string.token_is_empty, Toast.LENGTH_LONG).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://teslalogger.de/wakeup.php?t=" + token;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Toast.makeText(context, context.getString(R.string.response_is) + response, Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(context, R.string.http_get_failed, Toast.LENGTH_LONG).show();
            }
        });

        queue.add(stringRequest);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED))
        {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String deviceName = preferences.getString(PREFKEY_DEVICE_NAME, DEFAULT_DEVICE);
            BluetoothDevice connectedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (!TextUtils.isEmpty(deviceName) && deviceName != null && deviceName.equals(connectedDevice.getName()))
            {
                String token = preferences.getString(PREFKEY_TOKEN, DEFAULT_TOKEN);
                sendWakeup(context, token);
            }
        }
    }
}
