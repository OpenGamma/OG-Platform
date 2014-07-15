Workout examples of curve calibration and pricing.
======================================

Present value and risk measures
-------------------------------

The library is using a visitor pattern in the instrument description. The instances of those visitors related to computing risk measures are called "Calculator".

The simplest one is the **PresentValueDiscountingCalculator** which, as his name indicates, computes the present value by simple discounting (and forward) computation. In particular, no convexity adjustment or volatility is used.




> Written with [StackEdit](https://stackedit.io/).