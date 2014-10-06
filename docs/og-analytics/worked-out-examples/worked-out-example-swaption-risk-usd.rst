Swaption analysis - USD
==========

The code is available in the class **SwaptionUsdAnalysis**.

Curves
-----

In this example we will use several sets of curves.

* Standard: Uses overnight discounting. The discounting/overnight
  curve is calibrated with OIS. There are USD LIBOR curves for 3M, 1M
  and 6M. The 3M curve is calibrated with FRA and IRS. The 1M and 6M
  curves are calibrated with basis swaps.

* Negative: Uses overnight discounting. The discounting/overnight
  curve is calibrated with OIS. There are USD LIBOR curves for 3M, 1M
  and 6M. The 3M curve is calibrated with FRA and IRS. The 1M and 6M
  curves are calibrated with basis swaps. Some market quotes are
  negative rates. This is to explain the potential handling of
  negative strikes and rates.

Instruments
---------

Different swaps are built using the **FixedAnnuityDefinitionBuilder** and the **FloatingAnnuityDefinituionBuilder**.

**IRS LIBOR3M**

Two vanilla swaptions on Fixed vs LIBOR3M are constructed. 
* 5Yx5Y / strike 3%
* 1Yx2Y / Strike -0.05% / To test negative forward rates and negative strikes.

Results
------

**Swaption Standard**

The 5Yx5Y swaption with July 2014 USD curves. The results computed are

* *par rate* of the underlying swap
* *present value* of the swaption using SABR model
* Different types of curve sensitivities: point sensitivity, zero-coupon curves sensitivities and market quote sensitivities.


**Market Quotes Sensitivities**

The market quotes sensitivities are computed for the first swap (10Y) using the three curve providers. 

Using the standard curves, the sensitivity appears mainly on the forward LIBOR3M curve on the 10Y node.

Using the IMM curve, the sensitivity appears on all the quarterly IMM
dates swaps. This is expected as the swap are not overlapping and to
obtain the 10Y rate which is the most relevant for the the valuation,
roughly 40 quarterly rates have to be composed. It is not surprising to
see a sensitivity to all those rates.

