Curve calibration: multiple currencies
======================================

In this example we construct a set of curve composed of curves in USD, EUR and JPY. The curves in EUR and JPY are constructed from the USD curves using curss currency instruments (FX swaps and cross-currency swaps). The code related to the example can be found in the demo test **MulticurveBuildingDiscountingDiscountXCcyTest**.

Curve descriptions
------------------

The calibration is done on **28 September 2011**.::

    ZonedDateTime CALIBRATION_DATE = DateUtils.getUTCDate(2011, 9, 28)

In total we will construct 7 curves::

      String CURVE_NAME_DSC_USD = "USD Dsc";
      String CURVE_NAME_FWD3_USD = "USD Fwd 3M";
      String CURVE_NAME_DSC_EUR = "EUR Dsc";
      String CURVE_NAME_FWD3_EUR = "EUR Fwd 3M";
      String CURVE_NAME_DSC_JPY = "JPY Dsc";
      String CURVE_NAME_FWD3_JPY = "JPY Fwd 3M";
      String CURVE_NAME_FWD6_JPY = "JPY Fwd 6M";

**USD**

The first step is to construct the USD curves. This is done as in a simple way, by first calibrating the discounting/forward ON curve on OIS and then the forward USD LIBOR3M curve on Fixing and FRAs and IRSs. A documented example of simple curves calibrated in that way can be found at :doc:`worked-out-example-calibration-simple`.

**EUR**

The EUR curves we want to construct are related to EUR trades with collateral at USD Fed Fund. We want to calibrated the discounting curve and the forward EUR EURIBOR3M curve. The curves will be calibrated using single currency instruments and two types of instruments multi-currency instruments: FX swaps and cross-currency swaps. On the short term (up to 1 year), the most liquid multiple currency instrument is the FX swaps; for the longer term the most liquid one is the cross-currency swaps.

The FX swaps are simply a set of fixed cash flows in two currencies. As we have constructed the USD discounting already, it would be easy to construct a new curve, the EUR discounting, using FX swaps. Unfortunately this would give the curve only up to 1 year. If we want to go further down the curve, we need to use cross-currency swaps. The most liquid cross-currency swaps exchange one floating rate (USD LIBOR3M) for another floating rate of the same tenor in the other currency (EUR EURBOR3M). We now have two unknowns in EUR: the discounting and the forward for EURIBOR3M. To get a second constraint, on top of the instruments already mentioned, we add EUR IRSs [#f1]_ We obtain a system of two (sets of) unknowns and two (sets of) constraints. This calibration is done in the block 0. 

Later (block 3 and 4) we perform the same calibration but this time we change the order of the instruments. In block 3, the cross-currency swaps are used in the forward curve and the single currency IRSs are used in the discounting curve. We still have two constraints and two unknowns. The calibrated curves are the same. The difference is in the risk; now the cross-currency swap risk appear in the forward curve column. The numbers are the same but they appear in a different place. This emphasizes that the curve calibration and resulting risk problem is a global problem. It is easy to move the pieces around but at the end the curve should be the same and the total risk (as defined by the hedging instruments to use) is the same, even if the display on the reports may be different.

**JPY**

The JPY curves we want to construct are related to EUR trades with collateral at USD Fed Fund. The calibration case is different from the EUR one as in JPY the IRSs fixed v JPY LIBOR3M are not liquid; the liquid IRSs are v JPY LIBOR6M. We want to use the most liquid instruments which are cross-currency swaps USD LIBOR3M v JPY LIBOR3M, IRS Fixed v JPY LIBOR6M and basis swaps JPY LIBOR3M (+spread) v JPY LIBOR6M. With respect to the EUR case, we have to add the basis swaps, if not we would have 3 unknowns (discounting, JPY LIBOR3M and JPY LIBOR6M) and only 3 constraints.

In total we obtain a multi-curve provider object with 5 curves (2 USD and 3 JPY). The 3 JPY curves have to be calibrated as one unit, it is not possible to disentangle the three curves. 

Solving a three curve problem in one unit instead of three separated units has a small impact on performance, but this is the only way to solve the problem. This can be viewed in the performance section.  

Performance
-----------

The curve calibration computation times are:

* USD-EUR (3 units: USD Dsc // USD Fwd 3 // EUR Dsc - EUR Fwd 3) : 11 millisecond. Number of nodes: 13 // 8 // 13 - 8 = 42
* USD-JPY (3 units: USD Dsc // USD Fwd 3 // JPY Dsc - JPY Fwd 3 - JPY Fwd 6) : 14 millisecond. Number of nodes: 13 // 8 // 13 - 8 - 7 = 49
* USD-JPY (1 unit: USD Dsc - USD Fwd 3 - JPY Dsc - JPY Fwd 3 - JPY Fwd 6) : 22 millisecond. Number of nodes: 13 - 8 - 13 - 8 - 7 = 49

.. rubric:: Footnotes

.. [#f1] The EUR IRSs quoted are swaps with collateral at EONIA. In theory they can not be used directly in curve calibration with collateral in another currency. In this approach we ignore the convexity adjustment required. We refer to [HEN.2014.1]_ for more details on multi-curve framework with collateral and the associated convexity adjustment.

.. rubric:: References

.. [HEN.2014.1] Interest Rate Modelling in the Multi-curve Framework: Foundations, Evolution and Implementation. Applied Quantitative Finance. Palgrave Macmillan. ISBN: 978-1-137- 37465-3.