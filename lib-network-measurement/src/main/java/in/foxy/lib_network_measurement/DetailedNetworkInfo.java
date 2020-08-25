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
		NOT_CONNECTED, UNKNOWN, WIFI, TYPE_2G, TYPE_3G, TYPE_4G, TYPE_5G;
		
		@NonNull
		@Override
		public String toString () {
			return name().toLowerCase();
		}
	}
	private static TelephonyManager telephonyManager = null;
	private static ConnectivityManager connectivityManager = null;
	private static WifiManager wifiManager = null;
	private static NetworkRequest networkRequest;
	private static Map<String, Object> connectedWifi = null;
	private static List<Map<String,Object>> availableSimCards = null;
	private static List<Map<String,Object>> simCardNetworkCapabilities = null;
	
	public static void initialize (Context context) {
		telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		networkRequest = new NetworkRequest.Builder()
				                         .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
				                         .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
				                         .build();
		availableSimCards=new ArrayList<>();
		simCardNetworkCapabilities=new ArrayList<>();
		connectedWifi = new HashMap<String, Object>();
		extractAvailableSimCardsInfo(context);
	}
	
	/**
	 * Extracts [number,carrier, roamingEnabled]
	 * @param context
	 */
	private static void extractAvailableSimCardsInfo (Context context) {
		if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
			Log.e("DetailedNetworkInfo:extractAvailableSimCardsInfo()","permission not granted : READ_PHONE_STATE");
			return;
		}
		List<SubscriptionInfo> subscriptionInfos = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
		for (int i = 0; i < subscriptionInfos.size(); i++) {
			Map<String,Object> simCard = new HashMap<>();
			SubscriptionInfo subscriptionInfo = subscriptionInfos.get(i);
			simCard.put("number", subscriptionInfo.getNumber());
			simCard.put("carrier", "" + subscriptionInfo.getCarrierName());
			simCard.put("roamingEnabled", "" + subscriptionInfo.getDataRoaming());
			availableSimCards.add(simCard);
		}
	}
	
	/**
	 * provide information of connected wifi, i.e frequency,ipAddress,linkSpeed,signalStrength,signalLevel,rxLinkSpeed,wifiNetworkName
	 * @param context
	 * @return Map<String,Object> connectedWifi
	 */
	public static Map<String,Object> getConnectedWifiInfo (Context context) {
		int numberOfLevels = 5;
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		connectedWifi.put("frequency", wifiInfo.getFrequency());
		connectedWifi.put("ipAddress", wifiInfo.getIpAddress());
		connectedWifi.put("linkSpeed", wifiInfo.getLinkSpeed());
		connectedWifi.put("signalStrength", wifiInfo.getRssi());
		connectedWifi.put("signalLevel", WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			connectedWifi.put("rxLinkSpeed", wifiInfo.getRxLinkSpeedMbps());
			connectedWifi.put("wifiNetworkName", wifiInfo.getPasspointProviderFriendlyName());
		}
		return connectedWifi;
	}
	
	/**
	 * Extracts current active network type
	 * @param context
	 * @return  NOT_CONNECTED,UNKNOWN,WIFI,TYPE_2G,TYPE_3G,TYPE_4G,TYPE_5G
	 */
	public static String getActiveNetworkName (Context context) {
		String network_name = NETWORK.UNKNOWN.toString();
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info == null || ! info.isConnected())
			network_name = NETWORK.NOT_CONNECTED.toString();
		; // not connected
		if (info.getType() == ConnectivityManager.TYPE_WIFI) {
			network_name = NETWORK.WIFI.toString();
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
					network_name = NETWORK.TYPE_2G.toString();
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
					network_name = NETWORK.TYPE_3G.toString();
					break;
				case TelephonyManager.NETWORK_TYPE_LTE: // api<11: replace by 13
				case 18: // TelephonyManager.NETWORK_TYPE_IWLAN: not available in api<25: replace by 18
				case 19: // LTE_CA
					network_name = NETWORK.TYPE_4G.toString();
					break;
				case 20:
					network_name = NETWORK.TYPE_5G.toString();
					break;
				default:
					network_name = NETWORK.UNKNOWN.toString();
					break;
			}
		}
		return network_name;
	}
	
	/**
	 *  return basic information of available simcards in the slots.i.e number,carrier, roamingEnabled
	 * @return List<Map<String,Object>> availableSimCards;
	 */
	public static List<Map<String,Object>> getAvailableSimCards(){
		return availableSimCards;
	}
	
	/**
	 * Returns all signalling related information of the available sim cards.
	 * @param context
	 * @return List<Map<String,Object>> simCardNetworkCapabilities;
	 */
	public static List<Map<String,Object>> getSimCardNetworkCapabilities (Context context) {
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
						simStat.put("signalLevel",cellSignalStrengthWcdma.getLevel());
						simStat.put("asuLevel",cellSignalStrengthWcdma.getAsuLevel());
						simStat.put("dbm",cellSignalStrengthWcdma.getDbm());
						
					} else if (cellInfos.get(i) instanceof CellInfoGsm) {
						CellInfoGsm cellInfogsm = (CellInfoGsm) cellInfos.get(i);
						CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
						simStat.put("type","gsm");
						simStat.put("signalLevel",cellSignalStrengthGsm.getLevel());
						simStat.put("asuLevel",cellSignalStrengthGsm.getAsuLevel());
						simStat.put("dbm",cellSignalStrengthGsm.getDbm());
						
					} else if (cellInfos.get(i) instanceof CellInfoLte) {
						CellInfoLte cellInfoLte = (CellInfoLte) cellInfos.get(i);
						CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
						simStat.put("type","lte");
						simStat.put("signalLevel",cellSignalStrengthLte.getLevel());
						simStat.put("asuLevel",cellSignalStrengthLte.getAsuLevel());
						simStat.put("dbm",cellSignalStrengthLte.getDbm());
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
							simStat.put("rssi",cellSignalStrengthLte.getRssi());
							simStat.put("snr",cellSignalStrengthLte.getRssnr());
						}
						
					} else if (cellInfos.get(i) instanceof CellInfoCdma) {
						CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfos.get(i);
						CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
						simStat.put("type","cdma");
						simStat.put("signalLevel",cellSignalStrengthCdma.getLevel());
						simStat.put("asuLevel",cellSignalStrengthCdma.getAsuLevel());
						simStat.put("dbm",cellSignalStrengthCdma.getDbm());
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
							simStat.put("rssi",cellSignalStrengthCdma.getEvdoDbm());
							simStat.put("snr",cellSignalStrengthCdma.getEvdoSnr());
						}
					}
					simCardNetworkCapabilities.add(simStat);
				}
			}
		}
		return simCardNetworkCapabilities;
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

