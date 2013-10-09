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
