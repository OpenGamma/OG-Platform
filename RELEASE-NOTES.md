OpenGamma Platform 2.2 milestones
---------------------------------

These release notes cover changes from v2.1 to v2.2.


Upgrading from 2.1.0
====================

To 2.2.0-M10p05
---------------

Configuration compatibility
- No upgrade required

Database compatibility
- No upgrade required

API compatibility
- No changes

Analytics compatibility
- No expected differences

Bug fixes
* [PLAT-6353] [PLAT-6354] Fixes to credit functions. Removed single spread credit curve calibration for CDS and CDX; removed multiplication by 10,000 of observed market spreads in CDX curve calibration
* [PLAT-6321] Fix consistency errors when active nodes are rewritten



To 2.2.0-M10p05
---------------

Configuration compatibility
- No upgrade required

Database compatibility
- No upgrade required

API compatibility
- No changes

Analytics compatibility
- No expected differences

Bug fixes
* [PLAT-6321,PLAT-6325] Inconsistent internal state in GetTerminalValuesCallback


To 2.2.0-M10p04
---------------

Configuration compatibility
- No upgrade required

Database compatibility
- No upgrade required

API compatibility
- No changes

Analytics compatibility
- No expected differences

Bug fixes
* [PLAT-6263] Fix for quantity overflow issue
* Removed quantity in pv calculation in IRFut; Removed multiplication by quantity as the quantity is applied twice.
* [PLAT-6252] Added handling so missing market data is tracked in SingleComputationCycle; added null handling to ComputedValue deserialization logic.
* [PLAT-5933] Add market data source command line option to MarketDataSnapshotTool

To 2.2.0-M10p03
---------------

Configuration compatibility
- No upgrade required

Database compatibility
- No upgrade required

API compatibility
- No changes

Analytics compatibility
- No expected differences

Bug fixes
* [PLAT-5906] - Make semantics of shifts and scaling in scenarios consistent
* [PLAT-5947] - Fix for aliased subscriptions being stuck in a pending state
* [PLAT-5958] - Fix for missing live data server subscription responses with aliased subscriptions
* [PLAT-5961] - Clean up last known value cache correctly when fully unsubscribed from live market data
* [PLAT-6107] - VolatilitySurfaceSelector does not pick up all vol surface types
* [PLAT-6125, PLAT-6126, PLAT-6127] - fixes in regression testing framework

Improvements
* [PLAT-5662] - Add min/max functionality to scenario DSL


To 2.2.0-M10p02
---------------
* [PLAT-5959] - MarketDataSnapshotTool should allow a timeout to be specified

To 2.2.0-M10p01
---------------
* Improvement
    * [PLAT-5688] - Port CFG DB sub-system to Oracle

    Add the following dependency to your project's pom.xml for oracle database support.

    <dependency>
       <groupId>com.oracle</groupId>
       <artifactId>ojdbc6</artifactId>
       <version>11.2.0</version>
    </dependency>

    In order to add the oracle driver binary to your local mavan repository, issue following command :            ]

    mvn install:install-file -Dfile={Path/to/your/ojdbc.jar} -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0 -Dpackaging=jar


To 2.2.0-M10
------------
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
