Worked-out example: deliverable swap futures - discounting
==========================================

The code of this example can be found in the test: **SwapFuturesPriceDeliverableTransactionDiscountingMethodE2ETest**. 
The test class contains instruments based on three currencies, EUR, GBP and USD.
In this document we focus on the EUR example. The same argument applies for the other two cases. 

Curves
------

To compute the risk measures, we need a multi-curve provider. 
Let us first instantiate::

    MulticurveProviderDiscount MULTI_CURVE_EUR = new MulticurveProviderDiscount();

then the underlying curves are created and plugged into the provider::

    YieldCurve yieldCurve1 = YieldCurve.from(interpolatedCurve1);
    MULTI_CURVE_EUR.setCurve(EUR, yieldCurve1);
    YieldCurve yieldCurve2 = YieldCurve.from(interpolatedCurve2);
    MULTI_CURVE_EUR.setCurve(INDEX_EUR, yieldCurve2);

Here the two underlying curves respectively correspond to a discounting curve and a forward curve. 


Instrument
----------
After building two legs by **FloatingAnnuityDefinitionBuilder** and **FixedAnnuityDefinitionBuilder**, 
we create an underlying Fixed-to-Ibor swap security with data stored in the example::

    SwapFixedIborDefinition swapDefinition = new SwapFixedIborDefinition(toFixedLeg(fixedLeg), toIborLeg(iborLeg));

Here the private methods are called to convert each leg into the correct form for **SwapFixedIborDefinition**. 
Using this swap security, the swap futures security is created::

        SwapFuturesPriceDeliverableSecurityDefinition underlyingSwapFuture = new SwapFuturesPriceDeliverableSecurityDefinition(lastTradingDate, swapDefinition, NOTIONAL);

This is the "security" version of the instrument, i.e. the description of the fungible futures. 

We create a transaction on the above security with data stored in the file::

    SwapFuturesPriceDeliverableTransactionDefinition swapFutureTransaction = new SwapFuturesPriceDeliverableTransactionDefinition(underlyingSwapFuture, QUANTITY, TRADE_DATE, TRADE_PRICE);

The transaction is the purchase/sell of a given quantity on a given date for a given price. This is the definition version of the transaction, i.e. it contains all the dates and data relevant to the trade.

The definition version is converted to the *derivative* version::

    SwapFuturesPriceDeliverableTransaction TRANSACTION_EUR;
    TRANSACTION_EUR = swapFutureTransaction.toDerivative(REFERENCE_DATE, LASTMARG_PRICE);

In this transformation, the dates are replaced by time between the valuation date and the different cash flow dates. Moreover the last priced used in the margining is stored. This is used to compute the present value which is the difference between the current price and the last margin price.


In this examples all the computation are done with a *projection and discounting* model, that we simply call *discounting*. No convexity adjustment for the daily margining is computed in this implementation. 



Price and present value
-----------------------

After adjusting the issuer curve, the first output we produce is the current price of the DSF::

        FuturesPriceMulticurveCalculator FPMC = FuturesPriceMulticurveCalculator.getInstance();
        double price = TRANSACTION_EUR.accept(FPMC, MULTI_CURVE_EUR) * HUNDRED;
    
Here the extra factor of 100 is inserted to match the market standard convention.

The *present value* of the instrument is computed as the current price minus the last margin price multiplied by the notional, and the quantity.
The code to compute the present value simply read as::

    PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
    MultipleCurrencyAmount pv = TRANSACTION_EUR.accept(PVC, MULTI_CURVE_EUR);

The present value calculator is used through the visitor pattern. The result of the present value computation is a **MultipleCurrencyAmount** involving the information about the currency. The present value calculator is further described in worked-out-examples/PresentValueDiscountingCalculator.



Curve sensitivity
-----------------

The calculators used for curve sensitivity calculation are::

    PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
    ParameterSensitivityParameterCalculator<ParameterProviderInterface> PVSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
    MultipleCurrencyParameterSensitivity bucketedPv01 = PVSC.calculateSensitivity(TRANSACTION_EUR, MULTI_CURVE_EUR).multipliedBy(BASIS_POINT);

The **PVSC** computes the sensitivity to the curve parameters (*bucketed PV01*), in the present case they are zero rates. The computation is not scaled, i.e. it is for a movement of 1. The last part of the code (**multipliedBy**) multiply it by one basis point to match the market standard. In general, the OG-Analytics library uses absolute numbers (not percent or basis point) everywhere and provide the tools to rescale the output easily.

The format of the output is a **MultipleCurrencyParameterSensitivity** object which can be represented by::

      [EURDSFDisc-Definition, EUR]= (0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.05764, 0.0, 0.0, 0.0, 0.0, 0.0, 0.01062, 0.0, 0.0, -0.12091, -0.05946, 0.04278, -0.12089, -0.12710, 0.02621, 0.14404, -0.07253, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)  

for the sensitivity to the discount curve and we have the sensitivity to the forward curve::

     [EURDSFIndex-Definition, EUR]= (0.0, 0.0, 0.0, 0.0, 2.2240, 1.09377, -0.00243, 0.00410, 0.00433, -0.05201, -0.20521, -0.47863, -35.03866, -17.17521, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

Another way of expressing the curve sensitivity is *PV01*, the total amount of the curve sensitivity, which is computed by::

    PV01CurveParametersCalculator<ParameterProviderInterface> PV01C = new PV01CurveParametersCalculator<>(PVCSDC);
    ReferenceAmount<Pair<String, Currency>> pv01 = TRANSACTION_EUR.accept(PV01C, MULTI_CURVE_EUR);

The output format is ::

    ReferenceAmount{data={[EURDSFIndex-Definition, EUR]=-49.6260, [EURDSFDisc-Definition, EUR]=-0.21961}}

Note that we compute the sensitivity to curve parameters here. An alternative definition to the curve sensitivities is the market quote sensitivity. For more about the relation between market quote sensitivity and curve parameter sensitivity, see the documentation, worked-out-examples/worked-out-example-stir-futures-discounting.rst.