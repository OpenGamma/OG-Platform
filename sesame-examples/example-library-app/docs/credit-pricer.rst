
Example Credit Pricer Tool
--------------------------

The example class ``CreditPricerTool`` demonstrates how to initialize the OpenGamma market risk environment and request **PV** and **CS01** for a CDS.

#. **Initialise the market risk environment**

  The example first initialises the modules needed for in-memory masters/sources, an engine module and a module to populate example data.
  The dependency injection is handled by registering these modules with Google Guice, ultimately to provide an instance of the ``CreditPricer``.

#. **Populate configuration and market data**

  The ``DataLoadModule`` utilizes the ``DatabaseRestore`` class to populate the in-memory masters from the example configuration provided in **credit-import-data**.
  The call to populate the data is evoked when the ``CreditPricer`` prices the CDS.

#. **Request a Credit PV and CS01 result**

  The ``CreditPricer`` price() method takes the following steps to return a ``Result`` object containing PV and CS01.

  #. Populates the masters with config and market data

  #. Create the ``CalculationArguments`` based on an ``EmptyMarketDataSpec`` (the engine will source the data) and the valuation time.

  #. Create a ``ViewConfig`` The example config is created in the ``CreditViewUtils`` utility class. The view specifies which configuration and market data snapshots to use in the pricing as well specifying the outputs and analytic methodologies.

  #. Run the view, providing the ``ViewConfig``, the ``CalculationArguments``, the empty ``MarketDataEnvironment`` and the list of sample trades provided by the credit utility class.
