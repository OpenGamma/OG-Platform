OpenGamma Platform 2.2 milestones
---------------------------------

These release notes cover changes from v2.1 to v2.2.

Upgrading from 2.1.0
====================

To 2.2.0-M27p01
------------

* Improvement
    * [PLAT-6603] - Stub interpolation uses different indices

To 2.2.0-M27
------------

* Bug
    * [PLAT-6583] - Bad SQL grammer in Marketdatasnapshotmaster
    * [PLAT-6584] - Z-spread for inflation bonds mishandles the index ratio
* Improvement
    * [PLAT-6587] - Document deployment template and ensure relevant release notes are available
    * [PLAT-6590] - Move RedisSimulationSource behind an interface
* New Feature
    * [PLAT-6589] - A new test group is required for regression tests
* Task
    * [PLAT-6559] - PV - Equity TRS - FX Matrix does not store currency of underlying
    * [PLAT-6560] - PV01 - Equity TRS - Implement engine function
    * [PLAT-6561] - Bucketed PV01 - Equity TRS - Implement engine function
    * [PLAT-6562] - FX Currency Exposure - Equity TRS - Implement engine function


To 2.2.0-M26
------------

* Bug
    * [PLAT-6554] - Accrued interest for inflation bonds is not scaled
    * [PLAT-6563] - CurveNodeCurrencyVisitor.visitCashNode catches Exception which leads to strange behaviour
    * [PLAT-6576] - Index shifts can't be applied to black volatility surfaces
    * [PLAT-6580] - BondCapitalIndexedSecurityDefinition generates flows with adjusted periodic rate rather than actual rate
    * [PLAT-6581] - BondCapitalIndexedSecurityDiscountingMethod uses wrong price to calculate yield for government inflation bonds
* Improvement
    * [PLAT-6550] - Enhance and tidy tuple library
    * [PLAT-6557] - Add object for holding bucketed pv01 results
    * [PLAT-6558] - Regression test report entries should be ordered
    * [PLAT-6568] - Add factory for FloatingIndex
    * [PLAT-6574] - Add validation of swap objects
    * [PLAT-6579] - Exclude aggregate values from regression test results
* Task
    * [PLAT-6553] - Convert IndexCDSSecurity to use SecurityLink
    * [PLAT-6569] - Fix jenkins CI build


To 2.2.0-M25
------------

* Bug
    * [PLAT-6534] - Handle user session timeout
    * [PLAT-6535] - NonVersionedRedisConfigSource does not add unique id on results collection
* Improvement
    * [PLAT-6510] - Bulk permission checks
    * [PLAT-6543] - Move cashflows to Platform
    * [PLAT-6544] - Include stack trace in Failure even if it wasn't caused by an exception
    * [PLAT-6545] - MultiplecurrencyAmount: Reduce redundant map copies
    * [PLAT-6546] - Speed up curve lookup by optimising curve type hashCode
    * [PLAT-6547] - Speed up annuity pricing by avoiding intermediate objects
* Task
    * [PLAT-6549] - Implement all NonVersionedRedisHistoricalTimeSeriesSource methods 
    * [PLAT-6551] - Increase ZSpread test tolerance


To 2.2.0-M24
------------

* Bug
    * [PLAT-6481] - CsvSnapshotWriter prints the same X axis label in Y column
    * [PLAT-6497] - Assertion message is incorrect for CouponFloatingDefinition constructor
    * [PLAT-6511] - VolatilitySurfaceSelector does not support value requirement name 'BlackVolatilitySurface'
    * [PLAT-6522] - Class conversion issue occurs where no curve exposure exists for Bond PV01s
    * [PLAT-6523] - Useless error is thrown by ConfigDBInstrumentExposuresProvider when exposure can't be resolved
    * [PLAT-6528] - Failure constructor loses underlying exception
* Improvement
    * [PLAT-6525] - Add multimap support to fudge/joda conversion
    * [PLAT-6530] - Provide mechanism to avoid security warning on startup
* New Feature
    * [PLAT-6514] - Add BucketedPV01 for BondTRS
    * [PLAT-6515] - Add currency exposure for BondTRS
* Sub-task
    * [PLAT-6496] - OG-Analytics: Coupon Ibor Compounding with Spread Simple Interest - updating floating annuity definition builder


To 2.2.0-M23
------------

* Bug
    * [PLAT-6484] - Green screens for TRS fail due to bad field lookups
    * [PLAT-6489] - NonVersionedRedisHistoricalTimeSeriesSource null pointer error
    * [PLAT-6498] - Fix SQL server HTS database
* New Feature
    * [PLAT-6491] - Specify volatility surface shocks using the index of the expiry
* Improvement
    * [PLAT-6462] - Allow multiple war file locations to be merged dynamically
    * [PLAT-6477] - ComponentRepository lookup for Foo:bar keys
    * [PLAT-6480] - Result object should be able to be constructed with an Exception and a specific status
    * [PLAT-6483] - Extend generics in ArgumentChecker
    * [PLAT-6485] - Enhance Result model
    * [PLAT-6487] - Remove line breaks and duplicates from FailureResult's message
    * [PLAT-6495] - Better identification of web client IP address
    * [PLAT-6499] - Enhance return of failures from PermissionCheckProvider
    * [PLAT-6505] - Return permission denied for Bloomberg subscriptions
    * [PLAT-6506] - Add additional result statuses
    * [PLAT-6508] - Allow home page links to be dynamically extended
* Task
    * [PLAT-6490] - Add method to clone redis simulation source & change date
    * [PLAT-6494] - Add equals and hashCode for Redis timeseries and simulation source


To 2.2.0-M22
------------
* PLAT-6473 - Enhancing the security of startup required public and protected methods in og-component to change.
  This affects code which should not be called by applications, as the application-facing API stays the same.

* Bug
    * [PLAT-6473] - Enhanced security in startup logging
    * [PLAT-6474] - Metrics component factory JMX null check
    * [PLAT-6475] - Handle property rename from permission to requiredPermission
* Improvement
    * [PLAT-6464] - Move @Config annotation from AbstractCurrencyMatrix to CurrencyMatrix
* New Feature
    * [PLAT-6297] - OG-Analytics: BondFutureOptionMargin: Black pricing method


To 2.2.0-M21
------------

* PLAT-6443 Enhance component-based infrastructure factories
Renamed factories:
- com.opengamma.component.factory.master.DataSourceComponentFactory -> com.opengamma.component.factory.infrastructure.DataSourceComponentFactory
- com.opengamma.component.factory.master.DbConnectorComponentFactory -> com.opengamma.component.factory.infrastructure.DbConnectorComponentFactory
- com.opengamma.component.factory.master.DbManagementComponentFactory -> com.opengamma.component.factory.tool.DbManagementComponentFactory
- com.opengamma.component.factory.infrastructure.JMSConnectorComponentFactory -> com.opengamma.component.factory.infrastructure.ActiveMqJmsConnectorComponentFactory
- com.opengamma.component.factory.metric.MetricRepositoryFactory -> com.opengamma.component.factory.infrastructure.MetricsRepositoryComponentFactory

- MBeanServerComponentFactory no longer has "cacheManager" parameter (CacheManager JMX now works automatically)
- ActiveMqJmsConnectorComponentFactory no longer has "connectionFactory" parameter (no longer applicable)
- DataSourceComponentFactory, DbConnectorComponentFactory, CacheManagerComponentFactory and ActiveMqJmsConnectorComponentFactory now
support "classifierAliases" parameter, which may be a comma separated list
- protected methods have changed names

Common infrastructure files added:
- common/common-metrics.ini
- common/common-infra.ini
- common/common-infra-full.ini
- common/common-dbinfra-full.ini

A standard fullstack ini file can use:
#============================================================================
MANAGER.INCLUDE = classpath:common/common-metrics.ini
MANAGER.INCLUDE = classpath:common/common-infra-full.ini
MANAGER.INCLUDE = classpath:common/common-dbinfra-full.ini
MANAGER.INCLUDE = classpath:common/common-shiro.ini
MANAGER.INCLUDE = classpath:common/common-dbmasters.ini

#============================================================================

* Bug
    * [PLAT-6420] - CurveNodeConverter: FedFundsFutures use incorrect time series.
    * [PLAT-6449] - ISDACDXAsSingleNameParallelCS01Function does not create pricingCDS consistently...
    * [PLAT-6463] - ManageableMarketDataSnapshot.name does not allow nulls
* Improvement
    * [PLAT-6287] - CalendarSwapNode: change to use the DateSet config
    * [PLAT-6431] - Make Bloomberg live market data server work for Bloomberg Bpipe server
    * [PLAT-6435] - Better website error pages
    * [PLAT-6443] - Enhance infrastructure component factories
    * [PLAT-6454] - FixedSwapLegDetailsFormatter and FloatingSwapLegDetailsFormatter should use LocalDate.MIN/MAX instead of null to represent empty LocalDates
    * [PLAT-6456] - Reduce dependencies of OG-Analytics
    * [PLAT-6398] - Integrate user management with components
* New Feature
    * [PLAT-6317] - Bucketed PV01 for bonds
* Task
    * [PLAT-6266] - OG-Financial : integration of present value for linked bonds
    * [PLAT-6436] - Add a failure status PERMISSION_DENIED
    * [PLAT-6458] - Add an AnalyticsEnvironment to allow customisations
* Sub-task
    * [PLAT-6433] - Allow SessionProvider to open multiple service.


To 2.2.0-M20
------------

* ConfigLink API has changed - of method renamed to resolved/resolvable in order to
differentiate between the different types of links
* SecurityLink API has changed - of method renamed to resolved/resolvable in order to
differentiate between the different types of links
* ConventionLink API has changed - of method renamed to resolved/resolvable in order to
differentiate between the different types of links
* SnapshotLink has been added to allow access to different types of snapshots
* Snapshot database schema has been updated to support different types of snapshots
* SnapshotSource and SnapshotMaster have been updated to allow for searching of different
types of snapshots. These changes should be backwards compatible though some methods
have been deprecated.

* Bug
    * [PLAT-5845] - Bloomberg security type resolver does not as for SECURITY_TYP2
    * [PLAT-6100] - Deployment template doesn't work with windows
    * [PLAT-6224] - Deployment template includes a pom file in "lib" folder
    * [PLAT-6408] - Values not being passed correctly between job items
    * [PLAT-6409] - trade.setProviderId does not appear to be persisted in RemotePositionMaster
    * [PLAT-6412] - CouponNotionalVisitor does not support definitions of type CouponONArithmeticAverageDefinition
    * [PLAT-6413] - AnnuityAccrualDatesVisitor does not support definitions of type CouponONSpreadDefinition
    * [PLAT-6414] - OG-Analytics: SwapFuturesPriceDeliverableSecurityHullWhiteMethod - incorrect priceCurveSensitivity
    * [PLAT-6415] - Wrong calculation of Interest/Payment Amount in ZCS fixed leg
    * [PLAT-6417] - Occasional test failure in DependencyGraphTraceBuilder
    * [PLAT-6421] - NonVersionedRedisConfigSource doesn't handle missing configs correctly
    * [PLAT-6423] - MultiFileConfigSaver can't handle configs with slashes in the names
    * [PLAT-6426] - Change yearly index naming convention from 1Y to 12M
    * [PLAT-6432] - Bloomberg ticks being lost
* Improvement
    * [PLAT-5842] - Return zeroes for PV01 and YCNS in the case where there is no sensitivity to a particular curve
    * [PLAT-6175] - Move equity option market value requirement into the functions that need it.
    * [PLAT-6375] - jacobian matrix for exogenous curves are not used.
    * [PLAT-6377] - OG-Analytics : for curve construction test : declare as private all public function which are not a test
    * [PLAT-6407] - OG-Analytics: FuturesTransactionDefinition add referencePrice method
    * [PLAT-6422] - Implement NonVersionedRedisHistoricalTimeSeriesSource methods to satisfy engine
    * [PLAT-6424] - Modify ConventionMasterInitializer to add the ability to add securities
    * [PLAT-6425] - ForexDefinition does not provide useful feedback in failure cases
* New Feature
    * [PLAT-6176] - Function that provides cashflow information for bonds
    * [PLAT-6344] - Pricing and risk functions for TRS
    * [PLAT-6345] - Add example TRS views and relevant configs to examples-simulated
    * [PLAT-6361] - add a getInstance() in BondCapitalIndexedTransactionDiscountingMethod
    * [PLAT-6362] - add a PresentValueCurveSensitivityDiscountingInflationCalculator using a curve object with issuer 
    * [PLAT-6368] - finite difference calculator bumping both issuer and inflation curve
* Task
    * [PLAT-6181] - Tool to take online/hot backup of HSQL database
    * [PLAT-6383] - Add support for fixed payments to coupon notional visitor
    * [PLAT-6384] - Correct expiry convention for TWSE
    * [PLAT-6404] - Implement PermissionCheckProvider 
    * [PLAT-6406] - Add value theta and vega functions for equity future option
    * [PLAT-6411] - Fix SABR views in examples


To 2.2.0-M19
------------

* Any tool implementations relying on PortfolioWriter, PortfolioReader will
need refactoring due to changes made for PLAT-6304:
    * PortfolioReader was renamed to PositionReader
    * PortfolioWriter was renamed to PositionWriter
    * PortfolioTool was simplified as per the JIRA above

* Security manager and user database
    * Various minor fixes to M18 release
    * It is recommended to upgrade from M18 to M19
    * The ability to run in permissive security mode is also better supported, see M18 notes
* Bug
    * [PLAT-6385] - Fixing period start date not adjusted for fixing calendar in ibor coupons
    * [PLAT-6390] - FutureConverter fails for InterestRateFutures
    * [PLAT-6392] - Accrual end period adjustment not applied for IMM swaps
    * [PLAT-6395] - CurveNodeCurrencyVisitor: bill node refers to bondsNodeId
    * [PLAT-6399] - CurveNodeIdMapperBuilder does not handel calendarSwapIds
    * [PLAT-6400] - Running views create permission error
    * [PLAT-6401] - Payment date for notional exchange swap cashflow not adjusted
* Improvement
    * [PLAT-6391] - Create rates trade converters to replace functionality from FutureTradeConverter
    * [PLAT-6393] - Enhance component-based configuration
    * [PLAT-6394] - Reduce connections used in example servers
    * [PLAT-6396] - Add vol weighted calculation on absolute returns
    * [PLAT-6402] - Wire in support for exotic daycounts for swaps
* New Feature
    * [PLAT-6358] - OG-Analytics: Pricing DSF by discounting
* Task
    * [PLAT-6167] - Add Permissions field to ManageableSecurity and ManageableHistoricalTimeSeries
    * [PLAT-6379] - OG-Analytics: end-to-end test for STIR futures


To 2.2.0-M18
------------

Security manager and user database [PLAT-6341, PLAT-6348]
  The user source and master has been replaced with a new implementation.
  No backwards compatibility was maintained (as the old code was not in active use).
  WARNING: The database migration code deletes the old database tables and replaces them with different tables.
  The Eclipse based security system has been replaced with Apache Shiro.
  By default, the system will start with a permissive security manager and a console warning.
  Add security to your system as follows:

  In the INI file, add:
 MANAGER.INCLUDE = classpath:common/common-shiro.ini
  before:
 MANAGER.INCLUDE = classpath:common/common-dbmasters.ini

  In the INI file, change the start of the [webBasics] section to be:
 [webBasics]
 factory = com.opengamma.component.factory.web.WebsiteBasicsComponentFactory
 configMaster = ::central
 userMaster = ::central
 passwordService = ::main

  In the config properties file, add:
 shiro.hashIterations = <an integer number of your choosing, say between 4,000 and 10,000>
 shiro.cryptoSalt = <a string phrase of your choosing, say between 8 and 30 characters>

 If you want to run with open permissive security, then add
 MANAGER.INCLUDE = classpath:common/common-shiro-permissive.ini
  before:
 MANAGER.INCLUDE = classpath:common/common-dbmasters.ini

Configuration compatibility
- No changes

Database compatibility
- No upgrade required

API compatibility
- [PLAT-5535] OG-Analytics: InterestRateFutureSecurityDefinition now implements FuturesSecurityDefinition. The toDerivative method does not take a 
  Double data anymore; it was suppose to represent the last settlement price, but this is handled at the "Transaction" level. InterestRateFutureTransactionDefinition 
  now implements FuturesTransactionDefinition.
- [PLAT-6333] OG-Analytics: InterestRateFutureOptionMarginXXX now extends FuturesXXX.

Analytics compatibility
- No expected differences


* Bug
    * [PLAT-5904] - Bill market data not available for curve construction
    * [PLAT-5911] - CurveNodeCurrencyVisitor in visitVanillaIborLegRollDateConvention
    * [PLAT-6323] - IntrinsicIndexDataBundle with BitSet
    * [PLAT-6324] - Handle Large index in IntrinsicIndexDataBundle
    * [PLAT-6328] - index factor with defaulted names in IntrinsicIndexDataBundle
    * [PLAT-6340] - Errors in CAsynchronousTest
    * [PLAT-6347] - Timeseries load failing
    * [PLAT-6353] - Credit curve attempts to calibrate off a single spread in some cases
    * [PLAT-6354] - ISDACreditSpreadCurveFunction scales market spreads up 10,000
    * [PLAT-6360] - Error returned for short maturity in AnalyticBondPricer
    * [PLAT-6365] - ParameterSensitivityMulticurveUnderlyingMatrixCalculator: matrix for par curves incorrect
* Improvement
    * [PLAT-4530] - OG-Financial: SwapSecurityConverter uses Ibor leg convention when it requires only index convention
    * [PLAT-4533] - OG-Financial: InterestRateFutureSecurity converter and conventions
    * [PLAT-4589] - OG-Analytics: AnnuityDefinitionBuilder: method with stub
    * [PLAT-5123] - XCcy swap node for curve calibration
    * [PLAT-5394] - IndexIbor items in database
    * [PLAT-5535] - OG-Analytics: Generic method for interest rate futures
    * [PLAT-6293] - SwaptionSecurityConverter needs to allow for Swaptions using InterestRateSwapSecurity
    * [PLAT-6334] - Interpolator1D: Add javadoc
    * [PLAT-6342] - Add DataVersionException
    * [PLAT-6346] - InterestRateSwapConverter should return more specific type than SwapDefinition
    * [PLAT-6326] - Enhance API of FudgeResponse
    * [PLAT-6341] - Refactor model for user source/master
* New Feature
    * [PLAT-6331] - OG-Analytics: Create a BondFutures option pricing with Black expiry/strike
    * [PLAT-6333] - OG-Analytics: Create a STIRFutures option pricing with Black expiry/strike
    * [PLAT-6338] - OG-Analytics: Create an object for Black bond futures sensititivies (vega)
    * [PLAT-6348] - Add Apache Shiro for authn/authz
    * [PLAT-6355] - add tests for jacobian matrix in MulticurveBuildingDiscountingDiscountUSDSpreadTest
    * [PLAT-6356] - add function getCurve and getFixedCurve in YieldAndDiscountAddZeroFixedCurve
    * [PLAT-6357] - add function getCurve and getFixedCurve in YieldAndDiscountAddZeroSpreadCurve
    * [PLAT-6372] - change structure of InflationIssuerProvider : make it a subclass of ParameterInflationProviderInterface
    * [PLAT-6373] - add new data and function to the class InflationProviderDiscount
    * [PLAT-6374] - new test on sensitivity for capital index bonds using issuer curves 
* Task
    * [PLAT-6140] - More tests for ISDACompliantCreditCurveBuilder
    * [PLAT-6204] - add uk linked gilt to yield convention
    * [PLAT-6216] - OG-Financial : add convention for US, NL, KY, JE for inflation bond


To 2.2.0-M17
------------

** Bug
    * [PLAT-5965] - Trade loading should go through the exact same code paths no matter if UI or command line is used
    * [PLAT-5997] - Bucketed PV01 for multi-currency views do not add
    * [PLAT-6060] - Tenor-based calibrated curve shocks do not hit the nodal points correctly.
    * [PLAT-6113] - Bucketed PV01 with wrong currency in browser UI
    * [PLAT-6243] - test broke in index bond
    * [PLAT-6265] - Inflation linked bonds not loading in 'new UI'
    * [PLAT-6273] - OG-Analytics: ProviderUtils.mergeDiscountingProviders does not merge FXMatrix properly.
    * [PLAT-6292] - OG-Analytics: FXMatrixUtils.merge: handle the case of empty matrix.
    * [PLAT-6314] - Inconsistent equals and hashCode override in CreditCurveCalibrator
    * [PLAT-6315] - Swap loader does not persist ID resulting in a broken reference
    * [PLAT-6321] - Inconsistent internal state in GetTerminalValuesCallback (1)
    * [PLAT-6325] - Inconsistent internal state in GetTerminalValuesCallback (2)
* Improvement
    * [PLAT-4573] - Absence of YCNS shown as a "Missing inputs"
    * [PLAT-6242] - OG-Financial : access to the price index historical time series for index bond at the derivative level
* New Feature
    * [PLAT-6296] - OG-Analytics: New instrument: BondFutureOptionMargin
    * [PLAT-6303] - Initialise ServiceContext in pooled threads before running tasks
    * [PLAT-6329] - Expose Implied Vol for Swaptions under SABR
    * [PLAT-6330] - Scenarios: Add test script showing BucketedPV01 example
* Task
    * [PLAT-6139] - More tests for IMMDateLogic
    * [PLAT-6234] - add last known index fixing for capital index bond
    * [PLAT-6281] - Specify point shifts on fitted curves using point index instead of tenor
    * [PLAT-6286] - Parspread market quote curve sensitivity calculators with issuer for inflation instrument 
    * [PLAT-6291] - OG-Analytics: Create standard data tests for USD swaption under SABR
    * [PLAT-6295] - OG-Financial : add InflationIssuerCurveTypeConfiguration in curveTypeConfiguration
    * [PLAT-6302] - Refactor swap coupon conversion allowing for more exotic daycounts
    * [PLAT-6319] - access to days to settlement at inflationBondSecurity level  
    * [PLAT-6320] - access to the interpolation method at inflationBondSecurity level
* Epic
    * [PLAT-6275] - develop a new calibration including an issuer in the curve bundle and integrat it in og-financial


To 2.2.0-M16
------------

* Bug
    * [PLAT-1665] - The order of SABR parameters should always be: alpha, beta, rho, nu
    * [PLAT-5989] - Swap leg details throw error when Frequency is set to Never
    * [PLAT-6002] - YCNS returns only one sensitivity by curve.
    * [PLAT-6221] - Currency order incorrect in FXMatrixFunction
    * [PLAT-6226] - OG-Financial: RawVolatilityCubeDataFunction throws cast error
    * [PLAT-6252] - Certain values fail to persist in snapshot if they are missing
    * [PLAT-6254] - YCNS on a cross-currency swap only includes sensitivities in one currency
    * [PLAT-6256] - fix test for capital index bond
    * [PLAT-6262] - Multiple nodes in graph instead of single node with multiple outputs
    * [PLAT-6263] - BondAndBondFutureTradeWithEntityConverter uses int field for quantity
    * [PLAT-6264] - FXMatrixFunction should delegate FX rate manipulation to CurrencyMatrixFunctions
    * [PLAT-6284] - When opening twice the same view with sligtly different settings, the view blink
* Improvement
    * [PLAT-6136] - Improve AbstractTool option parsing printout
    * [PLAT-6250] - Add swap converter that takes fees into account
    * [PLAT-6260] - Add configuration object to represent an adjustment to apply over a date range
    * [PLAT-6261] - Configuration object to represent a set of dates
    * [PLAT-6277] - Swap leg details - typo
    * [PLAT-6289] - Add target uniqueid to logging MDC on function invocation
 New Feature
    * [PLAT-6251] - Greeks for volatility swap
    * [PLAT-6270] - Add allSuccessful(...) method to Result
    * [PLAT-6274] - OG-Analytics: FXMatrix: create a semi-smart merge tool
* Task
    * [PLAT-5882] - Wire up cashflow visitors for new coupon types & add curve provider capable visitors
    * [PLAT-6074] - More tests for ISDACompliantCurve
    * [PLAT-6138] - More tests for DoublesScheduleGenerator
    * [PLAT-6213] - SimulationUtils.patternForGlob needs to be public
    * [PLAT-6259] - Newly issued volatility swap needs to be covered
    * [PLAT-6269] - Refactor variable rate object to key from date rather than period.


To 2.2.0-M15
------------

* Bug
    * [PLAT-2277] - Automatic view update does not work always
    * [PLAT-4872] - ExampleLiveDataServer not properly validating return values
    * [PLAT-6084] - Act/Act ICMA throws error when used in a swap
    * [PLAT-6118] - SwapSecurityConverter does not use IborLeg convention
    * [PLAT-6172] - NPE in SwapLegDetailFormatter
    * [PLAT-6174] - Joda bean / Fudge encoding can't handle Collection<Failure> in FailureResult
    * [PLAT-6177] - Snapshot not appearing in 'green screens'
    * [PLAT-6178] - DiscountingInterpolatedPV01Function should override parents execute()
    * [PLAT-6180] - MarketSnapshotImportTool does not load xls snapshots properly
    * [PLAT-6197] - SABR Surfaces requirement in SABRDiscountingFunction need not have currency
    * [PLAT-6203] - CurveSpecification fudge builder not handling certain FXForwardNodes
    * [PLAT-6208] - Pillar year fractions are not relative to curve date in interpolated curves
* Improvement
    * [PLAT-4532] - OG-Financial: Interest Rate Security converter using hardcoded conventions
    * [PLAT-5978] - PresentValueMarketQuoteSensitivityDiscountingCalculator: Improve method for CompoundingFlat
    * [PLAT-6137] - Extract common data from web resources
    * [PLAT-6171] - ExampleLiveDataServer to read scalingFactor and maxMillisBetweenTicks values from config property file
    * [PLAT-6191] - FXMatrixFunction: return null exception is currency pair not available
    * [PLAT-6198] - Platform extremely slow of simple portoflios
    * [PLAT-6199] - FailureStatus value indicating a missing value for a non-nullable parameter
    * [PLAT-6209] - Cache latest versions in AbstractEHCachingSourceWithExternalBundle
    * [PLAT-6230] - Improve support for payment lags (mainly for OIS swaps)
* New Feature
    * [PLAT-5988] - Currency Exposure result should be available for all instruments
    * [PLAT-6164] - Add DiscountingInterpolatedParRateFunction
    * [PLAT-6168] - Add PeriodicallyCompoundedRateNode curve node
    * [PLAT-6193] - Adding support for CouponONArithmeticAverageSpread
    * [PLAT-6206] - Implement SABR Pricing from Swaption VolCube and FwdSurface with(out) Snapshots
* Task
    * [PLAT-5642] - Create CAD standard curves
    * [PLAT-5677] - OG-Analytics: Create standard set of data for test - USD rates
    * [PLAT-6169] - Anotate ManageableSecurity with type information 


To 2.2.0-M14
------------
- PLAT-5866.
  Volatility cubes have been refactored.
  Configuration for VolatilityCubeDefinitionSourceComponentFactory needs to remove "bloomberg=..." line.

* Bug
    * [PLAT-5325] - Custom Fudge builder needed for FunctionResult implementations
    * [PLAT-6109] - Swaps PV01 not working as requirements insufficient to build the derivative
    * [PLAT-6110] - JACOBIAN_BUNDLE removed from MultiCurvePricingFunction
    * [PLAT-6125] - regression-toolcontext.ini uses old security master
    * [PLAT-6126] - Golden copy capture tool assumes a reference data provider exists
    * [PLAT-6146] - Fix pricing bond futures with expiry after bond effective date
    * [PLAT-6155] - ExampleLiveDataServer breaks when subscriptions are missing in market-data.csv
    * [PLAT-6158] - Accrual period end date not adjusted in fixed annuity zero coupon
    * [PLAT-6162] - Binary installation resources use absolute paths
    * [PLAT-6163] - Accrual period of 0 calculated for 90 day STIR future
* Improvement
    * [PLAT-5808] - Handling of nulls in SuccessResult
    * [PLAT-5864] - Make volatility cube data snapshots beans
    * [PLAT-5865] - Make volatility cube definitions beans
    * [PLAT-5866] - Make volatility cube specifications beans
    * [PLAT-6117] - AnnuityDefinitionBuilder.couponIborSpreadWithNotional add a version with annuity convention
    * [PLAT-6135] - Add validation for dual stubs inputs
    * [PLAT-6141] - Create fudge builders for new volatility cube objects
    * [PLAT-6142] - Create named instances of cube quote type and volatility quote units
    * [PLAT-6143] - Add fudge builders for surface data
    * [PLAT-6145] - WebsiteBasicsComponentFactory should not mandate that all components are supplied
    * [PLAT-6147] - Implement hashCode() and equals() for links
    * [PLAT-6165] - Use intrinsic values for truncation
* New Feature
    * [PLAT-6061] - Create example volatility cube configurations in examples-simulated
    * [PLAT-6062] - Add a generic surface specification
    * [PLAT-6063] - Add a generic surface definition
    * [PLAT-6064] - Add a generic surface snapshot data object
    * [PLAT-6065] - Create a function that manipulates expiry / maturity / relative strike cubes into the form expected by the analytics library
    * [PLAT-6066] - Create a function that manipulates expiry / maturity / moneyness cubes into the form expected by the analytics library
    * [PLAT-6067] - Add a forward swap rate surface function and surface instrument provider
    * [PLAT-6068] - Add forward swap rate configurations to examples-simulated
    * [PLAT-6069] - Update non-linear SABR function to use new volatility cube and forward swap rate surface configurations
    * [PLAT-6103] - Create a data provider for the Carr-Lee model
    * [PLAT-6104] - Write Carr-Lee calculators that are in the same hierarchy as other pricing models.
    * [PLAT-6129] - Create bond trs analytics objects
    * [PLAT-6130] - Create equity TRS analytics objects
    * [PLAT-6131] - Add new objects to visitors
* Task
    * [PLAT-6075] - More tests for CDSCoupon
    * [PLAT-6077] - More tests for SimpleCreditCurveBuilder
    * [PLAT-6078] - More tests for ISDACompliantYieldCurve
    * [PLAT-6120] - More tests for ISDACompliantCreditCurve
    * [PLAT-6121] - More tests for AnnuityForSpreadFunction
* Sub-task
    * [PLAT-6048] - Configure WebPositionResources to use combined PositionMaster


To 2.2.0-M13
------------
- PLAT-6137 - AbstractPerRequestWebResource has been refactored to extract common code requiring changes in subclasses.
      If you have your own web resource class, you need to remove setUriInfo(), remove data(), create an empty
      data object in the constructor and pass it to the superclass, remove the instance variable for data, and
      make all data classes extend WebPerRequestData.


To 2.2.0-M13
------------
- PLAT-6049 - CombinedPositionMasterComponentFactory to use arbitrary number of masters
      This change will require modifications to server configuration files(.ini). It requires a default position master and a list of other masters that will be combined into a single positionmaster
      e.g
      [combinedPositionMaster]
      factory = com.opengamma.component.factory.master.CombinedPositionMasterComponentFactory
      classifier = combined
      defaultPositionMaster = ::central
      positionMaster0 = PositionMaster::user
      .
      .
      positionMasterN = PositionMaster::<classifier>

* Bug
    * [PLAT-6035] - Version on Web About page is incorrect
    * [PLAT-6036] - Add Joda-Beans mime type
    * [PLAT-6037] - FixingYearFractionsVisitor fixes for fixed coupon payments
    * [PLAT-6038] - Add backward compatible ibor convention lookup to ForwardRateAgreementSecurity
    * [PLAT-6041] - some configs can't be overriden through env variables in the start scripts
    * [PLAT-6042] - ManageablePosition clone method doesnt not do deep copy as specified
    * [PLAT-6053] - Simulation API does not allow creation of an empty (base) scenario
    * [PLAT-6086] - Can't serialize FailureResult with an exception
    * [PLAT-6091] - OG-Financial : index curve construction : the wrong name is used for the index 
    * [PLAT-6092] - OG-Financial : security converter : the wrong name is used for the index 
    * [PLAT-6101] - exogenous curves are not working in ui environment
    * [PLAT-6102] - quant sandbox : use the new forward definition 
* Improvement
    * [PLAT-5933] - MarketDataSnapshotTool should allow the market data source to be specified
    * [PLAT-6034] - Create XML view of positions in WebUI
    * [PLAT-6045] - OG-Financial : in FixedIncomeConverterDataProvider, converters are not working properly for inflation swaps
    * [PLAT-6076] - Add propagateFailure() method to Result
    * [PLAT-6079] - Move map() method from ResultGenerator to Result
    * [PLAT-6080] - AbstractRestfulJmsResultConsumer should be generic wrt Listener type
    * [PLAT-6093] - Add FailureStatus.PENDING_DATA
* New Feature
    * [PLAT-5576] - Carr-Lee model for FX volatility swaps
    * [PLAT-5629] - OG-Financial: Black Swaption - vega to node points
    * [PLAT-6017] - Ability to specify scenario output files as CSV
    * [PLAT-6021] - Converter from volatility swap securities to definitions
    * [PLAT-6028] - Add base integration functions for FX volatility swaps
    * [PLAT-6029] - Create a CashBalanceSecurity
    * [PLAT-6030] - Add support for CashBalanceSecurity to visitors
    * [PLAT-6031] - Add support for CashBalanceSecurity to web UIs
    * [PLAT-6032] - Add PV functions for cash balances
    * [PLAT-6033] - FX currency exposure function for all non-FX instruments
    * [PLAT-6094] - Equity TRS security definition
    * [PLAT-6095] - Add support for equity TRS to FinancialSecurityVisitor
    * [PLAT-6096] - Add bond TRS security
    * [PLAT-6097] - Add support for bond TRS to FinancialSecurityVisitor
    * [PLAT-6105] - Point FX volatility swap functions to the new calculators in OG-Analytics
    * [PLAT-6106] - Write a realized variance function that uses historical data
* Task
    * [PLAT-5973] - More tests for CDSAnalyticFactory
    * [PLAT-6015] - Tests for InterestRateSensitivityCalculator
    * [PLAT-6016] - More tests for ISDACompliantDateCurve
    * [PLAT-6039] - OG-Financial : add inflationSwapConverter in MultiCurvePricingFunction 
    * [PLAT-6040] - add zero coupon inflation swap in InterestRateInstrumentType
    * [PLAT-6044] - in FixedIncomeConverterDataProvider change DAYS_BEFORE_EFFECTIVE
    * [PLAT-6050] - More tests for MarketQuoteConverter
    * [PLAT-6051] - More tests for ISDACompliantDateYieldCurve
    * [PLAT-6052] - More tests for ISDACompliantDateCreditCurve
    * [PLAT-6054] - OG-Financial : add ZERO_COUPON_INFLATION_SWAP in canApplyTo in the YieldCurveNodePnLFunction
    * [PLAT-6055] - OG-Financial : add zero coupon inflation swap to SwapSecurityUtils
    * [PLAT-6057] - Upgrade to Joda-Beans 0.9.7
    * [PLAT-6089] - Tests for Carr-Lee volatility swap calculator
* Sub-task
    * [PLAT-6049] - Modify CombinedPositionMasterComponentFactory to use arbitrary number of masters


To 2.2.0-M12
------------
- [PLAT-5744] Server needs a restart to pick up new timeseries
    The fix requires to configure viewProcessorManager in spring config file. It is required to add new item into the masters property.
    The item should be:
         <bean class=" com.opengamma.financial.timeseries.HistoricalTimeSeriesSourceChangeProvider">
           <constructor-arg ref="[HISTORICAL_TIME_SERIES_SOURCE]" />
         </bean>
    where [HISTORICAL_TIME_SERIES_SOURCE] should be appropriate historical time series source.

* Bug
    * [PLAT-5744] - Server needs a restart to pick up new timeseries
    * [PLAT-5959] - MarketDataSnapshotTool should allow a timeout to be specified
    * [PLAT-5961] - InMemoryLKVLiveMarketDataProvider only removes the value specification from the underlying cache which causes a full unsubscription
    * [PLAT-5977] - Fix valuation time handling in swap detail visitors
    * [PLAT-5990] - Views change behavior by adding columns
    * [PLAT-6004] - getResults in InflationProviderDiscountingFunction is not calling the curve properly
    * [PLAT-6005] - CurveNodeCurrencyVisitor.visitCashNode should throw an exception clearly stating which security is missing from the security source
    * [PLAT-6007] - OG-Language Loader for security functions replaces the config source with a remote implementation that uses the security source URI, uses multiple function providers unnecessarily and adds some functions twice
    * [PLAT-6018] - Position name is not included in scenario output
    * [PLAT-6023] - VarianceSwapDefinitionTest depends on current date/time
* Improvement
    * [PLAT-5809] - FailureResult doesn't have anywhere to store exceptions
    * [PLAT-5960] - add a new timeCalculator using bus/252 and use it in the "brazilian" instruments
    * [PLAT-6020] - 'Conventions' green screen lacks a search by id field
    * [PLAT-6024] - Allow aliasing of column names in SimpleResultBuilder
* New Feature
    * [PLAT-2717] - Create FX Volatility Swap Security
    * [PLAT-5662] - Add min/max functionality to dsl
    * [PLAT-5752] - Support for bills 
    * [PLAT-5753] - New curve node:bill
    * [PLAT-5802] - Test bill node converter
    * [PLAT-5804] - Test bill security converter
    * [PLAT-5967] - Create swap index
    * [PLAT-5985] - Support for SwapIndex
    * [PLAT-5986] - Add support for PriceIndex
    * [PLAT-5998] - Add an expiry calculator that adjusts to a certain number of working days before the end of the month
    * [PLAT-5999] - Volatility swap definition and derivative in OG-Analytics
* Epic
    * [PLAT-1671] - Review database schemas and access methods
* Task
    * [PLAT-5930] - More tests for FiniteDifferenceSpreadSensitivityCalculator
    * [PLAT-5972] - More tests for MultiAnalyticCDSPricer
* Sub-task
    * [PLAT-5769] - If client owns JVM start token, don't issue restart on first failure
    * [PLAT-5969] - Add uniqueIdScheme  search property to SearchRequest
    * [PLAT-5991] - Create DelegatingSecurityMaster and its component factory
    * [PLAT-5992] - Add uniqueIdScheme request property to AbstractMetaDataRequest
    * [PLAT-5994] - Modify Web security resources to work with delegate security master


To 2.2.0-M11
------------
** Bug
    * [PLAT-5252] - Value of optional constraints on view definitions are lost after saving
    * [PLAT-5699] - Error pricing a bond with new curves in Bloomberg examples
    * [PLAT-5718] - Market data scaling in the scenario DSL should be consistent with curve scaling
    * [PLAT-5822] - Bloomberg installation with blank db will create a growing number of portfolios
    * [PLAT-5849] - Leg details on xccy swaps return blank cell
    * [PLAT-5850] - Cross currency swaps do not include notional payment for PV and bucketed PV01 calcs
    * [PLAT-5851] - Currency conversion in PV01 xccy swap seems wrong
    * [PLAT-5855] - Z-spreads are wrong with the new curve configuration
    * [PLAT-5857] - Supranational bonds throw an exception when calculating Duration and Convexity
    * [PLAT-5885] - Normalization of dirty price is wrong
    * [PLAT-5889] - Bill Bloomberg Loader: Incorrect Issuer name
    * [PLAT-5895] - Deadlock during view processor suspension
    * [PLAT-5901] - CurveNodeWithIdentifierBuilder: incorrect visitBillNode
    * [PLAT-5907] - FRASecurityConverter uses the number of whole months between start and end as the FRA tenor
    * [PLAT-5915] - Enhance ServiceContext and fix test failues
    * [PLAT-5917] - ToolContextUtils does not respect requested type
    * [PLAT-5928] - Hitting Save on a new trade does not close the trade screen
    * [PLAT-5944] - Region error for bond
    * [PLAT-5947] - InMemoryLKVLiveMarketDataProvider drops live data subscriptions when fully qualified live data specs collide with requested live data specs
    * [PLAT-5952] - Correct swap fixed rate display value
    * [PLAT-5957] - Bond loader is broken
    * [PLAT-5958] - StandardLiveDataServer only sends one response when subscription request contains multiple subscriptions which alias to the same fully-qualified specification
* Improvement
    * [PLAT-4539] - Upgrade ViewProcess MXBeans to have stats on successful calcs
    * [PLAT-5678] - Syntax for shifting surfaces should be similar to curves
    * [PLAT-5874] - Add OG-Language function for calculating an absolute date from a relative date and the conventions on an index
    * [PLAT-5875] - Add OG-Language function to create a FRA using an index to populate the majority of fields
    * [PLAT-5894] - Change xml editor to ace (http://ace.c9.io/) in green screens
    * [PLAT-5900] - Add Fed Fund Futures to InterestRateInstrumentType
    * [PLAT-5906] - Consistent semantics of shifts / scaling in scenarios
    * [PLAT-5909] - OG-Web: Change 'Calendars' to 'Holidays' in menu
    * [PLAT-5910] - Remove deprecated Guava makeComputingMap
    * [PLAT-5921] - Add currency property to bond PV01 and gamma PV01 functions
    * [PLAT-5922] - Catch case where bond / bill is not in security master when populating the curve market data snapshot
    * [PLAT-5926] - Add discounted payment amounts to swap details
    * [PLAT-5935] - Relative yield curve parallel shifts in scenarios
    * [PLAT-5941] - Add security data to scenario output format
    * [PLAT-5943] - Add the ability to specify whether the spot rate node or a market data ticker is used as the spot rate in FX forward curves.
    * [PLAT-5953] - Add yield conventions for MX bonds
    * [PLAT-5954] - Filter scenario report so it only includes positions
* New Feature
    * [PLAT-4579] - Adaptive mesh method
    * [PLAT-4580] - Implied tree model
    * [PLAT-5475] - Tool to load single config item either JodaXML or FudgeXML
    * [PLAT-5562] - FX volatility swap database support
    * [PLAT-5617] - Add support for new curves to the tool that creates time series
    * [PLAT-5637] - Database support for AmericanDepositaryReceiptSecurity
    * [PLAT-5638] - Database support for ExchangeTradedFundSecurity
    * [PLAT-5639] - Database support for EquityWarrantSecurity
    * [PLAT-5759] - Truncation of binomial tree
    * [PLAT-5781] - Run scenarios from a stand-alone script
    * [PLAT-5782] - Output view results an a text file for consumption by Excel
    * [PLAT-5846] - Add build number to Jax/about
    * [PLAT-5854] - Cross currency fixed for fixed swap loader
    * [PLAT-5869] - Add a floating rate note security
    * [PLAT-5870] - Add support for FRNs to visitors
    * [PLAT-5871] - Add support for FRNs in the web interfaces
    * [PLAT-5872] - Load FRNs as FloatingRateNoteSecurity rather than as a fixed-coupon bond in BondLoader
    * [PLAT-5887] - Tool for running stand-alone scenario scripts
    * [PLAT-5903] - Tool to export conventions in structured zip file
    * [PLAT-5913] - Create a script that given a text file with a list of identifiers it will load them as securities
    * [PLAT-5916] - Add ability to display FX volatility swaps in the web UIs
    * [PLAT-5936] - Add capability to load indices and their families in the config import tool
    * [PLAT-5940] - Ability to include Yield Convention and Coupon Type attributes for bonds
* Task
    * [PLAT-4729] - More tests for trinomial option pricing model
    * [PLAT-5897] - Add fudge builder for ConventionType
    * [PLAT-5898] - Create tool to allow export of all conventions in a ZIP file in JodaXML
    * [PLAT-5908] - Upgrade to Paranamer 2.6
    * [PLAT-5912] - Change Bloomberg Examples to new Bean Master


To 2.2.0-M10
------------
API compatibility
- [PLAT-5380] - Implement LegalEntities domain data model.
  Domain object model com.opengamma.core.organization replaced by com.opengamma.core.legalentity
  Organisation master from com.opengamma.master.orgs,com.opengamma.master.organisation,com.opengamma.masterdb.orgs replaced by
  Legal entity master from com.opengamma.master.legalentity,com.opengamma.masterdb.legalentity


Configuration compatibility
- [PLAT-5380] - Implement LegalEntities domain data model.
  In ini files replace:
      Change the classifiers of dbConnector from ::org to ::len
      DbOrganizationMasterComponentFactory with DbLegalEntityMasterComponentFactory
      OrganizationSourceComponentFactory with LegalEntitySourceComponentFactory
      EHCachingOrganizationMasterComponentFactory with EHCachingLegalEntityMasterComponentFactory
      organizationSource with legalEntitySource
      organizationMaster with legalEntityMaster
  In spring files:
    remove orgDbConnector and add lenDbConnector
    remove orgJmsConnector and add lenJmsConnector
    remove orgCacheManager and add lenCacheManager
  In property configuration files:
    replace 'org' with 'len' in value of db.schemaNames property



* Source compatibility
  * [PLAT-5000] Remove Map as super-interface of Map2. Required for JDK 8 compatibility.

* Bug
    * [PLAT-5103] - Jar version in .classpath differs from POM version
    * [PLAT-5767] - If back-end server is down at start up, service is active but doesn't respond
    * [PLAT-5793] - Volatility should be correctly annualised in Carr-Lee model
    * [PLAT-5820] - Present Value should return Present Value even if the trade has more than one currency
    * [PLAT-5821] - Add FX present value to list of requirements in multi-curve defaults function
    * [PLAT-5824] - Loading a bond throws an error - it recognizes it as a bill
    * [PLAT-5828] - Remote referencing of legal entity source fails
    * [PLAT-5830] - MarkToMarketPnLFunction does not throw an exception if the trade type property is not set when execute() is reached.
    * [PLAT-5843] - error in quant sanbox due to some changes in MultiCurveInterface
* Improvement
    * [PLAT-5721] - Integrator for Carr-Lee model
    * [PLAT-5762] - Compute assetPrice at expiry in OptionFunctionProvider
    * [PLAT-5818] - Functions to access a config-source
    * [PLAT-5823] - Replace ExternalIdBundleFunction with a type converter
    * [PLAT-5835] - ISINFunction: Clean up target and move to SecurityFunctions
    * [PLAT-5840] - Add ability to create/modify securities by providing xml definitions in WebUI
    * [PLAT-5841] - Allow z spreads to be used in merged outputs
    * [PLAT-5884] - Base asset price at expiry for trinomial tree
    * [PLAT-5888] - Notional  is not working for Fra
    * [PLAT-5890] - Remove System.out from BeanCompare
* New Feature
    * [PLAT-5715] - Document Deployment Template
    * [PLAT-5836] - Add SimpleRenamingFunction which adds alias for single ValueRequirementName
* Task
    * [PLAT-5000] - Ensure OpenGamma runs on JDK8
    * [PLAT-5811] - implement consistent forward formula for couponONCompoundedDiscountingMethod
    * [PLAT-5812] - have a more general implementation of the function parameterForwardSensitivity in Multicurve provider interface
    * [PLAT-5829] - add function to get the annually compounded forward in multicurveproviderinterface
    * [PLAT-5837] - Add EquityBlackVolatilitySurfaceFromSinglePriceFunction to BlackFunctions Repo
    * [PLAT-5838] - Add Cash, Equity, and EquityOption Securities to NotionalVisitor
    * [PLAT-5848] - Add separate converter for ForwardRateAgreement 
    * [PLAT-5859] - Remove BeanBuilderHack
    * [PLAT-5861] - Add type querying method to LegalEntityFilter
    * [PLAT-5863] - handle CouponFixedCompounding in CouponPaymentVisitor
    * [PLAT-5867] - handle CouponFixedCompounding in CouponFixedRateVisitor
    * [PLAT-5881] - Upgrade to Joda-Beans 0.9.6
* Sub-task
    * [PLAT-5672] - Improve response time of svcStart method
    * [PLAT-5673] - Improve response time of svcAccept
    * [PLAT-5892] - og-financial : create fudge builder for both sub class


To 2.2.0-M9
-----------
* Database Schemas
  * sec - security master tables will need upgrading from V67 to V68 either manually or using DB upgrade tool.
* Scenario support
  * [PLAT-4100] - Scenario support for raw curve data. YieldCurveSpecificationFunction renamed YieldCurveDataFunction
* Error codes
  * [PLAT-5792] - All tools now return 0 on success and -1 or -2 on failure if run via the main method

* Bug
    * [PLAT-1973] - Component loader: if a property being passed to a String field is undefined then the text referencing the property in the ini file is injected instead
    * [PLAT-3723] - Exceptions thrown during component factory initialisation don't halt the process
    * [PLAT-5744] - Server needs a restart to pick up new timeseries
    * [PLAT-5771] - Fix initial rate on ZC floating leg
    * [PLAT-5772] - ConfigSearchRequest with null type fails
    * [PLAT-5779] - MultiYieldCurveParRateMethodSeriesFunction throws NPE when starting the examples server
    * [PLAT-5783] - ScheduleCalculator generates empty schedule when frequency is greater than end date
    * [PLAT-5786] - FlexiBeanFudgeBuilder does not work correctly
    * [PLAT-5788] - Correct toDerivative() call in FixedIncomeDataProvider
    * [PLAT-5817] - Regression framework does not support capture of reference data
* Improvement
    * [PLAT-3012] - NPE if tool context file passed to a tool does not exist
    * [PLAT-4766] - Servers should publish their code/build version numbers
    * [PLAT-4890] - Component Repository requires an MBean server
    * [PLAT-5377] - OG-Financial: CurveNodeToDefinitionConverterTest - Holiday and Region sources
    * [PLAT-5644] - Refactor regression tool context configs so common across all tests
    * [PLAT-5735] - Set up development environment to extend development platform 
    * [PLAT-5740] - add function getinvestmentfactor in MulticurveProviderInterface
    * [PLAT-5776] - Add ability to enable JMS connectivity for db masters via a flag
    * [PLAT-5784] - Tidy up interpolated stub coupon code in FloatingAnnuityDefinitionBuilder
    * [PLAT-5785] - Remove commented blocks from new annuity builders
    * [PLAT-5790] - System.exit can fail to exit the JVM
    * [PLAT-5792] - Tools should check for failure on startup and exit
    * [PLAT-5794] - Add component factory for security bean master
* New Feature
    * [PLAT-4578] - Flexible binomial tree model
    * [PLAT-5497] - Renderer for YieldCurveData
    * [PLAT-5570] - DB Dump Tool doesn't capture ConventionMaster data
    * [PLAT-5595] - Database persistence for ibor index definitions
    * [PLAT-5596] - Database persistence for overnight index definitions
    * [PLAT-5597] - Database persistence for equity index components
    * [PLAT-5598] - Database persisntence for equity index definitions
    * [PLAT-5601] - Database persistence for bond index components
    * [PLAT-5602] - Database persistence for bond index definitions
    * [PLAT-5711] - Move config as sibling of project
    * [PLAT-5714] - Ensure extended engine functions can be used in deployment template 
    * [PLAT-5756] - Smoothing technique in binomial model
    * [PLAT-5773] - Prioritization/Weighting of Lifecycle start()/stop() operations
    * [PLAT-5797] - Bill nodes for curves
    * [PLAT-5798] - Add support for bills in curve node id mapper
    * [PLAT-5799] - Add support for bill nodes in node visitor
    * [PLAT-5800] - Create a bill security
    * [PLAT-5801] - Write a bill node converter.
    * [PLAT-5803] - Write a bill security converter
    * [PLAT-5805] - Write a Bloomberg bill security loader
    * [PLAT-5806] - Add support for bills to financial security visitor
    * [PLAT-5814] - Web UI support for BillSecurity
    * [PLAT-5816] - Add bill securities to appropriate integration functions
* Task
    * [PLAT-5704] - match input curve using DayPeriodPreCalculatedDiscountCurve
    * [PLAT-5777] - add the daycount act/365.25 to the file daycounts
    * [PLAT-5819] - Upgrade to Joda-Beans 0.9.4


To 2.2.0-M8
-----------
* Bug
  * [PLAT-3940] - @ExternalFunction annotations not picked up
  * [PLAT-5491] - Scripts can't be run from different directory (windows)
  * [PLAT-5529] - CDS Par Spread is incorrect
  * [PLAT-5536] - DbDateUtilsTest fails in Australia
  * [PLAT-5648] - Maven site stopped building
  * [PLAT-5686] - The dep graph debugger does not work on windows install
  * [PLAT-5694] - Fix the Java API for specifying point shifts and bucketed shifts for curves
  * [PLAT-5695] - Non-stub schedule with EOM adjuster produces 0 day (start date = end date) flow
  * [PLAT-5698] - Spot rate scaling in the scenario DSL should be consistent with curve scaling
  * [PLAT-5706] - Force annuity dates to start of day so that payments on val date are not included
  * [PLAT-5723] - FXForwardCurrencyExposurePnLFunction does not perform a currency conversion if the result currency is the base currency
  * [PLAT-5750] - Add spotRate() method to Scenario
  * [PLAT-5755] - Floating swap leg formatter accrual fraction column shows all elements of the array
  * [PLAT-5763] - Fix index lookup for new FRA security
  * [PLAT-5765] - Ignore signed notionals on new annuity builder, pay/receive flag should take precedence
  * [PLAT-5766] - Check for null compound method when calling isCompounding in new annuity builder
* Improvement
  * [PLAT-3451] - OG-Financial: allow the "convexity adjustment" for bond futures
  * [PLAT-4956] - Migration of data in BeanMaster
  * [PLAT-5073] - VOD calculation for CDS
  * [PLAT-5668] - Dividend in Carr-Lee model
  * [PLAT-5726] - Add bond securities to types handled in BondFutureConstantSpreadThetaFunction and rename
  * [PLAT-5732] - use the daycount of the index in CouponONCompoundedDefinition
  * [PLAT-5733] - example of curve construction SwapFixedCompoundedONCompounded using a MulticurveProviderForward
  * [PLAT-5734] - remove _fixingPeriodAccrualFactorsActAct from CouponONCompounded and CouponONCompoundedDefinition
  * [PLAT-5736] - create spring viewprocess xml path variable in properties
  * [PLAT-5737] - Add Mexican pesos to Currency
  * [PLAT-5738] - generalised the rate cut off for Overnight arithmetic average coupon
  * [PLAT-5739] - Add AUS-AONIA-OIS-COMPOUND to FloatingIndex
  * [PLAT-5741] - Change CurveGroupConfiguration.typesForCurves to use <? extends CurveTypeConfiguration>
  * [PLAT-5742] - use the daycounter business/252 in the toderive function for the files CouponFixedAccruedCompoundingDefinition, SwaptionPhysicalFixedCompoundedONCompoundedDefinition and SwaptionCashFixedCompoundedONCompoundedDefinition.
  * [PLAT-5743] - Refactor of new AnnuityDefinitionBuilders
  * [PLAT-5746] - Add support for CouponONArithmeticAverageDefinition in FloatingAnnuityDefinitionBuilder
  * [PLAT-5747] - Implement initial rate for FloatingAnnuityDefinitionBuilder
  * [PLAT-5751] - In TimeCalculator class add a function getTimeBetween using a calendar as input
* New Feature
  * [PLAT-5196] - Quantity column for bonds
  * [PLAT-5605] - Make the service console a normal window so that it can be minimized
  * [PLAT-5620] - Config env enhancements
  * [PLAT-5687] - Config upload tool should log which file is causing the error
  * [PLAT-5708] - Adaptive Quadrature Method
  * [PLAT-5712] - Add a user set property to point to FunctionConfigurationSourceComponentFactory
* Task
  * [PLAT-5565] - Check hashCode and equals for PiecewiseInterpolator1D
  * [PLAT-5703] - add tests to DayPeriodPreCalculatedDiscountCurve
  * [PLAT-5719] - Upgrade to Corporate-Parent 1.1.8
  * [PLAT-5724] - Remove BeanCompare equalsIgnoring
  * [PLAT-5725] - Check scaling and result properties on swaption black functions
* Sub-task
  * [PLAT-5670] - Share CSettings instance across all initialisation code
* Epic
  * [PLAT-5632] - Support for in-place versioning/upgrades for Joda Beans


To 2.2.0-M7
-----------

* Bug
  * [PLAT-4631] - ConfigItem instances aren't always serializable
  * [PLAT-5456] - Use last margin price in bond future conversion and remove converters that use incorrect analytics form
  * [PLAT-5494] - Currency of futures is mislabelled in securities tab
  * [PLAT-5513] - new constructor for CouponONArithmeticAverageDefinition implementing the rate cut off
  * [PLAT-5515] - The swap type returned by the swap security converter no longer matches that expected by the swaption converter
  * [PLAT-5518] - PositionGreeksFunction uses == to compare requirement names
  * [PLAT-5520] - Possible NPE in equity option functions
  * [PLAT-5527] - FailureResult cannot serialize FormattingTuple
  * [PLAT-5529] - CDS Par Spread is incorrect
  * [PLAT-5530] - Index CDS uses 105 bps as hard-coded value if market quote doesn't exist
  * [PLAT-5531] - Index CDS returns incorrect values if the market quote is not found
  * [PLAT-5555] - top level pom.xml points testng to non existant logback file
  * [PLAT-5569] - Re-pointing OG-Excel server does not restart service
  * [PLAT-5573] - Conventions with loops in cause a StackOverflowError during the compilation phase in FXMatrixFunction
  * [PLAT-5574] - OG-Financial: incorrect TS for FederalFundsFuture converter
  * [PLAT-5607] - VolatilitySurfaceManipulatorBuilder needs to be public
  * [PLAT-5608] - Cannot extract value columns into Excel from extant view
  * [PLAT-5610] - Config validator unexpected error
  * [PLAT-5611] - It should not be possible to upload malformed xml as a config
  * [PLAT-5612] - Config validator does not complain if curve has no nodes
  * [PLAT-5616] - Period calculation in scenario DSL is wrong for fitted curve and surface data
  * [PLAT-5633] - Web UI should capture ip address of client browser
  * [PLAT-5634] - Can not create snapshot
  * [PLAT-5646] - Add missing TestGroup.UNIT
  * [PLAT-5647] - Upgrade to Joda-Beans 0.9.1
  * [PLAT-5652] - Web-UI: Missing value in merged columns
  * [PLAT-5655] - Error in curve construction should be more explanatory
  * [PLAT-5657] - Add checks for null basket securities and null ISINs in bond future security converter
  * [PLAT-5658] - MarkToMarketSpotFuturesFunction can throw an NPE in the graph build phase if the superclass requirements are not satisfied
  * [PLAT-5663] - Add space support on the constraints to the dep graph debugger
  * [PLAT-5667] - Bloomberg Examples function configuration does not include CurveFunctions
  * [PLAT-5669] - AnalyticsParameterProviderBuilders: buildObject incorrect
  * [PLAT-5674] - Historical time-series rating does not have a named field on the serialized xml
  * [PLAT-5680] - NPE in integration tool context when loading LiveDataMetaDataProviders
  * [PLAT-5684] - Bond security conversion uses the first accrual date field, which can be null, without checking that it is not null
  * [PLAT-5685] - IRSSecurityConverter should use start of day instead of end of day
  * [PLAT-5692] - Bloomberg Examples function configuration does not include multi-curve pricing functions
* Improvement
  * [PLAT-1137] - ScheduleCalculator: enhance to include all conventions and stub treatment
  * [PLAT-3199] - Make sure that greeks are consistent for equity and index options, equity and index future options and commodity future options
  * [PLAT-4011] - Filter ViewClient results based on user's market data entitlements
  * [PLAT-4964] - Add support for generating unadjusted dates for dates with stubs at the start and end
  * [PLAT-4974] - Add support for forward rate interpolation when valuing stub period coupons
  * [PLAT-5006] - Ability to merge columns into a single, aliased column with common aggregates
  * [PLAT-5023] - Ability for regression testing framework to run against in-process engine
  * [PLAT-5073] - VOD calculation for CDS
  * [PLAT-5127] - Add support for stub periods when using new swap security
  * [PLAT-5242] - Snapshot time should be stored on the snapshot instance
  * [PLAT-5305] - Group config elements via config annotation and display in groups in UI menu 
  * [PLAT-5460] - Vanishing time to dividend payment in RGW model
  * [PLAT-5488] - OG-Analytics: IssuerDiscountBuildingRepository should take arrays of issuers for each curve
  * [PLAT-5514] - Utility script to collate all files of interest for error reporting
  * [PLAT-5525] - EqyOptRollGeskeWhaleyPresentValueCalculator has three largely identical methods
  * [PLAT-5526] - ListedEquityOptionRollGeskeWhaleyImpliedVolFunction contains too much logic that should be in OG-Analytics
  * [PLAT-5543] - Consider standardising DbMaster publication
  * [PLAT-5552] - OG-Financial: Bond converter should load rating
  * [PLAT-5559] - Add support for jmsMarketDataAvailabilityTopic to LiveMarketDataProviderFactoryComponentFactory
  * [PLAT-5561] - EngineDebugger should not attempt to adjust the live data provider name
  * [PLAT-5563] - The price type property is no longer used in CDS functions and should be removed
  * [PLAT-5584] - Turn down market data unsubscription logging
  * [PLAT-5606] - BucketedPV01Function should work at trade level and not only position
  * [PLAT-5618] - Disable forms authentication on /jax/components
  * [PLAT-5623] - Same username should not be able to be used from multiple ip addresses
  * [PLAT-5624] - Permissions check required when connecting to a running view
  * [PLAT-5635] - Provide access to view process that a view client is attached to
  * [PLAT-5636] - Web UI: Support starting a view by view process UniqueId
  * [PLAT-5640] - Remove deprecated web UI resources
  * [PLAT-5649] - UserEntitlementChecker should not fail unresolvable specs
  * [PLAT-5650] - Extends the set of Ibor-like conventions used in SwapSecurityConverter
  * [PLAT-5659] - Publish SecurityLoader and HTSLoader rest in OG-Bloomberg example server
  * [PLAT-5666] - Deactivate weight adjustment in Carr-Lee model
  * [PLAT-5683] - Quiet down messages from getRequirements() in bond and bond future functions
  * [PLAT-5696] - Refactor of AnnuityDefinitionBuilder
* New Feature
  * [PLAT-3706] - Duplicate current FX implied curve functionality using new curve configurations.
  * [PLAT-4416] - Create time series of Closing_Bid and Closing_Ask. Ensure displayed together
  * [PLAT-4829] - Tool for creating view regression tests
  * [PLAT-5196] - Quantity column for bonds
  * [PLAT-5451] - OG-Analytics: New instrument: AUD bond futures
  * [PLAT-5489] - Directly manipulate FX cross rates in scenarios
  * [PLAT-5496] - Importing configuration files one by one is very slow
  * [PLAT-5498] - Scheme altering wrapping HolidaySource
  * [PLAT-5510] - New date format chooser partially not visible
  * [PLAT-5517] - OG-Analytics: Create pricing method for AverageYieldBondFutures (AUD bond futures)
  * [PLAT-5528] - Add AUSTRALIAN to list of valid bond security types to BondLoader
  * [PLAT-5532] - implement an annuity definition for commodity coupon (both cash and physical settle)
  * [PLAT-5544] - CashFlowSecurity converter
  * [PLAT-5545] - Add support for cash flow securities to existing pricing functions
  * [PLAT-5546] - Add support for CashFlowSecurity in PortfolioLoaderTool
  * [PLAT-5547] - parspread market quote sensitivity calculator for future/forward/coupon/swap commodity
  * [PLAT-5551] - Add CashFlowSecurity to instruments supported in NotionalVisitor
  * [PLAT-5553] - FX volatility swap security object
  * [PLAT-5556] - parspread curve sensitivity calculator for future/forward/swap commodity
  * [PLAT-5557] - parspread market quote sensitivity calculator for future/forward/swap commodity
  * [PLAT-5566] - Tool to check new curve configs and show any problems
  * [PLAT-5568] - Make tracking for db masters configurable via a switch
  * [PLAT-5571] - Add flag to config import tool to only add but not update existing configs
  * [PLAT-5575] - Carr-Lee volatility swap valuation
  * [PLAT-5582] - Base index class definition
  * [PLAT-5585] - Ibor index definition
  * [PLAT-5586] - Overnight index definition
  * [PLAT-5587] - Equity index component
  * [PLAT-5588] - Equity index definition
  * [PLAT-5589] - Credit index definition
  * [PLAT-5592] - Credit index component
  * [PLAT-5593] - Bond index component
  * [PLAT-5594] - Bond index
  * [PLAT-5603] - Base index component definition
  * [PLAT-5605] - Make the service console a normal window so that it can be minimized
  * [PLAT-5619] - Add checking for exposure functions in configuration checker
  * [PLAT-5625] - ADR security object
  * [PLAT-5626] - Create ETF security object
  * [PLAT-5627] - Create an equity warrant security
  * [PLAT-5628] - Add equity warrants, ETF and ADR to financial security visitor
* Task
  * [PLAT-3715] - Test InterestRateFutureOptionMarginSecurityBlackSurfaceMethod
  * [PLAT-4409] - Conventions for AUD
  * [PLAT-4418] - Test and ensure consistency of PV and PNL for margined Futures and Options
  * [PLAT-4419] - Ensure Mid prices for live and closing are consistent
  * [PLAT-5383] - More tests for Roll-Geske-Whaley model
  * [PLAT-5550] - Remove function that calculates jump to default for CDX
  * [PLAT-5590] - implement a defintion file for swap fixed vs commodity
  * [PLAT-5641] - Create CAD convention
* Sub-task
  * [PLAT-5026] - create abstract class CommodityFutureSecurity definition and derivative file
  * [PLAT-5027] - create abstract class CommodityFutureTransaction definition and derivative file
  * [PLAT-5053] - create interface and parameter interface for commodity curve provider
  * [PLAT-5054] - create class describing a ""market" containing commodity and discount curves.
  * [PLAT-5111] - definition and derivative file for commodity forward
  * [PLAT-5112] - specific class for specific commodity future/forward according to the underlying type
  * [PLAT-5113] - present value using discounting for future/forward commodity
  * [PLAT-5114] - sensitivities using algorythme differentiantion for future/forward commodity
  * [PLAT-5115] - parspread calculator for future/forward commodity
  * [PLAT-5117] - last time calculator for future/forward commodity
  * [PLAT-5149] - create coupon commodity (abstract class) definition and derivative
  * [PLAT-5150] - create coupon commodity cash settle definition and derivative
  * [PLAT-5151] - create coupon commodity physical settle definition and derivative
  * [PLAT-5483] - improve the pricing method to take into account the rate cut-off
  * [PLAT-5505] - create forward commodity cash settle definition and derivative
  * [PLAT-5506] - create forward commodity physical settle definition and derivative
* Epic
  * [PLAT-3713] - Extend ValueRequirements of Interest Rate Future Options
  * [PLAT-4705] - Regression testing of views
  * [PLAT-5380] - Implement LegalEntities domain data model.


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

Full Issue List
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
