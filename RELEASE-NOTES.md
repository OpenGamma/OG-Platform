OpenGamma Platform 2.4
----------------------

These release notes cover changes from v2.3 to v2.4.

Upgrading to 2.4.0 from 2.3.0
=============================

* Bond future option quantity field passed into definition (OG-Financial)
* IR future option greeks pass underlying price correctly into black.getVolatility() call (OG-Analytics)
* Fixed thread leak in OpenGammaFudgeContext (OG-Util)

To 2.4.0-M2
-----------

* Improvement
    * [PLAT-6691] - Allow optional property placeholders in ini files

To 2.4.0-M1
-----------

* Bug
    * [PLAT-6666] - Stub period rate interpolation
    * [PLAT-6672] - CouponPaymentVisitor and CouponTenorVisitor do not support CouponIborCompoundingSpreadDefinition
* Improvement
    * [PLAT-6673] - Upgrade to Joda-Beans 1.1
    * [PLAT-6674] - Fix generation of testing outputs
* Task
    * [PLAT-6667] - Add CurveSensitivities object
    * [PLAT-6670] - Convert ReferenceAmount to a joda bean
