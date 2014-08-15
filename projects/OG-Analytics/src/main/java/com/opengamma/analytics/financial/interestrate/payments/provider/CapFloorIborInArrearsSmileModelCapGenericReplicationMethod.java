/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.InterpolatedSmileFunction;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *  Class used to compute the price and sensitivity of a Ibor cap/floor in arrears.
 *  The cap/floor are supposed to be exactly in arrears. The payment date is ignored and the start fixing period date is used instead.
 */
public class CapFloorIborInArrearsSmileModelCapGenericReplicationMethod {
  private static final Logger LOGGER = LoggerFactory.getLogger(CapFloorIborInArrearsSmileModelCapGenericReplicationMethod.class);

  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  private static final int MINIMUM_STEP = 6;
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
   * Computes the present value of an Ibor cap/floor in arrears by replication.
   * @param cap The cap/floor
   * @param curves The curves
   * @return The present value
   */
  public MultipleCurrencyAmount presentValue(final CapFloorIbor cap, final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(cap, "The cap/floor shoud not be null");
    ArgumentChecker.notNull(curves, "curves");
    final Currency ccy = cap.getCurrency();
    final CapFloorIbor capStandard = new CapFloorIbor(cap.getCurrency(), cap.getFixingPeriodEndTime(), cap.getPaymentYearFraction(), cap.getNotional(), cap.getFixingTime(),
        cap.getIndex(), cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor(), cap.getStrike(), cap.isCap());
    final double forward = curves.getSimplyCompoundForwardRate(cap.getIndex(), cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor());
    final double beta = (1.0 + cap.getFixingAccrualFactor() * forward) * curves.getDiscountFactor(ccy, cap.getFixingPeriodEndTime())
        / curves.getDiscountFactor(ccy, cap.getFixingPeriodStartTime());
    final double strikePart = (1.0 + cap.getFixingAccrualFactor() * cap.getStrike()) * presentValueStandard(capStandard, curves).getAmount(ccy);

    final InArrearsIntegrant integrant = new InArrearsIntegrant(capStandard, curves);
    double integralPart;
    try {
      if (cap.isCap()) {
        double upper = forward * Math.exp(Math.sqrt(cap.getFixingTime()));
        double strike = cap.getStrike();
        integralPart = INTEGRATOR.integrate(integrant, strike, upper);
        double reminder = integrant.evaluate(upper) * upper;
        double error = reminder / integralPart;

        int count = 0;
        while (Math.abs(error) > REL_ERROR && count < 10) {
          integralPart += INTEGRATOR.integrate(integrant, upper, 2.0 * upper);
          upper *= 2.0;
          reminder = integrant.evaluate(upper) * upper;
          error = reminder / integralPart;
          ++count;
          if (count == 10) {
            LOGGER.info("Relative error is greater than " + REL_ERROR);
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

  private final class InArrearsIntegrant extends Function1D<Double, Double> {

    private final CapFloorIbor _capStandard;
    private final MulticurveProviderInterface _curves;

    public InArrearsIntegrant(final CapFloorIbor capStandard, final MulticurveProviderInterface curves) {
      _capStandard = capStandard;
      _curves = curves;
    }

    @Override
    public Double evaluate(final Double x) {
      final CapFloorIbor capStrike = _capStandard.withStrike(x);
      return presentValueStandard(capStrike, _curves).getAmount(_capStandard.getCurrency());
    }
  }

  private MultipleCurrencyAmount presentValueStandard(final CapFloorIbor cap, final MulticurveProviderInterface curves) {
    final EuropeanVanillaOption option = new EuropeanVanillaOption(cap.getStrike(), cap.getFixingTime(), cap.isCap());
    final double forward = curves.getSimplyCompoundForwardRate(cap.getIndex(), cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor());
    final double df = curves.getDiscountFactor(cap.getCurrency(), cap.getPaymentTime());
    final double volatility = _smileFunction.getVolatility(cap.getStrike());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(option);
    final double price = func.evaluate(dataBlack) * cap.getNotional() * cap.getPaymentYearFraction();
    return MultipleCurrencyAmount.of(cap.getCurrency(), price);
  }
}
