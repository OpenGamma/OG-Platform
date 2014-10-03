/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.InterpolatedSmileFunction;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of a Ibor cap/floor in arrears.
 *  The cap/floor are supposed to be exactly in arrears. 
 *  The payment date is ignored and the start fixing period date is used instead.
 */
public class CapFloorIborInArrearsSmileModelCapGenericReplicationMethod {
  private static final Logger LOGGER = LoggerFactory
      .getLogger(CapFloorIborInArrearsSmileModelCapGenericReplicationMethod.class);

  private static final int MINIMUM_STEP = 6;
  private static final int MAX_COUNT = 10; // the maximum iteration count 
  private static final double ABS_TOL = 1.0;
  private static final double REL_TOL = 1E-10;
  private static final double REL_ERROR = 1E-9;
  private static final RungeKuttaIntegrator1D INTEGRATOR = new RungeKuttaIntegrator1D(ABS_TOL, REL_TOL, MINIMUM_STEP);

  private final InterpolatedSmileFunction _smileFunction;

  /**
   * Constructor specifying interpolated (and extrapolated) smile
   * @param smileFunction The interpolated smile
   */
  public CapFloorIborInArrearsSmileModelCapGenericReplicationMethod(final InterpolatedSmileFunction smileFunction) {
    ArgumentChecker.notNull(smileFunction, "smileFunction");
    _smileFunction = smileFunction;
  }

  /**
   * Computes the present value of an Ibor cap/floor in arrears by replication based on the paper, 
   * "Swap and Cap/Floors with Fixing in Arrears or Payment Delay," OpenGamma Quantitative Documentation
   * http://developers.opengamma.com/quantitative-research/In-Arrears-and-Payment-Delay-Swaps-and-Caps-OpenGamma.pdf
   * @param cap The cap/floor
   * @param curves The curves
   * @return The present value
   */
  public MultipleCurrencyAmount presentValue(final CapFloorIbor cap, final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(cap, "The cap/floor shoud not be null");
    ArgumentChecker.notNull(curves, "curves");
    final Currency ccy = cap.getCurrency();
    // Construct a "standard" CapFloorIbor whose paymentTime is set to be fixingPeriodEndTime
    CapFloorIbor capStandard = new CapFloorIbor(cap.getCurrency(), cap.getFixingPeriodEndTime(),
        cap.getPaymentYearFraction(), cap.getNotional(), cap.getFixingTime(), cap.getIndex(),
        cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor(), cap.getStrike(),
        cap.isCap());
    final double forward = curves.getSimplyCompoundForwardRate(cap.getIndex(), cap.getFixingPeriodStartTime(),
        cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor());
    final double beta = (1.0 + cap.getFixingAccrualFactor() * forward) *
        curves.getDiscountFactor(ccy, cap.getFixingPeriodEndTime())
        / curves.getDiscountFactor(ccy, cap.getFixingPeriodStartTime());

    final double df = curves.getDiscountFactor(capStandard.getCurrency(), capStandard.getPaymentTime());
    final double strikePart = (1.0 + cap.getFixingAccrualFactor() * capStandard.getStrike()) *
        presentValueStandard(forward, capStandard.getStrike(), capStandard.getFixingTime(),
            capStandard.isCap(), df, capStandard.getNotional(), capStandard.getPaymentYearFraction());

    final InArrearsIntegrant integrant = new InArrearsIntegrant(capStandard, curves);
    double integralPart;
    try {
      if (cap.isCap()) {
        double atmVol = _smileFunction.getVolatility(forward);
        double upper = forward * Math.exp(6.0 * atmVol * Math.sqrt(cap.getFixingTime()));
        double strike = cap.getStrike();
        integralPart = INTEGRATOR.integrate(integrant, strike, upper);
        double reminder = integrant.evaluate(upper) * upper;
        double error = reminder / integralPart;

        int count = 0;
        while (Math.abs(error) > REL_ERROR && count < MAX_COUNT) {
          integralPart += INTEGRATOR.integrate(integrant, upper, 2.0 * upper);
          upper *= 2.0;
          // The increase of integralPart in the next loop is bounded by reminder
          reminder = integrant.evaluate(upper) * upper;
          error = reminder / integralPart;
          ++count;
          if (count == MAX_COUNT) {
            LOGGER.info("Maximum iteration count, " + MAX_COUNT +
                ", has been reached. Relative error is greater than " + REL_ERROR);
          }
        }
      } else {
        double strike = cap.getStrike();
        integralPart = INTEGRATOR.integrate(integrant, REL_TOL * strike, strike);
      }
    } catch (final Exception e) {
      throw new MathException(e);
    }
    integralPart *= 2.0 * cap.getFixingAccrualFactor();
    final double pv = (strikePart + integralPart) / beta;
    return MultipleCurrencyAmount.of(cap.getCurrency(), pv);
  }
  
  /**
   * Computes the pv sensitivity of an Ibor cap/floor in arrears
   * @param cap The cap/floor
   * @param curves The curves
   * @return The sensitivity
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CapFloorIbor cap,
      final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(cap, "The cap/floor shoud not be null");
    ArgumentChecker.notNull(curves, "curves");
    final Currency ccy = cap.getCurrency();
    // Construct a "standard" CapFloorIbor whose paymentTime is set to be fixingPeriodEndTime
    CapFloorIbor capStandard = new CapFloorIbor(cap.getCurrency(), cap.getFixingPeriodEndTime(),
        cap.getPaymentYearFraction(), cap.getNotional(), cap.getFixingTime(), cap.getIndex(),
        cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor(), cap.getStrike(),
        cap.isCap());
    final double forward = curves.getSimplyCompoundForwardRate(cap.getIndex(), cap.getFixingPeriodStartTime(),
        cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor());
    final double beta = (1.0 + cap.getFixingAccrualFactor() * forward) *
        curves.getDiscountFactor(ccy, cap.getFixingPeriodEndTime())
        / curves.getDiscountFactor(ccy, cap.getFixingPeriodStartTime());

    double df = curves.getDiscountFactor(capStandard.getCurrency(), capStandard.getPaymentTime());
    double strikePart = (1.0 + cap.getFixingAccrualFactor() * capStandard.getStrike()) *
        presentValueStandard(forward, capStandard.getStrike(), capStandard.getFixingTime(),
            capStandard.isCap(), df, capStandard.getNotional(), capStandard.getPaymentYearFraction());
    double strikePartDelta = (1.0 + cap.getFixingAccrualFactor() * capStandard.getStrike()) *
        presentValueDeltaStandard(forward, capStandard.getStrike(), capStandard.getFixingTime(),
            capStandard.isCap(), df, capStandard.getNotional(), capStandard.getPaymentYearFraction());

    final InArrearsIntegrant integrant = new InArrearsIntegrant(capStandard, curves);
    double integralPart;
    double upper = 0.0;
    try {
      if (cap.isCap()) {
        double atmVol = _smileFunction.getVolatility(forward);
        upper = forward * Math.exp(6.0 * atmVol * Math.sqrt(cap.getFixingTime()));
        double strike = cap.getStrike();
        integralPart = INTEGRATOR.integrate(integrant, strike, upper);
        double reminder = integrant.evaluate(upper) * upper;
        double error = reminder / integralPart;

        int count = 0;
        while (Math.abs(error) > REL_ERROR && count < MAX_COUNT) {
          integralPart += INTEGRATOR.integrate(integrant, upper, 2.0 * upper);
          upper *= 2.0;
          // The increase of integralPart in the next loop is bounded by reminder
          reminder = integrant.evaluate(upper) * upper;
          error = reminder / integralPart;
          ++count;
          if (count == MAX_COUNT) {
            LOGGER.info("Maximum iteration count, " + MAX_COUNT +
                ", has been reached. Relative error is greater than " + REL_ERROR);
          }
        }
      } else {
        double strike = cap.getStrike();
        integralPart = INTEGRATOR.integrate(integrant, REL_TOL * strike, strike);
      }
    } catch (final Exception e) {
      throw new MathException(e);
    }
    integralPart *= 2.0 * cap.getFixingAccrualFactor();
    double pv = (strikePart + integralPart) / beta;

    double betaFwd = cap.getFixingAccrualFactor() * curves.getDiscountFactor(ccy, cap.getFixingPeriodEndTime())
        / curves.getDiscountFactor(ccy, cap.getFixingPeriodStartTime());
    double betaDscStart = (1.0 + cap.getFixingAccrualFactor() * forward) *
        curves.getDiscountFactor(ccy, cap.getFixingPeriodEndTime()) *
        cap.getFixingPeriodStartTime() / curves.getDiscountFactor(ccy, cap.getFixingPeriodStartTime());
    double betaDscEnd = -(1.0 + cap.getFixingAccrualFactor() * forward) *
        curves.getDiscountFactor(ccy, cap.getFixingPeriodEndTime()) *
        cap.getFixingPeriodEndTime() / curves.getDiscountFactor(ccy, cap.getFixingPeriodStartTime());

    List<DoublesPair> listDiscounting = new ArrayList<>();
    double strikePartDsc = -capStandard.getPaymentTime() * strikePart;
    double integralPartDsc = -capStandard.getPaymentTime() * integralPart;
    listDiscounting.add(DoublesPair.of(capStandard.getPaymentTime(), (strikePartDsc + integralPartDsc) /
        beta));
    listDiscounting.add(DoublesPair.of(cap.getFixingPeriodStartTime(), -pv * betaDscStart / beta));
    listDiscounting.add(DoublesPair.of(cap.getFixingPeriodEndTime(), -pv * betaDscEnd / beta));
    Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    mapDsc.put(curves.getName(capStandard.getCurrency()), listDiscounting);

    final List<ForwardSensitivity> listForward = new ArrayList<>();
    double strikePartFwd = strikePartDelta;
    double integralPartFwd = 0.0;
    final InArrearsDeltaIntegrant integrantFwd = new InArrearsDeltaIntegrant(capStandard, curves);
    try {
      if (cap.isCap()) {
        double strike = cap.getStrike();
        integralPartFwd = INTEGRATOR.integrate(integrantFwd, strike, upper);
      } else {
        double strike = cap.getStrike();
        integralPartFwd = INTEGRATOR.integrate(integrantFwd, REL_TOL * strike, strike);
      }
    } catch (final Exception e) {
      throw new MathException(e);
    }
    integralPartFwd *= 2.0 * cap.getFixingAccrualFactor();
    listForward.add(new SimplyCompoundedForwardSensitivity(capStandard.getFixingPeriodStartTime(),
        capStandard.getFixingPeriodEndTime(), capStandard.getFixingAccrualFactor(),
        (strikePartFwd + integralPartFwd - pv * betaFwd) / beta));
    Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    mapFwd.put(curves.getName(capStandard.getIndex()), listForward);
    
    return MultipleCurrencyMulticurveSensitivity.of(cap.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
  }

  private final class InArrearsIntegrant extends Function1D<Double, Double> {

    private final CapFloorIbor _capStandard;
    private final double _forward;
    private final double _expiry;
    private final double _df;

    public InArrearsIntegrant(final CapFloorIbor capStandard, final MulticurveProviderInterface curves) {
      _capStandard = capStandard;
      _forward = curves.getSimplyCompoundForwardRate(capStandard.getIndex(), capStandard.getFixingPeriodStartTime(),
          capStandard.getFixingPeriodEndTime(), capStandard.getFixingAccrualFactor());
      _expiry = capStandard.getFixingTime();
      _df = curves.getDiscountFactor(capStandard.getCurrency(), capStandard.getPaymentTime());
    }

    @Override
    public Double evaluate(final Double x) {
      return presentValueStandard(_forward, x, _expiry, _capStandard.isCap(), _df, _capStandard.getNotional(),
          _capStandard.getPaymentYearFraction());
    }
  }

  private final class InArrearsDeltaIntegrant extends Function1D<Double, Double> {

    private final CapFloorIbor _capStandard;
    private final double _forward;
    private final double _expiry;
    private final double _df;

    public InArrearsDeltaIntegrant(final CapFloorIbor capStandard, final MulticurveProviderInterface curves) {
      _capStandard = capStandard;
      _forward = curves.getSimplyCompoundForwardRate(capStandard.getIndex(), capStandard.getFixingPeriodStartTime(),
          capStandard.getFixingPeriodEndTime(), capStandard.getFixingAccrualFactor());
      _expiry = capStandard.getFixingTime();
      _df = curves.getDiscountFactor(capStandard.getCurrency(), capStandard.getPaymentTime());
    }

    @Override
    public Double evaluate(final Double x) {
      return presentValueDeltaStandard(_forward, x, _expiry, _capStandard.isCap(), _df, _capStandard.getNotional(),
          _capStandard.getPaymentYearFraction());
    }
  }

  private double presentValueStandard(final double forward, final double strike, final double expiry,
      final boolean isCall, final double df, final double notional, final double yearFraction) {
    double volatility = _smileFunction.getVolatility(strike);
    double price = BlackFormulaRepository.price(forward, strike, expiry, volatility, isCall) *
        df * notional * yearFraction;
    return price;
  }

  private double presentValueDeltaStandard(final double forward, final double strike, final double expiry,
      final boolean isCall, final double df, final double notional, final double yearFraction) {
    double volatility = _smileFunction.getVolatility(strike);
    double price = BlackFormulaRepository.delta(forward, strike, expiry, volatility, isCall) *
        df * notional * yearFraction;
    return price;
  }
}
