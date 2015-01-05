===============================================
Setting up an OpenGamma development environment
===============================================

Set up
======

Run ``mvn package -DskipTests`` to unpack the web resources

Running the fullstack server from you IDE
=========================================

Run **OpenGammaServer** from example server in your IDE.

This will start the OpenGamma Component server using the configuration the config/fullstack folder.

The example resources will be loaded and persisted in memory while the server is running.

The following can be added to your VM options ``-Dlogback.configurationFile=com/opengamma/util/warn-logback.xml`` to manage the logging level.
