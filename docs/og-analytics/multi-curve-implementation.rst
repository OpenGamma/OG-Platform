Multi-curve Implementation: Providers, Calculators and Sensitivities
=============

Introduction
------------

This note describes the implementation in the OG-analytics library of the multi-curve description, the curve sensitivity objects and the curve calibration process. The formulas used to price the different instruments are not described here. For the theory we refer to \cite{HEN.2014.1}. For the exact formulas used in the library we refer to \cite{OG.DOC01.1.0} and \cite{OG.DOC01.1.0}. The curve calibration setting, and in particular the conventions and the node available are described in \cite{OG.TECH01.1.0}.

Provider
--------

The base items for curves and volatilities description are called *providers*. They are  interfaces with methods providing financially meaningful quantities like risk-free discount factors associated to a currency and forward rates associated to an Ibor index or overnight index. The providers are independent of the actual implementation and of the way the data is stored.

**MulticurveProvider**


This is the main provider in the multi-curve framework. 

The interface is called **MulticurveProviderInterface**. Its main methods are 

    double getDiscountFactor(Currency ccy, Double time)
    double getForwardRate(IborIndex index, double startTime, double endTime, double accrualFactor)
    double getForwardRate(IndexON index, double startTime, double endTime, double accrualFactor)
    double getFxRate(final Currency ccy1, final Currency ccy2)
The methods provide the risk-free discount factor for a given currency at a given time to payment and the forward rate associated to a given Ibor-like or overnight index between two dates. The last method provides today's exchange rate between two currencies.

**InflationProvider**

This is the provider used for (linear) inflation products. On top of the multi-curve methods, it provides the estimated price index value linked to inflation products at a given time. 

Its main method is 

    double getPriceIndex(IndexPrice index, Double time)

**HullWhiteProvider**

This is the provider used for pricing methods in the Hull-White one-factor model with piecewise constant volatility. On top of the multi-curve methods, it provides the Hull-White one-factor parameters. The description of the model can be found in the documentation \cite{OG.DOC13.1.1}. 

Its main methods are 

    HullWhiteOneFactorPiecewiseConstantParameters getHullWhiteParameters();
Currency getHullWhiteCurrency();

The first method returns the set of model parameters. The second method indicates for which currency the parameters are valid. 

**IssuerProvider**

This is the provider used for (linear) products dependent of the instrument issuer (like bonds, bills or deposits). On top of the multi-curve methods, it provides the discount factor associated to a given issuer (or category of issuers) and a currency.

Its main methods is 

    double getDiscountFactor(LegalEntity issuer, Double time)

**Implementation**

Currently there are two implementations of the multi-curve interface: **MulticurveProviderDiscount** and **MulticurveProviderForward**. 

In both implementation the discounting curves and forward overnight curves are represented by **YieldAndDiscountCurve**. The curves are represented by (pseudo-)discount factors or equivalent quantities (like zero-coupon rates).

In the first implementation, the Ibor-forward rates are obtained through pseudo-discount factors. This is the standard implementation of forward curves as described in most literature. 

In the second implementation, the Ibor-forward rates are obtained directly and described by a **DoublesCurve**. The interpolation scheme, if any, will be applied to the forward rates directly. The theory behind this implementation  is described in \cite[Section 3.2]{HEN.2014.1}.

**Decorated provider**

In some cases one needs a provider which is very similar to another one with one curve or one point on a curve changed. The **decorated provider** have been created for that purpose. This is a implementation of the interface based on another implementation and one extra curve. In all cases, expect the specific case, the new implementation provides the same result as the underlying one. A ``if'' statement return the other curve value in the specific case.

This technique is used to create **MulticurveProviderDiscountingDecoratedIssuer**. An issuer curve is used as discounting curve for a given currency. This is useful to price bonds; the coupons and notional are priced as standard instruments with the currency risk-free curve replaced by the issuer curve.

**Other**

Other providers are available for model specific data requirements: SABR swaptions, Black swaption, SABR cap/floor, Black forex, smile forex, vanna-volga forex, Black equity, Libor Market Model, G2++, etc.