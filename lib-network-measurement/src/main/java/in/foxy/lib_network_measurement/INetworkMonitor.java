package in.foxy.lib_network_measurement;

import java.util.HashMap;

/**
 * INetworkMonitor:  it should be implementated by the user to get callbacks from this lib
 */
public interface INetworkMonitor
{
    /**
     * onNetworkAvailabilityChange: triggers everytime when network went online to offline or offline to online
     * @param networkAvailable
     * @return
     */
    
    void onNetworkAvailabilityChange(boolean networkAvailable);
    
    /**
     * onConnectedNetworkInfoAvailable : triggers initially and whenever any information of connected data network changes
     * @param networkType  : UNKNOWN,NOT_CONNECTED,WIFI,TYPE_2G,TYPE_3G,TYPE_4G,TYPE_5G
     * @param connectedNetwork :
     *   if(UNKNOWN || NOT_CONNECTED) : null
     *   if(WIFI) : frequency,ipAddress,linkSpeed,signalStrength,signalLevel
     *   else :  simCount,signalLevelSimX, asuLevelSimX, dbmSimX,
     *           MAY NOT AVAILABLE : rssiSimX, snrSimX
     */
    
    void onConnectedNetworkInfoAvailable(String networkType, HashMap<String,Object> connectedNetwork);
    
    /**
     * onNetworkSpeedChanges : triggers whenever connected data network upload or download speed changes.
     * @param uploadSpeed
     * @param downloadSpeed
     */
    void onNetworkSpeedChanges(int uploadSpeed,int downloadSpeed);
   
}

