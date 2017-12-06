package com.example.tanliang.mytec;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//import org.apache.http.conn.util.*;
import jcifs.*;
import jcifs.netbios.NbtAddress;

import org.xbill.DNS.*;

/**
 * @ClassName：ScanDeviceTool
 * @Description：TODO<局域网扫描设备工具类>
 * @author：zihao
 * @date：2015年9月10日 下午3:36:40
 * @version：v1.0
 */
public class ScanNet {

    private static final String TAG = ScanNet.class.getSimpleName();

    /**
     * 核心池大小
     **/
    private static final int CORE_POOL_SIZE = 1;
    /**
     * 线程池最大线程数
     **/
    private static final int MAX_IMUM_POOL_SIZE = 255;

    private String mDevAddress;// 本机IP地址-完整
    private String mLocAddress;// 局域网IP地址头,如：192.168.1.
    private Runtime mRun = Runtime.getRuntime();// 获取当前运行环境，来执行ping，相当于windows的cmd
    private Process mProcess = null;// 进程
    private String mPing = "ping  -c 3 -a ";// 其中 -c 1为发送的次数，-w 表示发送后等待响应的时间
    public List<String> mIpList = new ArrayList<String>();// ping成功的IP地址
    public Map<String, String> hosts = new HashMap<String, String>();
    private ThreadPoolExecutor mExecutor;// 线程池对象

    public void scan1() {
        Runnable run = new Runnable() {

            @Override
            public void run() {
                String macAdress = "001B7782831E";
                String dataUrl = "http://192.168.0.1/" + macAdress;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(dataUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                    wr.flush();
                    wr.close();
                    InputStream is = connection.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                    StringBuffer response = new StringBuffer();
                    String line;
                    while ((line = rd.readLine()) != null) {
                        response.append(line);
                        response.append('\r');
                    }
                    rd.close();
                    String responseStr = response.toString();
                    Log.d("Server response", responseStr);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        };
        run.run();
        ;
    }

    /**
     * TODO<扫描局域网内ip，找到对应服务器>
     *
     * @return void
     */
    public void scan() {
        mDevAddress = getLocAddress();// 获取本机IP地址
        //mLocAddress = getLocAddrIndex(mDevAddress);// 获取本地ip前缀
        mLocAddress = "192.168.0.";
        Log.d(TAG, "开始扫描设备,本机Ip为：" + mDevAddress);

        if (TextUtils.isEmpty(mLocAddress)) {
            Log.e(TAG, "扫描失败，请检查wifi网络");
            return;
        }

        /**
         * 1.核心池大小 2.线程池最大线程数 3.表示线程没有任务执行时最多保持多久时间会终止
         * 4.参数keepAliveTime的时间单位，有7种取值,当前为毫秒
         * 5.一个阻塞队列，用来存储等待执行的任务，这个参数的选择也很重要，会对线程池的运行过程产生重大影响
         * ，一般来说，这里的阻塞队列有以下几种选择：
         */
        mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_IMUM_POOL_SIZE,
                2000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
                CORE_POOL_SIZE));

        // 新建线程池
        for (int i = 100; i < 255; i++) {// 创建256个线程分别去ping
            final int lastAddress = i;// 存放ip最后一位地址 1-255

            Runnable run = new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    String ping = ScanNet.this.mPing + mLocAddress
                            + lastAddress;
                    String currnetIp = mLocAddress + lastAddress;
                    final byte p = (byte) lastAddress;
                    if (mDevAddress.equals(currnetIp)) // 如果与本机IP地址相同,跳过
                        return;

                    try {
                        mProcess = mRun.exec(ping);

                        int result = mProcess.waitFor();
                        BufferedReader br = new BufferedReader(new InputStreamReader(mProcess
                                .getInputStream()));
                        String inline;
                        InetAddress adr = InetAddress.getByAddress(new byte[]{(byte) 192, (byte) 168, 0, p});

                        Log.d(TAG, "正在扫描的IP地址为：" + currnetIp + "返回值为：" + result);
                        if (result == 0) {
                            Log.d(TAG, "扫描成功,Ip地址为：" + currnetIp);
                            mIpList.add(currnetIp);
                            NbtAddress nbtAddress = NbtAddress.getByName(currnetIp);
                            String name = nbtAddress.nextCalledName();
                            if (name == "" || name == null) {
                                name = nbtAddress.firstCalledName();
                            }


                            mIpList.add(name);
                            Log.d(TAG, "----name----：");
                            Log.d(TAG, name);
                            //hosts.put(currnetIp,adr.getHostName());
//                            while ((inline = br.readLine()) != null) {
//                                if (inline.indexOf("[") > -1) {
//                                    int start = inline.indexOf("Ping ");
//                                    int end = inline.indexOf("[");
//                                    String hostname = inline.substring(start + "Ping ".length(), end - 1);
//                                    System.out.println(hostname);
//                                    hosts.put(currnetIp, hostname);
//                                    Log.v(TAG,"-------");
//                                    Log.v(TAG,"hostname");
//                                }
//                            }
//                            br.close();
                        } else {
                            // 扫描失败
                            Log.d(TAG, "扫描失败");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "扫描异常" + e.toString());
                    } finally {
                        if (mProcess != null)
                            mProcess.destroy();
                    }
                }
            };

            mExecutor.execute(run);
        }

        mExecutor.shutdown();

        while (true) {
            try {
                if (mExecutor.isTerminated()) {// 扫描结束,开始验证
                    Log.d(TAG, "扫描结束,总共成功扫描到" + mIpList.size() + "个设备.");
                    break;
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    public void scan4() {
        mDevAddress = getLocAddress();// 获取本机IP地址
        //mLocAddress = getLocAddrIndex(mDevAddress);// 获取本地ip前缀
        mLocAddress = "192.168.0.";
        Log.d(TAG, "开始扫描设备,本机Ip为：" + mDevAddress);

        if (TextUtils.isEmpty(mLocAddress)) {
            Log.e(TAG, "扫描失败，请检查wifi网络");
            return;
        }

        try {
            mProcess = mRun.exec("arp -a");

            int result = mProcess.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(mProcess
                    .getInputStream()));
            String inline;
            while ((inline = br.readLine()) != null) {
                Log.v(TAG, inline);
            }
        } catch (Exception e) {
            Log.v(TAG, e.getMessage());
        }
    }

    /**
     * TODO<销毁正在执行的线程池>
     *
     * @return void
     */
    public void destory() {
        if (mExecutor != null) {
            mExecutor.shutdownNow();
        }
    }

    /**
     * TODO<获取本地ip地址>
     *
     * @return String
     */
    private String getLocAddress() {
        String ipaddress = "";

        try {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface networks = en.nextElement();
                // 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> address = networks.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (address.hasMoreElements()) {
                    InetAddress ip = address.nextElement();
                    if (!ip.isLoopbackAddress()
                            ) {
                        ipaddress = ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            Log.e("", "获取本地ip地址失败");
            e.printStackTrace();
        }

        Log.i(TAG, "本机IP:" + ipaddress);
        return ipaddress;
    }

    /**
     * TODO<获取本机IP前缀>
     *
     * @param devAddress // 本机IP地址
     * @return String
     */
    private String getLocAddrIndex(String devAddress) {
        if (!devAddress.equals("")) {
            return devAddress.substring(0, devAddress.lastIndexOf(".") + 1);
        }
        return null;
    }

    public void scan3(Context context1) {
        Log.d(TAG, "Let's sniff the network");
        final Context context = context1;
        try {
            //Context context = mContextRef.get();

            if (context != null) {
                mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_IMUM_POOL_SIZE,
                        2000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
                        CORE_POOL_SIZE));

                // 新建线程池
                for (int i = 100; i < 255; i++) {// 创建256个线程分别去ping
                    final int lastAddress = i;// 存放ip最后一位地址 1-255

                    Runnable run = new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub

                            try {

                                if (context != null) {

                                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                                    WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

                                    WifiInfo connectionInfo = wm.getConnectionInfo();
                                    int ipAddress = connectionInfo.getIpAddress();
                                    String ipString = Formatter.formatIpAddress(ipAddress);


                                    Log.d(TAG, "activeNetwork: " + String.valueOf(activeNetwork));
                                    Log.d(TAG, "ipString: " + String.valueOf(ipString));

                                    String prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);
                                    Log.d(TAG, "prefix: " + prefix);

                                    for (int i = 0; i < 255; i++) {
                                        String testIp = prefix + String.valueOf(i);

                                        InetAddress address = InetAddress.getByName(testIp);
                                        boolean reachable = address.isReachable(1000);
                                        String hostName = address.getCanonicalHostName();

                                        if (reachable)
                                            Log.i(TAG, "Host: " + String.valueOf(hostName) + "(" + String.valueOf(testIp) + ") is reachable!");
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "扫描异常" + e.toString());
                            } finally {
                                if (mProcess != null)
                                    mProcess.destroy();
                            }
                        }
                    };

                    mExecutor.execute(run);
                }

                mExecutor.shutdown();

            }

        } catch (Exception t) {
            Log.e(TAG, "Well that's not good.", t);
        }
    }

    public void scan5() {
        Runnable run=new Runnable() {
            @Override
            public void run() {
                try {

                    String ipAddress = "192.168.0.105";
                    String dnsblDomain = "in-addr.arpa";
                    Record[] records;
                    Lookup lookup = new Lookup(ipAddress + "." + dnsblDomain, Type.PTR);
                    //Lookup lookup = new Lookup(ipAddress , Type.PTR);
                    SimpleResolver resolver = new SimpleResolver();
                    resolver.setAddress(InetAddress.getByName("192.168.0.1"));
                    lookup.setResolver(resolver);
                    records = lookup.run();

                    if (lookup.getResult() == Lookup.SUCCESSFUL) {
                        for (int i = 0; i < records.length; i++) {
                            if (records[i] instanceof PTRRecord) {
                                PTRRecord ptr = (PTRRecord) records[i];
                                //System.out.println("DNS Record: " + records[0].rdataToString());
                                Log.d(TAG,"DNS 记录: " + records[0].rdataToString());
                            }
                        }
                    } else {
                        System.out.println("Failed lookup");
                    }

                } catch (Exception e) {
                    System.out.println("异常: " + e);
                }
            }
        };

        run.run();

    }


}