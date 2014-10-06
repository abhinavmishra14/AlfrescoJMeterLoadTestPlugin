AlfrescoJMeterLoadTestPlugin
============================

This plug-in can be used to test load on alfresco using jmeter's java request sampler
------------------------------------------------------------------------------------------

Note:

1- You can use the build script to deploy the plug-in to JMeter. Build script will deploy the jar and its dependencies to JMETER_HOME/lib/ext directory.
   
    It will also deploy the config.properties to JMETER_HOME/bin. If plugin will not find this property file at runtime then internal property file will be used by the plugin.

2- Set the environment variable as "JMETER_HOME".

    JMeter plug-in will use the properties file (config.properties) deployed at JMETER_HOME/bin to populate the initial info in the JMeter Java Request Sampler GUI.