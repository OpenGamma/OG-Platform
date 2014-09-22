/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorIborInArrearsSmileModelCapGenericReplicationMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborInArrearsSmileModelReplicationMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.InterpolatedSmileFunction;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pairs;

/**
 * This class wraps {@link CapletStripper} and {@link GeneralSmileInterpolator}. 
 * Given multi-curves and market caps, derive caplet volatility surface. 
 * Then the volatility surface is used to compute pv of target in-arrear product
 */
public class CouponInArrearsCalculation {
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRC = ParRateDiscountingCalculator.getInstance();

  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC =
      PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC =
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final MarketQuoteSensitivityBlockCalculator<MulticurveProviderInterface> MQSBC =
      new MarketQuoteSensitivityBlockCalculator<>(PSC);

  private final double[] _mrkPrices;
  private final double[] _errors;
  private final CapletStrippingResult _capletStrippingResult;
  private final MulticurveProviderInterface _curves;
  private final InterpolatedDoublesSurface _surface;

  private final double _time;

  /**
   * Constructor
   * @param stripper The caplet stripper
   * @param caps The market caps
   * @param mrkPrices The market cap prices/volatilities
   * @param type PRICE or VOL
   * @param errors The error values
   * @param guess The guess parameters
   * @param curves The multi curve
   */
  public CouponInArrearsCalculation(CapletStripper stripper, List<CapFloor> caps, double[] mrkPrices,
      MarketDataType type, double[] errors, DoubleMatrix1D guess, MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(stripper, "stripper");
    ArgumentChecker.notNull(caps, "caps");
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(guess, "guess");
    ArgumentChecker.notNull(curves, "curves");

    ArgumentChecker.notNull(mrkPrices, "mrkPrices");
    ArgumentChecker.notNull(errors, "errors");

    _mrkPrices = Arrays.copyOf(mrkPrices, mrkPrices.length);
    _errors = Arrays.copyOf(errors, errors.length);

    // perform caplet stripping
    long t0 = System.nanoTime();
    _capletStrippingResult = stripper.solve(_mrkPrices, type, _errors, guess);
    long t1 = System.nanoTime();
    _time = (t1 - t0) * 1e-9;
    _curves = curves;

    // The stripper works with discrete caplets. Need to construct a continuous surface in order to sample at any expiry-strike
    // For strippers that work with a volatility surface (e.g. the smile based strippers), we have thrown away information
    // which would be useful here
    DoublesPair[] expStrikes = _capletStrippingResult.getPricer().getExpiryStrikeArray();
    DoubleMatrix1D vols = _capletStrippingResult.getCapletVols();
    CombinedInterpolatorExtrapolator interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
        Interpolator1DFactory.LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    GridInterpolator2D interpolator2D = new GridInterpolator2D(interpolator, interpolator);

    _surface = new InterpolatedDoublesSurface(expStrikes, vols.getData(), interpolator2D);

  }

  /**
   * Computes non-corrected price
   * @param option caplet as option
   * @return The caplet price
   */
  public double simpleOptionPrice(SimpleOptionData option) {
    ArgumentChecker.notNull(option, "option");
    return BlackFormulaRepository.price(option, _surface.getZValue(option.getTimeToExpiry(), option.getStrike()));
  }

  /**
   * Computes non-corrected price
   * @param caplet The in-arrears caplet 
   * @return The caplet price
   */
  public double simpleCapletPrice(CapFloorIbor caplet) {
    ArgumentChecker.notNull(caplet, "caplet");
    // Construct a "standard" CapFloorIbor whose paymentTime is set to be fixingPeriodEndTime
    CapFloorIbor capStandard = new CapFloorIbor(caplet.getCurrency(), caplet.getFixingPeriodEndTime(),
        caplet.getPaymentYearFraction(), caplet.getNotional(), caplet.getFixingTime(), caplet.getIndex(),
        caplet.getFixingPeriodStartTime(), caplet.getFixingPeriodEndTime(), caplet.getFixingAccrualFactor(),
        caplet.getStrike(), caplet.isCap());
    SimpleOptionData option = CapFloorDecomposer.toOption(capStandard, _curves);
    return simpleOptionPrice(option);
  }

  /**
   * Compute present value of in-arrears caplet
   * @param caplet The caplet being priced
   * @param interpolator The smile interpolator and extrapolator
   * @return The present value
   */
  public MultipleCurrencyAmount presentValue(CapFloorIbor caplet, final GeneralSmileInterpolator interpolator) {
    ArgumentChecker.notNull(caplet, "caplet");
    ArgumentChecker.notNull(interpolator, "interpolator");

    double expiry = caplet.getFixingTime();

    // Pick up relevant caplet strikes and vols
    double[] sampleStrikes = _capletStrippingResult.getPricer().getStrikes();
    int nStrikes = sampleStrikes.length;
    double[] sampleVols = new double[nStrikes];
    for (int i = 0; i < nStrikes; i++) {
      sampleVols[i] = _surface.getZValue(expiry, sampleStrikes[i]);
    }

    // construct a interpolated/extrapolated smile
    double forward = caplet.accept(PRC, _curves);
    InterpolatedSmileFunction smileFunction = new InterpolatedSmileFunction(interpolator, forward, sampleStrikes,
        expiry, sampleVols);

    // compute pv 
    CapFloorIborInArrearsSmileModelCapGenericReplicationMethod inArrearsCal = new CapFloorIborInArrearsSmileModelCapGenericReplicationMethod(
        smileFunction);
    return inArrearsCal.presentValue(caplet, _curves);
  }

  /**
   * Compute present value of coupon in arrears
   * @param couponIbor The coupon being priced
   * @param interpolator The smile interpolator and extrapolator
   * @return The present value
   */
  public MultipleCurrencyAmount presentValue(CouponIbor couponIbor, final GeneralSmileInterpolator interpolator) {
    ArgumentChecker.notNull(couponIbor, "couponIbor");
    ArgumentChecker.notNull(interpolator, "interpolator");

    double expiry = couponIbor.getFixingTime();

    // Pick up relevant caplet strikes and vols
    double[] sampleStrikes = _capletStrippingResult.getPricer().getStrikes();
    int nStrikes = sampleStrikes.length;
    double[] sampleVols = new double[nStrikes];
    for (int i = 0; i < nStrikes; i++) {
      sampleVols[i] = _surface.getZValue(expiry, sampleStrikes[i]);
    }

      // construct a interpolated/extrapolated smile
    double forward = _curves.getSimplyCompoundForwardRate(couponIbor.getIndex(),
          couponIbor.getFixingPeriodStartTime(), couponIbor.getFixingPeriodEndTime(),
          couponIbor.getFixingAccrualFactor());
    InterpolatedSmileFunction smileFunction = new InterpolatedSmileFunction(interpolator, forward, sampleStrikes,
          expiry, sampleVols);

    // compute pv 
    CouponIborInArrearsSmileModelReplicationMethod inArrearsCal = new CouponIborInArrearsSmileModelReplicationMethod(
          smileFunction);
    return inArrearsCal.presentValue(couponIbor, _curves);
  }

  /**
   * Compute present value of in-arrears swap
   * @param swap The swap being priced, assuming 
   * @param interpolator The smile interpolator and extrapolator
   * @return The present value
   */
  public MultipleCurrencyAmount presentValue(Swap<? extends Payment, ? extends Payment> swap,
      final GeneralSmileInterpolator interpolator) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(interpolator, "interpolator");

    MultipleCurrencyAmount firstPV;
    MultipleCurrencyAmount secondPV;

    if (swap.getFirstLeg().getNthPayment(0) instanceof CouponIbor) {
      firstPV = presentValue((CouponIbor) swap.getFirstLeg().getNthPayment(0), interpolator);
      for (int j = 1; j < swap.getFirstLeg().getNumberOfPayments(); j++) {
        firstPV = firstPV.plus(presentValue((CouponIbor) swap.getFirstLeg().getNthPayment(j),
            interpolator));
      }
    } else {
      firstPV = swap.getFirstLeg().accept(PVDC, _curves);
    }

    if (swap.getSecondLeg().getNthPayment(0) instanceof CouponIbor) {
      secondPV = presentValue((CouponIbor) swap.getSecondLeg().getNthPayment(0), interpolator);
      for (int j = 1; j < swap.getSecondLeg().getNumberOfPayments(); j++) {
        secondPV = secondPV.plus(presentValue((CouponIbor) swap.getSecondLeg().getNthPayment(j),
            interpolator));
      }
    } else {
      secondPV = swap.getSecondLeg().accept(PVDC, _curves);
    }

    return firstPV.plus(secondPV);
  }

  /**
   * Compute parameter sensitivity for coupon in arrears
   * @param couponIbor The coupon in arrears
   * @param interpolator The smile interpolator and extrapolator
   * @return The parameter sensitivity
   */
  public MultipleCurrencyParameterSensitivity presentValueCurveSensitivity(final CouponIbor couponIbor,
      final GeneralSmileInterpolator interpolator) {
    ArgumentChecker.notNull(couponIbor, "couponIbor");
    ArgumentChecker.notNull(interpolator, "interpolator");

    double expiry = couponIbor.getFixingTime();

    // Pick up relevant caplet strikes and vols
    double[] sampleStrikes = _capletStrippingResult.getPricer().getStrikes();
    int nStrikes = sampleStrikes.length;
    double[] sampleVols = new double[nStrikes];
    for (int i = 0; i < nStrikes; i++) {
      sampleVols[i] = _surface.getZValue(expiry, sampleStrikes[i]);
    }

      // construct a interpolated/extrapolated smile
    double forward = _curves.getSimplyCompoundForwardRate(couponIbor.getIndex(),
          couponIbor.getFixingPeriodStartTime(), couponIbor.getFixingPeriodEndTime(),
          couponIbor.getFixingAccrualFactor());
    InterpolatedSmileFunction smileFunction = new InterpolatedSmileFunction(interpolator, forward, sampleStrikes,
          expiry, sampleVols);

    // compute pv 
    CouponIborInArrearsSmileModelReplicationMethod inArrearsCal = new CouponIborInArrearsSmileModelReplicationMethod(
          smileFunction);
    MultipleCurrencyMulticurveSensitivity sense = inArrearsCal.presentValueCurveSensitivity(couponIbor, _curves);

    return pointToParameterSensitivity(sense);
  }

  private MultipleCurrencyParameterSensitivity pointToParameterSensitivity(
      final MultipleCurrencyMulticurveSensitivity sensitivity) {
    MultipleCurrencyParameterSensitivity result = new MultipleCurrencyParameterSensitivity();
    // YieldAndDiscount
    for (final Currency ccySensi : sensitivity.getCurrencies()) {
      final Map<String, List<DoublesPair>> sensitivityDsc = sensitivity.getSensitivity(ccySensi)
          .getYieldDiscountingSensitivities();
      for (final Map.Entry<String, List<DoublesPair>> entry : sensitivityDsc.entrySet()) {
        if (_curves.getAllNames().contains(entry.getKey())) {
          result = result
              .plus(Pairs.of(entry.getKey(), ccySensi),
                  new DoubleMatrix1D(_curves.parameterSensitivity(entry.getKey(), entry.getValue())));
        }
      }
    }
    // Forward
    for (final Currency ccySensi : sensitivity.getCurrencies()) {
      final Map<String, List<ForwardSensitivity>> sensitivityFwd = sensitivity.getSensitivity(ccySensi)
          .getForwardSensitivities();
      for (final Map.Entry<String, List<ForwardSensitivity>> entry : sensitivityFwd.entrySet()) {
        if (_curves.getAllNames().contains(entry.getKey())) {
          result = result.plus(Pairs.of(entry.getKey(), ccySensi),
              new DoubleMatrix1D(_curves.parameterForwardSensitivity(entry.getKey(), entry.getValue())));
        }
      }
    }
    return result;
  }

  /**
   * Compute parameter sensitivity of swap
   * @param swap The swap
   * @param blockBundle Block bundle
   * @param interpolator The smile interpolator and extrapolator
   * @return The parameter sensitivity
   */
  public MultipleCurrencyParameterSensitivity presentValueCurveSensitivity(
      final Swap<? extends Payment, ? extends Payment> swap, final CurveBuildingBlockBundle blockBundle,
      final GeneralSmileInterpolator interpolator) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(blockBundle, "blockBundle");
    ArgumentChecker.notNull(interpolator, "interpolator");

    MultipleCurrencyParameterSensitivity firstSense;
    MultipleCurrencyParameterSensitivity secondSense;

    if (swap.getFirstLeg().getNthPayment(0) instanceof CouponIbor) {
      firstSense = presentValueCurveSensitivity((CouponIbor) swap.getFirstLeg()
          .getNthPayment(0), interpolator);
      for (int j = 1; j < swap.getFirstLeg().getNumberOfPayments(); j++) {
        firstSense = firstSense
            .plus(presentValueCurveSensitivity((CouponIbor) swap.getFirstLeg().getNthPayment(j), interpolator));
      }
    } else {
      firstSense = MQSBC.fromInstrument(swap.getFirstLeg(), _curves, blockBundle);
    }

    if (swap.getSecondLeg().getNthPayment(0) instanceof CouponIbor) {
      secondSense = presentValueCurveSensitivity((CouponIbor) swap.getSecondLeg()
          .getNthPayment(0), interpolator);
      for (int j = 1; j < swap.getSecondLeg().getNumberOfPayments(); j++) {
        secondSense = secondSense.plus(presentValueCurveSensitivity((CouponIbor) swap.getSecondLeg().getNthPayment(j),
            interpolator));
      }
    } else {
      secondSense = MQSBC.fromInstrument(swap.getSecondLeg(), _curves, blockBundle);
    }

    MultipleCurrencyParameterSensitivity res = firstSense.plus(secondSense);
    return res;
  }

  /**
   * Compute par rate of swap (assuming the first leg is fixed leg)
   * @param swap The swap
   * @param interpolator The smile interpolator and extrapolator
   * @return The par rate
   */
  public Double parRate(final Swap<? extends Payment, ? extends Payment> swap,
      final GeneralSmileInterpolator interpolator) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(interpolator, "interpolator");

    MultipleCurrencyAmount pvSecond;

    Annuity<? extends Payment> annuity = swap.getFirstLeg();
    ArgumentChecker.isTrue(annuity.getNthPayment(0) instanceof Coupon, "The first leg should be coupon payment");
    double pvbp = 0;
    for (int loopcpn = 0; loopcpn < annuity.getPayments().length; loopcpn++) {
      pvbp += ((Coupon) annuity.getNthPayment(loopcpn)).getPaymentYearFraction() *
          Math.abs(((Coupon) annuity.getNthPayment(loopcpn)).getNotional())
          *
          _curves.getDiscountFactor(annuity.getNthPayment(loopcpn).getCurrency(),
              annuity.getNthPayment(loopcpn).getPaymentTime());
    }

    if (swap.getSecondLeg().getNthPayment(0) instanceof CouponIbor) {
      pvSecond = presentValue((CouponIbor) swap.getSecondLeg().getNthPayment(0), interpolator);
      for (int j = 1; j < swap.getSecondLeg().getNumberOfPayments(); j++) {
        pvSecond = pvSecond.plus(presentValue((CouponIbor) swap.getSecondLeg().getNthPayment(j),
            interpolator));
      }
    } else {
      pvSecond = swap.getSecondLeg().accept(PVDC, _curves);
    }
    double pvSecondDouble = pvSecond.getAmount(swap.getSecondLeg().getCurrency()) *
        Math.signum(((Coupon) swap.getSecondLeg().getNthPayment(0)).getNotional());

    return pvSecondDouble / pvbp;
  }

  /**
   * Gets the time.
   * @return the time
   */
  public double getTime() {
    return _time;
  }

  /**
   * Gets the chi-square
   * @return chi-square
   */
  public double getChiSq() {
    return _capletStrippingResult.getChiSqr();
  }
}
