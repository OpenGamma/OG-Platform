OpenGamma Platform 2.4
----------------------

These release notes cover changes from v2.3 to v2.4.

Upgrading to 2.4.0 from 2.3.0
=============================

* Bug
    * [PLAT-6712] - Fix WARN message on server start up when initializing webBasics
    * [PLAT-6715] - A number of config files in demo-lx-1 aren't quite right and exit status on fullstack.sh doesn't seem right either.
* Improvement
    * [PLAT-6675] - Deprecating old security types
    * [PLAT-6676] - Forward rate agreement
    * [PLAT-6708] - Using class hierarchy when retrieving configurations from redis
    * [PLAT-6713] - Ensure snapshot market data is configured on deployment template


To 2.4.0-M2
-----------

* Bug
    * [PLAT-6665] - InMemorySecurityMaster replace() fails if UniqueId missing
    * [PLAT-6668] - FixedIncomeDataProvider uses valuation time zone to create zoned series
* Epic
    * [PLAT-6680] - Tidy OG-Platform v2.x
* Improvement
    * [PLAT-6691] - INI files - components can be defined as optional but properties cannot 
* Task
    * [PLAT-6677] - Remove Engine DSL
    * [PLAT-6678] - Remove Lambdava
    * [PLAT-6681] - Remove OG-Client project
    * [PLAT-6682] - Remove OG-Security project
    * [PLAT-6683] - Remove batch database
    * [PLAT-6686] - Remove AMQP
    * [PLAT-6687] - Simplify ini config files
    * [PLAT-6688] - Use CurrenciesVisitor directly
    * [PLAT-6689] - DEBT : Document currency pairs management 


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
