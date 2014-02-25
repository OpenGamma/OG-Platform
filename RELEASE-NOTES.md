OpenGamma Platform 2.1.1
---------------------------------
This is a patch release of version 2.1.0 which contains fixes.


Changes since 2.1.0
===================

To 2.1.1
--------
	  [PLAT-4799] Add greek functions for BRL swaptions
	  [PLAT-4574] CombinedHistoricalTimeSeriesMaster
	  For payer swaptions, the fixed leg notionals was negative, resulting in negative numeraire.  
	  Now the absolute value of the fixed leg's notional is used, so that the numeraire is positive.

To 2.1.2
--------

    [] Minor scaling for gamma and vega
    [] Configuration for swaption greeks
    [] Add daycount convention for the calculation of projected brl di forward
    [] Change constructor of the premium ir fut option
    [] Interest rate future option premium black methods
    [] Clean up display of Gamma PV01 field
    [] Theta for interest rate future options
    [] Interest rate future option functions that computes Value GammaP, value Theta and Value Delta
    [] Fixed Ibor and overnight compounded black swaption methods
    [] Calculate the par rate of the swaptions underlying swap or the forward value fed to the BlackScholes formula]
    [] For swaption: compute the change of dv01 for 1bps curve shift on both forward and discounting curves
    [] IR future option vol surface construction: passing underlying maturity information into the volsurf construction routine
    [] Compute swap theta
    [] BRL future curve construction Added calendar and daycount class member so that the curve node calculation takes into accout the 252 convention
    [PLAT-4880] - Create scripts for rsk database specify wrong version
    [PLAT-4850] Full log information is no longer available

To 2.1.3
--------

    [] Theta scaling for BRL swaption.
    [] Reverted the sign flip for short payer swaption change
    [PLAT-4910] Added a field to volatility surface specifications and future price curve specifications that allows direct use of the underlying security in calculations
    [PLAT-4909] Using sign in notional for swaptions and adding back functionality for ir future option greeks
    [] Changed IR futures price curve nodes maturity calculation, s.t. the delievery dates are take from the future security itself.  Require the security to be in the database

To 2.1.4
--------

    [PLAT-4914] Swap theta functions now return a single-valued result.
    [] Black calculators now use interest rate future option premia when calculating the present value.
    [] Valuation date holidays now taken into account when shifting fixing series in swap theta calculations.
    [] Fix for ArrayIndexOutOfBoundsException when shifting fixing series for swaps.
    [] Fix for the sign of the numeraire for BRL DI swaptions in greek calculations.
    [PLAT-4933] Performance improvement in HistoricalValuationFunction during dependency graph building
    
To 2.1.5
--------

    [] Fixes for FX forward currency conversion issues in P&L series.


To 2.1.6
--------

    [PLAT-4888, PLAT-4891, PLAT-4916, PLAT-4908, PLAT-4932] Fix database scripts and tests
    NOTE: This changes a number of create and migrate scripts (sec, rsk, org, exg, auth)
    DBAs may want to check the fixes:
    https://github.com/OpenGamma/OG-Platform/commit/faf5e208534ce41636dcff4a02839e940e11b36e
    https://github.com/OpenGamma/OG-Platform/commit/25d224cfcfc8278a52b62bbfb21cf90ffb9f81a0

    [PLAT-4959] InterestRateFutureOptionPremiumTransactionBlackSurfaceMethod: change the present value to use computed price.
    [PLAT-4958] Time series for last margin price no longer required if the trade date is the valuation date
    [PLAT-4957] Removing today's cashflows from swap theta calculations and adding javadocs
    [PLAT-4938] Adding conventions for KRW
    [PLAT-4954] Add support for Black analytic Vega for IR Futures Options
    [PLAT-4953] Display security level black gamma for ir future options.
    [PLAT-4952] Changed SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod.forward() so that it returns forward instead forwardModified

To 2.1.7
--------

    [PLAT-4949] Support clone-free InMemoryMasters
    [PLAT-4950] InMemoryPortfolioMaster performance enhancements
    [PLAT-4947] Mutable delegating PositionMaster and PortfolioMaster and SecurityMaster
    [PLAT-4972] Removed unnecessary log lines
    [] Corrected swaption valueTheta calculation so that the final value is scaled by annuity numeraire for PhysicalFixedIbor
    [] Scale the theoretical Black Theta by a factor of 365.25

To 2.1.8
--------

    [] Reverted the changes made for the FX Forward currency conversion issues from the 2.1.5 release due to their unintended side-effects.

To 2.1.9
---------
    [] Changed SwaptionBlackForwardThetaCalculator so that the theta column for swaption returns unscaled analytical black theta
    [PLAT-5057] added fix for null market data values not handled by DbBatchWriter
    [PLAT-5036] Quantity field fails to store in the batch db
    [PLAT-4947] Mutable delegating PositionMaster and PortfolioMaster and SecurityMaster
    [PLAT-4993] Backport regression tests to 2.1.x branch

To 2.1.10
---------
    [PLAT-5118] Added new constraint type ConversionMethod which allows the constraint provider to specify which currency conversion

To 2.1.11
---------
    [PLAT-5138] Catch exceptions building the domestic curve on a particular valuation date, allowing the function to return a result containing the curves it did build
    [PLAT-5034] Ensure main code runs without SQL server dependency
	
To 2.1.12
---------
    [PLAT-5218] Ability to sort outputs of ConfigImportExportTool
	[PLAT-5223] Fixed fudge builder to create arrays of correct type.
	
To 2.1.13
---------
    [PLAT-5288] Fix for NPE in VolatilitySurfaceDataFudgeBuilder
    
To 2.1.14
---------
    [PLAT-5312] Exception casting Object[] to Double[] in IRFutureOptionVolatilitySurfaceDataFunction

To 2.1.15
---------
    [PLAT-5331] YieldCurveNodeReturnSeriesFunction qualifies result using user-specified tenors, not resolved ones.

To 2.1.16
---------
    [PLAT-5345] Remove hard coded tolerance in FXImpliedYieldCurveSeriesFunction
	
To 2.1.17
---------
	[PLAT-5472] Implied deposit curve series tenors of less than 1M resolve to 0
	[PLAT-5138] Catch failures for individual valuation dates in FXImpliedYieldCurveSeriesFunction
	
To 2.1.18
---------
	[PLAT-5502] Spot and payment dates are incorrectly calculated in ImpliedDepositCurveSeriesFunction

To 2.1.19
---------
	[PLAT-5502] Further change to apply the correct spot lag for ImpliedDepositCurveSeriesFunction. Look up lag from FXSpotConvention.
	
To 2.1.20
---------
	[PLAT-5502] Brought implied depo curve function in line with the curve series function

To 2.1.21
---------
	[PLAT-5679] MultiYieldCurveParRateMethodSeriesFunction does not infer the YCHTS correctly

To 2.1.22
---------
	[TMPL-46] Change the behaviour of Forex P/L times series: use today FX rate and not scenario rate

OpenGamma Platform 2.1
---------------------------------

These release notes cover changes from v2.0 to v2.1.

What's New?
===========

Integration of pricing with the new multi-curve framework

Our new multi-curve framework is compatible with OIS discounting and collateral approaches,
and allows calibration with any instrument (IRS, OIS, futures, FX swaps, cross-currency swaps, etc)
as well as calibration of multiple curves simultaneously using a multi-dimensional root finder. 
It accepts complex curve entanglements and supports interpolated curves, functional curves,
and spread curves above another curve.  

You can provide exogenous dates as node points in interpolated curves (e.g. central bank meeting)
and produce full Jacobian/transition matrices. In addition, algorithmic differentiation has been
implemented for the entire curve-building process to achieve reduced computation times. 
You can access the framework via the web GUI or through the Excel interface.

Other key features and enhancements

* Integration of pricing with new multi-curve framework
* Market data snapshot import/export from CSVs
* Excel support for new multi-curve framework
* Improved performance when handling portfolio updates
* Batch database now has target meta-data table to allow for easier reporting using external OLAP tools
* Programmatic support for historical scenarios (e.g. (d2-d1)/d1 * today) allowing for scenarios like LTCM, Lehmans, etc
* Reduced memory footprint
* More human readable XML configurations
* Market data system performance improvements
* Ability to run system with no live data source
* Various improvements to scenario scripting language
* Migrated more configuration away from Spring
* Improved JMX monitoring of market data system and calc engine
* Jolokia support

Analytics Library updates

* Support for CME Deliverable Swap Futures
* Support for FED Fund Futures
* Support for Inflation Swaps (inc year-on-year and zero coupon)
* Support for Zero-Coupon Swaps
* Support for Inflation Bonds
* Improved convention modelling
* Improvements to credit derivatives modelling

491 issues were [resolved in JIRA](http://opnga.ma/17MAByc) in this relase.


Upgrading from 2.0.0
--------------------

Significant incompatible changes are noted here:

Configuration compatibility
- DbBatchMasterComponentFactory has been extended to include a 'computationTargetResolver' property
- SpringViewProcessorComponentFactory now has a 'jmsMarketDataAvailabilityTopic' property
- CurveSpecificationBuilderComponentFactory added for curves with similar setup to CurrencyPairsSourceComponentFactory
- EngineConfigurationComponentFactory should expose 'conventionSource = ConventionSource::shared'
- EngineConfigurationComponentFactory should expose 'organizationSource = OrganizationSource::shared'


Database compatibility
- rsk schema upgraded to version 51
- sec schema upgraded to version 67


API compatibility
- Security converters used with the legacy curve system have been renamed.
XSecurityConverter has become XSecurityConverterDeprecated for the following security types:
CapFloor, CreditDefaultSwap, Forex, FRA, Future, InterestRateFutureOption, InterestRateFuture, Swap, Swaption.


Analytics compatibility
- In FX implied curve construction, the curve node tenors are now adjusted correctly using holidays
and the appropriate number of settlement days for the currency pair. This may cause differences. [PLAT-4373]


Source code layout
- New project: og-engine-db - Database code for OG-Engine
- New project: og-financial-types - Types to support OG_Financial

To 2.1.1
-----------

As above, and:

Configuration compatibility
- No changes required

Database compatibility
- No upgrade required

API compatibility
- No significant changes

Analytics compatibility
- No expected differences

To 2.1.2
-----------

As above, and:

Configuration compatibility
- No changes required

Database compatibility
- No upgrade required

API compatibility
- No significant changes

Analytics compatibility
- No expected differences

To 2.1.3
-----------

As above, and:

Configuration compatibility
- No changes required

Database compatibility
- No upgrade required

API compatibility
- No significant changes

Analytics compatibility
- No expected differences

To 2.1.4
-----------

As above, and:

Configuration compatibility
- No changes required

Database compatibility
- No upgrade required

API compatibility
- No significant changes

Analytics compatibility
- No expected differences

To 2.1.5
-----------

As above, and:

Configuration compatibility
- No changes required

Database compatibility
- No upgrade required

API compatibility
- No significant changes

Analytics compatibility
- No expected differences

To 2.1.6
-----------

As above, and:

Configuration compatibility
- No changes required

Database compatibility
- No upgrade required

API compatibility
- No significant changes

Analytics compatibility
- No expected differences

To 2.1.7
-----------

As above, and:

Configuration compatibility
- No changes required

Database compatibility
- No upgrade required

API compatibility
- No significant changes

Analytics compatibility
- No expected differences

To 2.1.8
-----------

As above, and:

Configuration compatibility
- No changes required

Database compatibility
- No upgrade required

API compatibility
- No significant changes

Analytics compatibility
- No expected differences

To 2.1.9
-----------

As above, and:

Configuration compatibility
- No changes required

Database compatibility
- No upgrade required

API compatibility
- No significant changes

Analytics compatibility
- No expected differences

To 2.1.10
-----------

As above, and:

Configuration compatibility
- No changes required

Database compatibility
- No upgrade required

API compatibility
- No significant changes

Analytics compatibility
- No expected differences

To 2.1.11
-----------

As above, and:

Configuration compatibility
- No changes required

Database compatibility
- No upgrade required

API compatibility
- No significant changes

Analytics compatibility
- No expected differences

To 2.1.12
-----------

As above, and:

Configuration compatibility
- No changes required

Database compatibility
- No upgrade required

API compatibility
- No significant changes

Analytics compatibility
- No expected differences

To 2.1.13
-----------

As above, and:

Configuration compatibility
- No changes required

Database compatibility
- No upgrade required

API compatibility
- No significant changes

Analytics compatibility
- No expected differences

To 2.1.14
-----------

As above, and:

Configuration compatibility
- No changes required

Database compatibility
- No upgrade required

API compatibility
- No significant changes

Analytics compatibility
- No expected differences
