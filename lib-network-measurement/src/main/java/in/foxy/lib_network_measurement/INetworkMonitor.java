package in.foxy.lib_network_measurement;

public interface INetworkMonitor
{
    void isNetworkAvailable(boolean available);
    void onSignalStrengthChange(long strength);
    void ipAddress(String ipAddress);
    void downloadSpeed(String downloadSpeed);
    void onUploadSpeed(String uploadString);
}

