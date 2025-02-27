/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package cordova.plugins;

/*
 * Imports
 */

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import androidx.core.app.NotificationManagerCompat;

/**
 * Diagnostic plugin implementation for Android
 */
public class Diagnostic_Notifications extends CordovaPlugin{


    /*************
     * Constants *
     *************/


    /**
     * Tag for debug log messages
     */
    public static final String TAG = "Diagnostic_Notifications";


    /*************
     * Variables *
     *************/

    /**
     * Singleton class instance
     */
    public static Diagnostic_Notifications instance = null;

    private Diagnostic diagnostic;

    /**
     * Current Cordova callback context (on this thread)
     */
    protected CallbackContext currentContext;


    /*************
     * Public API
     ************/

    /**
     * Constructor.
     */
    public Diagnostic_Notifications() {}

    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        instance = this;
        diagnostic = Diagnostic.getInstance();

        super.initialize(cordova, webView);
    }


    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArry of arguments for the plugin.
     * @param callbackContext   The callback id used when calling back into JavaScript.
     * @return                  True if the action was valid, false if not.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Diagnostic.instance.currentContext = currentContext = callbackContext;

        try {
            if(action.equals("isRemoteNotificationsEnabled")) {
                callbackContext.success(isRemoteNotificationsEnabled() ? 1 : 0);
            } else if(action.equals("switchToNotificationSettings")) {
                switchToNotificationSettings();
                callbackContext.success();
            } else {
                diagnostic.handleError("Invalid action");
                return false;
            }
        }catch(Exception e ) {
            diagnostic.handleError("Exception occurred: ".concat(e.getMessage()));
            return false;
        }
        return true;
    }


    public boolean isRemoteNotificationsEnabled() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this.cordova.getActivity().getApplicationContext());
        boolean result = notificationManagerCompat.areNotificationsEnabled();
        return result;
    }

    public void switchToNotificationSettings() {
        Context context = this.cordova.getActivity().getApplicationContext();
        Intent settingsIntent = new Intent();
        String packageName = context.getPackageName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            diagnostic.logDebug("Switch to notification Settings");
            settingsIntent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            settingsIntent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName);
        } else {
            settingsIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            settingsIntent.setData(Uri.parse("package:" + packageName));
            diagnostic.logDebug("Switch to notification Settings: Only possible on android O or above. Falling back to application details");
        }
        cordova.getActivity().startActivity(settingsIntent);
    }

    /************
     * Internals
     ***********/


}
