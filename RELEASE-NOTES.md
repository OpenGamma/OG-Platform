OpenGamma Platform 2.3
----------------------

These release notes cover changes from v2.2 to v2.3.

Upgrading to 2.3.0 from 2.2.0
=============================

* Bug
    * [PLAT-6445] - OG-Analytics: ParRateDiscountingCalculator - incorrect for visitSwap
    * [PLAT-6643] - CurveBuildingBlock array out of bounds exception 
    * [PLAT-6662] - Duplicated stack traces in Failures can fill up the cache
* Improvement
    * [PLAT-6647] - Allow analytic annuity to have rate changed
    * [PLAT-6664] - Add snapshot docs
* Task
    * [PLAT-6641] - Integrate end to end bond test data into sources
    * [PLAT-6648] - Server and remote view set-up and docs 
    * [PLAT-6650] - Add SGD-SOR-VWAP index
    * [PLAT-6651] - Add CurveNodeIdMapper docs
    * [PLAT-6659] - Add ACT/365.FIXED daycount mapping
    * [PLAT-6663] - Test compiled 3rd party analytics on a remote server


To 2.3.0-M2
-----------

* Bug
    * [PLAT-6635] - Home page analytics link incorrect
    * [PLAT-6636] - Fix serialization of Joda-Beans in Fudge
* Task
    * [PLAT-6625] - TRS TBIll Model integration
    * [PLAT-6626] - TRS TBIll Configuration setup 
    * [PLAT-6627] - TRS TBIll Integration and example documentation 
    * [PLAT-6628] - Validation Bill TRS
    * [PLAT-6634] - Documentation for defining multiple holidays for swap leg parameters, e.g. reset, payment
