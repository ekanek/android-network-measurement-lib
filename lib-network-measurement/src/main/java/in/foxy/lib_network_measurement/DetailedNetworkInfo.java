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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Pardeep Kumar
 */
public class DetailedNetworkInfo {
	
	enum NETWORK {
		NOT_CONNECTED, UNKNOWN, WIFI, TYPE_2G, TYPE_3G, TYPE_4G, TYPE_5G
	}
	
	private static TelephonyManager telephonyManager = null;
	private static ConnectivityManager connectivityManager = null;
	private static WifiManager wifiManager = null;
	private static NetworkRequest networkRequest;
	private static Map<String, Object> wifiStats = null;
	private static List<Map<String,Object>> listSimCardInfo = null;
	private static List<Map<String,Object>> simCardSignalInfo = null;
	
	public static void initialize (Context context) {
		telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		networkRequest = new NetworkRequest.Builder()
				                         .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
				                         .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
				                         .build();
		
		listSimCardInfo=new ArrayList<>();
		simCardSignalInfo=new ArrayList<>();
		wifiStats = new HashMap<String, Object>();
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
		for (int i = 0; i < subscriptionInfos.size(); i++) {
			Map<String,Object> simInfo = new HashMap<>();
			SubscriptionInfo subscriptionInfo = subscriptionInfos.get(i);
			simInfo.put("simNumber", subscriptionInfo.getNumber());
			simInfo.put("simCarrier", "" + subscriptionInfo.getCarrierName());
			simInfo.put("simRoamingEnabled", "" + subscriptionInfo.getDataRoaming());
			listSimCardInfo.add(simInfo);
		}
	}
	
	public static Map<String,Object> getWifiSignalInfo (Context context) {
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
	
	public static List<Map<String,Object>> getSimSignalInfo (Context context) {
		if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Log.e("DetailedNetworkInfo:getSimSignalInfo()","permission not granted : ACCESS_COARSE_LOCATION");
			return null;
		}
		List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
		if (cellInfos != null) {
			for (int i = 0; i < cellInfos.size(); i++) {
				if (cellInfos.get(i).isRegistered()) {
					Map<String,Object> simStat=new HashMap<>();
					if (cellInfos.get(i) instanceof CellInfoWcdma) {
						CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfos.get(i);
						CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
						simStat.put("type","wcdma");
						simStat.put("signalLevelSim",cellSignalStrengthWcdma.getLevel());
						simStat.put("asuLevelSim",cellSignalStrengthWcdma.getAsuLevel());
						simStat.put("dbmSim",cellSignalStrengthWcdma.getDbm());
						
					} else if (cellInfos.get(i) instanceof CellInfoGsm) {
						CellInfoGsm cellInfogsm = (CellInfoGsm) cellInfos.get(i);
						CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
						simStat.put("type","gsm");
						simStat.put("signalLevelSim",cellSignalStrengthGsm.getLevel());
						simStat.put("asuLevelSim",cellSignalStrengthGsm.getAsuLevel());
						simStat.put("dbmSim",cellSignalStrengthGsm.getDbm());
						
					} else if (cellInfos.get(i) instanceof CellInfoLte) {
						CellInfoLte cellInfoLte = (CellInfoLte) cellInfos.get(i);
						CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
						simStat.put("type","lte");
						simStat.put("signalLevelSim",cellSignalStrengthLte.getLevel());
						simStat.put("asuLevelSim",cellSignalStrengthLte.getAsuLevel());
						simStat.put("dbmSim",cellSignalStrengthLte.getDbm());
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
							simStat.put("rssiSim",cellSignalStrengthLte.getRssi());
							simStat.put("snrSim",cellSignalStrengthLte.getRssnr());
						}
						
					} else if (cellInfos.get(i) instanceof CellInfoCdma) {
						CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfos.get(i);
						CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
						simStat.put("type","cdma");
						simStat.put("signalLevelSim",cellSignalStrengthCdma.getLevel());
						simStat.put("asuLevelSim",cellSignalStrengthCdma.getAsuLevel());
						simStat.put("dbmSim",cellSignalStrengthCdma.getDbm());
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
							simStat.put("rssiSim",cellSignalStrengthCdma.getEvdoDbm());
							simStat.put("snrSim",cellSignalStrengthCdma.getEvdoSnr());
						}
					}
					simCardSignalInfo.add(simStat);
				}
			}
			
		}
		return simCardSignalInfo;
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
	
	public static List<Map<String,Object>> getSimCardInfo(){
		return listSimCardInfo;
	}
	
    public static void registerNetworkMonitorCallback(final INetworkMonitor iNetworkMonitor){
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
		        iNetworkMonitor.onNetworkSpeedChanges(networkCapabilities.getLinkUpstreamBandwidthKbps(),
				        networkCapabilities.getLinkDownstreamBandwidthKbps());
	        }
        });
    }
}

