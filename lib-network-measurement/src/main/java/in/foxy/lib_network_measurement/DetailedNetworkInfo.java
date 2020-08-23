package in.foxy.lib_network_measurement;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import androidx.annotation.NonNull;
import java.util.HashMap;
import java.util.List;

enum NETWORK {
	NOT_CONNECTED, UNKNOWN, WIFI, TYPE_2G, TYPE_3G, TYPE_4G, TYPE_5G
}


/**
 * @author Pardeep Kumar
 */
public class DetailedNetworkInfo {
	private static TelephonyManager telephonyManager = null;
	private static ConnectivityManager connectivityManager = null;
	private static WifiManager wifiManager = null;
	private static NetworkRequest networkRequest;
	private static HashMap<String, String> simCardsInfo = null;
	private static HashMap<String, Object> wifiStats = null;
	private static HashMap<String,Object> simStats = null;
	
	public static void initialize (Context context) {
		telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		networkRequest = new NetworkRequest.Builder()
				                         .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
				                         .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
				                         .build();
		
		simCardsInfo = new HashMap<String, String>();
		wifiStats = new HashMap<String, Object>();
		simStats = new HashMap<String,Object>();
		extractAvailableSimCardsInfo(context);
	}
	
	/**
	 * Extracts noOfSimsAvailable,SimXNumber, simXCarrier, simXRoamingEnabled
	 * @param context
	 */
	
	private static void extractAvailableSimCardsInfo (Context context) {
		if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
			Log.e("DetailedNetworkInfo:extractAvailableSimCardsInfo()","permission not granted : READ_PHONE_STATE");
			return;
		}
		List<SubscriptionInfo> subscriptionInfos = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
		simCardsInfo.put("noOfSimsAvailable", "" + subscriptionInfos.size());
		for (int i = 0; i < subscriptionInfos.size(); i++) {
			SubscriptionInfo subscriptionInfo = subscriptionInfos.get(i);
			simCardsInfo.put("sim" + i + "Number", subscriptionInfo.getNumber());
			simCardsInfo.put("sim" + i + "Carrier", "" + subscriptionInfo.getCarrierName());
			simCardsInfo.put("sim" + i + "RoamingEnabled", "" + subscriptionInfo.getDataRoaming());
		}
	}
	
	private static HashMap<String,Object> getWifiSignalInfo (Context context) {
		int numberOfLevels = 5;
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		wifiStats.put("frequency", wifiInfo.getFrequency());
		wifiStats.put("ipAddress", wifiInfo.getIpAddress());
		wifiStats.put("linkSpeed", wifiInfo.getLinkSpeed());
		wifiStats.put("signalStrength", wifiInfo.getRssi());
		wifiStats.put("signalLevel", WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			wifiStats.put("rxLinkSpeed", wifiInfo.getRxLinkSpeedMbps());
			wifiStats.put("wifiNetworkName", wifiInfo.getPasspointProviderFriendlyName());
		}
		return wifiStats;
	}
	
	private static HashMap<String,Object> getSimSignalInfo (Context context) {
		if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Log.e("DetailedNetworkInfo:getSimSignalInfo()","permission not granted : ACCESS_COARSE_LOCATION");
			return null;
		}
		
		
		List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
		if (cellInfos != null) {
			simStats.put("simCount",cellInfos.size());
			for (int i = 0; i < cellInfos.size(); i++) {
				if (cellInfos.get(i).isRegistered()) {
					if (cellInfos.get(i) instanceof CellInfoWcdma) {
						CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfos.get(i);
						CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
						simStats.put("signalLevelSim"+i,cellSignalStrengthWcdma.getLevel());
						simStats.put("asuLevelSim"+i,cellSignalStrengthWcdma.getAsuLevel());
						simStats.put("dbmSim"+i,cellSignalStrengthWcdma.getDbm());
						
					} else if (cellInfos.get(i) instanceof CellInfoGsm) {
						CellInfoGsm cellInfogsm = (CellInfoGsm) cellInfos.get(i);
						CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
						simStats.put("signalLevelSim"+i,cellSignalStrengthGsm.getLevel());
						simStats.put("asuLevelSim"+i,cellSignalStrengthGsm.getAsuLevel());
						simStats.put("dbmSim"+i,cellSignalStrengthGsm.getDbm());
						
					} else if (cellInfos.get(i) instanceof CellInfoLte) {
						CellInfoLte cellInfoLte = (CellInfoLte) cellInfos.get(i);
						CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
						simStats.put("signalLevelSim"+i,cellSignalStrengthLte.getLevel());
						simStats.put("asuLevelSim"+i,cellSignalStrengthLte.getAsuLevel());
						simStats.put("dbmSim"+i,cellSignalStrengthLte.getDbm());
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
							simStats.put("rssiSim"+i,cellSignalStrengthLte.getRssi());
							simStats.put("snrSim"+i,cellSignalStrengthLte.getRssnr());
						}
						
					} else if (cellInfos.get(i) instanceof CellInfoCdma) {
						CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfos.get(i);
						CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
						simStats.put("signalLevelSim"+i,cellSignalStrengthCdma.getLevel());
						simStats.put("asuLevelSim"+i,cellSignalStrengthCdma.getAsuLevel());
						simStats.put("dbmSim"+i,cellSignalStrengthCdma.getDbm());
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
							simStats.put("rssiSim"+i,cellSignalStrengthCdma.getEvdoDbm());
							simStats.put("snrSim"+i,cellSignalStrengthCdma.getEvdoSnr());
						}
					}
				}
			}
			
		}
		return simStats;
	}
	
	
	/**
	 * Extracts current active network type
	 * @param context
	 * @return  NOT_CONNECTED,UNKNOWN,WIFI,TYPE_2G,TYPE_3G,TYPE_4G,TYPE_5G
	 */
	public static String getActiveNetworkName (Context context) {
		NETWORK network_name = NETWORK.UNKNOWN;
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info == null || ! info.isConnected())
			network_name = NETWORK.NOT_CONNECTED;
		; // not connected
		if (info.getType() == ConnectivityManager.TYPE_WIFI) {
			network_name = NETWORK.WIFI;
		}
		if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
			int networkType = info.getSubtype();
			switch (networkType) {
				case TelephonyManager.NETWORK_TYPE_GPRS:
				case TelephonyManager.NETWORK_TYPE_EDGE:
				case TelephonyManager.NETWORK_TYPE_CDMA:
				case TelephonyManager.NETWORK_TYPE_1xRTT:
				case 11: // TelephonyManager.NETWORK_TYPE_IDEN: not available api< 8: using 11 as global
				case 16:  // api < 25 class variable not present in TelephonyManager as NETWORK_TYPE_GSM
					network_name = NETWORK.TYPE_2G;
					break;
				case TelephonyManager.NETWORK_TYPE_UMTS:
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
				case TelephonyManager.NETWORK_TYPE_HSDPA:
				case TelephonyManager.NETWORK_TYPE_HSUPA:
				case TelephonyManager.NETWORK_TYPE_HSPA:
				case 12: // TelephonyManager.NETWORK_TYPE_EVDO_B not able in api< 9: replacing by 12 for global
				case 14: // TelephonyManager.NETWORK_TYPE_EHRPD not available in  api<11: replacing with 14 for global
				case 15: // TelephonyManager.NETWORK_TYPE_HSPAP: not available in api<13: replacing with for 15
				case 17: // TelephonyManager.NETWORK_TYPE_TD_SCDMA: not available in api<25: replacing by 17
					network_name = NETWORK.TYPE_3G;
					break;
				case TelephonyManager.NETWORK_TYPE_LTE: // api<11: replace by 13
				case 18: // TelephonyManager.NETWORK_TYPE_IWLAN: not available in api<25: replace by 18
				case 19: // LTE_CA
					network_name = NETWORK.TYPE_4G;
					break;
				case 20:
					network_name = NETWORK.TYPE_5G;
					break;
				default:
					network_name = NETWORK.UNKNOWN;
					break;
			}
		}
		return String.valueOf(network_name);
	}
	
	
	public static HashMap<String,String> getSimCardInfo(){
		return simCardsInfo;
	}
	
    public static void registerNetworkMonitorCallback(final Context context, final INetworkMonitor iNetworkMonitor){
        connectivityManager.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback(){
            @Override
            public void onAvailable(@NonNull android.net.Network network) {
                super.onAvailable(network);
                iNetworkMonitor.onNetworkAvailabilityChange(true);
            }
	
	        @Override
	        public void onLost(@NonNull android.net.Network network) {
		        super.onLost(network);
                iNetworkMonitor.onNetworkAvailabilityChange(false);
	        }
	
	        @Override
	        public void onUnavailable () {
		        super.onUnavailable();
		        iNetworkMonitor.onNetworkAvailabilityChange(false);
	        }
	
	        @Override
	        public void onCapabilitiesChanged (@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
		        super.onCapabilitiesChanged(network, networkCapabilities);
		        String getActiveNetwork = getActiveNetworkName(context);
		        if(getActiveNetwork.equalsIgnoreCase("wifi")){
			        iNetworkMonitor.onConnectedNetworkInfoAvailable(getActiveNetwork,getWifiSignalInfo(context));
		        }else
		        {
			        iNetworkMonitor.onConnectedNetworkInfoAvailable(getActiveNetwork,getSimSignalInfo(context));
		        }
		        
		        iNetworkMonitor.onNetworkSpeedChanges(networkCapabilities.getLinkUpstreamBandwidthKbps(),
				        networkCapabilities.getLinkDownstreamBandwidthKbps());
	        }
        });
    }
}

