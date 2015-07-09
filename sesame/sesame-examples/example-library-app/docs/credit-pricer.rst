
Example Credit Pricer
---------------------

The example class ``CreditPricerExample`` demonstrates how to initialize the OpenGamma market risk environment and request **PV** and **CS01** for a CDS.

#. **Initialise the market risk environment**

   The example first initialises the modules needed for in-memory masters/sources, an engine module and a module to populate example data.
   The dependency injection is handled by registering these modules with Google Guice, ultimately to provide an instance of the ``CreditPricer``.

#. **Populate configuration and market data**

   The ``DataLoadModule`` uses the ``DataLoader`` class to populate the in-memory masters from the example configuration provided in **credit-import-data**.
   The call to populate the data is invoked when the ``CreditPricer`` prices the CDS.

#. **Request a Credit PV and CS01 result**

   The ``CreditPricer`` price() method takes the following steps to return a ``Result`` object containing **PV** and **CS01**.

   #. Populates the masters with config and market data

   #. Create the ``CalculationArguments`` based on an ``EmptyMarketDataSpec`` (the engine will source the data) and the valuation time.

   #. Create a ``ViewConfig``, the example config is created in the ``CreditViewUtils`` utility class.
      The view specifies which configuration and market data snapshots to use in the pricing as well specifying the outputs and analytic methodologies.

   #. Run the view, providing the ``Engine`` with the ``ViewConfig``, the ``CalculationArguments``, the empty ``MarketDataEnvironment`` and the list of sample trades created by the credit utility class.
