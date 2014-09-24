===============
Metrics Capture
===============

Introduction
============
The engine supports the automatic capture of timing metrics for each function in a view.
The metrics are captured using `Coda Hale Metrics <http://metrics.codahale.com/>`_
and can be viewed either via JMX or reporting tools such as `Ganglia <http://ganglia.sourceforge.net/>`_.
The image below shows the the information displayed when viewing via JVisualVM. All the statistics
shown are automatically collected by Metrics.

.. image:: metrics-screenshot.png
   :width: 95%

Configuration
=============

To enable the metrics, the metrics configuration needs to be included in the component's ini
file. Near the top of the file include the common metrics configuration

.. code::

   MANAGER.INCLUDE = classpath:common/common-metrics.ini

In the section for ViewFactoryComponentFactory, add or amend the lines for ``defaultFunctionServices``
and ``metricRegistry``.

.. code::

   [engine]
   factory = com.opengamma.sesame.component.ViewFactoryComponentFactory
   classifier = main
   ...
   defaultFunctionServices = CACHING,METRICS
   metricRegistry = ::summary

Next update the associated properties file, with the metrics properties:

.. code::

   # Metrics
   metrics.repository=MyMetrics  # name for the repository
   ganglia.publish=false         # choose whether to publish to ganglia

If you want to publish to ganglia you will also need to provide the details of the server where it is running:

.. code::

   ganglia.publish=true
   ganglia.host=
   ganglia.port=
   ganglia.addressingMode=
   ganglia.ttl=

When complete you should be able to start the server, navigate to the MBeans with an appropriate application
(e.g. JVisualVM) and view the metrics section. (Note that you may need to enable JMX for your server first.)

Implementation
==============

The metrics are captured using proxies around the engine functions (this is the same method used
for caching). The metrics proxy sits inside the caching proxy so it only records timings for
invocations that are not cached. This is because otherwise it would be difficult to get accurate
measurements for the "real" calculation work being done.
