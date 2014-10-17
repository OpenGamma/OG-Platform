Curve calibration: inflation curves (ZC swaps)
=================================
In this example we construct a simple set of curve composed of only two curves in USD. The first curve is the discounting curve - calibrated on OISs - and the second curve is the CPI price index curve - calibrated from zero-coupon inflation swaps. The code related to the example can be found in the demo test **StandardDataSetsInflationUSD**.

Curve descriptions
------------------

The discounting and inflation curves are called::

    String CURVE_NAME_USD_OIS = "USD-OIS";
    String CURVE_NAME_CPI_USD = "USD-ZCHICP";

The first curve is used for discounting and for projecting Fed Fund rates. The calibration is done on one overnight (deposit) rate and on Overnight Indexed Swaps (OISs).

The second curve is used for projecting the price index (CPI). The calibration is done on inflation zero-coupon swaps.

Data
----

The OIS data is realistic data. The inflation data is arbitrary 2% level inflation rates.

Calibration 
-----------

A total of 6 calibrations of the same curve using different processes. 

 * Both curves calibrated in a single process using two units. We call the calibration method only once. In the description of the curves we use two units, one for the OIS/discounting curve and one for the inflation curve.
 * Inflation calibrated with the Multicurve of OIS externally provided (two step process). We call the calibration method a first time with the OIS/discounting curve alone. We use the output of that calibration as input to the next calibration. We pass the curve itself and the Jacobian matrices (CurveBuildingBlockBundle) associated as input to the second calibration (the objects are referred to as "known data" and :known block" in the code). 
 * Both curves calibrated in a single process using a single unit. This is a simultaneous calibration of both curves. This is only to prove that interest rate and inflation curve can be calibrated simultaneously. It does not add any value in this particular case.
 * The same as the first calibration process with extra information provided initially. The inflation curve includes seasonality (monthly adjustment according to some externally provided monthly multiplicative adjustments).
 * The same as the first calibration process with extra information provided initially. The inflation curve includes the already fixed price. As the standard in inflation is a three months fixing offset, the current and past index levels may be used for some coupons and fixing the already known part of the curve will impact the interpolation.
 * The same as the first calibration process with extra information provided initially. The seasonality adjustment and the known index parts of the previous two curves sets are combined in one curve.


