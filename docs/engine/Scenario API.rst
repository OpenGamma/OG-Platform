===========================
Low-level API for Scenarios
===========================
The scenario framework in the OpenGamma calculation engine provides a mechanism for transforming data as it moves
into and out of functions in the engine. This allows scenarios to be implemented by transforming values that
have been calculated by functions.

This guide is aimed at developers implementing scenario functions or maintaining the scenario mechanism in
the engine.

An example of a typical scenario might be a parallel curve shift, implemented by transforming
the value returned from the curve data function and adding the shift value to each data point. This transformed
value would then be used to construct the curve.

A key feature of the scenario framework is that scenarios aren't coupled to the functions whose data they transform
and the views on which they operate. This implies:

* Writers or normal engine functions don't have to implement or be aware of scenarios
* A view's configuration doesn't have to change to use a scenario

The classes that make up the scenario support can be divided into two categories:

* Definition of the transformations to apply to the data - this includes scenario definitions, scenario arguments
  and the environment
* Logic that consumes the definitions and performs the transformations on the data - these are the scenario functions

Scenario definitions are intentionally independent of view configuration. In order to execute a view with a scenario,
the scenario definition is applied to the view configuration to create a new view configuration which is then
executed.

This means a view can be executed with or without a scenario, and a scenario can be defined and applied to any view.

A view configuration can also contain a scenario. This covers the cases where a view makes no sense without an
associated scenario. For example, a view might contain the same column twice, in one case the values are calculated
with a shock applied to the curves and in one case the values are calculated using the standard curve without a shock.

ScenarioDefinition
==================
The ``ScenarioDefinition`` class is a collection of objects which define the transformations to apply to the
data calculated when a view is run. The transformations are divided into two categories

* Transformations applied to all calculations
* Transformations applied to specific columns or outputs in the view

Each transformation is specified by an instance of ``ScenarioArgument`` (see below).

A scenario definition also contains information about which functions must be included in the view configuration
in order to perform the transformations. These are available from ``ScenarioDefinition.getFunctionTypes()``.

Knowledge of the transformations and the functions needed to apply them means a scenario is self-describing.
A scenario can be applied to an existing view configuration using ``ViewConfig.withScenario(scenarioDefinition)``.
This produces a new view configuration which will apply the scenario when performing calculations.

Scenario Arguments
==================
A scenario argument describes a transformation that should be applied to some data during a calculation cycle.
All arguments must implement ``ScenarioArgument`` which contains a single method: ``getFunctionType()``. This
specifies which function consumes the argument and performs the transformation it describes. Functions must
be a subtype of ``ScenarioFunction``. There is a one-to-one relationship between argument and function types.

Scenario arguments classes should be immutable (like every other value used in function parameters) and should
be a Joda bean to allow them to be serialized. Apart from that there is no restriction on how they should be
implemented or what data they should contain. Typically, however, a scenario argument describes two separate things:

* Rules for matching data to which the transformation should be applied. Typical examples might be:
   * All curves whose currencies are USD
   * The market data value with the Bloomberg ticker "AAPL US Equity"
* A transformation to apply to any data that matches the rules

Scenario Functions
==================
A scenario function performs the logic to apply a transformation described by a scenario argument to a piece of
data. Scenario functions must implement ``ScenarioFunction``. This interface contains
a single method ``getArgumentType()`` which returns the type of the argument used by the function.

The design of scenario functions is based on decorators. The scenario function implements the same interface as
an engine function and has a reference to an instance of the engine function. Function calls are invoked on the
scenario function. The scenario function can call the real engine function, possibly after modifying the
arguments, and it can modify the return value of the function before returning it. It can
even return a value without ever calling the underlying function.

Scenario functions must implement two interfaces:

* ``ScenarioFunction`` (see above)
* Any other interface which is also implemented by a normal engine function

The constructor of a scenario function must include a parameter of the type of the engine function. This is the
delegate function to which the scenario function can forward method calls.

For example, data for building curves is provided by an implementation of ``CurveSpecificationMarketDataFn``.
There is a scenario function for performing shifts on curve input data called ``CurveDataShiftDecorator``. It
implements ``CurveSpecificationMarketDataFn`` and has a single constructor argument of type
``CurveSpecificationMarketDataFn``.

The method for requesting curve data is called ``requestData``. The implementation of
``CurveDataShiftDecorator.requestData`` performs the following steps:

#. Request the definition of the shift from the environment (see below)
#. Call ``requestData`` on the delegate function to get the unshifted curve data
#. Build and return a new curve data object by applying the shift to the data from the delegate function

Environment
===========
A scenario function obtains its arguments from the environment that is passed to all methods in engine functions.
Typically a scenario function will make the following call at the start of its method:

.. code-block:: java

    List<Arg> args = env.getScenarioArguments(this);

The generic type of the list matches the argument type returned from the function's ``getArgumentType()`` method.

Scenario functions should handle the possibility that the list of arguments will be empty. Typically they should
forward the the method call to the underlying function and return its value unchanged.

Filtered Scenario Definition
============================
A ``ScenarioDefinition`` contains the scenario arguments that apply to all calculations, plus the sets of
arguments that should only be used for specific columns or non-portfolio outputs.

A scenario function can't request the specific arguments for its column because functions are unaware of
which column they're in. Therefore a new environment is created for each column and output
containing only the arguments that apply. These arguments are passed to the environment in an instance of
``FilteredScenarioDefinition``.

A filtered scenario definition is created by calling ``ScenarioDefinition.filter()``, passing in the name of
the column or non-portfolio output. The filtered definition contains the column arguments plus the arguments
that apply to all calculations.

Argument pruning
================
Scenario arguments are carried by the environment, and are therefore part of the key used for storing and
looking up values in the cache. This has the potential to cause unnecessary cache misses and for the same value
to be repeatedly recalculated.

For example, consider a view which builds a curve, and which has one column using a scenario, where the
scenario doesn't affect curve construction. Ideally, the curve would be built once for the entire view and
taken from the cache on all subsequent invocations of the curve function. However, the presence of the
scenario argument in the environment for one column will change the hash code and equality of the cache key
for any method invocation, even if the scenario doesn't affect the value. This causes the cache key for
the invocation of the curve function to differ, which will cause the curve to be unnecessarily rebuilt.

To prevent this problem, the caching mechanism removes arguments from the environment used to create the
cache key if they don't affect the calculated value. This is possible because the arguments are associated
with a specific function type, and the engine has knowledge of the types in the tree of functions.

The logic is simple - the engine constructs a set of all functions in the function tree below the current
function. In then clones the environment and only includes scenario arguments for functions in the set.
In this way it ensures that any arguments that can't affect the calculated value are discarded.
