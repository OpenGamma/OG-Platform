OpenGamma Platform 2.2 milestones
---------------------------------

These release notes cover changes from v2.1 to v2.2.


Upgrading from 2.1.0
====================

To 2.2.0-M6
-----------

API compatibility
- [PLAT-3807] Regenerate curve/surface functions when new config objects added
  The method signature in FunctionConfigurationSource has changed - see Javadoc for detailed guidance.
  The VersionedSource SPI interface has been removed to support assumptions made by caching layers.

Configuration compatibility
- [PLAT-3807] Regenerate curve/surface functions when new config objects added
  The CompiledFunctionService instance used throughout should be constructed with a FunctionConfigurationSource,
  not a FunctionRepository in order to handle updates. Existing code will work, but adding config entries will
  still require a restart.

* Sub-task
  * [PLAT-5024] - implementation of CommodityForwardCurve
* Bug
  * [PLAT-3413] - OG-Financial: Modified duration multiplied by 100
  * [PLAT-5223] - VolatilitySurfaceDefinitionFudgeBuilder creates Object[] which can't be cast
  * [PLAT-5315] - Reducing an input to a more specific form throws NullPointerException
  * [PLAT-5331] - Replace resolved tenors with config tenors in labels on matrix output
  * [PLAT-5333] - Default uninstall leaves system in inconsistent state
  * [PLAT-5340] - Error cause when dragging and dropping dropdown before the data has loaded
  * [PLAT-5343] - A process with an open service handle prevents clean uninstall
  * [PLAT-5345] - Tolerance is hard coded to 10^-12 in FXImpliedYieldCurveSeriesFunction
  * [PLAT-5347] - Race hazard in InMemoryLKVLiveMarketDataProvider constructor
  * [PLAT-5348] - NPE in Engine tests
  * [PLAT-5353] - Market data availability notification can get lost
  * [PLAT-5354] - Market data available is reported while reference data is still down
  * [PLAT-5356] - Database import tool requires server restart
  * [PLAT-5357] - Incremental market data subscriptions which map to an existing subscription in the live data client never request another initial snapshot
  * [PLAT-5368] - Potential for a MarketDataManager to unsubscribe from market data that it never requested with concurrent view processes
  * [PLAT-5370] - PNL was removed from InterestRateFutureOptionBlackDefaults in 2.1 to 2.2 merge
  * [PLAT-5375] - DiscreteDividendFunction expects patchy availability of its requirements but does not override canHandleMissingRequirements
  * [PLAT-5376] - Thread pool used by remote live data client can't be configured from Spring
  * [PLAT-5384] - Remote live data client deadlocks if a finite thread pool is used
  * [PLAT-5391] - BondTransactionDiscountingMethod.presentValueFromCleanPrice quantity not taken into account
  * [PLAT-5392] - DbHolidayMaster has improper code when handling custom searches
  * [PLAT-5403] - NonVersionedRedisHistoricalTimeSeriesSource fails with version strings
  * [PLAT-5406] - Empty time series data is encoded as a zero-length array
  * [PLAT-5407] - OG-Financial: MarketQuotePositionFunction incorrect canApplyTo
  * [PLAT-5409] - Portfolio order gets scrambled on import
  * [PLAT-5410] - DatabaseRestoreTool dependent on file system ordering
  * [PLAT-5414] - User message is dispatched before contexts are initialised
  * [PLAT-5415] - Time series updater fails if duplicate series present
  * [PLAT-5420] - URL configuration dialog box stops service & it doesn't restart
  * [PLAT-5433] - run-tool passes class name as arg[0]
  * [PLAT-5436] - OG-Financial: BondTradeWithEntityConverter uses trade date instead of settlement date
  * [PLAT-5437] - OG-Financial: BondFunctionUtils pass 0.0 as closing price for bond futures
  * [PLAT-5438] - Net basis and gross basis are both calculated using the gross basis formula
  * [PLAT-5443] - Simulation only applies scenarios in batch mode
  * [PLAT-5462] - DbFunctionConfigurationSourceComponentFactory makes a static query
  * [PLAT-5471] - AssertionError: null thrown during graph building causes dropped rows
  * [PLAT-5472] - Implied deposit curve series tenors of less than 1M resolve to 0
  * [PLAT-5473] - Make ConventionTypesProvider log an error when there is no ConventionType rather than throwing an exception
  * [PLAT-5476] - run-tool displays console window when launched from start menu
  * [PLAT-5477] - EngineDebugger can no longer be started from a URL
  * [PLAT-5478] - Restore previous date parsing API in RowParser
  * [PLAT-5484] - Engine calculates delta cycle even though function parameters have changed
  * [PLAT-5486] - Properties can be incorrectly set for curve bundles
  * [PLAT-5502] - Spot and payment dates are incorrectly calculated in ImpliedDepositCurveSeriesFunction
  * [PLAT-5503] - AbstractJmsResultPublisher can't handle concurrent calls to send
* Epic
  * [PLAT-5423] - VIX Options
* Improvement
  * [PLAT-3436] - OG-Analytics: Add implementation of bond yield computation of more conventions
  * [PLAT-3448] - Improve the handling of PV01 and YieldCurveNodeSensitivities for Bonds
  * [PLAT-3449] - Improve the handling of PV01 and YieldCurveNodeSensitivities for Bond Futures
  * [PLAT-3807] - Regenerate curve/surface functions when new config objects added
  * [PLAT-4692] - Add Excel support for snapshot import/export
  * [PLAT-5286] - No early exercise condition in BjerksundStenslandModel
  * [PLAT-5316] - Embedded ActiveMQ sometimes fails to start quickly because of persisted messages
  * [PLAT-5320] - Add Ibor vs OIS swap type & conversion
  * [PLAT-5321] - Add USD-Federal Funds-H.15 & MXN-TIIE-Banxico
  * [PLAT-5322] - Add support for variable notional swaps
  * [PLAT-5327] - Clearer error messages when new curve functions throw exceptions in the compile phase
  * [PLAT-5352] - OG-Analytics: Fixed coupon bonds transaction - constructor from yield
  * [PLAT-5359] - LiveDataClient decorator that resubscribes automatically
  * [PLAT-5361] - InMemoryLKVLiveMarketDataProvider should remove LKV entries when subscription is removed
  * [PLAT-5364] - OG-Financial: accept ON and TN as tenor for CashNodeConverter
  * [PLAT-5378] - Improve dividend frequency handling
  * [PLAT-5385] - STIR Futures: par rate for HullWhiteFunction should use ParRateHullWhiteCalculator
  * [PLAT-5390] - Don't create new HTTP client instances with each request
  * [PLAT-5400] - Add properties to bond functions that use yield and curves to allow unambiguous selection of curves
  * [PLAT-5401] - Handling no early exercise in RGW model
  * [PLAT-5408] - OG-Analytics: Bond futures - Hull-White method: add test vs Numerical Integration
  * [PLAT-5421] - Check for null inputs in multi curve providers and implement hashCode() and equals() where necessary
  * [PLAT-5428] - Add top-level method to get all names from ParameterProviderInterface
  * [PLAT-5429] - Dividend payment after expiry in RGW model
  * [PLAT-5441] - Derivative by time to dividend payment in RGW model
  * [PLAT-5442] - OG-Analytics: BondFuturesSecurity - add basket at spot
  * [PLAT-5445] - Extend NextQuarterAdjuster to allow for Quarterly Cycles other than March
  * [PLAT-5446] - Remove assumptions of ExchangeTradedInstrumentExpiryCalculator from EquityFuturePriceCurveFunction
  * [PLAT-5449] - ActivEquityIndexFuturePriceCurveInstrumentProvider - Remove need for cast from ExchangeTradedInstrumentExpiryCalculator to FutureOptionExpiries
  * [PLAT-5458] - SuccessResult should not rely on code gen to implement interface
  * [PLAT-5487] - Delete function that interpolated bond yields directly
  * [PLAT-5504] - Refactor InterestRateSwapLeg/InterestRateSwapLegConvention
  * [PLAT-5508] - Add date format dropdown to csv/xls import dialog
* New Feature
  * [PLAT-3369] - Constant curve definitions
  * [PLAT-3370] - Spread curve definitions
  * [PLAT-4304] - Add function that produces issuer-specific curves.
  * [PLAT-5087] - Bond node for curve construction
  * [PLAT-5202] - add a conventionsource argument to AbstractMockSourcesTest in order to test the new swapconverter
  * [PLAT-5277] - OG-Analytics: create a util than return the next (non-)business day from a calendar
  * [PLAT-5279] - ListedEquityOption Refactor: Break out ImpliedVol computation as separate Function
  * [PLAT-5280] - ListedEquityOption Prototyping: Extend to allow one to bootstrap Vols from PreviousMarks
  * [PLAT-5281] - Change issuer curve type configuration to use legal entity filters
  * [PLAT-5313] - Need a FileUtils.copyResourceToTempFile() operation
  * [PLAT-5317] - Create a function that constructs bond curves directly from yields
  * [PLAT-5318] - Add new versions of bond pricing functions that start from the clean price
  * [PLAT-5323] - Add bond security converter that uses available information to construct legal entities
  * [PLAT-5324] - NoPaddingTimeSeriesSamplingFunction very inefficient for large time series
  * [PLAT-5326] - Create OptionSecurityVisitors.java, a set of visitors to output common fields of OptionSecurity's
  * [PLAT-5330] - Extract common fields (name, unique id) from CurveDefinition into an abstract top-level class
  * [PLAT-5332] - Create a factory for curve spread functions and make functions implement NamedInstance
  * [PLAT-5334] - Add bond analytics that use market yield
  * [PLAT-5336] - Add bond analytics that use yield curves
  * [PLAT-5349] - Create curve specifications for spread curves
  * [PLAT-5350] - Create an abstract base class for curve specifications
  * [PLAT-5351] - Create curve specifications for constant curves
  * [PLAT-5360] - Add ForwardRateAgreementSecurity
  * [PLAT-5362] - Roll-Geske-Whaley model
  * [PLAT-5363] - Analytic Greeks for Roll-Geske-Whaley model
  * [PLAT-5365] - Add support for constant curves in interpolated issuer curves
  * [PLAT-5366] - Add support for spread curves in issuer curve configurations
  * [PLAT-5371] - Add bond node converter
  * [PLAT-5372] - Function that creates issuer curves using fitting rather than interpolation on yields
  * [PLAT-5374] - Add curve calculation type to bond functions that use curves
  * [PLAT-5382] - Extension of RGW model for multiple discrete dividends
  * [PLAT-5398] - Add converter for bond futures that uses legal entity information
  * [PLAT-5399] - Add bond futures support to functions that price bonds using curves
  * [PLAT-5422] - Add calculators for bond and bond future PV01 and bucketed PV01
  * [PLAT-5426] - Add function to calculate gamma PV01 for the provider form of curve bundles
  * [PLAT-5427] - OG-Analytics: Created ParSpreadRate calculators
  * [PLAT-5439] - Add functions that calculate net basis and gross basis for a bond future deliverable basket
  * [PLAT-5444] - Create VixFutureAndOptionExpiryCalculator
  * [PLAT-5448] - Add theta calculator for bond futures that uses new curve providers
  * [PLAT-5452] - OG-Analytics: Add the Australian Governement bond convention
  * [PLAT-5453] - Add function that provides theta for bond futures
  * [PLAT-5480] - implement the Implement the rate cut-off mechanism for CouponONArithmeticAverage
* Task
  * [PLAT-4867] - Swap notional = 0 is valid
  * [PLAT-5278] - Tests for BjerksundStenslandModel
  * [PLAT-5342] - Hard stop the JVM before the wait notify hint expires
  * [PLAT-5402] - OG-Analytics: Bond futures - separate security and trade
  * [PLAT-5404] - Tool to remove time series data from a database dump
  * [PLAT-5412] - Don't restart JVM after external controlled stop.
  * [PLAT-5424] - FileUtils.TEMP_DIR variable should be public
  * [PLAT-5450] - Remove VersionedSource
  * [PLAT-5461] - Identify and correct any queries to "latest" config from functions

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
