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

We use two sets of parameters (volatilities):

* SABR: Expiry and tenor dependent set of parameters (alpha, beta, rho, nu). The smile is the one implied by the SABR parameters.
* Normal volatility: Expiry and tenor dependent set of volatilities. The normal smile is flat (same normal volatility for all strikes).

The data are not calibrated to the same prices (we use them for different curves, so there is no way to have a coherent set of data for all curves and all strikes).

Instruments
---------

Different swaps are build using the **FixedAnnuityDefinitionBuilder** and the **FloatingAnnuityDefinituionBuilder**.

**IRS LIBOR3M**

Two vanilla Fixed vs LIBOR3M are constructed. 

* Forward start - Receiver - 2Yx10Y - Strike 3.00%
* Forward start - Receiver - 2Yx10Y - Strike -0.05% (negative strike)

Two swaptions based on those swaps: Long swaption with standard exercise date (two business day before the swap effective date).

Results
------

**Swaption Standard **

Results using a standard set of curves (with realistic data).

**Swaption negative strike** 

Results using a curve with negatives rates (OIS and LIBOR).

**Present Value**

The present value using the different models. The present value is represented by a MultipleCurrencyAmount.

**Implied volatility**

The implied volatility results depends on the type of model. For the SABR model, it the the Black implied volatility. For the normal model, it is the normal implied volatility.

**Market Quotes Sensitivities**

The market quotes sensitivities are the sensitivity to the quotes used in the curve calibration, i.e. interest rate risk. 

**Parameters sensitivity**

The meaning of this result strongly depends on the type of model. For the SABR model it is the sensitivity to the interpolated SABR parameters used in the pricing (alpha, beta, rho and nu). For the normal model, it is the sensitivity to the implied volatility used . The last result is also called Vega in some places.