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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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

    private static int notificationId = 1;

    static void sendWakeup(final Context context, String token)
    {
        if (TextUtils.isEmpty(token))
        {
            createNotification(context, context.getString(R.string.title_error), context.getString(R.string.token_is_empty), MainActivity.CHANNEL_ERROR);
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
                        createNotification(context, context.getString(R.string.title_info), context.getString(R.string.response_is) + response, MainActivity.CHANNEL_INFO);
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                createNotification(context, context.getString(R.string.title_error), error.getMessage(), MainActivity.CHANNEL_ERROR);
            }
        });

        queue.add(stringRequest);
    }

    private static void createNotification(Context context, String title, String message, String channelId)
    {

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message)
                .setTicker(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (channelId.equals(MainActivity.CHANNEL_ERROR))
            builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);

        notificationManager.notify(notificationId, builder.build());
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
