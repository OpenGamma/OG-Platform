OpenGamma Platform 2.5
----------------------

These release notes cover changes from v2.4 to v2.5.

Upgrading to 2.5.0 from 2.4.0
=============================

To 2.5.0
-----------

* Bug
    * [PLAT-6795] - Fix application of EOM flag when not at EOM


To 2.5.0-M3
-----------

* Bug
    * [PLAT-6734] - InterestRateFutureOptionMarginSecurityBlackSmileMethod: bug in the way the volatility is called
    * [PLAT-6796] - NPE on  single leg swap cashflow 
* Improvement
    * [PLAT-6806] - Ensure Cashflow outputs work for single leg swaps/notional exchange


To 2.5.0-M2
-----------

* Bug
    * [PLAT-6741] - PV inconsistencies with CouponONArithmeticAverage and CouponONArithmeticAverageSpread
    * [PLAT-6745] - OpenGammaFudgeContext leaves non-daemon threads open


To 2.5.0-M1
-----------

* New Feature
    * [PLAT-4609] - Support Oracle for DB Masters
* Bug
    * [PLAT-6728] - FloatingAnnuityDefinitionBuilder does not handle initial notional correctly
* Task
    * [PLAT-6727] - Create end-to-end test for curves calibrated on STIR futures.

