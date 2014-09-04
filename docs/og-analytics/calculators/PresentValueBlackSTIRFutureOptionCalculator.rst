PresentValueBlackSTIRFutureOptionCalculator
=============================

Compute the present value using data comprising a multi-curve provider
and the Black implied volatility for STIR futures at
expiry/strike/delay points. 

The strike is expressed as a futures price (not rate).

Currently, only one instrument is implemented in the calculator:
* InterestRateFutureOptionMarginTransaction

The current implementation of the volatility provider are:

* BlackSTIRFuturesExpLogMoneynessProvider
* BlackSTIRFuturesSmileProvider

**BlackSTIRFuturesExpLogMoneynessProvider**
The Black volatility are stored in a surface. The dimensions are
expiration and log moneyness,
i.e. ln(1.0-strikePrice)/(1.0-futuresPrice)).
Note that in this case the internal representation uses futures rates
(1-price) to store the data, even if the interface uses the price.

**BlackSTIRFuturesSmileProvider**
The volatilities are stored in a surface on dimention
expiry/strike. The delay dimension is ignored. The Black implied
volatility is directly interpolated (using any two dimensional
interpolator).
