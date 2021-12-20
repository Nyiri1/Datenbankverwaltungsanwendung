package org.o7planning.sqlietereal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

//Database Helper Class
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "storagev2.db"; // The custom name of the Database
    public static final String TABLE_NAME = "storageTable";
    public static final String COL_0 = "ID";
    public static final String COL_1 = "BARCODE";
    public static final String COL_2 = "PRODUKTNAME";
    public static final String COL_3 = "MHD";
    public static final String COL_4 = "LEVEL";
    public static final String COL_5 = "NOTIFIED";

    private static final int LEVEL_0_THRESHHOLD = -3;//Abgelaufen seit mehr als 3 Tagen
    private static final int LEVEL_1_THRESHHOLD = -3;//Abgelaufen seit weniger als 3 Tagen
    private int LEVEL_2_THRESHHOLD = 4;//Kurz vor dem Ablaufen 1-7 Tage (Default: 4)
    private static final int LEVEL_3_THRESHHOLD = 10;//Läuft innerhalb von 10 Tagen ab
    private static final int LEVEL_4_THRESHHOLD = 10;//Hat noch mehr als 10 Tage Haltbarkeit
    private static final int LEVEL_9_THRESHHOLD = 0;//Benachrichtigungen manuell deaktiviert

    SimpleDateFormat dateFormat;
    Date currentDate;
    Date futureDate;
    

    /*Method creates the database and the table*/
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault()); //New
        currentDate = Calendar.getInstance().getTime();
        // SQLiteDatabase db = this.getWritableDatabase(); //Is going to create your database and table? //just for checking
    }

    @Override
    //Creates a Database whenever the onCreate Method is called
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table storageTable (ID INTEGER PRIMARY KEY AUTOINCREMENT, BARCODE TEXT, PRODUKTNAME TEXT,MHD TEXT,LEVEL INTEGER, NOTIFIED INTEGER)"); //It executes whatever query you pass
        Log.d("9938", "IS THIS METHOD CALLED?");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS studentTable");
    }

    public boolean insertData(String barcode, String produktname, String mhd, String level){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, barcode);
        contentValues.put(COL_2, produktname);
        contentValues.put(COL_3, mhd);
        contentValues.put(COL_4, level);
        contentValues.put(COL_5, 0); //On insertion notified is set to NO
        long result = db.insert(TABLE_NAME, null, contentValues);

        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null); //querying instance and save result in res of type cursor
        return res;
    }

    // Zum manuellen Anpassen vom Produkt
    public boolean updateData(String id, String barcode, String produktname, String mhd, String level, String notified){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_0, id);
        contentValues.put(COL_1, barcode);
        contentValues.put(COL_2, produktname);
        contentValues.put(COL_3, mhd);
        contentValues.put(COL_4, level);
        contentValues.put(COL_5, notified);
        db.update(TABLE_NAME, contentValues, "ID = ?", new String[] { id }); //the question mark is going to be replaced by the string we pass
        return true;
    }

    public Integer deleteData(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?", new String[] {id});
    }

    public void updateLevel(Context context){
        Log.d("9999", "In DatabaseHelper.updateLevel(Context context)");
        Cursor data = getAllData();
        if(data.getCount() == 0){ }
        else{
            while(data.moveToNext()){
                String Id = String.valueOf(data.getInt(0));
                String Barcode = data.getString(1);
                String Produktname = data.getString(2);
                String Mhd = data.getString(3);
                int level = data.getInt(4);
                String Notified = data.getString(5);
                int newLevel = determineLevel(Mhd, level, context);
                Log.d("9938", "Id: " + Id);
                Log.d("9938", "Produktname: " + Produktname);
                Log.d("9938", "Barcode: " + Barcode);
                Log.d("9938", "Mhd: " + Mhd);
                Log.d("9938", "Old Level " + level);
                Log.d("9938", "New Level " + newLevel);
                if(newLevel!=level){
                    // Wenn sich das Produkt im Threshold geändert hat
                    // Wird das Level entsprechend angepasst und die Notified Flag resettet
                    boolean isUpdate = updateData(Id, Barcode, Produktname, Mhd, String.valueOf(newLevel), "0");
                }else{
                    // Ansonsten wird das alte Level übernommen
                    // Dieser Schritt ist ggf. nicht notwendig
                    boolean isUpdate = updateData(Id, Barcode, Produktname, Mhd, String.valueOf(level), Notified);
                }
            }
        }
    }

    private int determineLevel(String Mhd, int level, Context context){
        try {
            LEVEL_2_THRESHHOLD = SettingsActivity.getSettingsLevel2(context);
            //dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault()); //New
            //currentDate = Calendar.getInstance().getTime();
            Log.d("9939", "In DatabaseHelper.determineLevel(...) new Level_2_threshhold is: "+LEVEL_2_THRESHHOLD);
            futureDate = dateFormat.parse(Mhd);
            Log.d("9938", "C1");
            long diff = futureDate.getTime() - currentDate.getTime();
            Log.d("9938", "C2");
            long days = diff / (24 * 60 * 60 * 1000);
            Log.d("9938", "C3");
            long hours = (diff / (60 * 60 * 1000)) % 24;
            Log.d("9938", "C4");
            long minutes = (diff / (60 * 1000)) % 60;

            Log.d("9938", "Mhd: " + Mhd + "Difference in days: " + days + " In hours: " +hours + " Minutes: " + minutes + "! Old level: " + level);
            Log.d("9938", "Days: " + days + " Threshhold: " + LEVEL_2_THRESHHOLD);
            if(level==9) return level; //Manuell ausgetragen
            else if(days>=LEVEL_4_THRESHHOLD) return 4; //10
            else if(days>=LEVEL_2_THRESHHOLD) return 3; //4
            else if(days>=0) return 2; //Noch nicht abgelaufen und variabel 1-7 Tage übrig
            else if(days>=LEVEL_1_THRESHHOLD) return 1; //-3
            else return 0; //Abgelaufen seit mehr als 3 Tagen

        }catch(Exception e){
            e.printStackTrace();
        }

        return level;
    }
}


