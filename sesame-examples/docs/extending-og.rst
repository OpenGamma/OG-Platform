===================
Extending OpenGamma
===================

Custom Analytics
================

A simple example of calculating your own PV on an interest rate swap via an *InterestRateSwapCalculator* implementation can be seen in *ThirdPartyInterestRateSwapCalculator*

Here a *MultipleCurrencyAmount* of $42 is returned for all PV calculations.

The *ThirdPartyRemoteTest* class shows how this third party implementation is fed into the *ViewConfig* rather than the OG specific *InterestRateSwapCalculatorFactory* class

With the marketdata and fullstack servers running in your IDE the *ThirdPartyRemoteTest* can be run against the 'remote' fullstack server.

Adding custom code to an OG server
==================================

From within your project directory run::

    mvn package

The resulting jar file can then be placed in *{OG install location}/lib/*, which will be available on the classpath of the fullstack server after a restart.

This can be tested by firing up the local fullstack server rather than through the IDE and running any remote views that call into the custom code.