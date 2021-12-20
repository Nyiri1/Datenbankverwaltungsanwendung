package org.o7planning.sqlietereal;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

//Zunächst existierte die Produktübersicht als ListView
//Nun wurde geändert auf RecyclerView
//Der ListView wird nicht mehr verwendet

public class CustomListview extends ArrayAdapter<String> {

    private String[] Produktname;
    private String[] Mhd;
    private Integer[] imgid;
    private Integer[] level;
    private Integer[] colors;
    private Activity context;

    String colorStr;


    public CustomListview(Activity context, String[] Produktname, String[] Mhd, Integer[] imgid, Integer[] level, Integer[] colors) {

        super(context, R.layout.listview_layout, Produktname);

        this.context = context;
        this.Produktname = Produktname;
        this.Mhd = Mhd;
        this.imgid=imgid;
        this.level=level;
        this.colors=colors;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View r=convertView;
        ViewHolder viewHolder=null;
        if(r==null){
            LayoutInflater layoutInflater = context.getLayoutInflater();
            r = layoutInflater.inflate(R.layout.listview_layout,null,true);
            viewHolder = new ViewHolder(r);
            r.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) r.getTag();
        }
        viewHolder.ivw.setImageResource(imgid[position]);
        viewHolder.tvw1.setText(Produktname[position]);
        viewHolder.tvw2.setText("MHD: " + Mhd[position]);

        viewHolder.background.setBackgroundColor(colors[level[position]]);

        return r;
    }

    class ViewHolder{
        TextView tvw1;
        TextView tvw2;
        ImageView ivw;
        RelativeLayout background;

        ViewHolder(View v){
            tvw1 = (TextView) v.findViewById(R.id.tvfruitname);
            tvw2 = (TextView) v.findViewById(R.id.tvdescription);
            ivw = (ImageView) v.findViewById(R.id.imageView);
            background = (RelativeLayout) v.findViewById(R.id.lv_column_background);

        }
    }
}
