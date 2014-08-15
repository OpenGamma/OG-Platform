Curve calibration: STIR futures
===============================

This set of curves in USD can be used for different examples and tests. It is implemented in the calss **RecentDataSetsMulticurveFutures3MUsd**. In particular it can be used to analyse swap risks in the analysis file **SwapRiskUsdAnalysis**.

Curve descriptions
------------------

There are 4 curves in this example:
* Discounting/forward overmight: calibrated with ON deposit and OIS.
* Forward Libor 3M: calibrated with fixing, STIR futures and IRS
* Forward Libor 1M: calibrated with fixing, FRAs and basis swaps 1M v 3M
* Forward Libor 6M: calibrated with fixing, FRAs and basis swaps 3M v 6M

Calculators
-----------

The calculator used for the *target* numbers in the curve calibration are flexible. The standard calculator is the **ParSpreadMarketQuoteDiscountingCalculator**. It computes the difference between the standard market quote of the instrument computed from the curve and the one in the instrument. If the difference is 0, the instrument is ATM. This calculator is good for instruments like FRA and swaps. For STIR futures, the standard market quote is the price (often something like 99.5%). The relation between price and rate is rate = 1-price. This means that the sensitivity wrt the STIR futures has the opposite sing of the sensitivity to a FRA rate. To have more consitent risk figures in term of sign, another calculator can be used: **ParSpreadRateDiscountingCalculator**. In that case the number obtain is the difference between the standard "rate" of the instrument computed from the curve and the one in the instrument. For FRAs and swaps, the rate is the same as the market quotes. For STIR futures, the rate is 1-price. The calculator is used not only for the calibration itself but also for the computation of hte Jacobian matrix. The risk figures obtained will heavily depend on it.

Method
------

The method to obtain the calibrated curves is: ::

    getCurvesUSDOisL1L3L6(ZonedDateTime calibrationDate, boolean marketQuoteRisk)

It calibrates the curves for a given *calibration date*. The second argument allows to choose between the calculator on *market quotes* and the one on *rates*.