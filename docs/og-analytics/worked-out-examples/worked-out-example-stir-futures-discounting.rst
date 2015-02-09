Worked-out example: STIR futures - discounting
==========================================

The code of this example can be found in the test: **STIRFuturesTransactionDiscountingMethodE2ETest**

Curves
------

To compute the risk measures, we need a multi-curve provider. In this example we simply load an existing one. For worked out example on curve calibration see **MulticurveBuildingDiscountingDiscountUSD2DemoTest**. ::

    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR = 
      StandardDataSetsMulticurveEUR.getCurvesEurOisL3();

This load in a multi-curve provider and the building block of the Jacobian matrices. The second object is required for sensitivity to market quote computation.

The provider consists of two curves: a EUR discounting curve - calibrated on EUR EONIA swaps - and a EUR EURIBOR 3M forward curve.

Instrument
----------
We create a STIR futures security with data stored in the example::

    InterestRateFutureSecurityDefinition ERZ4_SEC_DEFINITION =
      new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, EUREURIBOR3M, NOTIONAL, FUTURE_FACTOR, NAME, CALENDAR);

This is the "security" version of the instrumnet, i.e. the description of the fungible futures. 

We create a transaction on the above security with data stored in the file::

    InterestRateFutureTransactionDefinition ERZ4_TRA_DEFINITION =
      new InterestRateFutureTransactionDefinition(ERZ4_SEC_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE);

The transaction is the purchase/sell of a given quantity on a given date for a given price.
 
This is the definition version of the transaction, i.e. it contains all the dates and data relevant to the trade.

The definition version is converted to the *derivative* version::

    InterestRateFutureTransaction ERZ4_TRA = 
      ERZ4_TRA_DEFINITION.toDerivative(REFERENCE_DATE, LAST_MARGIN_PRICE);

In this transformation, the dates are replaced by time between the valuation date and the different cash flow dates. Moreover the last priced used in the margining is stored. This is used to compute the present value which is the difference between the current price and the last margin price.


In this examples all the computation are done with a *projection and discounting* model, that we simply call *discounting*. No convexity adjustment for the daily margining is computed in this implementation. Another wroket out example deal with convexity adjustment; **STIRFuturesTransactionHullWhiteMethodE2ETest**.

Price
-----

The first output we produce is the *price of the security. The *futures price* is obtained by computing the forward rate for the Euribor rate underlying the futures, denoted *F*. The price is *P = 1 - F*. This is the current price of the futures.

The code to compute the price is::

    InterestRateFutureSecurityDiscountingMethod METHOD_STIR = InterestRateFutureSecurityDiscountingMethod.getInstance();
    double priceComputed = METHOD_STIR.price(ERZ4_TRA.getUnderlyingSecurity(), MULTICURVE);

Present value
-------------

The nextoutput we produce is the *present value* of the instrument. The *futures price*s, denoted *F*, was computed in the previous section. The present value is computed as the current price minus the last margin price multiplied by the notional, the accrual factor and the quantity.

The code to compute the present value simply read as::

    PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
    MultipleCurrencyAmount pvComputed = FRA.accept(PVDC, MULTICURVE);

The present value calculator is used through the visitor pattern. The result of the present value computation is a **MultipleCurrencyAmount**. It is not only the present value as a double but also the information about the currency in which it is expressed. In some cases, like for FX forward or cross-currency swaps, the output will contains multiple currencies. The output will be the present value of each leg in its original currency. The present value calculator is further described in :doc:`../PresentValueDiscountingCalculator`.

Par rate
--------

The par rate is a double. It can be obtained by::

    ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
    double parRate = ERZ4_TRA.accept(PRDC, MULTICURVE);

The calculator provide an easy way to price different instrument in a generic framework. Most of the methods are also implemented in specific method relative to one instrument. The STIR futures par rate can be obtained also as::

    double parRateMethod = METHOD_STIR.parRate(ERZ4_TRA.getUnderlyingSecurity(), MULTICURVE);

Par Spread to Market Quote
--------------------------

In the curve calibration, an important calculator is the **ParSpreadMarketQuoteDiscountingCalculator**. It computes the quantity to be added to the market quote (price) to obtain an instrument with a present value of 0::

    ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
    double parSpreadComputed = ERZ4_TRA.accept(PSMQDC, MULTICURVE);
    
Curve sensitivity
-----------------

The calculators used for curve sensitivity calculation are::

    PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
    ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
    MarketQuoteSensitivityBlockCalculator<MulticurveProviderInterface> MQSBC = new MarketQuoteSensitivityBlockCalculator<>(PSC);

In practice, to compute the sensitivity to market quotes, only one of them is enough::

    double BP1 = 1.0E-4;
    MultipleCurrencyParameterSensitivity pvmqsComputed = MQSBC.fromInstrument(FRA, MULTICURVE, BLOCK).multipliedBy(BP1);

The **MQSBC** compute the sensitiviy to the market quotes. The computation is not scaled, i.e. it is for a movement of 1. The last part of the code (**multipliedBy**) multiply it by one basis point to match the market standard. In general, the OG-Analytics library uses absolute numbers (not percent or basis point) everywhere and provide the tools to rescale the output easily.

The format of the output is a *MultipleCurrencyParameterSensitivity* object which can be represented by::

    [EUR-DSCON-OIS, EUR]= (3.0E-4, 3.0E-4, 0.0, 0.0, 1.7334, 3.0714, 4.6402, -18.8887, -0.9835, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    [EUR-EURIBOR3M-FRAIRS, EUR]= (-2398.5241, -2479.7772, -2479.144, 9422.5946, 912.6277, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

The information stored is, for each relevant curve and currency, the sensitivity to each market quote. In this case, with a STIR futures, most of the sensitivities are 0.

The example code also contains the same result computed in three steps to describe the internal process used to compute the market quote sensitivity::

    MultipleCurrencyMulticurveSensitivity pvPointSensi = FRA.accept(PVCSDC, MULTICURVE);
    MultipleCurrencyParameterSensitivity pvParameterSensi = PSC.pointToParameterSensitivity(pvPointSensi, MULTICURVE);
    MultipleCurrencyParameterSensitivity pvMarketQuoteSensi = MQSBC.fromParameterSensitivity(pvParameterSensi, BLOCK).multipliedBy(BP1);

The first step consists in computing the *point sensitivity*, i.e. the sensitivity with respect to each discount factor and each forward rate. This first output looks like::

    {EUR={}
    {EUR-EURIBOR3M-FRAIRS=[ForwardSensitivity[start=0.8273972602739726, end=1.073972602739726, af=0.25, value=3.125E7]]}}

The instrument has no sensitivity to the discounting curve. There is a sensitivity to one forward rate which starts at time 0.83, finishes at time 1.07 with an accrual factor of 0.25. The value of the sensitivity is 31,250,000 to a movement of 1.0 (100%) in the forward rate.

The second step consists in projecting the above sensitivity to the internal parameters of the curve. In this case the curve is represented by yield curve stored as interpolated curve on zero-coupon rates. The sensitivity obtained from this second step is the sensitivity to the zero-coupon rates (rescaled to one basis point)::

    [EUR-EURIBOR3M-FRAIRS, EUR]= (0.0, 0.0, -7312.09396, 9480.9079, 915.4560, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

The third step consist in multiplying the sensitivity to the parameters by the Jacobian matrix to obtain the market quote sensitivity.

The (generalised) Jacobian matrices are stored in the **CurveBuildingBlockBundle**. It contains, fore each curve, the curve on which it depends and the transistion matrix between market quotes and curves parameters. In the above example the object look like::

    EUR-DSCON-OIS=[
    {EUR-DSCON-OIS=[0, 16]}
    1.0139, 0.0000, 0.0000, ...
    0.5069, 0.5069, 0.0000, ...
    0.0169, 0.0169, 0.9799, ...
    ...]
    EUR-EURIBOR3M-FRAIRS=[
    {EUR-DSCON-OIS=[0, 16], EUR-EURIBOR3M-FRAIRS=[16, 17]},
    0.0000, 0.0000, 0.0000, ..., 0.0000, 1.0132, 0.0000, 0.0000, ...
    0.0000, 0.0000, 0.0000, ..., 0.0000, 0.5038, 0.5094, 0.0000, ...
    0.0000, 0.0000, 0.0000, ..., 0.0000, 0.3352, 0.3390, 0.3390, ...
    ...]

The first matrix is of dimension 16x16 (it has been cut to fit in the table). It contains the sensitivity of the EUR-DSCON-OIS curve parameters to the input (market quotes) of the EUR-DSCON-OIS curve. The sensitivitires appear mainly on the diagonal but not only there. There are small sensitivities off-diagonal. The second matrix is of dimension 17x(16+17). It contains the sensitivity of the EUR-EURIBOR3M-FRAIRS curve parameters to the EUR-DSCON-OIS and EUR-EURIBOR3M-FRAIRS market data. Sensitivity are mainly on the diagonal of the second 17x17 block, but there are sensitivities everywhere: parameters of the EUR-EURIBOR3M-FRAIRS are not only dependent on the market quote of the same curve but also on the market quote of the previous curve EUR-DSCON-OIS.
