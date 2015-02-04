Worked-out example: bond futures - discounting method with curves based on annually compounded rates
==========================================

The code of this example can be found in the test: **BondFuturesTransactionAnnuallyCompoundingE2ETest**. 
The test class contains four instruments. Here let us focus on the Long Guilt Futures, but the same argument applies for the other examples in the code. 

Curves
------

To compute the risk measures, we need a curve provider represented by **IssuerProviderDiscount** in which issuer curves and **MulticurveProviderDiscount** are contained. 
We first instantiate::

    IssuerProviderDiscount ISSUER_PROVIDER_LGT = new IssuerProviderDiscount();

then the underlying curves are created and plugged into the provider::

    ISSUER_PROVIDER_LGT.setCurve(Pairs.of((Object) ISSUER_NAME_LGT, filter), yieldCurve);
    ISSUER_PROVIDER_LGT.setCurve(GBP, repoCurve);

In the present case the provider consists of two curves: a repo rate as a constant curve and a issuer curve for Long Guilt.
Note that all of the curves here are created by **YieldPeriodicCurve** for annually compounded rates.

Instrument
----------
We create an underlying bond security with data stored in the example::

    BondFixedSecurityDefinition bondFixed = BondFixedSecurityDefinition.from(GBP, firstAccrualDate, firstCouponDate,
        maturityDate, paymentPeriod, fixedRate, settlementDays, GBP_CALENDAR, DAY_COUNT, BUSINESS_DAY, yieldConvention,
        isEOM, legalEntity);

Using this bond security, the bond futures security is created::

        BondFuturesSecurityDefinition bondFuturesDefinition = new BondFuturesSecurityDefinition(tradingLastDate,
        noticeFirstDate, noticeLastDate, deliveryFirstDate, deliveryLastDate, NOTIONAL, deliveryBasket,
        conversionFactor);

Here the delivery basket contains only one bond. This is the "security" version of the instrument, i.e. the description of the fungible futures. 

We create a transaction on the above security with data stored in the file::

    BondFuturesTransactionDefinition transactionDefinition = new BondFuturesTransactionDefinition(bondFuturesDefinition, QUANTITY, TRADE_DATE, TRADE_PRICE);

The transaction is the purchase/sell of a given quantity on a given date for a given price.
 
This is the definition version of the transaction, i.e. it contains all the dates and data relevant to the trade.

The definition version is converted to the *derivative* version::

    BondFuturesTransaction TRANSACTION_LGT; 
    TRANSACTION_LGT = transactionDefinition.toDerivative(VALUATION_DATE, LAST_MARGIN_PRICE);

In this transformation, the dates are replaced by time between the valuation date and the different cash flow dates. Moreover the last priced used in the margining is stored. This is used to compute the present value which is the difference between the current price and the last margin price.


In this examples all the computation are done with a *projection and discounting* model, that we simply call *discounting*. No convexity adjustment for the daily margining is computed in this implementation. 

Spread
------

Generally the bond futures computed from the issuer curve does not agree with the bond market price. 
Thus the issuer discount curve is adjusted with the spread. 
In particular we compute the spread added to the annually compounded rates::

    double spreadComputed = BOND_METHOD.zSpreadFromCurvesAndClean(bondAtSpot, ISSUER_PROVIDER_LGT, BOND_MARKET_PRICE_LGT / HUNDRED, true, NUM_PERIODS) / BP1;

The flag **true** is used for the periodic compounded spread. One uses **false** for a continuously compounded case. 
In the code the spread is expressed in basis point.
Then we create a new provider with the adjusted curve::

     IssuerProviderIssuerDecoratedSpreadPeriodic curveWithSpread = 
         new IssuerProviderIssuerDecoratedSpreadPeriodic(ISSUER_PROVIDER_LGT, legalEntity, spreadComputed * BP1, NUM_PERIODS);


Price and present value
-----------------------

After adjusting the issuer curve, the first output we produce is the current price of the option::

        FuturesPriceIssuerCalculator FPIC = FuturesPriceIssuerCalculator.getInstance();
        double priceFuturesComputed = futures.accept(FPIC, curveWithSpread) * HUNDRED;
    
Here the extra factor of 100 is inserted to follow the market standard convention.

The *present value* of the instrument is computed as the current price minus the last margin price multiplied by the notional, and the quantity.
The code to compute the present value simply read as::

    PresentValueIssuerCalculator PVIC = PresentValueIssuerCalculator.getInstance();
    MultipleCurrencyAmount pvTransactionComputed = TRANSACTION_LGT.accept(PVIC, curveWithSpread);

The present value calculator is used through the visitor pattern. The result of the present value computation is a **MultipleCurrencyAmount** involving the information about the currency. The present value calculator is further described in worked-out-examples/PresentValueDiscountingCalculator.



Curve sensitivity
-----------------

The calculators used for curve sensitivity calculation are::

    PresentValueCurveSensitivityIssuerCalculator PVCSIC = PresentValueCurveSensitivityIssuerCalculator.getInstance();
    ParameterSensitivityParameterCalculator<ParameterIssuerProviderInterface> PSSFC = new ParameterSensitivityParameterCalculator<>(PVCSIC);
    DoubleMatrix1D bucketedTransactionComputed = PSSFC.calculateSensitivity(TRANSACTION_LGT, curveWithSpread).multipliedBy(BP1).getSensitivity(CURVE_NAME_LGT, GBP));

The **PSSFC** computes the sensitivity to the curve parameters (*bucketed PV01*), in the present case they are annually compounded zero rates. The computation is not scaled, i.e. it is for a movement of 1. The last part of the code (**multipliedBy**) multiply it by one basis point to match the market standard. In general, the OG-Analytics library uses absolute numbers (not percent or basis point) everywhere and provide the tools to rescale the output easily.

Here we look at the sensitivity to the issuer curve rather than the repo curve (in the present case the repo curve is a flat curve).
The format of the output is a **DoubleMatrix1D** object which can be represented by::

(0.0, 0.0, 0.0, 0.0, -0.0011,-0.0040, -0.0090, -0.0132, -0.0170, -0.0204, -0.0237, -0.0266, -0.0291, -0.0312, -0.6130, -0.1539, 0.0, 0.0)

Another way of expressing the curve sensitivity is *PV01*, total amount of the curve sensitivity, which is computed by::

    PV01CurveParametersCalculator<ParameterIssuerProviderInterface> PV01PC = new PV01CurveParametersCalculator<>(PVCSIC);
    double pv01TransactionComputed = TRANSACTION_LGT.accept(PV01PC, curveWithSpread).getMap().get(Pairs.of(CURVE_NAME_LGT, GBP));

Again we focus on the sensitivity to the issuer curve.

Note that we compute the sensitivity to curve parameters here. An alternative definition to the curve sensitivities is the market quote sensitivity. For more about the relation between market quote sensitivity and curve parameter sensitivity, see the documentation, worked-out-examples/worked-out-example-stir-futures-discounting.rst.