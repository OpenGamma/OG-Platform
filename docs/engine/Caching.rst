===========================================
Caching in the OpenGamma calculation engine
===========================================
Caching of values calculated by functions is one of the most important services provided by the OpenGamma
calculation engine. The goal of the caching system is twofold:

#. Providing an almost transparent mechanism to boost performance by calculating expensive values once
   and reusing them many times
#. Removing the burden of implementing caching from function writers

Caching is implemented using dynamic proxies (instances of ``java.lang.reflect.Proxy``). When the engine builds
a cacheable function it wraps it in a proxy that implements the function's interface.

When another function calls a method on a cacheable function it is actually calling the proxy. The proxy then has
the option to call the underlying function and calculate the value or to return a cached value and skip the
call.

Enabling and disabling caching
==============================
Like most services provided by the engine, caching is optional and can be disabled if not required. In normal
operation caching should be enabled as it can provide significant performance gains. Disabling caching can
make debugging simpler as all functions are executed every time they are called.

If the ``ViewFactory`` is created using ``ViewFactoryComponentFactory`` (the recommended approach) then
caching is enabled by default. Caching can be disabled by specifying a value for the property
``defaultFunctionServices`` in the ``.ini`` file and not including the value ``CACHING``. The legal values
for function services are defined in the ``FunctionService`` enum.

If no engine services are required then the following configuration can be specified

``defaultFunctionServices=``

If the ``ViewFactory`` is created directly in code the default services are specified by the constructor argument
``defaultServices`` of type ``EnumSet<FunctionService>``.

Caching can be disabled for a single view even if it is enabled by default in the view factory. The method
``ViewFactory.createView()`` allows the required services to be specified using a set of services.

Core concepts for function writers
==================================
This section outlines the details of the caching implementation that must be understood when writing engine functions.

Always calculate a value
------------------------
Functions should be written naively so they always calculate a value when they are called, regardless of whether
it is expensive to calculate and whether the function will be called repeatedly. The engine will take care of
skipping a method call if a cached value is available.

Functions should never implement their own caching.

Use the ``@Cacheable`` annotation to request caching
----------------------------------------------------
If the function writer thinks the return value of a method should be cached then the method should
be annotated with ``@Cacheable``. The annotation can be on the interface or the implementation. If the engine
encounters a call to an annotated method and there is a cached value available it will skip the call and
immediately return the value.

Memoization
-----------
Caching in the OpenGamma engine is based on the concept of memoization. That is, if a method is called multiple
times with exactly the same set of arguments then the same value can be returned.

When a cacheable method is invoked a key is constructed and used to query the cache. The cache key is based on the
ID of the object receiving the method call, the method and the method arguments. If there is no cache entry for the key,
the method is run and the return value is cached.

When the method is invoked again a key is constructed in the same way and used to query the cache. If a
value is found in the cache it is returned without calling the underlying method.

This model is simple to understand but imposes some restrictions on function implementations.

Stateless functions
-------------------
If a method's return value only depends on its arguments it implies the function must have no mutable state.
If the function has mutable state that affects the return value there is no guarantee the same value
will be returned if the method is called repeatedly with the same arguments.

Statelessness is also necessary as the OpenGamma calculation engine is multithreaded and functions are
called concurrently from multiple threads.

Method arguments
----------------
The arguments to a cacheable method are used to create a cache key. This implies two things:

* Parameter classes must have sensible implementations of ``hashCode()`` and ``equals()``. If a class inherits those
  methods from ``Object`` its instances are only equal with themselves. In this case the calculated value will be
  correct but it will never be cached.

* Arguments must not be mutated after the method is called. If a method mutates one of its arguments it is also
  mutating the key used to cache its return value. If this happens the behaviour is undefined and it is possible
  an incorrect value will be returned from the cache. For this reason immutable types are strongly preferred
  for method parameters. Arrays types are particularly discouraged.

Implementation details
======================
This section outlines the details of the design and implementation of the caching mechanism. It is primarily
of interest to someone wishing to maintain or modify the engine. The key classes in the system are:

``ViewFactory``
---------------
The view factory contains a single cache which is shared between all views it creates. This means that commonly
used values can be reused in different views without having to be recalculated. The cache is an instance of
the Google Guava class ``com.google.common.cache.Cache``.

Clearing the cache
^^^^^^^^^^^^^^^^^^
The ``ViewFactory`` class has a method ``clearCache()`` which discards all cached values. It is exposed as
a JMX operation. When this is invoked the existing cache is not affected as it is possible that the cache is in
use by a view and if it were cleared it could lead to inconsistencies in the results.

Clearing the cache is achieved by swapping the cache for a new, empty cache. When each view starts a calculation
cycle it requests a cache from the view factory. So a view that is using the existing cache will have a consistent
view of the data until the end of the cycle, and at the start of the next cycle it will be given an empty cache.

Invalidating cache entries
^^^^^^^^^^^^^^^^^^^^^^^^^^
If a function uses a piece of external data (e.g. market data or configuration) when calculating a cached value then
the cache entry becomes invalid if the data changes. Invalid entries are not immediately removed from the cache,
for the same reason that clearing the cache doesn't immediately remove the cached data. A view might be
using the cache and removing cache entries in the middle of a calculation cycle can cause inconsistencies in
the results.

Invalidation simply marks a cache entry as stale. The next time a view requests a cache, a new cache is created
and valid entries are copied from the old cache to the new cache. Invalid entries are not copied, and therefore
new values will be calculated in the next calculation cycle.

This is not fully implemented yet. There is a mechanism to listen for notifications when configuration data changes
(in ``ViewFactoryComponentFactory.decorateSources()``) but no action is taken when a notification arrives.

``View``
--------
The view requests a cache from the view factory at the start of each calculation cycle. This guarantees the
functions have a consistent view of the data for the duration of the cycle. Data is never removed from a cache
which is in use. Removing entries is achieved by creating a new cache in the view factory which is provided to
each view at the start of its next calculation cycle.

``CachingProxyDecorator``
-------------------------
This is the core of the caching implementation. It provides ``java.lang.reflect.Proxy`` instances which implement
function interfaces and cache their results. The engine inserts the proxies between functions, so a function
calling a method on another function will actually be invoking a method on the proxy. The proxy has a
reference to the underlying function instance. When a method is invoked on the proxy the proxy's invocation
handler performs the following steps:

#. Check if the method has a ``@Cacheable`` annotation. Function interfaces can have multiple methods and
   it is possible that some of them are cacheable and some aren't. If the method isn't cacheable the corresponding
   method is invoked on the underlying function and its value is immediately returned.
#. Create a cache key for the method call. Cache keys are of type ``MethodInvocationKey``. The key contains all
   the details of the method call:

   * The ID of the underlying function instance that is the receiver of the call
   * The method that was invoked (an instance of ``java.lang.reflect.Method``)
   * The method arguments

#. Create a task to invoke the underlying method (an instance of ``CallableMethod``) but don't execute it.
#. Query the cache using the key, passing in the task

   * If there is a cached value for the key, it is returned
   * If there is no value, the cache executes the task which calls the underlying function and calculates a value
   * If the value is being calculated on another thread but isn't available yet, the cache lookup blocks until
     the value is available

``CallableMethod``
------------------
The ``CallableMethod`` class is an executable task that invokes the cacheable method on the underlying function.
It is invoked by the cache to calculate a value if there isn't one in the cache.

Before it invokes the method it pushes the method invocation key onto a thread local stack
in the class ``ExecutingMethodsThreadLocal``. When the method returns the key is popped off the stack.
Therefore the stack contains the keys of all cacheable methods currently executing.

This information is used by the cache invalidation mechanism. When a piece of external data is requested
(e.g. market data or configuration) the cache invalidator associates the ID of the data with the keys of the
executing methods. The assumption is that the data potentially affects the values calculated by the methods.

When a piece of external data changes (e.g. live market data ticks or a user edits some configuration) the
engine can find the keys for all cached values calculated from the data and invalidate the cache entries.

``MethodInvocationKey``
-----------------------
This is the type used as the cache key. It contains all the data about a method call:

* The ID of the function instance that is the underlying receiver of the call
* The method that was invoked (an instance of ``java.lang.reflect.Method``)
* The method arguments

Two keys are considered equal if:

* Their methods are equal
* Their arguments are equal according to ``Arrays.deepEquals()``
* Their receivers have the same ID

Function equality
-----------------
The basic idea of memoization is that invoking the same function with the same set of arguments will always
return the same result. The important thing to consider is the definition of "same" when talking
about function instances. It is not sufficient for two functions to be instances of the same class.

Functions are normal Java classes which can have constructor arguments and references to other functions.
The other functions are referenced through interfaces which can have multiple different implementations.
Any of the function's fields and dependencies can affect how it calculates its value. So two functions are
equal only if they have the same constructor arguments, they refer to the same functions, those functions have
the same constructor arguments, and so on.

In fact, for two function instances to be considered identical, the entire tree of objects below them in the
graph of functions must be equal.

Functions classes have few constraints on their structure and interface, so there is no way for the engine to
introspect function instances to determine if they are equal. However, functions are built by the engine from a
``FunctionModel`` which provides a way to check for equality.

Individual function instances are represented as nodes in the model, where the node contains
all the details about the function's constructor arguments and dependencies. These nodes have well defined
equality semantics, so if the nodes for two functions are equal then the functions themselves are identical.

This fact is used by the class ``FunctionBuilder`` when building function instances. A map is maintained whose
keys are instances of ``FunctionModelNode`` and whose values are the automatically generated IDs of the function
instances. This map is checked whenever a function is requested from the builder. If the map contains an ID 
for the function's node it implies an identical function has already been built. In this case, the new 
function instance reuses the existing function's ID. If there is no entry in the map for the node, a new 
ID is allocated for the new function.

When the caching mechanism creates a cache key for a method invocation it uses the ID of the receiver function
as part of the key. This ensures that the cache key for two function calls can only be equal if their functions
have the same ID. This ensures that two functions can only share values from the cache if they are identical.
