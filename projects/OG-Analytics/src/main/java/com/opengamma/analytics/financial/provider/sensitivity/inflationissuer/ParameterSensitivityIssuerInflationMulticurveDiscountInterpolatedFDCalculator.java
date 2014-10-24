/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.inflationissuer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveSimple;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * For an instrument, computes the sensitivity of a multiple currency amount (often the present value) to the parameters used in the curve including inflation curve.
 * The computation is done by shifting each node point in each curve; the curves must be interpolated yield curves for discounting and forward curves.
 * The return format is MultipleCurrencyParameterSensitivity object.
 * This is a very inefficient way to compute the sensitivities. It should be used only for tests purposes or when speed is irrelevant. 
 */
public class ParameterSensitivityIssuerInflationMulticurveDiscountInterpolatedFDCalculator {

  /**
   * The value calculator.
   */
  private final InstrumentDerivativeVisitor<ParameterInflationIssuerProviderInterface, MultipleCurrencyAmount> _valueCalculator;
  /**
   * The shift used for finite difference.
   */
  private final double _shift;

  /**
   * Constructor
   * @param valueCalculator The value calculator.
   * @param shift The shift used for finite difference.
   */
  public ParameterSensitivityIssuerInflationMulticurveDiscountInterpolatedFDCalculator(
      final InstrumentDerivativeVisitor<ParameterInflationIssuerProviderInterface, MultipleCurrencyAmount> valueCalculator,
      final double shift) {
    ArgumentChecker.notNull(valueCalculator, "Calculator");
    _valueCalculator = valueCalculator;
    _shift = shift;
  }

  /**
   * Compute the sensitivity by finite difference on all points. The curves must be interpolated yield curves.
   * Only the discounting and forward curves sensitivity is computed.
   * @param instrument The instrument.
   * @param inflationIssuer The market (all discounting and forward curves should be of the type YieldCurve with InterpolatedDoublesCurve.
   * @return The parameter sensitivity.
   */
  public MultipleCurrencyParameterSensitivity calculateSensitivity(final InstrumentDerivative instrument, final InflationIssuerProviderDiscount inflationIssuer) {
    MultipleCurrencyParameterSensitivity result = new MultipleCurrencyParameterSensitivity();
    final MultipleCurrencyAmount pvInit = instrument.accept(_valueCalculator, inflationIssuer);
    final MultipleCurrencyAmount pvInitMinus = pvInit.multipliedBy(-1.0);
    final int nbCcy = pvInit.size();
    final List<Currency> ccyList = new ArrayList<>();
    for (int loopccy = 0; loopccy < nbCcy; loopccy++) {
      ccyList.add(pvInit.getCurrencyAmounts()[loopccy].getCurrency());
    }

    // Inflation
    final Set<IndexPrice> indexPrice = inflationIssuer.getPriceIndexes();
    for (final IndexPrice index : indexPrice) {
      final PriceIndexCurve curveIndex = inflationIssuer.getCurve(index);
      ArgumentChecker.isTrue(curveIndex instanceof PriceIndexCurveSimple, "PriceIndexCurve should be of type PriceIndexCurveSimple");
      PriceIndexCurveSimple curveIndexSimple = (PriceIndexCurveSimple) curveIndex;
      ArgumentChecker.isTrue(curveIndexSimple.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveIndexSimple.getCurve();
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[][] sensitivity = new double[nbCcy][nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        final double[] yieldBumped = curveInt.getYDataAsPrimitive().clone();
        yieldBumped[loopnode] += _shift;
        final PriceIndexCurveSimple dscBumped = new PriceIndexCurveSimple(new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumped, curveInt.getInterpolator(), true));
        final InflationIssuerProviderDiscount marketDscBumped =
            new InflationIssuerProviderDiscount(inflationIssuer.getInflationProvider().withPriceIndex(index, dscBumped), inflationIssuer.getIssuerProvider());
        final MultipleCurrencyAmount pvBumped = instrument.accept(_valueCalculator, marketDscBumped);
        final MultipleCurrencyAmount pvDiff = pvBumped.plus(pvInitMinus);
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)) / _shift;
        }
      }
      final String name = inflationIssuer.getName(index);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(Pairs.of(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }

    // Discounting
    final Set<Currency> ccyDiscounting = inflationIssuer.getCurrencies();
    for (final Currency ccy : ccyDiscounting) {
      final YieldAndDiscountCurve curve = inflationIssuer.getCurve(ccy);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      final YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[][] sensitivity = new double[nbCcy][nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        final double[] yieldBumped = curveInt.getYDataAsPrimitive().clone();
        yieldBumped[loopnode] += _shift;
        final YieldAndDiscountCurve dscBumped = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumped, curveInt.getInterpolator(), true));
        final InflationIssuerProviderDiscount marketDscBumped =
            new InflationIssuerProviderDiscount(inflationIssuer.getInflationProvider().withDiscountFactor(ccy, dscBumped), inflationIssuer.getIssuerProvider());
        final MultipleCurrencyAmount pvBumped = instrument.accept(_valueCalculator, marketDscBumped);
        final MultipleCurrencyAmount pvDiff = pvBumped.plus(pvInitMinus);
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)) / _shift;
        }
      }
      final String name = inflationIssuer.getName(ccy);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(Pairs.of(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }
    // Forward ON
    final Set<IndexON> indexON = inflationIssuer.getIndexesON();
    for (final IndexON index : indexON) {
      final YieldAndDiscountCurve curve = inflationIssuer.getCurve(index);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      final YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[][] sensitivity = new double[nbCcy][nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        final double[] yieldBumped = curveInt.getYDataAsPrimitive().clone();
        yieldBumped[loopnode] += _shift;
        final YieldAndDiscountCurve fwdBumped = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumped, curveInt.getInterpolator(), true));
        final InflationIssuerProviderDiscount marketFwdBumped =
            new InflationIssuerProviderDiscount(inflationIssuer.getInflationProvider().withForward(index, fwdBumped), inflationIssuer.getIssuerProvider());
        final MultipleCurrencyAmount pvBumped = instrument.accept(_valueCalculator, marketFwdBumped);
        final MultipleCurrencyAmount pvDiff = pvBumped.plus(pvInitMinus);
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)) / _shift;
        }
      }
      final String name = inflationIssuer.getName(index);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(Pairs.of(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }
    // Forward Ibor
    final Set<IborIndex> indexForward = inflationIssuer.getIndexesIbor();
    for (final IborIndex index : indexForward) {
      final YieldAndDiscountCurve curve = inflationIssuer.getCurve(index);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      final YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[][] sensitivity = new double[nbCcy][nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        final double[] yieldBumped = curveInt.getYDataAsPrimitive().clone();
        yieldBumped[loopnode] += _shift;
        final YieldAndDiscountCurve fwdBumped = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumped, curveInt.getInterpolator(), true));
        final InflationIssuerProviderDiscount marketFwdBumped =
            new InflationIssuerProviderDiscount(inflationIssuer.getInflationProvider().withForward(index, fwdBumped), inflationIssuer.getIssuerProvider());
        final MultipleCurrencyAmount pvBumped = instrument.accept(_valueCalculator, marketFwdBumped);
        final MultipleCurrencyAmount pvDiff = pvBumped.plus(pvInitMinus);
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)) / _shift;
        }
      }
      final String name = inflationIssuer.getName(index);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(Pairs.of(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }

    // Discounting issuer
    final Set<Pair<Object, LegalEntityFilter<LegalEntity>>> issuerCcies = inflationIssuer.getIssuers();
    for (final Pair<Object, LegalEntityFilter<LegalEntity>> ic : issuerCcies) {
      final YieldAndDiscountCurve curve = inflationIssuer.getCurve(ic);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      final YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[][] sensitivity = new double[nbCcy][nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        final double[] yieldBumped = curveInt.getYDataAsPrimitive().clone();
        yieldBumped[loopnode] += _shift;
        final YieldAndDiscountCurve icBumped = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumped, curveInt.getInterpolator(), true));
        final InflationIssuerProviderDiscount providerIcBumped =
            new InflationIssuerProviderDiscount(inflationIssuer.getInflationProvider(), inflationIssuer.getIssuerProvider().withIssuerCurve(ic, icBumped));
        final MultipleCurrencyAmount pvBumped = instrument.accept(_valueCalculator, providerIcBumped);
        final MultipleCurrencyAmount pvDiff = pvBumped.plus(pvInitMinus);
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)) / _shift;
        }
      }
      final String name = inflationIssuer.getIssuerProvider().getName(ic);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(Pairs.of(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }
    return result;
  }

}
