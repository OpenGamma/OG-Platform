/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.method.market;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.market.description.MarketDiscountBundle;
import com.opengamma.analytics.financial.interestrate.market.description.MarketDiscountingTimeDecorated;
import com.opengamma.analytics.financial.interestrate.market.description.MarketForwardTimeDecorated;
import com.opengamma.analytics.financial.interestrate.market.description.MarketPriceIndexTimeDecorated;
import com.opengamma.analytics.financial.interestrate.method.PricingMarketMethod;
import com.opengamma.analytics.math.differentiation.FiniteDifferenceType;
import com.opengamma.util.money.Currency;

/**
 * Class used to compute interest rate instrument sensitivity by finite difference.
 */
public class SensitivityFiniteDifferenceMarket {

  /**
   * Compute the present value rate sensitivity for a set of node time by finite difference.
   * @param instrument The instrument.
   * @param market The market.
   * @param ccy The currency.
   * @param nodeTimes The node times to be bumped.
   * @param deltaShift The bump size.
   * @param method The method to compute the present value sensitivity.
   * @param differenceType {@link FiniteDifferenceType#FORWARD}, {@link FiniteDifferenceType#BACKWARD}, or {@link FiniteDifferenceType#CENTRAL}. 
   * Indicates how the finite difference is computed. Not null
   * @return The array of sensitivity with respect the to the given node times.
   */
  public static double[] curveSensitivity(final InstrumentDerivative instrument, final MarketDiscountBundle market, Currency ccy, double[] nodeTimes, double deltaShift, PricingMarketMethod method,
      final FiniteDifferenceType differenceType) {
    Validate.notNull(instrument, "Instrument");
    Validate.notNull(method, "Method");
    Validate.notNull(differenceType, "Difference type");
    int nbNode = nodeTimes.length;
    double[] result = new double[nbNode];
    double pv = method.presentValue(instrument, market).getAmount(ccy);
    MarketDiscountBundle marketBumped;
    switch (differenceType) {
      case FORWARD:
        for (int loopnode = 0; loopnode < nbNode; loopnode++) {
          marketBumped = new MarketDiscountingTimeDecorated(market, ccy, nodeTimes[loopnode], deltaShift);
          final double bumpedpv = method.presentValue(instrument, marketBumped).getAmount(ccy);
          result[loopnode] = (bumpedpv - pv) / deltaShift;
        }
        return result;
      case CENTRAL:
        for (int loopnode = 0; loopnode < nbNode; loopnode++) {
          marketBumped = new MarketDiscountingTimeDecorated(market, ccy, nodeTimes[loopnode], deltaShift);
          final double bumpedpvPlus = method.presentValue(instrument, marketBumped).getAmount(ccy);
          marketBumped = new MarketDiscountingTimeDecorated(market, ccy, nodeTimes[loopnode], -deltaShift);
          final double bumpedpvMinus = method.presentValue(instrument, marketBumped).getAmount(ccy);
          result[loopnode] = (bumpedpvPlus - bumpedpvMinus) / (2 * deltaShift);
        }
        return result;
      case BACKWARD:
        for (int loopnode = 0; loopnode < nbNode; loopnode++) {
          marketBumped = new MarketDiscountingTimeDecorated(market, ccy, nodeTimes[loopnode], -deltaShift);
          final double bumpedpv = method.presentValue(instrument, marketBumped).getAmount(ccy);
          result[loopnode] = (pv - bumpedpv) / deltaShift;
        }
        return result;
    }
    throw new IllegalArgumentException("Can only handle forward, backward and central differencing");

  }

  /**
   * Compute the present value rate sensitivity for a set of node time by finite difference. The computation is done in an non-symmetrical forward way.
   * @param instrument The instrument.
   * @param market The market.
   * @param ccy The currency.
   * @param nodeTimes The node times to be bumped.
   * @param deltaShift The bump size.
   * @param method The method to compute the present value sensitivity.
   * @return The array of sensitivity with respect the to the given node times.
   */
  public static double[] curveSensitivity(final InstrumentDerivative instrument, final MarketDiscountBundle market, Currency ccy, double[] nodeTimes, double deltaShift, PricingMarketMethod method) {
    return curveSensitivity(instrument, market, ccy, nodeTimes, deltaShift, method, FiniteDifferenceType.CENTRAL);
  }

  public static double[] curveSensitivity(final InstrumentDerivative instrument, final MarketDiscountBundle market, IborIndex index, double[] nodeTimes, double deltaShift, PricingMarketMethod method,
      final FiniteDifferenceType differenceType) {
    Validate.notNull(instrument, "Instrument");
    Validate.notNull(method, "Method");
    Validate.notNull(differenceType, "Difference type");
    int nbNode = nodeTimes.length;
    double[] result = new double[nbNode];
    double pv = method.presentValue(instrument, market).getAmount(index.getCurrency());
    MarketDiscountBundle marketBumped;
    switch (differenceType) {
      case FORWARD:
        for (int loopnode = 0; loopnode < nbNode; loopnode++) {
          marketBumped = new MarketForwardTimeDecorated(market, index, nodeTimes[loopnode], deltaShift);
          final double bumpedpv = method.presentValue(instrument, marketBumped).getAmount(index.getCurrency());
          result[loopnode] = (bumpedpv - pv) / deltaShift;
        }
        return result;
      case CENTRAL:
        for (int loopnode = 0; loopnode < nbNode; loopnode++) {
          marketBumped = new MarketForwardTimeDecorated(market, index, nodeTimes[loopnode], deltaShift);
          final double bumpedpvPlus = method.presentValue(instrument, marketBumped).getAmount(index.getCurrency());
          marketBumped = new MarketForwardTimeDecorated(market, index, nodeTimes[loopnode], -deltaShift);
          final double bumpedpvMinus = method.presentValue(instrument, marketBumped).getAmount(index.getCurrency());
          result[loopnode] = (bumpedpvPlus - bumpedpvMinus) / (2 * deltaShift);
        }
        return result;
      case BACKWARD:
        for (int loopnode = 0; loopnode < nbNode; loopnode++) {
          marketBumped = new MarketForwardTimeDecorated(market, index, nodeTimes[loopnode], -deltaShift);
          final double bumpedpv = method.presentValue(instrument, marketBumped).getAmount(index.getCurrency());
          result[loopnode] = (pv - bumpedpv) / deltaShift;
        }
        return result;
    }
    throw new IllegalArgumentException("Can only handle forward, backward and central differencing");
  }

  public static double[] curveSensitivity(final InstrumentDerivative instrument, final MarketDiscountBundle market, IndexPrice index, double[] nodeTimes, double deltaShift,
      PricingMarketMethod method, final FiniteDifferenceType differenceType) {
    Validate.notNull(instrument, "Instrument");
    Validate.notNull(method, "Method");
    Validate.notNull(differenceType, "Difference type");
    int nbNode = nodeTimes.length;
    double[] result = new double[nbNode];
    double pv = method.presentValue(instrument, market).getAmount(index.getCurrency());
    MarketDiscountBundle marketBumped;
    switch (differenceType) {
      case FORWARD:
        for (int loopnode = 0; loopnode < nbNode; loopnode++) {
          marketBumped = new MarketPriceIndexTimeDecorated(market, index, nodeTimes[loopnode], deltaShift);
          final double bumpedpv = method.presentValue(instrument, marketBumped).getAmount(index.getCurrency());
          result[loopnode] = (bumpedpv - pv) / deltaShift;
        }
        return result;
      case CENTRAL:
        for (int loopnode = 0; loopnode < nbNode; loopnode++) {
          marketBumped = new MarketPriceIndexTimeDecorated(market, index, nodeTimes[loopnode], deltaShift);
          final double bumpedpvPlus = method.presentValue(instrument, marketBumped).getAmount(index.getCurrency());
          marketBumped = new MarketPriceIndexTimeDecorated(market, index, nodeTimes[loopnode], -deltaShift);
          final double bumpedpvMinus = method.presentValue(instrument, marketBumped).getAmount(index.getCurrency());
          result[loopnode] = (bumpedpvPlus - bumpedpvMinus) / (2 * deltaShift);
        }
        return result;
      case BACKWARD:
        for (int loopnode = 0; loopnode < nbNode; loopnode++) {
          marketBumped = new MarketPriceIndexTimeDecorated(market, index, nodeTimes[loopnode], -deltaShift);
          final double bumpedpv = method.presentValue(instrument, marketBumped).getAmount(index.getCurrency());
          result[loopnode] = (pv - bumpedpv) / deltaShift;
        }
        return result;
    }
    throw new IllegalArgumentException("Can only handle forward, backward and central differencing");
  }

}
