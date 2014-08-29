Curve calibration: STIR futures
===============================

This set of curves in USD can be used for different examples and tests. It is implemented in the calss **RecentDataSetsMulticurveStandardGbp**. In particular it can be used to analyse swap risks in the analysis file **SwapRiskGbpAnalysis**.

Curve descriptions
------------------

There are 4 curves in this example:
* Discounting/forward overmight: calibrated with ON deposit and OIS.
* Forward Libor 6M: calibrated with fixing, FRAs and IRS Fixed v 6M

Method
------

The method to obtain the calibrated curves is: ::

    getCurvesGbpOisL6(ZonedDateTime calibrationDate)

It calibrates the curves for a given *calibration date*