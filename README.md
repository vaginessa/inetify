# Inetify is a Free Open Source Android tool providing two features related to Wifi networks:

- Give a notification if a Wifi network does not provide internet access 

This can be useful if you have set up your phone to automatically connect to a Wifi network that requires authentication on a web page (a "captive portal"). Should you forget to authenticate, Inetify notifies you that there is no internet access.

It is also possible to manually test internet access and to ignore certain Wifi networks for the automatic check.

- Automatically activate Wifi when near a Wifi network and deactivate it otherwise (experimental) 

This can help to save battery by deactivating Wifi when it is not needed, and avoid using mobile data while Wifi could be used instead.

This feature is experimental: Depending on the accuracy of localization, it may be unreliable. Inetify can use GPS, but this uses considerably more battery than network based locations, which can be very inaccurate.

The Wifi locations Inetify should look out for can be added to a list and viewed on a map.

Inetify runs in the background and tries to be very conservative on battery usage.

Please note, that the reliability of finding Wifi locations depends on the accuracy of location evaluation. Inetify can use GPS, but this uses considerably more energy than network based locations, which are relatively inaccurate.

# Acknowledgements

The Wifi locations are shown with OpenStreetMap/osmdroid: http://code.google.com/p/osmdroid

To load and parse the internet site and get the page title from it, Inetify uses jsoup: http://jsoup.org

The Inetify icons were composed of two icons from Oxygen Icons: http://www.oxygen-icons.org
