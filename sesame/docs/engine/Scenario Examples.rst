=================
Scenario Examples
=================
THIS DOCUMENT IS OBSOLETE. SEE "Scenario Framework"

This document gives examples of using the low-level scenario API.

Running a view with a scenario
==============================
Running a view with a scenario is a four step process:

* Define the shocks to apply to the market data - these are instances of ``ScenarioArgument``
* Create a ``ScenarioDefinition`` containing the scenario arguments
* Create a ``ViewConfig`` containing the scenario
* Use the ``ViewConfig`` to create and execute a view as normal

Scenario arguments
------------------
Scenario arguments typically contain two pieces of information:

* A rule to decide whether a shock should be applied to a piece of market data - normally called a 'matcher'
* A definition of the shock

For example:

.. code-block:: java

    // a matcher that matches the EUR/USD FX rate
    MarketDataMatcher eurUsdMatcher = MarketDataMatcher.idEquals("BLOOMBERG_TICKER", "EUR Curncy");

    // a scenario argument to shift the EUR/USD rate by 0.1
    MarketDataShock eurUsdShock = MarketDataShock.absoluteShift(0.1, eurUsdMatcher);

    // -------------------------------------------------------

    // a matcher that matches all US equities
    MarketDataMatcher equityMatcher = MarketDataMatcher.idLike("BLOOMBERG_TICKER", "* US Equity");

    // a scenario argument to scale equity values by 5%
    MarketDataShock equityShock = MarketDataShock.relativeShift(0.05, equityMatcher);

    // -------------------------------------------------------

    // a matcher tha4t matches a curve named "USD Discounting"
    CurveSpecificationMatcher usdDiscountingMatcher = CurveSpecificationMatcher.named("USD Discounting");

    // scenario argument to shift the USD discounting curve input data down by 10 basis points
    CurveDataShift parallelShift = new CurveDataParallelShift(-10.0 / 10000, usdDiscountingMatcher);

    // -------------------------------------------------------

    // a matcher that matches all curves with names starting "USD LIBOR"
    CurveSpecificationMatcher usdLiborMatcher = CurveSpecificationMatcher.nameLike("USD LIBOR*");

    // a scenario argument to apply shifts to specific points on the curve
    CurveDataPointShifts pointShifts = CurveDataPointShifts.relative(usdLiborMatcher,
                                                                     PointShift.of(Tenor.ofMonths(3), 0.05),  // +  5%
                                                                     PointShift.of(Tenor.ofMonths(6), 0.1),   // + 10%
                                                                     PointShift.of(Tenor.ofMonths(9), 0.05)); // +  5%

Scenario definition
-------------------
A scenario definition is a container of scenario arguments. A definition contains two sets of arguments:

* Arguments that apply to the whole view
* Arguments that apply to specific columns or non-portfolio outputs

.. code-block:: java

    // a scenario definition containing market data shocks that should be applied to the whole view
    ScenarioDefinition allColumns = new ScenarioDefinition(eurUsdShock, equityShock);

    // a scenario definition containing market data shocks that apply to the whole view and a curve shock that doesn't
    ScenarioDefinition specificColumns = allColumns.with(parallelShift, "col 1", "col 3");

View configuration
------------------
View configurations and scenario definitions are completely independent. This allows a scenario to be defined
and applied to any existing view, and also for a view to be executed with or without applying a scenario.
The view configuration class contains a method to create a new configuration containing a scenario:

.. code-block:: java

    // existing view configuration and scenario definition
    ViewConfig viewConfig = ...;
    ScenarioDefinition scenarioDefinition = ...;

    // create a new view configuration by adding the scenario to the config. the config and scenario are unchanged
    ViewConfig configWithScenario = viewConfig.withScenario(scenarioDefinition);

Using the scenario API programmatically
=======================================
It is possible for a function to set up scenario arguments before invoking another function, which will then execute
with the scenario applied. Typically this would be done in a loop, generating a range of scenario arguments,
executing the function once with each set of arguments, and aggregating the results.

In this use case there are four steps:

* Create the ``ScenarioArgument`` instances. This is exactly the same as shown above
* Create a ``FilteredScenarioDefinition`` containing the arguments
* Create derive a new ``Environment`` containing the scenario definition
* Invoke the function using the new environment

For example:

.. code-block:: java

    List<ScenarioArgument<?, ?>> args = ...;

    // create a scenario definition
    FilteredScenarioDefinition scenarioDefinition = new FilteredScenarioDefinition(args);

    // 'env' is the environment argument passed into the function
    Environment envWithScenario = env.withScenarioDefinition(scenarioDefinition);

    // invoke the function, passing in the environment containing the scenario
    Result<Double> = fn.doCalc(envWithScenario, ...);

When using the API programmatically, it is up to the user to ensure the necessary scenario function types are
added to the ``ViewConfig``. Each ``ScenarioArgument`` implementation has a type parameter which specifies
its function type. If an argument type is used programmatically, its function type must be added to the
view configuration's default configuration using ``FunctionModelConfig.decorateWith()``. See the implementation
of ``ViewConfig.withScenario()`` for an example.


2.5-M3 migration guide
======================
The low-level scenario API changed significantly in the 2.5-M3 release.

ScenarioArgument and ScenarioFunction introduced
------------------------------------------------
Previously, scenario arguments and scenario functions didn't have to implement any specific interface.
Now, arguments must implement ``ScenarioArgument`` and functions must implement ``ScenarioFunction``. These
provide type safety and enforce the relationship between a function and its arguments.

ScenarioDefinition introduced
-----------------------------
A scenario was previously defined as a ``Map<Class<?>, Object>``, where the keys were the type of the scenario
functions and the values were the associated scenario arguments. This has been replaced with ``ScenarioDefinition``
which adds the ability to apply arguments to specific columns.

DecoratorConfig replaced with ViewConfig.withScenario()
-------------------------------------------------------
Previously, scenario functions were added to view configuration using ``DecoratorConfig.decorate()``. This
has been removed in favour of using ``ViewConfig.withScenario()``.

Previously the user needed to know the
implicit dependency between argument and function types, and pass the correct function types to the ``decorate``
method. That information is now carried by ``ScenarioArgument`` which enables the scenario definition to fully
specify the perturbations and the functions required to apply them.

Environment.withScenarioArguments() renamed withScenarioDefinition()
--------------------------------------------------------------------
The method has been renamed and the parameter type has changed from a map to a scenario definition.

CycleArguments.scenarioArguments removed
----------------------------------------
The scenario is now specified in the ``ViewConfig`` and not ``CycleArguments``.

MarketDataShockDecorator.Shocks removed
---------------------------------------
Use ``List<MarketDataShock>`` instead.

CurveDataPointShiftsDecorator.Shifts removed
--------------------------------------------
Use ``List<CurveDataPointShifts>`` instead.
