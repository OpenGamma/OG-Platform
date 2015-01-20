
Example Curve Bundle Provider Tool
----------------------------------

The example class ``CurveBundleProviderTool`` demonstrates how to initialize the OpenGamma market risk environment and request a ``MulticurveBundle``.

#. **Initialise the market risk environment**

   The example first initialises the modules needed for in-memory masters/sources, an engine module and a module to populate example data.
   The dependency injection is handled by registering these modules with Google Guice, ultimately to provide an instance of the ``CurveBundleProvider``.

#. **Populate configuration and market data**

   The ``DataLoadModule`` utilizes the ``DatabaseRestore`` class to populate the in-memory masters from the example configuration provided in **curve-import-data**.
   The call to populate the data is evoked when the ``CurveBundleProvider`` builds the multicurve.

#. **Request a curve bundle**

   The ``CurveBundleProvider`` buildMulticurve() method takes the the **curve construction configuration name**, the **snapshot name**, the **currency matrix name** and the **valuation time** as arguments.
   The following steps then result in the return of the MulticurveBundle

   #. Populates the masters with config and market data

   #. Create a list of ``MarketDataBuilder`` for raw data, multicurve data and fx matrix data.

   #. Create an instance of the ``MarketDataEnvironmentFactory`` based on the snapshot ``MarketDataFactory`` and the ``MarketDataBuilder``

   #. Build the ``MarketDataEnvironment`` based on the specified snapshot ``MarketDataSpecification``, the valuation time and the single ``MarketDataRequirement`` of the named curve construction configuration