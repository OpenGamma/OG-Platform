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

**IRS LIBOR6M**

**OIS FEDFUND**

**Fed Fund swaps**

