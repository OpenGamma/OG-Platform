Welcome to the OpenGamma Platform!
----------------------------------
OpenGamma's flagship technology, the OpenGamma Platform, is a comprehensive solution for analytics capable of 
satisfying the full range of front-office and risk requirements. It supports pre-trade ad-hoc calculations, 
near-real-time streaming analytics, batch/overnight risk calculations, and complex historical scenarios and stress 
tests in the same system.

Built as a completely open architecture, the OpenGamma Platform is designed so that every component can be 
individually used, or individually replaced, based on customer requirements. We don't believe in forklift 
upgrades, and we built the OpenGamma Platform so that they're never necessary: individual projects can use 
OpenGamma components when they provide a clear advantage, and later migrate additional portions of their 
infrastructure if and when time and resources permit.

Visit the developer website at http://developers.opengamma.com for more information, downloads, docs and more

The Examples server
-------------------
This download package contains only what is needed to run our example system.
It relies on access to Bloomberg. Please use a different download if you do not have Bloomberg.
If you're interested in looking at the source code, you should look at one of the source download packages.

Pre-requisites
--------------
All you should need is the latest Java 1.6 JRE/JDK.  You'll need at least 2GB RAM and a dual-core processor,
although you might get by with 1GB in a pinch.  Production systems will generally have higher requirements.

Initializing the database
-------------------------
To do the initial database setup, you should run:

  scripts/init-examples-bloomberg-db.sh

on Linux and MacOS X, and 

  scripts\init-examples-bloomberg-db.bat

on Windows.

You only need to do this the first time you run the server, or if you want to reset the databases
to the original configurations.

Running the server
------------------

To start the server itself, you should run:

  scripts/examples-bloomberg.sh

on Linux and MacOS X or

  scripts\examples-bloomberg.bat

on Windows.

This particular script sends debugging output to the console.  It is normal to see some Exceptions
thrown when starting a view in this mode.

Using the server
----------------

Once the service is up and running, just point your browser at:

    http://localhost:8080

to access OpenGamma's web user interface.  Alternatively, visit: 

    http://localhost:8080/jax/components

to get a sense of the underlying power of the system available via REST.


More information
----------------
For more information go to http://developers.opengamma.com
