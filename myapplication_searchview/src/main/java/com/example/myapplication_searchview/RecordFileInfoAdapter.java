package com.example.myapplication_searchview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class RecordFileInfoAdapter extends BaseAdapter {
    private Context context;//创建一个上下文对象
    private List<RecordFileInfo> datas;//创建一个List数组，里面存放Animal对象，用来接收MainActivity传过来的数据

    public RecordFileInfoAdapter(Context context, List<RecordFileInfo> datas) {
        this.context = context;
        this.datas = datas;
    }

    @Override
    public int getCount() {
        //获取数据的长度
        return datas.size();
    }

    @Override
    public Object getItem(int i) {
        //适配器放入了很多条数据，获取数据所在的位置
        return datas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        RecordFileInfo anm = (RecordFileInfo) getItem(i);//实例化给定位置上的对象
        View v;//创建视图v，这是是用来返回给ListView的，作为ListView的子视图
        ViewHold viewHold;//创建临时的储存器ViewHold，它的作用是把你getView方法中每次返回的View存起来，可以下次再用
        if (view == null){
            v = LayoutInflater.from(context).inflate(R.layout.adapter_view,null);
            //将adapter_view视图作为子视图放入v中
            viewHold = new ViewHold();

            //绑定id，建立与adapter_view视图的连接
            viewHold.animalImage = v.findViewById(R.id.tv3);
            viewHold.animalName = v.findViewById(R.id.tv1);
            viewHold.animalTell = v.findViewById(R.id.tv2);

            v.setTag(viewHold);//储器中的视图设置到v中
        }else{
            v = view;
            viewHold = (ViewHold)v.getTag();
        }
        //将制定位置上的数据显示到空间中
        viewHold.animalName.setText(anm.getName());
        viewHold.animalTell.setText(anm.getTime());
        viewHold.animalImage.setText(anm.getChangDu());
        //返回视图v在main中显示
        return v;
    }

    class ViewHold{
        //将数据(也就是Animals对象)进行实例化，方便与xml文件里面的控件对接
        TextView animalImage;
        TextView animalName;
        TextView animalTell;
    }
}
