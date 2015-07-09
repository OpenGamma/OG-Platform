======================
Bond Model Integration
======================

Analytic results are are provided via the ``BondCalculator``

Multicurve
==========

The ``DiscountingBondCalculator`` is the discounting calculator implementation for ``BondCalculator``.

*Creation of the calculator is dependent on the*:

* ``IssuerProviderFn`` -  to provide the multicurve bundle for curves by issuer.
* ``BondAndBondFutureTradeConverter`` - to convert the trade/security into the analytics object.
* ``CurveDefinitionFn`` - to obtain cure definitions by curve name.
* ``Environment`` - to provide the valuation time and market data
* ``BondTrade`` - containing the ``BondSecurity`` or the subtypes ``MunicipalBondSecurity``, ``InflationBondSecurity``, ``GovernmentBondSecurity`` or ``CorporateBondSecurity``
* ``MarketDataFn`` - to provide the market price

Outputs
=======

Present value
-----------
**Present Value From Curves**

Variable: PRESENT_VALUE_CURVES
String: "Present Value From Curves"
Output type: MultipleCurrencyAmount
Description: Present value of bonds and bills from the curve linked to the issuer.

**Present Value From Market Clean Price**

Variable: PRESENT_VALUE_CLEAN_PRICE
String: "Present Value From Market Clean Price"
Output type: MultipleCurrencyAmount
Description: Present value of bonds from the market clean price. The spot value is discounted to today and the intermediary coupons are added/removed as required.

**Present Value From Market Yield**

Variable: PRESENT_VALUE_YIELD
String: "Present Value From Market Yield"
Output type: MultipleCurrencyAmount
Description: Present value of bonds from the market yield. The spot value is discounted to today and the intermediary coupons are added/removed as required.



Price 
-------

**Market Clean Price**

Variable: CLEAN_PRICE_MARKET
String: "Market Clean Price"
Output type: Double
Description: The data source quote for the clean price. No computation is done on the price; the output is the input from the data source. The price is reported as a decimal number (i.e. 1.01 for 101 %).

**Clean Price From Curve**

Variable: CLEAN_PRICE_CURVES
String: "Clean Price From Curve"
Output type: Double
Description: The bond clean price computed from the curve linked to the issuer. The clean price is computed for the standard settlement date of the bond from the valuation date, not for the settlement date of the actual trade. The price is reported as a decimal number (i.e. 1.01 for 101 %).

**Clean Price from Market Yield**

Variable: CLEAN_PRICE_YIELD
String: "Clean Price from Market Yield"
Output type: Double
Description: The bond clean price computed from the data source bond yield. The price is reported as a decimal number (i.e. 1.01 for 101 %).

Yield
------

**Yield To Maturity From Market Clean Price**

Variable: YIELD_TO_MATURITY_CLEAN_PRICE
String: "Yield To Maturity From Market Clean Price"
Output type: Double
Description: The bond yield-to-maturity computed from the data source clean price. The yield is computed in the convention of the bond. The yield is reported as a decimal number (i.e. 0.02 for 2 %).

**Yield To Maturity From Curve**

Variable: YIELD_TO_MATURITY_CURVES
String: "Yield To Maturity From Curve"
Output type: Double
Description: The bond yield-to-maturity computed from the data source clean price. The yield is computed in the convention of the bond. The yield is reported as a decimal number (i.e. 0.02 for 2 %).

**Market Yield To Maturity**

Variable: YIELD_TO_MATURITY_MARKET
String: "Market Yield To Maturity"
Output type: Double
Description: 
The data source quote for the yield-to-maturity. No computation is done on the yield; the output is the input from the data source. The yield is reported as a decimal number (i.e. 0.02 for 2 %).

Other
--------
* PV01, in the form of a ``ReferenceAmount``
* Bucketed PV01, in the form of ``BucketedCurveSensitivities``
* Z-Spread, in the form of a ``Double``

