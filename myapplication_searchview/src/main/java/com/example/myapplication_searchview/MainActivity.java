package com.example.myapplication_searchview;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication_searchview.AnimalAdapter;
import com.example.myapplication_searchview.Animals;
import com.example.myapplication_searchview.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView mListView1;
    //创建ListView对象，将这个对象与xml文件中的ListView控件通过id的方式绑定绑定
    private List<RecordFileInfo> datas = new ArrayList<RecordFileInfo>();
    //创建一个List数组，用来存放数据
    private RecordFileInfoAdapter animalAdapter;
    //用来设置一个适配器的实现类对象
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        initDatas();//初始化数据
        mListView1 = (ListView) findViewById(R.id.lv1);//绑定控件
        animalAdapter = new RecordFileInfoAdapter(this, datas);//创建适配器的实现类对象，并且将本类的class对象和定义的数据作为参数传入
        mListView1.setAdapter(animalAdapter);//为ListView绑定一个适配器
        mListView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //为ListView创建一个监听事件，方便我们对它进行操作
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(MainActivity.this, "您单击了" + datas.get(i).getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initDatas() {
        //将Animals的实现类对象传到数据列表当中
        RecordFileInfo animal1 = new RecordFileInfo("喜羊羊","喜气洋洋过大年","长度1");
        RecordFileInfo animal2 = new RecordFileInfo("懒羊羊","我就是喜欢吃","长度2");
        RecordFileInfo animal3 = new RecordFileInfo("灰太狼","我一定会回来的","长度3");
        RecordFileInfo animal4 = new RecordFileInfo("小灰灰","喜羊羊哥哥，带我一起玩","长度4");
        for (int i = 0; i < 10; i++) {
            datas.add(animal1);
            datas.add(animal2);
            datas.add(animal3);
            datas.add(animal4);
        }
    }
}
