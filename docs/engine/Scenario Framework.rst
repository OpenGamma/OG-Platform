==================
Scenario Framework
==================
The scenario framework provides a mechanism for applying transformations to market data and using the transformed
data to drive calculations. The core idea of the framework is that the transformations are defined independently
of the market data, and can be applied to any arbitrary set of data.

The scenario framework is one of the two main services offered by the OpenGamma calculation engine, the other being
the execution of the risk calculations themselves. The engine provides an operation to create market data
used for calculating measures in one or more hypothetical scenarios. The inputs to this operation are:

* The set of market data used as the basis for the scenario data
* The scenario definition which tells the engine how to transform the base data to create the data for each scenario

The key concepts in the framework are perturbations and market data filters.

Perturbations
=============
A perturbation defines a single transformation that can be applied to a piece of market data. Common examples might
be a parallel shift of one basis point applied to all points on a curve, or increasing the quoted market value
of a security by 5 percent.

A perturbation is an instance of a type that implements the ``Perturbation`` interface. The most important method
on this interface is ``apply``. The ``apply`` method takes a piece of market data as an argument and returns
a copy of the market data with the transformation applied.

A perturbation contains the logic for applying the transformation (for example shifting each of the points on a
curve) and the data that defines the magnitude of the shift (for example the size of the shift to apply to
each point).

The OpenGamma platform includes perturbation implementations for the common transformations applied to the
standard market data types, and it is also possible to add user defined perturbations for standard
or custom types of market data.

Market Data Filters
===================
Market data filters are used by the engine to choose which market data is transformed. A market data filter defines
a rule for deciding whether a piece of data should be transformed.

A filter can select exactly one piece of market data, for example the USD discounting curve. Or it can
apply to an arbitrary number of pieces of data, for example the market value of all options whose underlying
is an equity in the pharmaceutical sector.

When the engine is building market data for a scenario, it applies each filter to every piece of eligible market
data, and if the filter matches, the filter's perturbation is applied to the data. The engine ensures that
no item of market data is perturbed more than once, even if it matches multiple filters. After a piece of
data has been perturbed it is no longer eligible for matching. Therefore the order in which the filters
are applied to the data is significant.

Using the Scenario Framework
============================
Running a Single Scenario
-------------------------
At the simplest level, using the scenario framework consists of the following steps:

* Create a ``MarketDataFilter`` that selects some market data
* Create a ``Perturbation`` that transforms the selected data
* Bundle the filter and perturbation into a ``PerturbationMapping``
* Create a ``ScenarioDefinition`` containing the mapping
* Pass the scenario definition to ``Engine.buildScenarioMarketData`` to build the data
* Pass the market data to ``Engine.runScenarios`` to perform the calculations

This will yield a set of ``ScenarioResults`` containing results for one scenario.

Running Multiple Scenarios
--------------------------
Another common application of scenarios is to repeat the same set of calculations with the market data
subjected to a range of perturbations of different magnitudes. For example, comparing the results when
a curve is shifted by -10bp, -5bp, 0, +5bp and +10bp.

This can be achieved by creating a single ``MarketDataFilter`` and multiple ``Perturbation`` instances, one
for each scenario. These are bundled into the same ``PerturbationMapping`` which always contains one
filter but can contain an arbitrary number of perturbations.

When this mapping is passed to ``Engine.buildScenarioMarketData``, the returned data will contain a
set of market data for each scenario. There is one scenario defined by each perturbation in the mapping.
When this data is used to run the scenarios, the returned ``ScenarioResults`` will contain one set of
results for each scenario.

Running Multiple Scenarios in Combination
-----------------------------------------
The most complex application of the scenario framework is to create scenarios by combining perturbations across
multiple independent pieces of market data.

An example of this is to apply a range of shocks to both FX rates and volatilities. There is one scenario for
each combination of FX rate shock and volatility shock. So given a set of 5 shocks for FX rates and 7
shocks for volatilities, the output would contain 35 sets of results. This can be visualised as a table
with FX shocks on one axis and vol shocks on the other. Each cell contains a set of results for a scenario.

This is achievable by using multiple perturbation mappings in the same scenario definition. The engine creates
a set of market data for every combination of perturbations, where each scenario contains a perturbation
from each mapping.

So in the example above, there would be two perturbation mappings in the scenario definition, one containing a
filter to select FX rates and the other with a filter to select volatilities. Each scenario would contain one
FX rate perturbation and one volatility perturbation. The total number of scenarios would be the number of
perturbations in the FX mapping multiplied by the number of perturbations in the vol mapping.

This can be extended to an unlimited number of dimensions simply by including more mappings in the scenario definition.

Standard Filter and Perturbation Types
======================================
Curves
------
Filters
~~~~~~~
* ``AllCurvesMulticurveFilter`` - matches all curves in the system.
* ``CurveNameMulticurveFilter`` - matches any curve in the system whose name equals the name in the filter.
* ``CurrencyMulticurveFilter`` - matches any curve in the system whose currency equals the filter's currency.
  For discounting curves this is the curve currency, for forward curves this is the currency of the curve's index.
* ``IndexMulticurveFilter`` - matches any forward curve in the system whose index equals the filter's index.
* ``IndexNameMulticurveFilter`` - matches any forward curve in the system where the name of the curve's index
  equals the name in the filter.

Perturbations
~~~~~~~~~~~~~
* ``MulticurveInputParallelShift`` - applies a parallel shift to the market quotes used to build a curve. This
  transforms the curve input data and therefore has no effect if pre-calibrated curves are provided to the engine
  by the user.
* ``MulticurveOutputParallelShift`` - applies a parallel shift to a calibrated curve.
* ``MulticurvePointShift`` - applies shifts to the nodal points in a calibrated curve. Different shifts can be
  applied to different points. This requires the curve's ``MulticurveBundle`` to contain the same ``CurveNodeId``
  data as the perturbation.

FX Rates
--------
Filters
~~~~~~~
* ``CurrencyPairFilter`` - matches an FX rate whose currency pair is the same as the filter's currency pair or
  is the inverse of the filter's currency pair.

Perturbations
~~~~~~~~~~~~~
* ``FxRateShift`` - applies a shift to an FX rate. Correctly handles the case where the currency pair in the
  market data environment is the inverse of the pair matched by the filter.

