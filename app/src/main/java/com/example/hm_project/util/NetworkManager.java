package com.example.hm_project.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;

/***
 * 현재 모바일 기기에서 Network 통신이 되는지 확인한다.
 * 와이파이와 모바일 데이터 중 하나만 연결 되어 있어도 true를 반환한다.
 * 와이파이 모바일 데이터 둘다 연결이 안되어 있다면 false를 반환한다.
 * return : true/false
 * WIFI가 우선순위로 연결됨.
 */
public class NetworkManager {
    private static final String DEBUG_TAG = "NetworkStatus";

    public static boolean networkCheck(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiConn = false;
        boolean isMobileConn = false;
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if(networkInfo !=null){
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    isWifiConn |= networkInfo.isConnected(); //현재 WIFI로 연결되어 있나
                }
                if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    // isMobileConn |= networkInfo != null && networkInfo.isConnectedOrConnecting();
                    isMobileConn |= networkInfo.isConnected(); //현재 모바일 데이터로 연결되어 있나
                }
            }else{
                Log.d(DEBUG_TAG, "NO Network connected: ");
            }

        Log.d(DEBUG_TAG, "Wifi connected: " + isWifiConn);
        Log.d(DEBUG_TAG, "Mobile connected: " + isMobileConn);

        //모바일 네트워크로 서버 접속이 지금은 불가능 하기 때문에 Wifi 로만 return 하기로 한다.
        return isWifiConn;
       // return isWifiConn || isMobileConn;

    }
}
