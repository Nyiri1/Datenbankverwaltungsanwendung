package oldfiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class MyRebootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent){
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            //Toast.makeText(context, "Boot completed", Toast.LENGTH_LONG).show();

            Intent serviceIntent = new Intent(context, NotificationService.class);
            serviceIntent.putExtra("caller", "RebootReceiver");
            context.startService(serviceIntent);
        }

        if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
            //Toast.makeText(context, "Connectivity changed", Toast.LENGTH_SHORT).show();
        }
    }
}
