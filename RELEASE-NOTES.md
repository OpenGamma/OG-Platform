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

To 2.1.0-M3p01
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

To 2.1.0-M3p02
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

To 2.1.0-M3p03
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

To 2.1.0-M3p04
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

To 2.1.0-M3p05
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

To 2.1.0-M3p01
------------
As above, and:
 [PLAT-4562] Swaptions need to use calendars when calculating pvbp, as BRL won't work otherwise.
 [PLAT-4538] Add code that preserves old FX implied curve functionality if the calendar and settlement days are not set in the convention

To 2.1.0-M3p02
------------
As above, and:
[PLAT-4642] Adding toDerivative methods to BRL-type swap definitions
[PLAT-4638] Non-forward theta for fx option
[PLAT-4636] Fixing PV01 graph build
[PLAT-4639] Test new theta
[PLAT-4637] Notionals aren't resolved correctly for Swaptions in the NotionalFunction
Multiplied spot exchange rate to the valueGamma to take into account misaligned currency
Changed Scale factor default from 1.0 to empty

To 2.1.0-M3p03
------------
As above, and:
 [PLAT-4658] Added handling for missing market data in snapshots
 [PLAT-4666] Adding a scaling factor for THETA and FORWARD_DRIFTLESS_THETA for FX options

To 2.1.0-M3p04
------------
As above, and:
    [PLAT-4694] added ability to provide a custom instance of a BloombergLiveDataServer in a subclass factory.
    [PLAT-4697] Correcting properties for FX forward constant spread
    [PLAT-4697] Adding a function that will convert a multi-valued theta into a single-valued theta for FX forwards
    [PLAT-4698] Fix value theta
    [PLAT-4696] Adding support for Notional for single barriers and scaling percentage value gamma by spot
    [PLAT-4686] Forex barrier option Greeks    
                Driftless theta added
    [PLAT-4691] Adding second getResults() method to swap theta to allow the graph to build
    [PLAT-4687] Adding horizon functions with defaults to the main function configuration and setting the days property forward explicitly in the graph building phase
    [PLAT-4686] Forex barrier option Greeks
    [PLAT-4671] Scaling value theta by the notional and sign
    [PLAT-4671] Adding greeks for single barrier options
    [PLAT-4677] Digital Option Greeks    
                Forward Greeks corrected

To 2.1.0-M3p05
------------
As above, and:
    PLAT-4722:  Scaling ValueGammaP and ValueTheta according to domestic/foreign currencies
    PLAT-4722:  Scaling ValueGammaP according to domestic/foreign currencies
    Setting the sign of ValueTheta according to the pay currency of the FX forward
    [PLAT-4694] added ability to override the BBG subscription prefix
