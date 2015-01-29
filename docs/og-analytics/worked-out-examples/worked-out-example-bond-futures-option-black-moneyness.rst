Worked-out example: bond futures option - black model with annually compounded rates and log-moneyness/expiry volatility surface
==========================================

The code of this example can be found in the test: **BondFuturesOptionMarginTransactionBlackExpLogMoneynessMethodE2ETest**.  
The test includes three instruments and the same set of outputs is examined for each case. 
In this document we focus on the first instrument, an option on Euro-Schatz bond futures. 

Curves
------

The curves are created based on annually compounded rates. 
See the documentation, worked-out-examples/worked-out-example-bond-futures-discounting-compounding.rst.

Parameter provider
------------------

Given a volatility surface in the form of **InterpolatedDoublesSurface** where the volatility surface is parametrised by option's expiry and log moneyness, the parameter provider for option valuation based on normal model is created by::

    BlackBondFuturesExpLogMoneynessProviderDiscount BLACK_PROVIDER_SCH; 
    BLACK_PROVIDER_SCH = new BlackBondFuturesExpLogMoneynessProviderDiscount(wrapper, VOL_SURFACE_MONEYNESS, LEGAL_ENTITY_SCH);

Here the log moneyness is defined by log( (strike price) / (underlying futures price) ), and the legal entity **LEGAL_ENTITY_SCH** should accommodated with the curves and instrument described below. 


Instrument
----------
First we create an underlying bond futures security with data stored in the example::

        BondFuturesSecurityDefinition bondFuturesDefinition = 
            new BondFuturesSecurityDefinition(tradingLastDate, noticeFirstDate, noticeLastDate, deliveryFirstDate, deliveryLastDate, NOTIONAL, deliveryBusket, conversionFactor);

Then we create an option on the bond futures::

    BondFuturesOptionMarginSecurityDefinition underlyingOption = new BondFuturesOptionMarginSecurityDefinition(
        bondFuturesDefinition, lastTradingDate, expirationDate, strike, isCall);

We create a transaction on the above security with data stored in the file::

    BondFuturesOptionMarginTransactionDefinition transactionDefinition = 
        new BondFuturesOptionMarginTransactionDefinition(underlyingOption, QUANTITY, TRADE_DATE, TRADE_PRICE);

The transaction is the purchase/sell of a given quantity on a given date for a given price.
 
This is the definition version of the transaction, i.e. it contains all the dates and data relevant to the trade.

The definition version is converted to the *derivative* version::

    BondFuturesOptionMarginTransaction TRANSACTION_SCH;
    TRANSACTION_SCH = transactionDefinition.toDerivative(VALUATION_DATE, LAST_MARGIN_PRICE);

In this transformation, the dates are replaced by time between the valuation date and the different cash flow dates. Moreover the last priced used in the margining is stored. This is used to compute the present value which is the difference between the current price and the last margin price.


Price and present value
-----------------------

After adjusting the issuer curve as in the documentation, worked-out-examples/worked-out-example-bond-futures-discounting-compounding.rst, the first output we produce is the current price of the option::

   FuturesPriceBlackBondFuturesCalculator FPBFC = FuturesPriceBlackBondFuturesCalculator.getInstance();
   double price = TRANSACTION_SCH.getUnderlyingSecurity().accept(FPBFC, blackNew) * HUNDRED;

Here the extra factor of 100 is inserted to follow the market standard convention. 

The *present value* of the instrument is computed as the current price minus the last margin price multiplied by the notional, and the quantity.
The code to compute the present value simply read as::

    PresentValueBlackBondFuturesOptionCalculator PVBFC = PresentValueBlackBondFuturesOptionCalculator.getInstance();
    MultipleCurrencyAmount pv = TRANSACTION_SCH.accept(PVBFC, blackNew); 

The present value calculator is used through the visitor pattern. The result of the present value computation is a **MultipleCurrencyAmount** involving the information about the currency. The present value calculator is further described in worked-out-examples/PresentValueDiscountingCalculator.


 


    
Curve sensitivity
-----------------

The calculators used for curve sensitivity calculation are::

      PresentValueCurveSensitivityBlackBondFuturesOptionCalculator PVCSBFC = PresentValueCurveSensitivityBlackBondFuturesOptionCalculator.getInstance();
      ParameterSensitivityParameterCalculator<BlackBondFuturesProviderInterface> PSSFC = new ParameterSensitivityParameterCalculator<>(PVCSBFC);
      DoubleMatrix1D bucketedPv01 = PSSFC.calculateSensitivity(TRANSACTION_SCH, blackNew).multipliedBy(BP1).getSensitivity(CURVE_NAME_SCH, EUR);
      
The **PSSFC** computes the sensitivity to the curve parameters (*bucketed PV01*), in the present case they are annually compounded zero rates.  The computation is not scaled, i.e. it is for a movement of 1. The last part of the code (**multipliedBy**) multiply it by one basis point to match the market standard. In general, the OG-Analytics library uses absolute numbers (not percent or basis point) everywhere and provide the tools to rescale the output easily.

Here we look at the sensitivity to the issuer curve rather than the repo curve (in the present case the repo curve is a flat curve). 
The format of the output is a *DoubleMatrix1D* object which can be represented by::

    (0.0, 0.0, 0.0, 0.0, 0.0, -0.0019, -0.0039, -0.0058, -0.0076, -0.0092, -0.0110, -0.0124, -0.0192, -1.0044, 0.0, 0.0, 0.0, 0.0)

Another way of expressing curve sensitivity is *PV01*, total amount of the curve sensitivity, which is computed by::

    PV01CurveParametersCalculator<BlackBondFuturesProviderInterface> PV01PC = new PV01CurveParametersCalculator<>(PVCSBFC);
    double pv01 = TRANSACTION_SCH.accept(PV01PC, blackNew).getMap().get(Pairs.of(CURVE_NAME_SCH, EUR));

Again we focus on the sensitivity to the issuer curve. 

Note that we compute the sensitivity to curve parameters here. An alternative definition to the curve sensitivities is the market quote sensitivity. For more about the relation between market quote sensitivity and curve parameter sensitivity, See the documentation, worked-out-examples/worked-out-example-stir-futures-discounting.rst.

Option Greeks
-----------------

Finally we produce option Greeks, delta, gamma, theta and vega. These are respectively computed by::

    DeltaBlackBondFuturesCalculator DBFC = DeltaBlackBondFuturesCalculator.getInstance();
    double delta = TRANSACTION_SCH.getUnderlyingSecurity().accept(DBFC, blackNew);
::

    GammaBlackBondFuturesCalculator GBFC = GammaBlackBondFuturesCalculator.getInstance();
    double gamma = TRANSACTION_SCH.getUnderlyingSecurity().accept(GBFC, blackNew);
::

    ThetaBlackBondFuturesCalculator TBFC = ThetaBlackBondFuturesCalculator.getInstance();
    double theta = TRANSACTION_SCH.getUnderlyingSecurity().accept(TBFC, blackNew);
::

    VegaBlackBondFuturesCalculator VBFC = VegaBlackBondFuturesCalculator.getInstance();
    double vega = TRANSACTION_SCH.getUnderlyingSecurity().accept(VBFC, blackNew);
