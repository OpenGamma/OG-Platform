/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOptionBlackMethod;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionBlackMethod;
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.equity.option.EquityOptionBlackMethod;
import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurveAffineDividends;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.RollGeskeWhaleyModel;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates implied volatility for American options using Roll-Geske-Whaley model
 */
public final class EqyOptRollGeskeWhaleyImpliedVolatilityCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> {

  /** Static instance */
  private static final EqyOptRollGeskeWhaleyImpliedVolatilityCalculator s_instance = new EqyOptRollGeskeWhaleyImpliedVolatilityCalculator();

  /**
   * The Black present value calculator
   * The model is chosen to be consistent with {@link EquityBlackVolatilitySurfaceFromSinglePriceFunction}
   */
  private static final EquityOptionBlackPresentValueCalculator s_pvCalculator = EquityOptionBlackPresentValueCalculator.getInstance();

  /**
   * Gets the (singleton) instance of this calculator
   * @return The instance of this calculator
   */
  public static EqyOptRollGeskeWhaleyImpliedVolatilityCalculator getInstance() {
    return s_instance;
  }

  private EqyOptRollGeskeWhaleyImpliedVolatilityCalculator() {
  }

  @Override
  public Double visitEquityIndexOption(final EquityIndexOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    return EquityIndexOptionBlackMethod.getInstance().impliedVol(option, data);
  }

  @Override
  public Double visitEquityOption(final EquityOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    return EquityOptionBlackMethod.getInstance().impliedVol(option, data);
  }

  @Override
  public Double visitEquityIndexFutureOption(final EquityIndexFutureOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    return EquityIndexFutureOptionBlackMethod.getInstance().impliedVol(option, data);
  }

  /**
   * Calculates implied volatility for American options using Roll-Geske-Whaley model
   * @param derivative  The derivative
   * @param data The market data bundle
   * @param marketPrice The market price of security
   * @return The implied volatility
   */
  public Double getRollGeskeWhaleyImpliedVol(final InstrumentDerivative derivative, final StaticReplicationDataBundle data, final Double marketPrice) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(data, "data");

    if (derivative instanceof EquityOption) {
      final EquityOption option = (EquityOption) derivative;
      return impliedVolEquityOption(option, data, marketPrice);
    } else if (derivative instanceof EquityIndexOption) {
      final EquityIndexOption option = (EquityIndexOption) derivative;
      return impliedVolEquityIndexOption(option, data, marketPrice);

    } else if (derivative instanceof EquityIndexFutureOption) {
      final EquityIndexFutureOption option = (EquityIndexFutureOption) derivative;
      return impliedVolEquityIndexFutureOption(option, data, marketPrice);
    }
    throw new OpenGammaRuntimeException("Unexpected InstrumentDerivative type");
  }

  private Double impliedVolEquityIndexOption(final EquityIndexOption option, final StaticReplicationDataBundle data, final Double marketPrice) {
    final double optionPrice;
    final double strike = option.getStrike();
    final double timeToExpiry = option.getTimeToExpiry();
    final boolean isCall = option.isCall();
    if (marketPrice == null) {
      optionPrice = option.accept(s_pvCalculator, data) / option.getUnitAmount();
    } else {
      optionPrice = marketPrice;
    }

    return getImpliedVol(optionPrice, strike, timeToExpiry, isCall, false, data);
  }

  private Double impliedVolEquityOption(final EquityOption option, final StaticReplicationDataBundle data, final Double marketPrice) {
    final double optionPrice;
    final double strike = option.getStrike();
    final double timeToExpiry = option.getTimeToExpiry();
    final boolean isCall = option.isCall();
    if (marketPrice == null) {
      optionPrice = option.accept(s_pvCalculator, data) / option.getUnitAmount();
    } else {
      optionPrice = marketPrice;
    }

    return getImpliedVol(optionPrice, strike, timeToExpiry, isCall, true, data);
  }

  private Double impliedVolEquityIndexFutureOption(final EquityIndexFutureOption option, final StaticReplicationDataBundle data, final Double marketPrice) {
    final double optionPrice;
    final double strike = option.getStrike();
    final double timeToExpiry = option.getExpiry();
    final boolean isCall = option.isCall();
    if (marketPrice == null) {
      optionPrice = option.accept(s_pvCalculator, data) / option.getPointValue();
    } else {
      optionPrice = marketPrice;
    }

    return getImpliedVol(optionPrice, strike, timeToExpiry, isCall, false, data);
  }

  private Double getImpliedVol(final double optionPrice, final double strike, final double timeToExpiry, final boolean isCall, final boolean eqyOpt, final StaticReplicationDataBundle data) {
    final double spot = data.getForwardCurve().getSpot();
    final double discountRate = data.getDiscountCurve().getInterestRate(timeToExpiry);

    Double impliedVol = null;
    if (isCall) {
      final RollGeskeWhaleyModel model = new RollGeskeWhaleyModel();
      final ForwardCurve fCurve = data.getForwardCurve();
      double[] divTime = null;
      double[] divAmount = null;
      if (fCurve instanceof ForwardCurveAffineDividends) {
        final AffineDividends div = ((ForwardCurveAffineDividends) fCurve).getDividends();
        divTime = div.getTau();
        divAmount = div.getAlpha();
      } else {
        divTime = new double[] {0. };
        divAmount = new double[] {0. };
      }
      impliedVol = model.impliedVolatility(optionPrice, spot, strike, discountRate, timeToExpiry, divAmount, divTime);
    } else {
      final double volatility = data.getVolatilitySurface().getVolatility(timeToExpiry, strike);
      if (eqyOpt) {
        final BjerksundStenslandModel model = new BjerksundStenslandModel();
        double costOfCarry = discountRate;
        double modSpot = spot;

        final ForwardCurve fCurve = data.getForwardCurve();
        if (fCurve instanceof ForwardCurveAffineDividends) {
          final AffineDividends div = ((ForwardCurveAffineDividends) fCurve).getDividends();
          final int number = div.getNumberOfDividends();
          int i = 0;
          while (i < number && div.getTau(i) < timeToExpiry) {
            modSpot = modSpot * (1. - div.getBeta(i)) - div.getAlpha(i) * data.getDiscountCurve().getDiscountFactor(div.getTau(i));
            ++i;
          }
        } else {
          costOfCarry = Math.log(fCurve.getForward(timeToExpiry) / spot) / timeToExpiry;
        }

        if (timeToExpiry < 7. / 365.) {
          final double fwd = optionPrice / data.getDiscountCurve().getDiscountFactor(timeToExpiry);
          impliedVol = BlackFormulaRepository.impliedVolatility(fwd, fCurve.getForward(timeToExpiry), strike, timeToExpiry, false);
        } else {
          impliedVol = model.impliedVolatility(optionPrice, modSpot, strike, discountRate, costOfCarry, timeToExpiry, false, Math.min(volatility * 1.5, 0.2));
        }
      } else {
        impliedVol = volatility;
      }
    }

    return impliedVol;
  }

}
