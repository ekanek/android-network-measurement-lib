# android-network-measurement-lib
  This library helps you to fetch all network related information from your device  including
  - Available Simcards in device
  - Connected wifi info
  - sim card/wifi signal related info

# How to Use
 Step 1. Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.ekanek:android-network-measurement-lib:Tag'
	}

Step 3. Initialize library
        DetailedNetworkInfo.initialize(Context context)

# Public Methods
  - DetailedNetworkInfo.getConnectedWifiInfo()
    - provides (frequency,ipAddress,linkSpeed,signalStrength,signalLevel,rxLinkSpeed,wifiNetworkName)
  - DetailedNetworkInfo.getActiveNetworkName()
    - provides (NOT_CONNECTED,UNKNOWN,WIFI,TYPE_2G,TYPE_3G,TYPE_4G,TYPE_5G)
  - DetailedNetworkInfo.getAvailableSimCards();
    - provides [number,carrier, roamingEnabled]
  - DetailedNetworkInfo.getSimCardNetworkCapabilities();
    - provides (type,signalLevel, asuLevel, dbm, rssi, snr)
        - rssi,snr if available
  - DetailedNetworkInfo.registerNetworkMonitorCallback(INetworkMonitor iNetworkMonitor)
