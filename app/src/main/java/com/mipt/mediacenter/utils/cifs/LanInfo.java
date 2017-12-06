package com.mipt.mediacenter.utils.cifs;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.R.integer;
import android.util.Log;

/**
 * local ip, LAN net segment, LAN NetBios Name list.
 *
 * @author slieer
 *
 */
public class LanInfo {
    public static final String TAG = "LanInfo";

    /**
     * smb://slieer:slieer@192.168.1.100/Users/
     * smb://192.168.51.230/SharedDocs/
     * @param smbFilePath
     * @return [remoteIp, remoteShareDir, user, password]
     */
    public static String[] getNodeInfo(String smbFilePath){
        String[] strArr = smbFilePath.split("@");
        String remotePath = null;
        String user = null;
        String password = null;
        if(strArr.length > 1){
            remotePath = strArr[1];
            String[] userInfo = strArr[0].replace("smb://", "").split(":");
            user = userInfo[0];
            password = userInfo[1];
        }else{
            remotePath = smbFilePath.replace("smb://", "");
        }
        if(remotePath.endsWith("/")){
            remotePath = remotePath.substring(0, remotePath.length() - 1);
        }

        int index = remotePath.indexOf("/");
        return new String[]{remotePath.substring(0, index), remotePath.substring(index + 1), user, password};
    }

    /**
     * string[ip, netSegment]
     * @return
     */
    public static String[] getIPInfo(){
        String ip = null;
        try {
            ip = getLocalIp1();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        Log.i(TAG, "local ip:" + ip);
        String[] re = null;
        if(ip != null){
            int len = ip.lastIndexOf(".");
            String segment = ip.substring(0, len);
            re = new String[]{ip, segment};
        }

        return re;
    }


    /*    private static String getLocalIp() throws UnknownHostException,
                SocketException {
            return Inet4Address.getLocalHost().toString();
        }
    */
    private static String getLocalIp1() throws SocketException {
        Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface
                .getNetworkInterfaces();
        InetAddress ip = null;
        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
            String netInterfaceName = netInterface.getName();
            if(netInterfaceName != null && netInterfaceName.equals("lo"))continue;
            //Log.i(TAG, "netInterface.getName:" + netInterface.getName());
            Enumeration<InetAddress> addresses = netInterface
                    .getInetAddresses();
            while(addresses.hasMoreElements()) {
                ip = (InetAddress) addresses.nextElement();
                if (ip != null && ip instanceof Inet4Address) {
                    //Log.i(TAG, "本机的IP = " + ip.getHostAddress());
                    return ip.getHostAddress();
                }
            }
        }
        return null;
    }
}