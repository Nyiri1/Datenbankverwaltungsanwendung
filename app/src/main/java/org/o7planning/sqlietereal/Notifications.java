package org.o7planning.sqlietereal;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

//0=channel_none 4=channel_high shows,noise,intrude 3=channel_default shows,noise 2=channel_low shows,not intrusive 1=channel_min only shows in shade 0=channel_none does not show

public class Notifications extends Application {
    public static final String CHANNEL_1_ID[] = {"channel_none", "channel_min", "channel_low", "channel_default", "channel_high"};

    @Override
    public void onCreate(){
        super.onCreate();

        createNotificationChannel();
    }

    static public int getSettingsWarningLevel(Context context){
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return prefs.getInt("Saved_Notification_Importance_Level", 0); // Default Value noch auf einen Nutzerfreundlichen Wert anpassen
    }

    private void createNotificationChannel(){
        int settings = getSettingsWarningLevel(this)+2;

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            Log.v("BUGFIX", "in switch");
            switch(settings) {
                case 0:
                    Log.v("BUGFIX", "Case 0");
                    NotificationChannel channel_0 = new NotificationChannel(
                            CHANNEL_1_ID[settings],
                            "Channel 0",
                            NotificationManager.IMPORTANCE_NONE
                    );
                    Log.v("BUGFIX", "Case 0 channel created");
                    channel_0.setDescription("This is channel 0");
                    //Description what the channel is for
                    //many settings but user has ultimate control

                    NotificationManager manager0 = getSystemService(NotificationManager.class);
                    manager0.createNotificationChannel(channel_0);
                    break;
                case 1:
                    Log.v("BUGFIX", "Case 1");
                    NotificationChannel channel_1 = new NotificationChannel(
                            CHANNEL_1_ID[settings],
                            "Channel 1",
                            NotificationManager.IMPORTANCE_MIN
                    );
                    channel_1.setDescription("This is channel 1");

                    NotificationManager manager = getSystemService(NotificationManager.class);
                    manager.createNotificationChannel(channel_1);
                    break;
                case 2:
                    Log.v("BUGFIX", "Case 2");
                    NotificationChannel channel_2 = new NotificationChannel(
                            CHANNEL_1_ID[settings],
                            "Channel 2",
                            NotificationManager.IMPORTANCE_LOW
                    );
                    channel_2.setDescription("This is channel 2");

                    NotificationManager manager2 = getSystemService(NotificationManager.class);
                    manager2.createNotificationChannel(channel_2);
                    break;
                case 3:
                    Log.v("BUGFIX", "Case 3");
                    NotificationChannel channel_3 = new NotificationChannel(
                            CHANNEL_1_ID[settings],
                            "Channel 3",
                            NotificationManager.IMPORTANCE_DEFAULT
                    );
                    channel_3.setDescription("This is channel 3");

                    NotificationManager manager3 = getSystemService(NotificationManager.class);
                    manager3.createNotificationChannel(channel_3);
                    break;
                case 4:
                    Log.v("BUGFIX", "Case 4");
                    NotificationChannel channel_4 = new NotificationChannel(
                            CHANNEL_1_ID[settings],
                            "Channel 4",
                            NotificationManager.IMPORTANCE_HIGH
                    );
                    channel_4.setDescription("This is channel 4");

                    NotificationManager manager4 = getSystemService(NotificationManager.class);
                    manager4.createNotificationChannel(channel_4);
                    break;
            }
        }
    }

}
