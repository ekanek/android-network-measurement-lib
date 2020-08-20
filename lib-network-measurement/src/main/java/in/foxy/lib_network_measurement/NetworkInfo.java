package in.foxy.lib_network_measurement;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;;

import androidx.annotation.NonNull;

public class NetworkInfo
{
    public static String getSimCardName(Context context){
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getSimOperatorName();
    }



    public static String getActiveNetworkName(Context context){
        String network_name = "unknown";
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info == null || !info.isConnected())
            return "-"; // not connected
        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            network_name = "wifi";
        }
        if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int networkType = info.getSubtype();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN: // api< 8: replace by 11
                case TelephonyManager.NETWORK_TYPE_GSM: // api<25: replace by 16
                    network_name = "2g";
                    break;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // api< 9: replace by 12
                case TelephonyManager.NETWORK_TYPE_EHRPD: // api<11: replace by 14
                case TelephonyManager.NETWORK_TYPE_HSPAP: // api<13: replace by 15
                case TelephonyManager.NETWORK_TYPE_TD_SCDMA: // api<25: replace by 17
                    network_name = "3g";
                    break;
                case TelephonyManager.NETWORK_TYPE_LTE: // api<11: replace by 13
                case TelephonyManager.NETWORK_TYPE_IWLAN: // api<25: replace by 18
                case 19: // LTE_CA
                    network_name = "4g";
                    break;
                default:
                    network_name = "unknown";
                    break;

            }
        }
        return network_name;
    }


    private static int getWifiSignalStrength(Context context)
    {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
        return level;
    }

    private static void getGSMSignalStrength(Context context, final INetworkMonitor iNetworkMonitor){
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new PhoneStateListener(){
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                iNetworkMonitor.onSignalStrengthChange(signalStrength.getLevel());
            }
        },PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    public static void registerNetworkMonitorCallback(final Context context, final INetworkMonitor iNetworkMonitor){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();
        connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback(){
            @Override
            public void onAvailable(@NonNull android.net.Network network) {
                super.onAvailable(network);
                iNetworkMonitor.isNetworkAvailable(true);
            }

            @Override
            public void onLinkPropertiesChanged(@NonNull android.net.Network network, @NonNull LinkProperties linkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties);
                iNetworkMonitor.ipAddress(""+linkProperties.getLinkAddresses().get(0));
            }

            @Override
            public void onCapabilitiesChanged(@NonNull android.net.Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                String getActiveNetworkName=getActiveNetworkName(context);
                if(getActiveNetworkName.equals("wifi")){
                    iNetworkMonitor.onSignalStrengthChange(getWifiSignalStrength(context));
                }else  {
                    getGSMSignalStrength(context,iNetworkMonitor);
                }
                iNetworkMonitor.downloadSpeed(""+networkCapabilities.getLinkDownstreamBandwidthKbps());
                iNetworkMonitor.onUploadSpeed(""+networkCapabilities.getLinkUpstreamBandwidthKbps());
            }

            @Override
            public void onBlockedStatusChanged(@NonNull android.net.Network network, boolean blocked) {
                super.onBlockedStatusChanged(network, blocked);
            }

            @Override
            public void onLosing(@NonNull android.net.Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);
            }

            @Override
            public void onLost(@NonNull android.net.Network network) {
                super.onLost(network);
                iNetworkMonitor.isNetworkAvailable(false);
            }


        });

    }

}

