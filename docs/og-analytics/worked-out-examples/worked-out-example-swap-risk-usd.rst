Swap risk - USD
==========

Curves
-----

In this example we will use several sets of curves.

* Standard: Uses overnight discounting. The discounting/overnight curve is calibrated with OIS. There are USD LIBOR curves for 3M, 1M and 6M. The 3M curve is calibrated with FRA and IRS. The 1M and 6M curves are calibrated with basis swaps.

* Futures: Uses overnight discounting. The discounting/overnight curve is calibrated with OIS.

* IMM dates. Take as an input another multi-curve provider. From the provider, compute synthetic rates for IMM dates swaps (OIS and IRS). This allow to compute the risk related to IMM dates with curve coherent with market data on standard swaps. In the example, the input provider used for the IMM date curves is the standard provider. It can be changed to the futures based curves (or any other curves) easily.

Instruments
---------

Different swaps are build using the **FixedAnnuityDefinitionBuilder** and the **FloatingAnnuityDefinituionBuilder**.

**IRS LIBOR3M**

Three vanilla Fixed vs LIBOR3M are constructed. 
* Spot start - 10Y tenor
* Forward start - 5Yx5Y
* Spot start - 2Y tenor

**IRS LIBOR6M**

**OIS FEDFUND**

**Fed Fund swaps**

Results
------

**Present Value**

Computes the present value of the first swap and each of its leg using
the standard curves.

The present value of the first swap is also computed using the IMM
date curves build on the standard curve. There is a difference of
nodes used in both curves, but appart from a secondary interpollation
effect, the present value is the same for both curves.

The present vaue is also computed with the futures curves for the same
swap. The data used to calibrate the standard and the futures curves
are not the same; the present values can be different.

**Par Rate**

The par rate of the spot starting swap and the forward starting swaps
are computed with the standard curves.

**Market Quotes Sensitivities**

The market quotes sensitivities are computed for the first swap (10Y)
using the three curve providers. 

Using the standard curves, the sensitivity appears mainly on the
forward LIBOR3M curve on the 10Y node.

Using the IMM curve, the sensitivty appears on all the quarterly IMM
dates swaps. This is expected as the swap are not overlapping and to
obtain the 10Y rate wich is the most relevant for the the valuation,
roughly 40 quarterly rates have to be composed. It is not suprising to
see a sensitivity to all those rates.

