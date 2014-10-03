Workout examples: interest rate instrument pricing and curve calibration
========================================================================

Present value and risk measures
-------------------------------

The library is using a visitor pattern in the instrument description. The instances of those visitors related to computing risk measures are called "Calculator".

The simplest one is the **PresentValueDiscountingCalculator** which, as his name indicates, computes the present value by simple discounting (and forward) computation. In particular, no convexity adjustment or volatility is used.

We suggest to start by the workout example: :doc:`worked-out-example-fra`

Curve calibration
-----------------
We suggest to start with the calibration examples in the following order:

* Simple USD curve calibration: :doc:`worked-out-example-calibration-simple`
* Curve calibration with futures: :doc:`worked-out-example-calibration-futures`
* Curve calibration using exogenous dates: TODO
* Curve calibration with cross-currency instruments: :doc:`worked-out-example-calibration-xccy`
* Curve calibration with spread curves (turn-of-year, different interpolators in different part of the curve, LIBOR curve as a spread to overnight curve): :doc:`worked-out-example-calibration-meeting-spread`