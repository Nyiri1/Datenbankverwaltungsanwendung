package org.o7planning.sqlietereal;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Person;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.o7planning.sqlietereal.Notifications.CHANNEL_1_ID;

// Hintergrundservice für die Notifications
public class ExampleJobService extends JobService {
    public static final String TAG = "ExampleJobService";
    private boolean jobCancelled = false;

    DatabaseHelper myDb;

    Date currentDate;

    Long dateOfLastNotification;

    SimpleDateFormat dateFormat;

    //Steht dieser Parameter auf 1 wird nur eine Notification pro Produkt pro Threshold ausgegeben
    //Eventuell ändern auf tägliche Benachrichtigung oder eine Option für den Nutzer hinzufügen
    private static final String DEVELOPERS_OPTION_UPDATE_LEVEL = "0";

    long days;

    String text;
    String newText;

    @Override
    public boolean onStartJob(JobParameters params){
        Log.d(TAG, "Job started");



        myDb = new DatabaseHelper(this); //Calls Construktor of DatabaseHelper

        currentDate = Calendar.getInstance().getTime();
        dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());

        doBackgroundWork(params);

        return true;
    }

    private void doBackgroundWork(final JobParameters params){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (jobCancelled) {
                    return;
                }
                    dateOfLastNotification = getLastNotificationAndUpdateSession(ExampleJobService.this);
                    if(currentDate.getTime()-dateOfLastNotification > (10)) { // Eine Meldung soll alle 10 Sekunden erfolgen {Zu Testzwecken}
                        Log.d(TAG, "Time span: " + (currentDate.getTime()-dateOfLastNotification) + " ms");
                        myDb.updateLevel(ExampleJobService.this);
                        checkNotified();
                        Log.d(TAG, "App is shut down i can synchronice and notificate");
                        dateOfLastNotification = Calendar.getInstance().getTime().getTime();
                        setLastNotificationAndUpdateSession(dateOfLastNotification);
                    }else{
                        Log.d(TAG, "The time span since last notification is not big enough");
                    }
                }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params){
        Log.d(TAG, "Job cancelled before completion");
        jobCancelled = true;
        return true;
    }

    static public int getAppStatus(Context context){
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Log.d("Timers", "App status is: " + prefs.getInt("App_Status", 1));
        return prefs.getInt("App_Status", 1);
    }

    //Wenn eine Benachrichtigung erfolgen soll
    //Wird durch die Produkte iteriert und ein Ausgabestring zusammengebaut
    //Am Ende erfolgt die Ausgabe der Benachrichtigung
    public void checkNotified(){
        newText = "";
        int priority = getSettingsWarningLevel(this);
        Intent resultIntent = new Intent(this, Uebersichtactivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Cursor data = myDb.getAllData();
        if(data.getCount() == 0){
            //Toast.makeText(MainActivity.this,"The Database was empty  :(.", Toast.LENGTH_LONG).show();
        }
        else{
            Log.d("Letztertest", "in check notified");
            int settings = getSettingsWarningLevel(this)+2;
            while(data.moveToNext()){
                String Id = String.valueOf(data.getInt(0));
                String Barcode = data.getString(1);
                String Produktname = data.getString(2);
                String Mhd = data.getString(3);
                int level = data.getInt(4);
                Log.d("Letztertest", "Level: " + level);
                Log.d(TAG, "Produktname: " + Produktname);
                String Notified = data.getString(5);

                Log.d("AUSGEBUNG", "Produktname: " + Produktname);
                Log.d("AUSGEBUNG", "Mhd: " + Mhd);
                Log.d("AUSGEBUNG", "level: " + level);
                Log.d("AUSGEBUNG", "notified" + Notified);

                if(Notified.equals("0")){
                    Log.d("Letztertest", "erster sprung");
                    if(level == 0){
                        if(myDb.updateData(Id, Barcode, Produktname, Mhd, String.valueOf(level), DEVELOPERS_OPTION_UPDATE_LEVEL)){
                            try {
                                Date futureDate = dateFormat.parse(Mhd);
                                long diff = futureDate.getTime() - currentDate.getTime();
                                days = diff / (24 * 60 * 60 * 1000);
                                days++;

                            }catch(Exception e){
                                e.printStackTrace();
                            }
                            //test begin
                            int savedValue = getSettingsLevel2(this);
                            //test end

                            appendText(Produktname);

                            Log.d("Letztertest", "BN3");
                        }
                    }else if(level == 1){
                        Log.d("Letztertest", "BN2");
                        if(myDb.updateData(Id, Barcode, Produktname, Mhd, String.valueOf(level), DEVELOPERS_OPTION_UPDATE_LEVEL)) {
                            try {
                                Log.d("Letztertest", "BN21");
                                Date futureDate = dateFormat.parse(Mhd);
                                long diff = futureDate.getTime() - currentDate.getTime();
                                days = diff / (24 * 60 * 60 * 1000);
                                days++;

                            }catch(Exception e){
                                e.printStackTrace();
                            }

                            appendText(Produktname);
                        }
                    }
                    else if(level == 2) {
                        Log.d("Letztertest", "zweiter sprung");
                        if(myDb.updateData(Id, Barcode, Produktname, Mhd, String.valueOf(level), DEVELOPERS_OPTION_UPDATE_LEVEL)) {
                            try {
                                Date futureDate = dateFormat.parse(Mhd);
                                long diff = futureDate.getTime() - currentDate.getTime();
                                days = diff / (24 * 60 * 60 * 1000);
                                days++;

                                if(days==1 & diff>0) text = "morgen";
                                else if(days==1 & diff<0) text = "heute";
                                else text = "in " +days+" Tagen";
                                Log.d("Letztertest", "BN1");
                            }catch(Exception e){
                                e.printStackTrace();
                            }

                            appendText2(Produktname);
                        }
                    }
                }
            }
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID[settings])
                    .setSmallIcon(R.drawable.ic_one)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(newText))
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true)
                    .setContentTitle("Produkte überschreiten das MHD")

                    .setPriority(priority) //Max = 2 High = 1 Default = 0 Low = -1 Min=-2
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .build(); //look into setCategory
            //if we send the same id it will overwrite the sent notification
            notificationManager.notify(2, notification);
            Log.d(TAG, "Notifying done");
        }
    }

    private void appendText(String produktname){
        newText = newText + produktname + " " + days +" Tage übrig\n";
    }

    private void appendText2(String produktname){
        newText = newText + produktname + " läuft " + text +" ab\n";
    }

    // Erfasse welches Threshhold der Nutzer für Level 2 eingestellt hat
    private int getSettingsLevel2(Context context){
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return prefs.getInt("Saved_Level2_Level", 4);
    }

    // Erfasse welche Warnungsart der Nutzer gewählt hat
    static public int getSettingsWarningLevel(Context context){
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return prefs.getInt("Saved_Notification_Importance_Level", 0); //adapt default value!!!!
    }

    // Erfasse wann die letzte Benachrichtigung ausgegeben wurde
    static public Long getLastNotificationAndUpdateSession(Context context){
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return prefs.getLong("Saved_Last_Notification_And_Update_Level", 0);
    }

    // Speichere ab wann die letzte Benachrichtigung ausgegeben wurde
    private void setLastNotificationAndUpdateSession(Long setting) {
        SharedPreferences prefs = this.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("Saved_Last_Notification_And_Update_Level", setting);
        editor.apply();
    }
}
