OpenGamma Platform 2.2 milestones
---------------------------------

These release notes cover changes from v2.1 to v2.2.

2.2.0-M2p04
===========

[PLAT-6798] Add date adjustment for start date


2.2.0-M2p02
===========

[PLAT-5041] Fix ISDACompliantYieldCurveFuncion ignoring instrument daycount
[PLAT-5056] Changed hardcoded weekend holiday source to spring configured holiday source
[PLAT-5055] Never concatenate strings in ArgumentChecker


2.2.0-M2p01
===========

[PLAT-4983] Fixed zeroing results based on puf/spread input
[PLAT-4983] Allow principal and upfront results for non-IMM CDS



Upgrading from 2.1.0
====================

To 2.2.0-M2
-----------

Configuration compatibility
- [PLAT-4236] Added ability to query functions via green screens.
  - 'functionConfigurationSource' property should be set on the WebsiteBasicsComponentFactory.
  - this will affect engine and fullstack type ini files, as well as any other ini where the WebsiteBasicsComponentFactory is used.
  - example configuration is provided in /examples-simulated/config/fullstack/fullstack-examplessimulated.ini


Database compatibility
- Version of ElSql library updated to v0.8.
This has a change in meaning for the @INCLUDE(:variable) tag.
If you have any application-specific .elsql files then they must be changed as follows:
Search for "@INCLUDE(:" and replace with "@VALUE(:"
Note that @VALUE does not have a space output after it, whereas @INCLUDE(:variable) did.


API compatibility
- The sub-classes of ValueProperties are no longer publicly visible. Details for correcting any affected code can be
  found in the Javadoc for the ValueProperties.isNearInfiniteProperties method.

- The representation of a dependency graph has changed. This affects the DependencyGraph and DependencyNode classes,
  and any uses of them. Graphs (and nodes) are now immutable and it is only possible to navigate a graph from root to
  leaf without maintaining additional data structures. This can reduce the memory foot-print of the application by
  allowing node instances to be shared between graphs. Additional methods have been added to DependencyGraphExplorer
  to provide similar functionality to the indices that were previously available for navigation.

- CompiledFunctionResolver, FunctionResolver, and FunctionRepository now have getFunction methods for retrieving
  metadata on a single function by the unique identifier held in a dependency node.

- ComputationTargetFilter is now an interface and is no longer extended from a dependency graph node filter.


To 2.2.0-M1
-----------

Configuration compatibility
- [PLAT-4684] To support managing market data snapshot from the UI, WebsiteBasicsComponentFactory entry in the .ini configuration files needs the following properties
  marketDataSnapshotMaster, 
  (liveMarketDataProviderFactory or marketDataSpecificationRepository), 
  viewProcessor,
  computationTargetResolver and
  volatilityCubeDefinitionSource
  
  Example configuration is provided in /examples-simulated/config/fullstack/fullstack-examplessimulated.ini

- [PLAT-4782] The legacy analytics web UI has been retired. WebsiteAnalyticsComponentFactory, which used to construct
  its server-side components, has been removed. Any instances of this can be safely deleted from component
  configuration .ini files.
  
- [PLAT-4804] SpringJettyComponentFactory has been replaced by EmbeddedJettyComponentFactory. Replacement default ini
  configuration is:
    [jetty]
    factory = com.opengamma.component.factory.EmbeddedJettyComponentFactory
    resourceBase = ${jetty.resourceBase}


Database compatibility
- No upgrade required


API compatibility
- [PLAT-4782] The legacy analytics web UI has been retired. The dependency on the CometD long-polling library, and the
  custom RESTful end-points that it used, have been removed.

- [PLAT-4381] A new setter has been added to ViewClient which allows a Map<String, String> of context values to be
set and propagated down to the associated view process so that it can be used to trace the progress of a request
through the system. It use the logback MDC mechanism (http://logback.qos.ch/manual/mdc.html) which means that any
context values can be output in log statements with a suitable configuration file.

- ExternalIdSearch is now immutable
Change constructor to of() factory

- FXForwardCurveDefinition is now immutable
Change constructor to of() factory
Change getTenors() to getTenorsArray()

- FuturePriceCurveDefinition is now immutable
Change constructor to of() factory
Handle change of return type from array to list

- ValueSnapshot is now a bean
Change constructor to of() factory
The set method has been retained for ease of upgrade

- HistoricalTimeSeriesRating is now immutable
Change constructor to of() factory

- HistoricalTimeSeriesRatingRule is now immutable
Change constructor to of() factory

- ScenarioDslParameters and ScenarioDslScript are now immutable
Change constructor to of() factory

- CurveKey is now immutable
Change constructor to of() factory

- VolatilityCubeKey is now immutable
Change constructor to of() factory

- VolatilitySurfaceKey is now immutable
Change constructor to of() factory

- YieldCurveKey is now immutable
Change constructor to of() factory

- ManageableYieldCurveSnapshot is now immutable
Change constructor to of() factory


Analytics compatibility
- No expected differences


Changes since 2.1.0
===================

To 2.2.0-M1
-----------
http://jira.opengamma.com/issues/?jql=fixVersion%20%3D%20%222.2.0-M1%22
