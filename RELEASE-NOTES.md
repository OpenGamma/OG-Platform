OpenGamma Platform 2.2 milestones
---------------------------------

These release notes cover changes from v2.1 to v2.2.


Upgrading from 2.1.0
====================

To 2.2.0-M5p1
-------------
* Bug
    * [PLAT-5314] Fix bug with headless timeseries updates

To 2.2.0-M5
-----------
* Bug
    * [PLAT-5144] Updated time series not taken into account when using Historical Data Sources
    * [PLAT-5148] Swap converter: Correct zero-coupon features (Frequency: NEVER)
    * [PLAT-5155] Move StubCalculationMethod from FloatingInterestRateSwapLeg to base class
    * [PLAT-5156] Change not null validation on StubCalculationMethod
    * [PLAT-5159] Recovery rate not correctly set for CDX hazard curve
    * [PLAT-5162] Stop NPE when asking for a timeseries without specifying the start date
    * [PLAT-5166] Fix depgraph errors after structure update and server restart
    * [PLAT-5171] Deadlock between web client threads and view process worker
    * [PLAT-5172] Pausing a running view generates unnecessary warnings
    * [PLAT-5181] MergingViewProcessListener confuses results when recompilations occur
    * [PLAT-5182] Example server fails to start
    * [PLAT-5191] Bond security converter allows FRNs to pass through and be created as fixed coupon bonds
    * [PLAT-5192] Handle more conventions for pricing / risk for bonds
    * [PLAT-5193] Convexity is not calculated for bonds
    * [PLAT-5198] Bond measure scaling
    * [PLAT-5203] DV01Function does not work when the shift property is not set
    * [PLAT-5205] ViewProcess running against LATEST/LATEST can't use earlier cache values
    * [PLAT-5206] NullPointerException during ValueProperties union operation
    * [PLAT-5214] If valuation date before time series start, bundle resolution can fail
    * [PLAT-5217] NullPointerException when caching convention bundles without external identifiers
    * [PLAT-5226] Maven poms do not fully declare versions
    * [PLAT-5243] Time-series tooltips not displaying
    * [PLAT-5250] Currency on present value from clean price does not work
    * [PLAT-5256] Fix payment lag when converting new swap security
    * [PLAT-5257] Fix accrual period business day convention on new swap converter
    * [PLAT-5258] ScheduleCalculator ignores convention when using day of month adjuster
    * [PLAT-5275] Remove early exercise condition in BjerksundStenslandModel
    * [PLAT-5284] ArrayIndexOutOfBoundsException in .LogNaturalCubicMonotonicityPreservingInterpolator1D
    * [PLAT-5288] NPE in VolatilitySurfaceDataFudgeBuilder deserialisation when xs and ys are Object arrays
    * [PLAT-5291] Fix UnitScaling for Positions containing numerous trades
    * [PLAT-5308] Exchange traded upload format didn't support premium amount/ccy/date
    * [PLAT-5309] OG-Bloomberg leaks connection threads


* Improvement
    * [PLAT-5165] Add EquityForwardCurveFromFutureCurveFunction and defaults to DemoStandardFunctionConfiguration
    * [PLAT-5168] Support having no live data sources
    * [PLAT-5169] Add component.master.enforceSchemaVersion to simulated examples db masters
    * [PLAT-5170] MarketDataAvailabilityProvider hints for historical data don't include the date
    * [PLAT-5174] Add support for BondFutureSecurity and EquityIndexDividendFutureSecurity to PositionGreekContractMultiplier
    * [PLAT-5175] Add QuantityFunction to DemoStandardFunctionConfiguration
    * [PLAT-5184] add exchangeInitialNotional & exchangeFinalNotional to swap and swaption blotter
    * [PLAT-5197] When changing view def feedback correct errors for gadgets/depgraphs where value req no longer exists
    * [PLAT-5218] Ability to sort outputs of ConfigImportExportTool
    * [PLAT-5234] Change NamedInstance pattern to be map based
    * [PLAT-5240] Add ACT/365.25 day count
    * [PLAT-5241] Let the daycount in TimeCalculator be configurable
    * [PLAT-5259] Add BusinessDayConventions constants
    * [PLAT-5266] Add DayCounts constants
    * [PLAT-5300] Allow bond yields to be used in CurveMarketDataFunction
    * [PLAT-5307] Add Jacobian for purely interpolated curves

* New Feature
    * [PLAT-5157] Move Obligor class out of the credit package and refactor for use with bonds
    * [PLAT-5201] Add accrued interest function for curves
    * [PLAT-5209] Security market price for bond futures
    * [PLAT-5210] Security market price for dividend futures
    * [PLAT-5245] Replace string references in bond securities with legal entities
    * [PLAT-5246] Use legal entity information in curve providers
    * [PLAT-5247] Add equivalent constructors for bonds that use a full legal entity object
    * [PLAT-5280] ListedEquityOption Prototyping: Extend to allow one to bootstrap Vols from PreviousMarks
    * [PLAT-5290] Create SecurityMarkCurrentFunction to compute ValueRequirementNames.MARK_CURRENT

* Task
    * [PLAT-5143] Tool to identify view definitions / function repositories that contain ambiguity
    * [PLAT-5160] Return unhandle messages to the Java stack
    * [PLAT-5185] Handle large intrinsic value for equity option
    * [PLAT-5216] Allow -gui/-silent flag to be passed to spawned executables
    * [PLAT-5267] Upgrade to Corporate-Parent 1.1.6
    * [PLAT-5274] Add MARK_PREVIOUS and MARK_CURRENT to ValueRequirementNames
    * [PLAT-5287] Upgrade to Joda-Beans 0.9.0

To 2.2.0-M4
-----------
* Bug
    * [PLAT-3028] - Logback 0.9.17 doesn't support Java 7 suppressed exceptions
    * [PLAT-4900] - Forward starting swaps are asking for future fixings when constructing cashflows
    * [PLAT-5041] - ISDACompliantYieldCurveFuncion ignored instrument daycount
    * [PLAT-5056] - Fix ISDACompliantCDSFunction to use real holiday source
    * [PLAT-5089] - FloatingIndex doesn't have correct AUD-LIBOR-BBA name
    * [PLAT-5093] - CompiledViewDefinition.getMarketDataSelections holds incorrect data
    * [PLAT-5094] - ArrayIndexOutOfBoundsException when applying market data manipulators
    * [PLAT-5095] - Single parameter versions of RemoteConventionSource.getSingle() throw a NullPointerException
    * [PLAT-5097] - Late detection of position changes cause terminal outputs to be dropped
    * [PLAT-5098] - Position quantities of 0 are represented as 0E-8 and displayed as 0.0000000
    * [PLAT-5108] - DirectBeanFudgeBuilder tries to instantiate abstract classes
    * [PLAT-5110] - NonVersionedRedisHolidaySource should count weekends as holidays
    * [PLAT-5118] - Currency conversion can be ambiguous
    * [PLAT-5121] - Graph fragments built after expired resolutions detected aren't pruned
    * [PLAT-5124] - RedisSimulationSeriesResolver uses equality operator for string comparison
    * [PLAT-5128] - View process hangs waiting for jobs to complete
    * [PLAT-5129] - SingleComputationCycleExecutor can't handle empty execution plans
    * [PLAT-5139] - Upgrade to Corporate-Parent 1.1.5

* Improvement
    * [PLAT-5085] - Allow NormalHistoricalVaRFunction to pass optional parameters down to dependent functions
    * [PLAT-5088] - Ability to retry failed subscriptions
    * [PLAT-5090] - ViewClient.attachToViewProcess should throw more useful exceptions
    * [PLAT-5137] - Split FinancialSecurityUtils visitors into concrete classes
    * [PLAT-5138] - Catch failures for individual valuation dates in FXImpliedYieldCurveSeriesFunction

* New Feature
    * [PLAT-5120] - Create ListedEquityOptionBjerksundStenslandFunction family, BjS with option price as vol requirement
    * [PLAT-5135] - Formatter for SpreadDoublesCurve in the web client

* Task
    * [PLAT-5100] - Merge changes from 2.1 to 2.2
    * [PLAT-5109] - WebsiteBasicsComponentFactory requires the batch master but does not use it


To 2.2.0-M3
-----------

API compatibility
- [PLAT-4969] Move convention expiry calculators
  The expiry calculators have moved package to com.opengamma.financial.convention.expirycalc.
  Organize imports should fix broken code.

- [PLAT-4955] Add full convention source and master
  Major refactor of convention code adding support by database.
  Add ConventionType class to give types a name.
  Rename Convention class to FinancialConvention.
  Rename ConventionVisitor class to FinancialConventionVisitor.
  Rename ConventionVisitorAdapter class to FinancialConventionVisitorAdapter.
  Renamed getConvention() methods on source to getSingle() and changed Class parameter to end.
  Move com.opengamma.financial.convention.percurrency package to .initializer and refactor.

Configuration compatibility
- [PLAT-4955] Add full convention source and master
  All convention master entries require work.
  Use 'com.opengamma.component.factory.master.InMemoryConventionMasterComponentFactory'.
  or 'com.opengamma.component.factory.master.DbConventionMasterComponentFactory'.
  Each have a 'populateDefaultConventions' option which can be set to true to initialize from hard coded values.
  The 'ConventionSourceComponentFactory' takes an extra 'conventionMaster' argument.  


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
