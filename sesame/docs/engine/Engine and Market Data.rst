================================
Using the Engine and Market Data
================================
This document covers some of the core concepts needed when working with the OpenGamma calculation engine.
It describes the different phases of execution in the engine, the use of the Engine API to build
market data and how to use that market data to perform calculations.

Execution Phases in the Engine
==============================
At the highest level, performing calculations in the OpenGamma calculation engine can be divided into two
distinct phases:

1. Build the market data required by the calculations
2. Run the functions that perform the calculations, passing in the market data from step 1

The API of the ``Engine`` interface reflects this division. It has two operations for building market
data, ``buildMarketData`` and ``buildScenarioMarketData``; it also has two operations for performing calculations,
``runView`` and ``runScenarios``.

Using the Engine API
--------------------
Overview
********
When calculating results for a single scenario, the typical sequence of calls to the engine is:

1. ``buildMarketData`` - builds the market data for a single scenario
2. ``runView`` - calculates risk measures for a single scenario

When calculating results for multiple scenarios, the typical sequence of calls is:

1. ``buildScenarioMarketData`` - builds multiple sets of market data, one for each scenario
2. ``runScenarios`` - calculates risk measures for a set of scenarios

When performing calculations for very large portfolios it may be necessary to submit the trades in batches
to limit the memory used by the engine. In this case it may be possible to build a single set of market
data that can be used when calculating the results for every batch of trades. This provides better performance
than building a separate set of market data for each batch.

In this case, the sequence of calls is:

1. ``buildMarketData`` / ``buildScenarioMarketData`` once, passing in all trades
2. ``runView`` / ``runScenarios`` once for each batch of trades

Building Market Data
********************
The first task when calculating a set of results is establishing what market data is required for the calculations
and finding or creating it. In this context, "market data" can refer to any data from an external data provider
(e.g. security market prices, FX rates) or any data derived from it (e.g. curves, volatility surfaces).

The methods on the ``Engine`` interface for creating market data are ``buildMarketData`` and
``buildScenarioMarketData``. ``buildMarketData`` creates a single set of market data for performing one
set of calculations. ``buildScenarioMarketData`` creates multiple sets of market data for performing the same
set of calculations multiple times for different scenarios.

These two methods take a similar set of arguments. They both require the view configuration, the supplied
market data environment, the calculation arguments and the portfolio. ``buildScenarioMarketData`` also requires
a scenario definition.

View Config
~~~~~~~~~~~
The ``ViewConfig`` defines the calculations that will be performed. It specifies the classes used to calculate
the results and the arguments used when creating instances.

Market Data Environment
~~~~~~~~~~~~~~~~~~~~~~~
The market data environment contains any data that the user wishes to supply from an external source. This environment
will be empty if the user wishes the engine to provide all the market data for the calculations.
This is a common use case, and there is a factory method for creating empty environments:
``MarketDataEnvironmentBuilder.empty()``.

If an item of market data is available in this environment it won't be built by the engine. It is possible for
all the required market data to be provided in the environment, in which case the engine will not build any
market data. In this case it is possible to skip the first phase altogether and immediately perform the
calculations.

If the environment contains a subset of the required data the engine will build the remainder.
If the engine needs to build market data that depends on other data, for example curves that are built from
market quotes, the engine will attempt to get the underlying data from the environment before requesting it
from other sources.

Calculation Arguments
~~~~~~~~~~~~~~~~~~~~~
The calculation arguments specify arguments used by the engine when running the calculations. The important
arguments when building market data are:

1. Valuation time used in the calculations
2. ``MarketDataSpecification`` - this tells the engine where to request observable market data, e.g. FX rates or
   security market prices. This can be a live data provider (e.g. Bloomberg or Reuters), historical data or
   a previously saved snapshot of data.

Portfolio
~~~~~~~~~
This is the set of trades or securities for which the calculations are performed.

Scenario Definition
~~~~~~~~~~~~~~~~~~~
A scenario definition is required as an argument to ``buildScenarioMarketData``. A scenario definition allows the
engine to create multiple sets of market data by applying transformations to a single set of base market data. The base
data is taken from the market data environment argument ``baseData`` or created by the engine, as documented in
`Market Data Environment`_.

The scenario framework is documented in full `here <Scenario Framework.rst>`_.

Performing Calculations
***********************
Risk measures are calculated with calls to ``runView`` for a single scenario or ``runScenarios`` for
multiple scenarios. The market data required for the calculations must be provided as an argument. This can be
created by a call to ``buildMarketData`` or ``buildScenarioMarketData``, created using data from an external source,
or a mixture of the two.

The other arguments are the same as for ``buildMarketData`` and ``buildScenarioMarketData``, documented in
`Market Data Environment`_.
