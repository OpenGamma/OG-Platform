Multi-curve Implementation: Providers, Calculators and Sensitivities
====================================================================

Introduction
------------

This note describes the implementation in the OG-analytics library of the multi-curve description, the curve sensitivity objects and the curve calibration process. The formulas used to price the different instruments are not described here. For the theory we refer to [HEN.2014.1]_. For the exact formulas used in the library we refer to [OG.2012b]_ and Quantitative Research (2012b). The curve calibration setting, and in particular the conventions and the node available are described in [OG.2013]_.

Provider
--------

The base items for curves and volatilities description are called *providers*. They are  interfaces with methods providing financially meaningful quantities like risk-free discount factors associated to a currency and forward rates associated to an Ibor index or overnight index. The providers are independent of the actual implementation and of the way the data is stored.

**MulticurveProvider**


This is the main provider in the multi-curve framework. 

The interface is called **MulticurveProviderInterface**. Its main methods are

.. code:: java

    double getDiscountFactor(Currency ccy, Double time)
    double getForwardRate(IborIndex index, double startTime, double endTime, double accrualFactor)
    double getForwardRate(IndexON index, double startTime, double endTime, double accrualFactor)
    double getFxRate(final Currency ccy1, final Currency ccy2)

The methods provide the risk-free discount factor for a given currency at a given time to payment and the forward rate associated to a given Ibor-like or overnight index between two dates. The last method provides today's exchange rate between two currencies.

**InflationProvider**

This is the provider used for (linear) inflation products. On top of the multi-curve methods, it provides the estimated price index value linked to inflation products at a given time. 

Its main method is::

    double getPriceIndex(IndexPrice index, Double time)

**HullWhiteProvider**

This is the provider used for pricing methods in the Hull-White one-factor model with piecewise constant volatility. On top of the multi-curve methods, it provides the Hull-White one-factor parameters. The description of the model can be found in the documentation  Quantitative Research (2012a).

Its main methods are::

    HullWhiteOneFactorPiecewiseConstantParameters getHullWhiteParameters();

Currency getHullWhiteCurrency();

The first method returns the set of model parameters. The second method indicates for which currency the parameters are valid. 

**IssuerProvider**

This is the provider used for (linear) products dependent of the instrument issuer (like bonds, bills or deposits). On top of the multi-curve methods, it provides the discount factor associated to a given issuer (or category of issuers) and a currency.

Its main methods is::

    double getDiscountFactor(LegalEntity issuer, Double time)

**Implementation**

Currently there are two implementations of the multi-curve interface: **MulticurveProviderDiscount** and **MulticurveProviderForward**. 

In both implementation the discounting curves and forward overnight curves are represented by **YieldAndDiscountCurve**. The curves are represented by (pseudo-)discount factors or equivalent quantities (like zero-coupon rates).

In the first implementation, the Ibor-forward rates are obtained through pseudo-discount factors. This is the standard implementation of forward curves as described in most literature. 

In the second implementation, the Ibor-forward rates are obtained directly and described by a **DoublesCurve**. The interpolation scheme, if any, will be applied to the forward rates directly. The theory behind this implementation  is described in [HEN.2014.1]_ (Section 3.2).

**Decorated provider**

In some cases one needs a provider which is very similar to another one with one curve or one point on a curve changed. The **decorated provider** have been created for that purpose. This is a implementation of the interface based on another implementation and one extra curve. In all cases, expect the specific case, the new implementation provides the same result as the underlying one. A "if" statement return the other curve value in the specific case.

This technique is used to create **MulticurveProviderDiscountingDecoratedIssuer**. An issuer curve is used as discounting curve for a given currency. This is useful to price bonds; the coupons and notional are priced as standard instruments with the currency risk-free curve replaced by the issuer curve.

**Other**

Other providers are available for model specific data requirements: SABR swaptions, Black swaption, SABR cap/floor, Black forex, smile forex, vanna-volga forex, Black equity, Libor Market Model, G2++, etc.

Calculator
----------

The different calculators (for present value, par spread, curve sensitivity, etc.) implement the **InstrumentDerivativeVisitor** interface. The type of data required is given by a specific provider.
The code for the declaration looks something like::

    extends AbstractInstrumentDerivativeVisitor <MulticurveProviderInterface, MultipleCurrencyAmount>

For calculators that require more complex data structure than the multi-curve, we use the composition. We use the base with

.. code:: java

    PresentValueDiscountingProviderCalculator PVDC = PresentValueDiscountingProviderCalculator.getInstance();
    public MultipleCurrencyAmount visit(InstrumentDerivative derivative, InflationProviderInterface multicurves) {
    try {
      return derivative.accept(this, multicurves); } catch (Exception e) {
      return derivative.accept(PVDC, multicurves.getMulticurveProvider()); }
    }

The calculator specific visitor implementation are added below.

Note that all calculators related to present value and present value sensitivities provide multiple- currency results (**MultipleCurrencyAmount** or **MultipleCurrencyMulticurveSensitivity**). It should be clear for each number in which currency it is express.




Curve sensitivities
-------------------
**MulticurveSensitivity**

This is the base object to store the sensitivity to each point of the curves used in the pricing. The data is stored as a map of sensitivities associated to strings representing the curve name.
The sensitivity has two parts. The first one is the sensitivity to the discounting

.. code:: java

    Map<String, List<DoublesPair>> _sensitivityYieldDiscounting

For each curve (String) the list contains the payment times and the sensitivity to the zero-coupon rate at that time. Each payment date will have its own sensitivity. The sensitivities are not grouped by curve node or parameter at this stage.
The second part is the sensitivity to the forward rates::

    Map<String, List<ForwardSensitivity>> _sensitivityForward 

Each **ForwardSensitivity** contains::

    double _startTime; 
    double _endTime;
    double _accrualFactor; 
    double _value;

For each curve, the object stored the sensitivity with respect to each forward rate, described by its start date, end date and accrual factor. The sensitivities are stored for each forward. This allows to produce reset or fixing reports, with details of each date (not only average between nodes).

**MultipleCurrencyMulticurveSensitivity**

A multiple currencies version of **MulticurveSensitivity**. For each currency, there is one multi-curves sensitivity. The sensitivity currency can be different from the curve currency. When curves are based on cross-currency instruments, one can have a sensitivity in one currency from changing the curve in another currency.

**SimpleParameterSensitivity**

Represent the sensitivity of a (dimensionless) value to the parameters used in the curves descriptions. The object is implemented as a map between a string and a DoubleMatrix1D. The vectors associated to a given curve always have the same size as the number of parameters is fixed.

**MultipleCurrencyParameterSensitivity**

Represent the sensitivity of a currency dependent value (like the present value) to the parameters used in the curves descriptions. The object is implemented as a map between a Pair<Currency, String> and a DoubleMatrix1D. The string is the curve name and the currency is the currency of the sensitivity. The vectors associated to a given curve always have the same size as the number of parameters is fixed. Those objet can be added easily using addition on **DoubleMatrix1D**.

**CurveBuildingBlock**

The curves linked together as a map of their name to the number of parameters. The map key is the string with the curve name. The data is composed of a pair of integers. The first one is the start index of the curve parameters in the array of all parameters of the block. The second integer is the number of parameters in the curve. See Table 1 for a couple of examples embedded in a larger object.
The idea behind the object is that at calibration time, the parameters in a given curve will de- pend on the inputs of the curves previously or simultaneously calibrated. The huber of parameters and their order are recorded to be used in the sensitivity computation at a later stage.

**CurveBuildingBlockBundle**

The object represents a bundle of different curve and for each of them the CurveBuildingBlock and the associated (inverse) Jacobian matrix. The data is stored as a map from the curve name (string) to a pair of **CurveBuildingBlock** and **DoubleMatrix2D**.

An example of object content for very simplified curves is given in Table 1. In that example we have two curve, the USD Dsc curve and the USD Fwd 3M curve. The first one depends only on itself and has 5 parameters. The matrix provided is the derivative of the curve parameters to the market quotes used to calibrate the curve. The second curve depends on the previous curve and the current curve. The previous curve has 5 parameters and the current one has 4 parameters. The matrix is a 4x9 matrix. It represent the sensitivity of the 4 parameters of the current curve to the 9 relevant market quotes: the 5 from the previous curve and the 4 from the current one.

USD Dsc =
{USD Dsc=[0, 5]}

+------+-------+-------+-------+------+
| 1.01 |  0.00 |  0.00 |  0.00 | 0.00 |
+------+-------+-------+-------+------+
| 0.00 |  1.01 |  0.00 |  0.00 | 0.00 |
+------+-------+-------+-------+------+
| 0.01 | -0.03 |  1.01 |  0.00 | 0.00 |
+------+-------+-------+-------+------+
| 0.01 |  0.00 | -0.02 |  0.99 | 0.00 |
+------+-------+-------+-------+------+
| 0.00 |  0.00 |  0.00 | -0.03 | 1.00 |
+------+-------+-------+-------+------+


USD Fwd 3M =
{USD Dsc=[0, 5], USD Fwd 3M=[5, 4]}

+------+-------+-------+-------+-------+-------+-------+-------+------+
| 0.00 | 0.00  | 0.00  |  0.00 |  0.00 |  1.00 |  0.00 |  0.00 | 0.00 |
+------+-------+-------+-------+-------+-------+-------+-------+------+
| 0.00 | 0.00  | 0.00  |  0.00 |  0.00 |  0.51 |  0.50 |  0.00 | 0.00 |
+------+-------+-------+-------+-------+-------+-------+-------+------+
| 0.00 | 0.00  | 0.00  | -0.01 |  0.00 |  0.00 |  0.00 |  0.99 | 0.00 |
+------+-------+-------+-------+-------+-------+-------+-------+------+
| 0.00 | 0.00  | 0.00  |  0.00 | -0.01 |  0.00 |  0.00 | -0.02 | 1.00 |
+------+-------+-------+-------+-------+-------+-------+-------+------+

Table 1: A simplified example of **CurveBuildingBlockBundle**. The example contains two curves, one which depends only on itself and a second one that depends also on the previous one.

.. rubric:: References

.. [HEN.2014.1] Henrard, M. (2014). Interest Rate Modelling in the Multi-curve Framework: Foundations, Evolution and Implementation. Applied Quantitative Finance. Palgrave Macmillan. ISBN: 978-1-137- 37465-3.

.. [OG.2012b] Quantitative Research (2012b). The Analytic Framework for Implying Yield Curves from Market Data, version 1.0. OpenGamma Documentation 1, OpenGamma. Available at http://docs.opengamma.com/display/DOC/Analytics.

.. [OG.2013] Quantitative Research (2013). Curve calibration in opengamma platform. Technical Documenta- tion 1, OpenGamma. Version 1.0.
