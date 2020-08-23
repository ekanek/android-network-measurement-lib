package in.foxy.lib_network_measurement;

import java.util.HashMap;
import java.util.Map;

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
     * onNetworkSpeedChanges : triggers whenever connected data network upload or download speed changes.
     * @param uploadSpeed
     * @param downloadSpeed
     */
    void onNetworkSpeedChanges(int uploadSpeed,int downloadSpeed);
   
}

