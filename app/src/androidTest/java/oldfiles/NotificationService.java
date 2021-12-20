package oldfiles;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.o7planning.sqlietereal.DatabaseHelper;
import org.o7planning.sqlietereal.R;
import org.o7planning.sqlietereal.Uebersichtactivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static org.o7planning.sqlietereal.Notifications.CHANNEL_1_ID;

public class NotificationService extends Service {

    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;

    Timer timer;
    TimerTask timerTask;
    String TAG = "Timers";
    int Your_X_SECS = 8;


    DatabaseHelper myDb;
    SimpleDateFormat dateFormat;
    Date currentDate;
    long days;
    String text;

    private static final String DEVELOPERS_OPTION_UPDATE_LEVEL = "0";

    private static final int LEVEL_0_THRESHHOLD = -3;//Abgelaufen seit mehr als 3 Tagen
    private static final int LEVEL_1_THRESHHOLD = -3;//Abgelaufen seit weniger als 3 Tagen
    private int LEVEL_2_THRESHHOLD = 4;//Kurz vor dem Ablaufen 1-7 Tage (Default: 4)
    private static final int LEVEL_3_THRESHHOLD = 10;//Läuft innerhalb von 10 Tagen ab
    private static final int LEVEL_4_THRESHHOLD = 10;//Hat noch mehr als 10 Tage Haltbarkeit
    private static final int LEVEL_9_THRESHHOLD = 0;//Benachrichtigungen manuell deaktiviert

    private static final int MODE = 0; //0 = developement //1 = normal use

    @Override
    public IBinder onBind (Intent arg0){
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }
    @Override
    public void onCreate(){
        Log.d(TAG, "onCreate");

        myDb = new DatabaseHelper(this); //Calls Construktor of DatabaseHelper

        currentDate = Calendar.getInstance().getTime();
        dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
    }
    @Override
    public void onDestroy(){
        Log.d(TAG, "onDestroy");
        stopTimerTask();
        super .onDestroy();
    }

    private void setLevel2Threshold(){
        LEVEL_2_THRESHHOLD = getSettingsLevel2(this);
    }

    private int getSettingsLevel2(Context context){
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return prefs.getInt("Saved_Level2_Level", 4);
    }

    final Handler handler = new Handler();
    public void startTimer(){
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 5000, Your_X_SECS * 1000);
    }
    public void stopTimerTask(){
        if(timer != null){
            timer.cancel();
            timer = null;
        }
    }
    public void initializeTimerTask(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(getAppStatusLink() == 0) {
                            myDb.updateLevel(NotificationService.this);
                            checkNotified();
                            Log.d(TAG, "App is shut down i can synchronice and notificate");
                        }
                    }
                });

            }
        };
    }

    public int getAppStatusLink(){
        return(getAppStatus(this));
    }

    public void updateLevel(){
        Cursor data = myDb.getAllData();
        if(data.getCount() == 0){
            //do nothing
        }
        else{
            while(data.moveToNext()){
                String Id = String.valueOf(data.getInt(0));
                String Barcode = data.getString(1);
                String Produktname = data.getString(2);
                String Mhd = data.getString(3);
                int level = data.getInt(4);
                String Notified = data.getString(5);

                int newLevel = determineLevel(Mhd, level);
                Log.v("timer", "New Level: " + newLevel);
                if(newLevel!=level){
                    boolean isUpdate = myDb.updateData(Id, Barcode, Produktname, Mhd, String.valueOf(newLevel), "0");
                }else{
                    boolean isUpdate = myDb.updateData(Id, Barcode, Produktname, Mhd, String.valueOf(level), Notified);
                }
            }
        }
    }

    private int determineLevel(String Mhd, int level){
        try {
            setLevel2Threshold();
            Date futureDate = dateFormat.parse(Mhd);
            long diff = 0;
            try {
                diff = futureDate.getTime() - currentDate.getTime();
            }catch(Exception e){
                e.printStackTrace();
            }

            long days = diff / (24 * 60 * 60 * 1000);
            long hours = diff / (60 * 60 * 1000);

            Log.v("timer", "Mhd: " + Mhd + "Difference in days: " + days + "! Old level: " + level + " In hours: " +hours);

            if(level==9) return level; //Manuell ausgetragen
            else if(days>=LEVEL_4_THRESHHOLD-1) return 4; //Noch mehr als 10 Tage
            else if(days>LEVEL_2_THRESHHOLD-1) return 3; //Variables Level 2(1-7 Tage) threshold bis 10 Tage
            else if(days>=0) return 2; //Noch nicht abgelaufen und variabel 1-7 Tage übrig
            else if(days>LEVEL_1_THRESHHOLD-1) return 1; //Abgelaufen seit weniger als 3 Tagen
            else return 0; //Abgelaufen seit mehr als 3 Tagen

        }catch(Exception e){
            e.printStackTrace();
        }
        return level;
    }

    static public int getSettingsWarningLevel(Context context){
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return prefs.getInt("Saved_Notification_Importance_Level", 0); //adapt default value!!!!
    }

    static public int getAppStatus(Context context){
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Log.d("Timers", "App status is: " + prefs.getInt("App_Status", 1));
        return prefs.getInt("App_Status", 1);
    }

    public void checkNotified(){
        int priority = getSettingsWarningLevel(this);
        Intent resultIntent = new Intent(this, Uebersichtactivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Cursor data = myDb.getAllData();
        if(data.getCount() == 0){
            //Toast.makeText(MainActivity.this,"The Database was empty  :(.", Toast.LENGTH_LONG).show();
        }
        else{
            Log.d("Letztertest", "in check notified");
            while(data.moveToNext()){
                String Id = String.valueOf(data.getInt(0));
                String Barcode = data.getString(1);
                String Produktname = data.getString(2);
                String Mhd = data.getString(3);
                int level = data.getInt(4);
                Log.d("Letztertest", "Level: " + level);
                String Notified = data.getString(5);
                int settings = getSettingsWarningLevel(this)+2;

                Log.d("AUSGEBUNG", "Produktname: " + Produktname);
                Log.d("AUSGEBUNG", "Mhd: " + Mhd);
                Log.d("AUSGEBUNG", "level: " + level);
                Log.d("AUSGEBUNG", "notified" + Notified);

                if(Notified.equals("0")){
                    Log.d("Letztertest", "erster sprung");
                    if(level == 2) {
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





                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

                            Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID[settings])
                                    .setSmallIcon(R.drawable.ic_one)
                                    .setContentIntent(resultPendingIntent)
                                    .setAutoCancel(true)
                                    .setContentTitle("Warnung")
                                    .setContentText(Produktname + " überschreitet MHD " + text)
                                    .setPriority(priority) //Max = 2 High = 1 Default = 0 Low = -1 Min=-2
                                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                    .build(); //look into setCategory
                            //if we send the same id it will overwrite the sent notification
                            notificationManager.notify(1, notification);
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

                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

                            Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID[settings])
                                    .setSmallIcon(R.drawable.ic_one)
                                    .setContentIntent(resultPendingIntent)
                                    .setAutoCancel(true)
                                    .setContentTitle("Warnung")
                                    .setContentText(Produktname + " hat MHD seit: " + days + " Tagen überschritten")
                                    .setPriority(priority)
                                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                    .build(); //look into setCategory
                            //if we send the same id it will overwrite the sent notification
                            notificationManager.notify(1, notification);
                        }
                    }else if(level == 0){
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


                            Log.d("Letztertest", "BN3");
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

                            Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID[settings])
                                    .setSmallIcon(R.drawable.ic_one)
                                    .setContentIntent(resultPendingIntent)
                                    .setAutoCancel(true)
                                    .setContentTitle("Warnung!")
                                    .setContentText(Produktname + " hat MHD seit: " + days + " Tagen überschritten" + savedValue + " " + priority)
                                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                    .build(); //look into setCategory
                            //if we send the same id it will overwrite the sent notification
                            notificationManager.notify(1, notification);
                        }
                    }
                }
            }
        }
    }

    private void createNotification(){
        NotificationManager mNotificationManager = (NotificationManager) getSystemService( NOTIFICATION_SERVICE ) ;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext() , default_notification_channel_id ) ;
        mBuilder.setContentTitle( "My Notification" ) ;
        mBuilder.setContentText( "Notification Listener Service Example" ) ;
        mBuilder.setTicker( "Notification Listener Service Example" ) ;
        mBuilder.setSmallIcon(R.drawable. ic_launcher_foreground ) ;
        mBuilder.setAutoCancel( true ) ;
        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
            int importance = NotificationManager. IMPORTANCE_HIGH ;
            NotificationChannel notificationChannel = new NotificationChannel( NOTIFICATION_CHANNEL_ID , "NOTIFICATION_CHANNEL_NAME" , importance) ;
            mBuilder.setChannelId( NOTIFICATION_CHANNEL_ID ) ;
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(notificationChannel) ;
        }
        Objects.requireNonNull(mNotificationManager).notify(( int ) System. currentTimeMillis () , mBuilder.build()) ;
    }
}
