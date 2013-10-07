OpenGamma Platform 2.1
---------------------------------

These release notes cover changes from v2.0 to v2.1.

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
