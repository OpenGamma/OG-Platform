=======================================
Setting up an OpenGamma dev environment
=======================================

Set up
======
Firstly, go though the process of setting up a local OG server instance. This will include obtaining the OG server zip/tarball, creating and initializing a postgres database, installing ActiveMQ and then configuring the *fullstack* and *marketdata* servers
 
Next, simply create a new maven project, adding the dependencies needed for the OG-Platform, databases and test framworks. The pom.xml included in this project shows an example of this.

Running the marketdata server from you IDE
==========================================
Obtain the **marketdata.ini** file from *{OG install location}/platform/config/marketdata/* and your configured **marketdata.properties** file from *{OG install location}/config/*, place these in the *config/marketdata/* folder in your project.

Also add *{OG install location}/platform/config/default-ehcache.xml* to the *config* folder of your project.

Change the following to point to the marketdata web resources::

    jetty.resourceBase = {OG install location}/platform/web-marketdata

Next run **com.opengamma.component.OpenGammaComponentServer**
VM options::

 -Dlogback.configurationFile=com/opengamma/util/warn-logback.xml -Xmx4g -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M

Program arguments::

    classpath:marketdata\marketdata.properties

Running the fullstack server from you IDE
=========================================

Once the the marketdata server is running, obtain the **fullstack.ini** and **db.ini** files from *{OG install location}/platform/config/fullstack/* and your configured **fullstack.properties** file from *{OG install location}/config/*, place these in the *config/fullstack/* folder in your project.

Change the following to point to the fullstack web resources::

    jetty.resourceBase = {OG install location}/platform/web-engine

Next run **com.opengamma.component.OpenGammaComponentServer**
VM options::

 -Dlogback.configurationFile=com/opengamma/util/warn-logback.xml -Xmx4g -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M

Program arguments::

    classpath:fullstack\fullstack.properties

 