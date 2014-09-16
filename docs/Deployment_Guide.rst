================
Deployment Guide
================


Overview
--------


This guide provides instructions on how to deploy a new instance of the OpenGamma platform.

It is assumed that Bloomberg market-data is available, postgreSQL is installed and is your database of choice, and that you have appropriate user permissions on the server.

Obtaining the source code
-------------------------

Download and unpack the latest deployment template

Unix::

  tar xjf sesame-server-{version}-server.tar.bz2

Windows::

  sesame-server-{version}-server-windows.zip


Folder structure
----------------

.. image:: images/DeploymentFoldersScreenShot.png


* The top level **config** directory contains property files. They include place-holders that need to be replaced to match your specific set-up.
* The top level **lib** directory will be initially blank, but can be used for custom libraries and extensions.
* The core of the platform is in the **platform** directory.
* The **platform/config** directory contains the ini files that the config properties feed into.
* As platform is separated from config and lib, this enables an easy way to update the platform with new versions, by just overwriting the platform directory.


ActiveMQ
--------

Install
~~~~~~~

Visit http://activemq.apache.org to get the latest release.

Unix::

  wget http://www.us.apache.org/dist/activemq/apache-activemq/5.9.0/apache-activemq-5.9.0-bin.tar.gz

  tar zxvf apache-activemq-5.9.0-bin.tar.gz

Windows::

  Download and unzip the binary or source installation

Start
~~~~~

From within the ActiveMQ installation directory

Unix::

  bin/activemq start

Windows::

  bin\activemq start

Check
~~~~~

Check the ActiveMQ web console at: http://localhost:8161/admin/topics.jsp

Or you can confirm that the ActiveMQ default port number 61616 is established and listening

Unix::

  netstat -na |grep 61616

Windows::

  netstat -na |find "61616"
 
Add the ActiveMQ details to both ``config/fullstack/fullstack.properties`` and ``config/marketdata/marketdata.properties``

Substitute **REPLACE-ACTIVEMQ-SERVER** with your server IP address::

  activeMQ.brokerURL=failover:(tcp://REPLACE-ACTIVEMQ-SERVER:61616?daemon=true)?timeout=3000

If you have a standard local installation of ActiveMQ , the configuration should look like this::

  activeMQ.brokerURL=failover:(tcp://localhost:61616?daemon=true)?timeout=3000

**It is recommended to use explicit IP addresses for installations beyond local evaluations (where it is anticipated that external client application interacting with OpenGamma API might be used)**

PostgresSQL
-----------

Install
~~~~~~~

If you don't already have an a postgreSQL instance installed, visit http://www.postgresql.org/download/, download the latest distribution and follow the instructions relevant to your installation. for evaluation purposed allows stick to the defaults as much as possible.

Create og_db database
.....................

Ensure postgreSQL is started and create a new database named **og_db** with **og_db_owner** as the owner

Unix::

  createuser -P og_db_owner
  createdb -O og_db_owner og_db

Windows::

  A graphical installer is available for postgreSQL on windows which includes the GUI tool pgAdmin III.
  Here we can create og_db_owner as a Login Role, and set the role to own a newly created og_db database.
  **Please note down the password for og_db_owner as you will need it as part of the next step.**

Edit the config/dbtoolcontect/dbtoolcontext-postgres.properties file  as illustrated below::

  db.url = jdbc:postgresql://REPLACE-POSTGRES-BATCH-SERVER/og_db
  db.username = og_db_owner
  db.password = <og_db_owner password>

Initialise the database using the db-create-tool in platform/scripts

Unix::

  db-create-tool.sh -c classpath:dbtoolcontext/dbtoolcontext-postgres.properties -w

Windows::

  db-create-tool.bat -c classpath:dbtoolcontext\dbtoolcontext-postgres.properties -w


Oracle (11g)
------------

Install
~~~~~~~

Create an empty og_db database schema using Oracle standard tools

DB Tools Connection config
~~~~~~~~~~~~~~~~~~~~~~~~~~

Enable OpenGamma DB tools to connect top your database.

In ``config/dbtoolcontext`` directory, create a **dbtoolcontext-oracle.properties** file (by copying the existing dbtoolcontext-postgres.properties file).

For Oracle support, it should contain the following lines::

    # Next configuration file in the chain is the INI file
    MANAGER.NEXT.FILE = classpath:dbtoolcontext/dbtoolcontext.ini

    db.dialect = com.opengamma.util.db.Oracle11gDbDialect
    db.driver = oracle.jdbc.driver.OracleDriver
    db.url = dbc:oracle:thin:@//[HOST][:PORT]/SERVICE
    db.username = REPLACE-ORACLE-USERNAME
    db.password = REPLACE-ORACLE-PASSWORD

    db.schemaNames = cfg,cnv,eng,exg,pos,prt,secb,snp,usr,len,hts,cfg

    db.scriptsResource = classpath:db

    #Global
    time.zone = Europe/London

``Replace ORACLE-HOST, SERVICE-NAME, REPLACE-ORACLE-USERNAME and REPLACE-ORACLE-PASSWORD with the appropriate values``

**Important Note : db.schemaNames, db.scriptsResource, time.zone, MANAGER.NEXT.FILE properties must have the same values as the default dbtoolcontext-postgres.properties**


Initialise the database using the db-create-tool in platform/scripts

Unix::

  db-create-tool.sh -c classpath:dbtoolcontext/dbtoolcontext-oracle.properties -w

Windows::

  db-create-tool.bat -c classpath:dbtoolcontext\dbtoolcontext-oracle.properties -w


Bloomberg access
----------------

The OpenGamma Bloomberg module supports connections to either a SAPI or Managed B-PIPE instance.

In order to configure the Market Data Server, you will need to obtain access to one of these instances, along with connection details.

Add your **Bloomberg** details to ``config/marketdata/marketdata.properties``::

  bloomberg.host=REPLACE-BLOOMBERG-SERVER
  bloomberg.port=8194

Marketdata server
-----------------

Next add your **Market Data** server details to  ``config/fullstack/fullstack.properties``::

  component.remoteProviders.baseUri = http://REPLACE-MARKETDATA-SERVER:8090/jax


With the Bloomberg and ActiveMQ place-holders updated in marketdata.properties, it is now possible to start the market data server

From within ``platform/scripts``, you can run the marketdata server.

Unix::
  marketdata.sh start

Windows::
  marketdata.bat start

Fullstack server
----------------

Update the database details in ``config/fullstack/fullstack.properties`` according to your database:

**PostgreSQL** ::

    db.dialect = com.opengamma.util.db.PostgresDbDialect
    db.driver = org.postgresql.Driver
    db.url = jdbc:postgresql://REPLACE-POSTGRES-FIN-SERVER/og_db
    db.username = REPLACE-POSTGRES-FIN-USERNAME
    db.password = REPLACE-POSTGRES-FIN-PASSWORD

**Oracle (11g)** ::

    db.dialect = com.opengamma.util.db.Oracle11gDbDialect
    db.driver = oracle.jdbc.driver.OracleDriver
    db.url = dbc:oracle:thin:@//[HOST][:PORT]/SERVICE
    db.username = REPLACE-ORACLE-USERNAME
    db.password = REPLACE-ORACLE-PASSWORD

With the Bloomberg server, ActiveMQ and database details updated in fullstack.properties, it is now possible to start the fullstack server
From within ``platform/scripts``, you can run the fullstack server.

Unix::

  fullstack.sh start

Windows::

  fullstack.bat start


``platform/logs/fullstack-console.log`` should show::

  Jul 04, 2014 12:55:39 PM com.sun.jersey.server.impl.application.WebApplicationImpl _initiate
  INFO: Initiating Jersey application, version 'Jersey: 1.17.1 02/28/2013 12:47 PM'

Check your Installation
-----------------------

Replace localhost with the IP address of your installation machine(s)

* **marketdata** : Check marketdata server URL is up  : http://localhost:8090
* **fullstack** : Check fullstack server URL is up : http://localhost:8080
* **Bloomberg** Connection : Open this URL : http://localhost:8080/jax/securities ::

  - Scroll down to Load securities
  - Add "EUR003M Index" in the Identifiers section
  - Click "Add"

.. image:: images/LoadSecurityByIDScreenShot.png

You should see the screen below, proving that the OpenGamma Platfrom was able to fetch the Security details from Bloomberg

.. image:: images/SecurityDetailScreenShot.png

**Congratulations you are now running the Opengamma Platform !**


Upgrading the server
--------------------

Firstly, and importantly, ensure that both the marketdata and fullstack instance have been stopped.

From within ``platform/scripts``

Unix::

  marketdata.sh stop
  fullstack.sh stop

Windows::

  marketdata.bat stop
  fullstack.bat stop

As mentioned above, upgrading the server can be as simple as downloading a new deployment template and replacing the platform folder with the latest one.

It would be good practice to copy and rename either the platform directory or the entire structure when upgrading. This ensures backup of old log files and an easy way to revert to an older instance.

Once the new source files are in place, read the release notes: https://github.com/OpenGamma/OG-Platform/blob/master/RELEASE-NOTES.md, select the relevant release from the branch/tag drop-down.

The release notes should inform you of any database upgrades, configuration updates needed and API changes.

Finally start the marketdata and fullstack services again.
