OpenGamma Platform 2.1.0-M3px
---------------------------

This is a patch release of the M3 milestone release of version 2.1.0 which contains enhancements.


Upgrading from 2.0.0
====================

To 2.1.0-M1
-----------

Configuration compatibility
- No changes required

Database compatibility
- No upgrade required

API compatibility
- No significant changes

Analytics compatibility
- No expected differences

To 2.1.0-M2
-----------
As above, and:

Configuration compatibility
- DbBatchMasterComponentFactory has been extended to include a computationTargetResolver property. This must be added if batch runs are performed.

Database compatibility
- rsk schema upgraded to version 51

API compatibility
- Security converters used with the legacy curve system have been renamed. XSecurityConverter has become XSecurityConverterDeprecated for the following security types: CapFloor, CreditDefaultSwap, Forex, FRA, Future, InterestRateFutureOption, InterestRateFuture, Swap, Swaption.

Analytics compatibility
- No expected differences

To 2.1.0-M3
-----------
As above, and:

Configuration compatibility
- DbBatchMasterComponentFactory now requires the computationTargetResolver property added in 2.1.0-M2.

Database compatibility
- No upgrade required

API compatibility
- No significant changes

Analytics compatibility
- In FX implied curve construction, the curve node tenors are now adjusted correctly using holidays and the appropriate number of settlement days for the currency pair. This may cause differences. [PLAT-4373]

To 2.1.0-M3
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



Changes since 2.0.0
===================

To 2.1.0-M1
-----------
http://jira.opengamma.com/issues/?jql=fixVersion%20%3D%20%222.1.0-M1%22

To 2.1.0-M2
-----------
As above, and http://jira.opengamma.com/issues/?jql=fixVersion%20%3D%20%222.1.0-M2%22

To 2.1.0-M3
-----------
As above, and http://jira.opengamma.com/issues/?jql=fixVersion%20%3D%20%222.1.0-M3%22

To 2.1.0-M3px
------------
 [PLAT-4562] Swaptions need to use calendars when calculating pvbp, as BRL won't work otherwise.
 [PLAT-4538] Add code that preserves old FX implied curve functionality if the calendar and settlement days are not set in the convention


