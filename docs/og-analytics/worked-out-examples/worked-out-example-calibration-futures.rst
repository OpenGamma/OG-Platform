Curve calibration: STIR futures and Deliverable Swap Futures
============================================================

In this example we construct a set of curve composed of only two curves in USD. The code related to the example can be found in the demo test **MulticurveBuildingHullWhiteDiscountUSDFuturesDemoTest**. The goal of this example is to demonstrate how to construct the same set of curves using two calculators: one with simple discounting and one using a Hull-White one-factor model. The discounting and Hull-White models with provide different results for the STIR futures and Deliverable Swap Futures (DSF).

Curve descriptions
------------------

The calibration is done on **26 April 2013**.::

    ZonedDateTime CALIBRATION_DATE = DateUtils.getUTCDate(2013, 4, 26);

In the discounting and Fed Fund forward curve called::

    String CURVE_NAME_DSC_USD = "USD Dsc"

the instruments are 1 overnight deposit and 10 OIS. The generators of those instrument (the market standard conventions) are stored in a separate file (to use the same conventions for the different examples) and called through::

    GeneratorInstrument<? extends GeneratorAttribute>[] DSC_USD_GENERATORS = CurveCalibrationConventionDataSets.generatorUsdOnOis(1, 11, 0)

The market data for the different instruments are::

    double[] DSC_USD_MARKET_QUOTES = new double[] 0.0022, 0.00127, 0.00125, 0.00126, 0.00126, 0.00125, 0.001315, 0.001615, 0.00243, 0.00393, 0.00594, 0.01586 }

The tenors of the different deposits and swaps are stored in::

    Period[] DSC_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), ...

In the USD LIBOR 3M forward curve called::

    String CURVE_NAME_FWD3_USD = "USD Fwd 3M"

the instruments are 1 Libor fixing, 4 STIR futures and 4 DSF. The generators of the futures have to be created individually as the futures have different dates and the DSF have different coupons.

The market data for the different instruments are::

    double[] FWD3_USD_MARKET_QUOTES = new double[] {0.0027560,
        0.99715, 0.99700, 0.99680, 0.99660,
        (100 + 7.0 / 32.0 + 3.0 / (32.0 * 4.0)) / 100.0, (100 + 17.0 / 32.0) / 100.0, (101 + 2.0 / 32.0) / 100.0, (98 + 21.0 / 32.0) / 100.0 };

The data was selected to represent the level of the market at the calibration date. The DSF have strange quotes as the are quoted in 32th and quarter of 32th.

The indexes used in the curves are::

    IndexON FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
    IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");

Calculators
-----------

The calibration is done using two different par spread calculators::

    ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
    ParSpreadMarketQuoteHullWhiteCalculator PSMQHWC = ParSpreadMarketQuoteHullWhiteCalculator.getInstance();

and the associated par spread curve sensitivity calculators. The first one does the computations by simple discounting while the second uses Hull-White one-factor model and the associated convexity adjustments.

For the Hull-White model, we have used the following parameters::

    double MEAN_REVERSION = 0.01;
    double[] VOLATILITY = new double[] {0.01, 0.011, 0.012, 0.013, 0.014 };
    double[] VOLATILITY_TIME = new double[] {0.5, 1.0, 2.0, 5.0 };

The Hull-White volatility parameters can be constant or piecewise constant like in the above example.

Results
-------

Using different calculators, the resulting calibrated curves will be different

+-----------------+--------+--------+--------+--------+--------+--------+--------+--------+--------+--------+
|     Calculator  | Fixing |   EDM3 |   EDU3 |   EDZ3 |   EDH4 |   EDU4 |  CTPM3 |  CFPM3 |  CNPM3 |  CBPM3 |
+=================+========+========+========+========+========+========+========+========+========+========+
| Discounting (%) | 0.2793 | 0.2853 | 0.2925 | 0.3013 | 0.3109 | 0.3622 | 0.3716 | 0.8771 | 1.9122 | 2.9302 |
+-----------------+--------+--------+--------+--------+--------+--------+--------+--------+--------+--------+
|  Hull-White (%) | 0.2793 | 0.2850 | 0.2916 | 0.2996 | 0.3081 | 0.3563 | 0.3715 | 0.8771 | 1.9121 | 2.9301 |
+-----------------+--------+--------+--------+--------+--------+--------+--------+--------+--------+--------+
|      Diff (bps) |   0.00 |  -0.03 |  -0.09 |  -0.16 |  -0.27 |  -0.59 |  -0.01 |  -0.01 |  -0.01 |  -0.01 |
+-----------------+--------+--------+--------+--------+--------+--------+--------+--------+--------+--------+

We have used 5 STIR futures. They are not consecutive futures (EDM4 is missing); this would not be a standard way to construct the curves, but we selected this method to show that any combination of instrument can be used to calibrate the curves and not only instruments that cover the full range. The level of volatility used are realistic but have not been calibrated to the market for this example. The convexity adjustment are realistic but not necessarily in line with the market on that date.

The rates with Hull-White calculator (F^{HW}) are lower than the rates with Discounting calculator (F^{Dsc}). This is expected as $\gamma>1$ and
::math

F^{Dsc} = (1-\Phi) >  \frac{1}{\gamma} \left( 1-\Phi + \frac1{\delta}(1-\gamma) \right) = F^{HW}

The detailed formula can be found in Quantitative Research (2013).

We have used the 4 DSF (2 year, 5 year, 10 year and 30 year). The adjustment on those instruments are very small as we have used the next expiry, which is the most liquid, and there is less than 2 months to expiry.

Performance
-----------

A performance method return the time required for a certain number of curve calibrations. As standard the test does not run::

     @Test(enabled = false)

The flag should be changed to **True** for the method to run. The default is set to **False** so that the performance run, which can be time consuming is not run at each regression test.

The curves have 12 nodes in discounting curve and 9 nodes in forward curve (4 STIR futures and 4 DSF).

By default the test is run with 100 curve calibrations. The time output is for the full set of calibration (not by calibration). Running those tests on a **Mac Pro 3.2 GHz Quad-Core Intel Xeon**, the results were (the test was run several times to make sure that the HotSpot kicks-in):

* Discounting: 5ms for one pair of curves and the Jacobian matrices
* Hull-White: 11 ms for one pair of curves and the Jacobian matrices, using Hull-White one factor for STIR futures and DSF.

.. rubric:: References

.. [OG.2013] Quantitative Research (2013). The Analytic Framework for Implying Yield Curves from Market Data, version 1.3 - March 2013. *OpenGamma Documentation 6*, OpenGamma. Available at http://docs.opengamma.com/display/DOC/Analytics.