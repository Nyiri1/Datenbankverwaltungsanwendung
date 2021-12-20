package oldfiles;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/* I dont use this anymore

import static org.o7planning.sqlietereal.Notifications.CHANNEL_1_ID;
import static org.o7planning.sqlietereal.SettingsActivity.getSettingsLevel2;

public class TimerService extends IntentService {//Deprecated?

    //test
    DatabaseHelper myDb;
    SimpleDateFormat dateFormat;
    Date currentDate;
    long days;
    String text;

    private static final int LEVEL_0_THRESHHOLD = -3;//Abgelaufen seit mehr als 3 Tagen
    private static final int LEVEL_1_THRESHHOLD = -3;//Abgelaufen seit weniger als 3 Tagen
    private static final int LEVEL_2_THRESHHOLD = 4;//Kurz vor dem Ablaufen 1-7 Tage (Default: 4)
    private static final int LEVEL_3_THRESHHOLD = 10;//Läuft innerhalb von 10 Tagen ab
    private static final int LEVEL_4_THRESHHOLD = 10;//Hat noch mehr als 10 Tage Haltbarkeit
    private static final int LEVEL_9_THRESHHOLD = 0;//Benachrichtigungen manuell deaktiviert

    private static final int MODE = 0; //0 = developement //1 = normal use



    public TimerService(){
        super("Timer Service");
    }

    @Override
    public void onCreate(){
        super.onCreate(); //deprecated
        Log.v("timer", "Timer service has started.");

        myDb = new DatabaseHelper(this); //Calls Construktor of DatabaseHelper

        currentDate = Calendar.getInstance().getTime();
        dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
    }

    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);

        return START_STICKY; //if the application is terminated android will restart and reexecute the service
    }

    //Method will be called by the Android System when the Service is started
    @Override
    protected void onHandleIntent(Intent intent){
        if(intent == null){//Once the service is restarted the intent parameter is empty since the service is reexecuted not through an intent
            //Let us try to execute a different code when the service is restarted
            int time;
            if (MODE == 0) time = 5;
            else{
                //write good timespan in here;
                time = 5;
            }

            for(int i = 0; i < time; i++) {
                Log.v("timer", "i (intent is null) = " + i);

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {

                }
            }


            //updateLevel();
            //checkNotified();

            return;
        }

        ResultReceiver receiver = intent.getParcelableExtra("receiver");
        int time = intent.getIntExtra("time", 0);

        for(int i = 0; i < time; i++){
            Log.v("timer", "i (intent is not null) = " + i);

            try{
                Thread.sleep(1000);
            }catch(Exception e){

            }
        }

        Bundle bundle = new Bundle();
        bundle.putString("message", "Counting done...");

        receiver.send(1234, bundle);
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

    public void checkNotified(){
        int priority = getSettingsWarningLevel(this);


        Cursor data = myDb.getAllData();
        if(data.getCount() == 0){
            //Toast.makeText(MainActivity.this,"The Database was empty  :(.", Toast.LENGTH_LONG).show();
        }
        else{
            while(data.moveToNext()){
                String Id = String.valueOf(data.getInt(0));
                String Barcode = data.getString(1);
                String Produktname = data.getString(2);
                String Mhd = data.getString(3);
                int level = data.getInt(4);
                String Notified = data.getString(5);
                int settings = getSettingsWarningLevel(this)+2;

                if(Notified.equals("0")){
                    if(level == 2) {
                        if(myDb.updateData(Id, Barcode, Produktname, Mhd, String.valueOf(level), "1")) {
                            try {
                                Date futureDate = dateFormat.parse(Mhd);
                                long diff = futureDate.getTime() - currentDate.getTime();
                                days = diff / (24 * 60 * 60 * 1000);

                                if(days==0 & diff>0) text = "in 1 Tagen";
                                else if(days==0 & diff<0) text = "heute";
                                else text = "in " +days+" Tagen";
                            }catch(Exception e){
                                e.printStackTrace();
                            }



                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

                            Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID[settings])
                                    .setSmallIcon(R.drawable.ic_one)
                                    .setContentTitle("Warnung")
                                    .setContentText(Produktname + " überschreitet MHD " + text)
                                    .setPriority(-2) //Max = 2 High = 1 Default = 0 Low = -1 Min=-2
                                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                    .build(); //look into setCategory
                            //if we send the same id it will overwrite the sent notification
                            notificationManager.notify(1, notification);


                        }
                    }else if(level == 1){
                        if(myDb.updateData(Id, Barcode, Produktname, Mhd, String.valueOf(level), "1")) {
                            try {
                                Date futureDate = dateFormat.parse(Mhd);
                                long diff = futureDate.getTime() - currentDate.getTime();
                                days = -diff / (24 * 60 * 60 * 1000);
                            }catch(Exception e){
                                e.printStackTrace();
                            }

                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

                            Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID[settings])
                                    .setSmallIcon(R.drawable.ic_one)
                                    .setContentTitle("Warnung")
                                    .setContentText(Produktname + " hat MHD seit: " + days + " Tagen überschritten")
                                    .setPriority(-2)
                                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                    .build(); //look into setCategory
                            //if we send the same id it will overwrite the sent notification
                            notificationManager.notify(1, notification);
                        }
                    }else if(level == 0){
                        if(myDb.updateData(Id, Barcode, Produktname, Mhd, String.valueOf(level), "1")){
                            try {
                                Date futureDate = dateFormat.parse(Mhd);
                                long diff = futureDate.getTime() - currentDate.getTime();
                                days = -diff / (24 * 60 * 60 * 1000);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                            //test begin
                            int savedValue = getSettingsLevel2(this);
                            //test end



                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

                            Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID[settings])
                                    .setSmallIcon(R.drawable.ic_one)
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
}*/
