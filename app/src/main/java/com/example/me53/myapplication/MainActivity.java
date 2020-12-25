/*
 * 20190825加入自动重新连接
 */
package com.example.me53.myapplication;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;






public class MainActivity extends AppCompatActivity {
    public String TAG = "MyTAG";
    //private String host = "tcp://内网ip:服务器端口号";
    private String host = "tcp://115.159.217.166:1883";    //
    private String userName = "admin";
    private String passWord = "password";
    private String clientId = "AndroidClient1";
    private String messagg_val;
    private TextView t_time;
    private String p;
    private String m;
    private int i = 1;
    private MqttClient client;
    private MqttConnectOptions options;
    public Handler handler;
    private String myTopic = "test/key1";
    private String wendu_Topic = "sensor/temper";
    private String shidu_Topic = "sensor/humidity";
    private String shuiwei_Topic = "sensor/water";
    private String work_topic = "sensor/work";
    private String timeTopic = "sensor/time";
    private ScheduledExecutorService scheduler;
    private Switch sw1;
    private Switch sw2;
    private Switch sw3;
    private TextView wendu;
    private TextView shidu;
    private TextView turang;
    private TextView text_nowtime;

    private LineChart lineChart; //折线图控件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();                                          //MQTT初始化
        setContentView(R.layout.activity_main);
        //隐藏系统默认标题
        ActionBar actionBar = getSupportActionBar();
        //初始化控件
        lineChart = findViewById(R.id.lc);
//        initLineChart();   //初始化图表函数



        handler = new Handler() {
            @RequiresApi(api = Build.VERSION_CODES.N)

            int i=0;

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                    try {
                        client.subscribe(myTopic, 1);      //订阅test/key1这个主题
                        client.subscribe(timeTopic, 1);    //订阅主题“mytopic”
                        client.subscribe(wendu_Topic, 1);   //订阅主题“wendu_Topic”
                        client.subscribe(shidu_Topic, 1);  //订阅主题“shidu_Topic”
                        client.subscribe(shuiwei_Topic, 1);//订阅主题“shuiwei_Topic”
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (msg.what == 2) {
                    Toast.makeText(MainActivity.this, "连接失败，系统正在重连", Toast.LENGTH_SHORT).show();
                    System.out.println("连接失败，系统正在重连");
                }
                else if (msg.what == 3) {
                    Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "handleMessage");

                    messagg_val = (String) msg.obj;
                    // Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    if (messagg_val.indexOf("temper") != -1) {
                        //    wendu.setText(messagg_val);
                        String regEx = "[^0-9.]";  //正则表达式
                        Pattern p = Pattern.compile(regEx);
                        Matcher m = p.matcher(messagg_val);
                        wendu.setText(m.replaceAll("").trim());
                        //  AppCompatw AppCompatActivity();Activity mapdata=ne

                        float temp=0; //定义初始变量

                        //int i=0;

                        ys1[i] = convertToFloat(m.replaceAll("").trim(), temp);
                        i++;
                        if(i==7) i=0;

                        initLineChart();              //设置数据

                    } else if (messagg_val.indexOf("humidity") != -1) {
//                        shidu.setText(messagg_val);
                        String regEx = "[^0-9.]";  //正则表达式
                        Pattern p = Pattern.compile(regEx);
                        Matcher m = p.matcher(messagg_val);
                        shidu.setText(m.replaceAll("").trim());
                    } else if (messagg_val.indexOf("water") != -1) {
                        //                       turang.setText(messagg_val);
                        String regEx = "[^0-9.]";  //正则表达式
                        Pattern p = Pattern.compile(regEx);
                        Matcher m = p.matcher(messagg_val);
                        turang.setText(m.replaceAll("").trim());
                    }

                }

            }

        };

        //Button button1=(Button)findViewById(R.id.button1);
        Button button2 = (Button) findViewById(R.id.button2);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                publish("on");
            }
        });
        startReconnect();
        initlistener();  //设置监听
    }


    //!!!!!!!!!!连接服务器
    private void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.connect(options);
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = 2;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    //配置连接服务器函数
    private void startReconnect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!client.isConnected()) {
                    connect();
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }

    private void init() {
        try {
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(host, clientId,
                    new MemoryPersistence());
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            //！！！！！1注意这个地方，如果设置为  true 则每次都会显示上次保留的数据
            options.setCleanSession(false);   //！！！！！1注意这个地方
            //设置连接的用户名
            options.setUserName(userName);
            //设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            //设置回调
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    //连接丢失后，一般在如上的这个函数里面进行重连
                    System.out.println("connectionLost----------");

                }

                //接收订阅消息函数，如果消息到达后 就直接判断处理

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    String str1 = new String(mqttMessage.getPayload());
                    Log.d(TAG, "messageArrived: " + str1);
                    Message msg = new Message();
                    msg.what = 3;   //收到消息标志位
                    msg.obj = s + str1;
                    handler.sendMessage(msg);    // hander 回传

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    //publish后会执行到这里
                    System.out.println("deliveryComplete---------"
                            + iMqttDeliveryToken.isComplete());

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MqttCallback mqttCallback = new MqttCallback() {

        //接收订阅消息函数，如果消息到达后 就直接判断处理

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {//This method is called when a message arrives from the server

            String str1 = new String(message.getPayload());
            Log.d(TAG, "messageArrived: " + str1);
            Message msg = new Message();
            msg.what = 3;
            msg.obj = topic + str1;
            handler.sendMessage(msg);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            //Called when delivery for a message has been completed
        }

        @Override
        public void connectionLost(Throwable arg0) {
            // This method is called when the connection to the server is lost.
            Log.d(TAG, "connectionLost: ");
        }
    };

    public void publish(String msg) {
        String topic = myTopic;
        Integer qos = 0;
        Boolean retained = false;
        try {
            client.publish(topic, msg.getBytes(), qos.intValue(), retained.booleanValue());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void publishmessageplus(String message2) {
        if (client == null || !client.isConnected()) {
            return;
        }
        MqttMessage message = new MqttMessage();
        message.setPayload(message2.getBytes());
        try {
            client.publish(work_topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    // 测试发送消息
    private void publishMessage() {
        if (client == null || !client.isConnected()) {
            return;
        }
        MqttMessage message = new MqttMessage();
        String time = t_time.getText().toString();
        message.setPayload(time.getBytes());
        try {
            client.publish(timeTopic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            scheduler.shutdown();
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    //初始化监听
    private void initlistener() {
        wendu = (TextView) findViewById(R.id.wendu);
        shidu = (TextView) findViewById(R.id.shidu);
        // t_time = (TextView) findViewById(R.id.t_time);
        turang = (TextView) findViewById(R.id.turang);
        //  text_nowtime = findViewById(R.id.text_nowtime);
        //   Button send_time = findViewById(R.id.send_time);
        sw1 = findViewById(R.id.sw1);
        sw2 = findViewById(R.id.sw2);
        sw3 = findViewById(R.id.sw3);

        //按键发送数据
        sw1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    publishmessageplus("A");
                } else {
                    publishmessageplus("B");
                }

            }
        });
        sw2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    publishmessageplus("C");
                } else {
                    publishmessageplus("D");
                }

            }
        });
        sw3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    publishmessageplus("E");
                } else {
                    publishmessageplus("F");
                }

            }
        });
//        send_time.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String send_time_text = t_time.getText().toString();
//                if (send_time_text.contains("-"))
//                {
//                    publishMessage();
//                }else {
//                    Toast.makeText(MainActivity.this, "请选择定时时间", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//        t_time.setOnClickListener(new View.OnClickListener() {   //时间选择器
//            @Override
//            public void onClick(View v) {
//                Date date = new Date();
//                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                String format1 = format.format(date);
//                TimeSelector timeSelector = new TimeSelector(MainActivity.this, new TimeSelector.ResultHandler() {
//                    @Override
//                    public void handle(String time) {
//                        t_time.setText(time);
//                    }
//                }, format1, "2050-1-1 24:00:00");
//                timeSelector.show();
//            }
//        });
    }


    /**
     * 初始化图表数据
     */
    private void initLineChart() {
        lineChart.animateXY(2000, 2000); // 呈现动画
        Description description = new Description();
        description.setText(""); //自定义描述
        lineChart.setDescription(description);
        Legend legend = lineChart.getLegend();
        legend.setTextColor(Color.WHITE);
        setYAxis();             //设置Y轴数据
        setXAxis();             //设置x轴数据
        setData();              //设置数据
    }

    float[] ys1 = new float[]{0, 0, 0, 0, 0, 0, 0};


    private void setData() {

        // 模拟数据1
        List<Entry> yVals1 = new ArrayList<>();
        //float[] ys1 = new float[]{0, 90f, 80f, 90f, 80f, 80f,100f};


        // 模拟数据2
        List<Entry> yVals2 = new ArrayList<>();
        float[] ys2 = new float[]{0f, 0f, 0f, 0f, 0f, 0f, 0f};
        // 模拟数据3
        List<Entry> yVals3 = new ArrayList<>();
        float[] ys3 = new float[]{0f, 0f, 0f, 0f, 0f, 0f, 0f};

        for (int i = 0; i < ys1.length; i++) {
            yVals1.add(new Entry(i, ys1[i]));
            yVals2.add(new Entry(i, ys2[i]));
            yVals3.add(new Entry(i, ys3[i]));
        }
        // 2. 分别通过每一组Entry对象集合的数据创建折线数据集
        LineDataSet lineDataSet1 = new LineDataSet(yVals1, "最高温度");
        LineDataSet lineDataSet2 = new LineDataSet(yVals2, "平均温度");
        LineDataSet lineDataSet3 = new LineDataSet(yVals3, "最低温度");
        lineDataSet2.setCircleColor(Color.RED); //设置点圆的颜色
        lineDataSet3.setCircleColor(Color.GREEN);//设置点圆的颜色
        lineDataSet1.setCircleRadius(5); //设置点圆的半径
        lineDataSet2.setCircleRadius(5); //设置点圆的半径
        lineDataSet3.setCircleRadius(5); //设置点圆的半径
        lineDataSet1.setDrawCircleHole(false); // 不绘制圆洞，即为实心圆点
        lineDataSet2.setDrawCircleHole(false); // 不绘制圆洞，即为实心圆点
        lineDataSet3.setDrawCircleHole(false); // 不绘制圆洞，即为实心圆
        lineDataSet2.setColor(Color.RED); // 设置为红色
        lineDataSet3.setColor(Color.GREEN); // 设置为黑色
        // 值的字体大小为12dp
        lineDataSet1.setValueTextSize(12f);
        lineDataSet2.setValueTextSize(12f);
        lineDataSet3.setValueTextSize(12f);
        //将每一组折线数据集添加到折线数据中
        LineData lineData = new LineData(lineDataSet1, lineDataSet2, lineDataSet3);
        //设置颜色
        lineData.setValueTextColor(Color.WHITE);
        //将折线数据设置给图表
        lineChart.setData(lineData);

    }

    /*
     * 设置Y轴数据
     */
    private void setYAxis() {
        // X轴
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawAxisLine(false); // 不绘制X轴
        xAxis.setDrawGridLines(false); // 不绘制网格线
        // 模拟X轴标签数据
        final String[] weekStrs = new String[]{"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        xAxis.setLabelCount(weekStrs.length); // 设置标签数量
        xAxis.setTextColor(Color.GREEN); // 文本颜色
        xAxis.setTextSize(12f); // 文本大小为18dp
        xAxis.setGranularity(1f); // 设置间隔尺寸
        // 使图表左右留出点空位
        xAxis.setAxisMinimum(-0.1f); // 设置X轴最小值
        //设置颜色
        xAxis.setTextColor(Color.WHITE);
        // 设置标签的显示格式
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return weekStrs[(int) value];
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // 在底部显示

    }

    /*
     * 设置X轴数据
     */
    private void setXAxis() {
        // X轴
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawAxisLine(false); // 不绘制X轴
        xAxis.setDrawGridLines(false); // 不绘制网格线
        // 模拟X轴标签数据
        final String[] weekStrs = new String[]{"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        xAxis.setLabelCount(weekStrs.length); // 设置标签数量
        xAxis.setTextColor(Color.GREEN); // 文本颜色
        xAxis.setTextSize(12f); // 文本大小为18dp
        xAxis.setGranularity(1f); // 设置间隔尺寸
        // 使图表左右留出点空位
        xAxis.setAxisMinimum(-0.1f); // 设置X轴最小值
        //设置颜色
        xAxis.setTextColor(Color.WHITE);
        // 设置标签的显示格式
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return weekStrs[(int) value];
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // 在底部显示
    }


    //把String转化为float
    public static float convertToFloat(String number, float defaultValue) {
        if (TextUtils.isEmpty(number)) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(number);
        } catch (Exception e) {
            return defaultValue;
        }


    }
}