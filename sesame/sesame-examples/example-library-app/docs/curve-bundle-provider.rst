
Example Curve Bundle Provider
-----------------------------

The example class ``CurveBundleProviderExample`` demonstrates how to initialize the OpenGamma market risk environment and request a ``MulticurveBundle``.

#. **Initialise the market risk environment**

   The example first initialises the modules needed for in-memory masters/sources, an engine module and a module to populate example data.
   The dependency injection is handled by registering these modules with Google Guice, ultimately to provide an instance of the ``CurveBundleProvider``.

#. **Populate configuration and market data**

   The ``DataLoadModule`` utilizes the ``DataLoader`` class to populate the in-memory masters from the example configuration provided in **curve-import-data**.
   The call to populate the data is evoked when the ``CurveBundleProvider`` builds the multicurve.

#. **The curve configuration**

   The ``CurveConstructionConfiguration``, named ''USD_FF_DSCON-OISFFS_L3M-FRAIRS_L1M-BS_L6M-BS', is the entry point for specifying the multicurve curve bundle.
   The example included in curve-import-data makes reference to the following ``InterpolatedCurveDefinition`` curves

   #. 'USD-OIS-FFS-NCS' - USD curve built from Overnight and Fed Fund Swaps.
   #. 'USD-IRSL1M-BSL1ML3M-NCS' - USD curve built from Interest Rate Swap Libor 1 month and Basis Swap Libor 1 and 3 months
   #. 'USD-FRAL3M-IRSL3M-NCS' - USD curve build from 3 month Forward Rate Agreement and Interest Rate Swap Libor 3 month
   #. 'USD-FRAL6M-BSL3ML6M-NCS' - USD curve build from 6 month Forward Rate Agreement and Basis Swap Libor 3 and 6 months

   All the curves use natural cubic spline (NCS) interpolation.

   The curves nodes then make reference to various ``CurveNodeIdMapper``, which links the node instrument to the marketdata provider.
   The nodes also reference the conventions used to create the instrument.

#. **Request a curve bundle**

   The ``CurveBundleProvider`` buildMulticurve() method takes the the **curve construction configuration name**, the **snapshot name**, the **currency matrix name** and the **valuation time** as arguments.
   The following steps then result in the return of the MulticurveBundle

   #. Populates the masters with config and market data

   #. Create a list of ``MarketDataBuilder`` for raw data, multicurve data and fx matrix data.

   #. Create an instance of the ``MarketDataEnvironmentFactory`` based on the snapshot ``MarketDataFactory`` and the ``MarketDataBuilder``

   #. Build the ``MarketDataEnvironment`` based on the specified snapshot ``MarketDataSpecification``, the valuation time and the single ``MarketDataRequirement`` of the named curve construction configuration.
