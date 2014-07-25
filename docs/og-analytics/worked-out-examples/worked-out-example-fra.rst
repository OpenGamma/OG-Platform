Worked-out example: Forward rate agreement
======================

The code of this example can be found in the test: **ForwardRateAgreementDiscountingMethodE2ETest**

Curves
------

To compute the risk measures, we need a multi-curve provider. In this example we simply load an existing one. For example on curve calibration see xxx. ::

    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR = StandardDataSetsMulticurveUSD.getCurvesUSDOisL3();
This load in a multi-curve provider and the building block of the Jacobian matrices. The second object is required for sensitivity to market quote computation.

The provider consists of two curves: a USD discounting curve (also used for Fed Fund projection, but this is not used in the example) and a USD LIBOR 3M forward curve.

Instrument
----------
We create a simple FRA with data stored in the example:

    ForwardRateAgreementDefinition FRA_DEFINITION = ForwardRateAgreementDefinition.from(ACCRUAL_START_DATE, ACCRUAL_END_DATE, NOTIONAL, USDLIBOR3M, FRA_RATE, CALENDAR);
This is the definition version of the instrument, i.e. it contains all the dates and data relevant to the trade.

The definition version is converted to the *derivative* version. 

    Payment FRA = FRA_DEFINITION.toDerivative(VALUATION_DATE);
Note that the type of the derivative version of the FRA is **Payment** and not **ForwardRateAgreement**. This is because a priori we don't know the type resulting of the *toDerivative* transformation. In most of the cases it will be a standard **ForwardRateAgreement**, but if the valuation date is between the FRA fixing and the FRA settlement date (2 business day later in USD or EUR), the result will be a fixed cash flow. The transformation between **Definition** and **Derivative** version is not only a transformation of dates into time (double) but also taking the different fixing, exercise and similar information into account.

Present value
-------

The first output we produce is the *present value* of the instrument. The present value is obtained in this case by forward estimation and discounting to today(1). In the present value, all the cash flow in the future (including today's cash flows) are taken into account; the past cash flows are not taken into account.
The code to compute the present value simply read as

    PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
    MultipleCurrencyAmount pvComputed = FRA.accept(PVDC, MULTICURVE);

The present value calculator is used through the visitor pattern. The result of the present value computation is a **MultipleCurrencyAmount**. It is not only the present value as a double but also the information about the currency in which it is expressed. In some cases, like for FX forward or cross-currency swaps, the output will contains multiple currencies. The output will be the present value of each leg in its original currency. The present value calculator is further described in :doc:`../PresentValueDiscountingCalculator`.

Par rate
--------

The par rate is a double. It can be obtained by

    ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
    double parRate = FRA.accept(PRDC, MULTICURVE)

The calculator provide an easy way to price different instrument in a generic framework. Most of the methods are also implemented in specific method relative to one instrument. The FRA par rate can be obtained also as

    ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
    double parRateMethod = METHOD_FRA.parRate((ForwardRateAgreement) FRA, MULTICURVE);

Par Spread to Market Quote
--------

In the curve calibration, an important calculator is the **ParSpreadMarketQuoteDiscountingCalculator**. It computes the quantity to be added to the market quote to obtain an instrument with a present value of 0.

    final double parRate = FRA.accept(PSMQDC, MULTICURVE);
    
Curve sensitivity
-----------------

The calculators used for curve sensitivity calculation are

    PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
    ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
    MarketQuoteSensitivityBlockCalculator<MulticurveProviderInterface> MQSBC = new MarketQuoteSensitivityBlockCalculator<>(PSC);

In practice, to compute the sensitivity to market quotes, only one of them is enough:

    double BP1 = 1.0E-4;
    MultipleCurrencyParameterSensitivity pvmqsComputed = MQSBC.fromInstrument(FRA, MULTICURVE, BLOCK).multipliedBy(BP1);
The **MQSBC** compute the sensitiviyt to the market quotes. The computation is not scaled, i.e. it is for a movement of 1. The last part of the code (**multipliedBy**) multiply it by one basis point to match the market standard. In general, the OG-Analytics library uses absolute numbers (not percent or basis point) everywhere and provide the tools to rescale the output easily.

The format of the output is a *MultipleCurrencyParameterSensitivity* object which can be represented by

    [USD-DSCON-OIS, USD]= (-0.01, -0.01, 0.00, -0.01, -0.03, -0.55, -1.04, 0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00)
    [USD-LIBOR3M-FRAIRS, USD]= (119.74, 120.93, -26.46, -460.75, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00)
The information stored is, for each relevant curve and currency, the sensitivity to each market quote. In this case, with a short term FRA, most of the sensitivities are 0.

The example code also contains the same result computed in three steps to describe the internal process used to compute the market quote sensitivity.

    MultipleCurrencyMulticurveSensitivity pvPointSensi = FRA.accept(PVCSDC, MULTICURVE);
    MultipleCurrencyParameterSensitivity pvParameterSensi = PSC.pointToParameterSensitivity(pvPointSensi, MULTICURVE);
    MultipleCurrencyParameterSensitivity pvMarketQuoteSensi = MQSBC.fromParameterSensitivity(pvParameterSensi, BLOCK).multipliedBy(BP1);

The first step consists in computing the *point sensitivity*, i.e. the sensitivity with respect to each discount factor and each forward rate. This first output looks like

    USD=
    {USD-DSCON-OIS=[[0.6383561643835617, -14798.719687495473]]}
    {USD-LIBOR3M-FRAIRS=[ForwardSensitivity[start=0.6383561643835617, end=0.8876712328767123, af=0.25277777777777777, value=-2529910.310523003]]}
The instrument has sensitivity to one discounting points at time 0.63... for a value of -14,749. There is also the sensitivity to one forward rate which starts at time 0.63, finishes at time 0.88 with an accrual factor of 0.2527. The value of the sensitivity is 2,529,910 to a movement of 1.0 (100%) in the rate.

The second step consists in projecting the above sensitivity to the internal parameters of the curve. In this case both curves are represented by yield curve stored as interpolated curve on zero-coupon rates. The sensitivity obtained from this second step is the sensitivity to the zero-coupon rates (rescaled to one basis point):

    {[USD-DSCON-OIS, USD]= (0.0, 0.0, 0.0, 0.0, 0.0, -0.74, -0.73, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    [USD-LIBOR3M-FRAIRS, USD]= (0.0, 291.91, -78.14, -463.50, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) }

The third step consist in multiplying the sensitivity to the parameters by the Jacobian matrix to obtain the market quote sensitivity.

The (generalised) Jacobian matrices are stored in the **CurveBuildingBlockBundle**. It contains, fore each curve, the curve on which it depends and the transistion matrix between market quotes and curves parameters. In the above example the object look like:

    USD-DSCON-OIS=[
    {USD-DSCON-OIS=[0, 17]},
    1.0139, 0.0000, 0.0000, 0.0000, ...
    0.5069, 0.5069, 0.0000, 0.0000, ...
    0.0000, 0.0000, 1.0138, 0.0000, ...
    0.0179, 0.0179, -0.078, 1.0559, ...
    ...]
    USD-LIBOR3M-FRAIRS=[
    {USD-DSCON-OIS=[0, 17], USD-LIBOR3M-FRAIRS=[17, 15]}, 
    0.0000, 0.0000, 0.0000, ..., 0.0000, 1.0139, 0.0000, 0.0000, ...
    0.0000, 0.0000, 0.0000, ..., 0.0000, 0.5094, 0.5038, 0.0000, ...
    0.0000, 0.0000, 0.0000, ..., 0.0000, 0.3390, 0.3353, 0.3389, ...
    ...
    0.0000, 0.0000, 0.0000, ..., 0.0627, 0.0004, 0.0000, 0.0000, ...
    ...]
The first matrix is of dimension 17x17 (it has been cut to fit in the table). It contains the sensitivity of the USD-DSCON-OIS curve parameters to the input of the USD-DSCON-OIS curve. The sensitivitires appear mainly on the diagonal but not only there. There are small sensitivities off-diagonal. The second matrix is of dimension 15x(17+15). It contains the sensitivity of the USD-LIBOR3M-FRAIRS curve parameters to the USD-DSCON-OIS and USD-LIBOR3M-FRAIRS market data. Sensitivity are mainly on the diagonal of the second 15x15 block, but there are sensitivities everywhere: parameters of the USD-LIBOR3M-FRAIRS are not only dependent on the market quote of hte same curve but also on the market quote of the previous curve USD-DSCON-OIS.

----------

(1) Actually to price the *forward rate agreement* in the multi-curve framework, we also have suppose a deterministic spread hypothesis between the discounting and forward curves. This simplifying hypothesis is the standard one used for forward rate agreement pricing.
