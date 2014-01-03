/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.EquityOptionBlackPresentValueCalculator;
import com.opengamma.analytics.financial.equity.EqyOptRollGeskeWhaleyPresentValueCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurveAffineDividends;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.RollGeskeWhaleyModel;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class ListedEquityOptionRollGeskeWhaleyImpliedVolFunction extends ListedEquityOptionRollGeskeWhaleyFunction {

  /** 
   * The Black present value calculator
   * The model is chosen to be consistent with {@link EquityBlackVolatilitySurfaceFromSinglePriceFunction}
   */
  private static final EquityOptionBlackPresentValueCalculator s_pvCalculator = EquityOptionBlackPresentValueCalculator.getInstance();
//  private static final EqyOptRollGeskeWhaleyPresentValueCalculator s_pvCalculator = EqyOptRollGeskeWhaleyPresentValueCalculator.getInstance();

  /** Default constructor */
  public ListedEquityOptionRollGeskeWhaleyImpliedVolFunction() {
    super(ValueRequirementNames.IMPLIED_VOLATILITY);
  }
  
  @Override
  protected Set<ComputedValue> computeValues(InstrumentDerivative derivative, StaticReplicationDataBundle market, FunctionInputs inputs, Set<ValueRequirement> desiredValues,
      ComputationTargetSpecification targetSpec, ValueProperties resultProperties) {
    
    // Get market price
    Double marketPrice = null;
    final ComputedValue mktPriceObj = inputs.getComputedValue(MarketDataRequirementNames.MARKET_VALUE);
    if (mktPriceObj == null) {
      s_logger.info(MarketDataRequirementNames.MARKET_VALUE + " not available," + targetSpec);
    } else {
      marketPrice = (Double) mktPriceObj.getValue();
    }
    // Get details of option for impliedVol Call
    // If market price is not available. compute from model instead
     double optionPrice;
    final double strike;
    final double timeToExpiry;
    final boolean isCall;
    if (derivative instanceof EquityOption) {
      final EquityOption option = (EquityOption) derivative;
      strike = option.getStrike();
      timeToExpiry = option.getTimeToExpiry();
      isCall = option.isCall();
      if (marketPrice == null) {
        optionPrice = derivative.accept(s_pvCalculator, market) / option.getUnitAmount();
      } else {
        optionPrice = marketPrice;
      }
    } else if (derivative instanceof EquityIndexOption) {
      final EquityIndexOption option = (EquityIndexOption) derivative;
      strike = option.getStrike();
      timeToExpiry = option.getTimeToExpiry();
      isCall = option.isCall();
      if (marketPrice == null) {
        optionPrice = derivative.accept(s_pvCalculator, market) / option.getUnitAmount();
      } else {
        optionPrice = marketPrice;
      }
    } else if (derivative instanceof EquityIndexFutureOption) {
      final EquityIndexFutureOption option = (EquityIndexFutureOption) derivative;
      strike = option.getStrike();
      timeToExpiry = option.getExpiry();
      isCall = option.isCall();
      if (marketPrice == null) {
        optionPrice =  derivative.accept(s_pvCalculator, market) / option.getPointValue();
      } else {
        optionPrice = marketPrice;
      }

    } else {
      throw new OpenGammaRuntimeException("Unexpected InstrumentDerivative type");
    }

    final double spot = market.getForwardCurve().getSpot();
    final double discountRate = market.getDiscountCurve().getInterestRate(timeToExpiry);
    
    Double impliedVol = null;
    if (isCall) {
      final RollGeskeWhaleyModel model = new RollGeskeWhaleyModel();
      final ForwardCurve fCurve = market.getForwardCurve();
      double[] divTime = null;
      double[] divAmount = null;
      if (fCurve instanceof ForwardCurveAffineDividends) {
        final AffineDividends div = ((ForwardCurveAffineDividends) market.getForwardCurve()).getDividends();
        divTime = div.getTau();
        divAmount = div.getAlpha();
      } else {
        divTime = new double[] {0. };
        divAmount = new double[] {0. };
      }
    
      try {
        impliedVol = model.impliedVolatility(optionPrice, spot, strike, discountRate, timeToExpiry, divAmount, divTime);
      } catch (final IllegalArgumentException e) {
        s_logger.info(MarketDataRequirementNames.IMPLIED_VOLATILITY + " undefined" + targetSpec);
        impliedVol = market.getVolatilitySurface().getVolatility(timeToExpiry, strike);
      }
    } else {
      final double volatility = market.getVolatilitySurface().getVolatility(timeToExpiry, strike);
      if (derivative instanceof EquityOption) {
        final BjerksundStenslandModel model = new BjerksundStenslandModel();
        double costOfCarry = discountRate;
        double modSpot = spot;

        final ForwardCurve fCurve = market.getForwardCurve();
        if (fCurve instanceof ForwardCurveAffineDividends) {
          final AffineDividends div = ((ForwardCurveAffineDividends) fCurve).getDividends();
          final int number = div.getNumberOfDividends();
          int i = 0;
          while (i < number && div.getTau(i) < timeToExpiry) {
            modSpot = modSpot * (1. - div.getBeta(i)) - div.getAlpha(i) * market.getDiscountCurve().getDiscountFactor(div.getTau(i));
            ++i;
          }
        } else {
          costOfCarry = Math.log(fCurve.getForward(timeToExpiry) / spot) / timeToExpiry;
        }

        try {
          if (timeToExpiry < 7. / 365.) {
            final double fwd = optionPrice / market.getDiscountCurve().getDiscountFactor(timeToExpiry);
            impliedVol = BlackFormulaRepository.impliedVolatility(fwd, fCurve.getForward(timeToExpiry), strike, timeToExpiry, false);
          } else {
            impliedVol = model.impliedVolatility(optionPrice, modSpot, strike, discountRate, costOfCarry, timeToExpiry, false, Math.min(volatility * 1.5, 0.2));
          }
        } catch (final IllegalArgumentException e) {
          s_logger.info(MarketDataRequirementNames.IMPLIED_VOLATILITY + " undefined" + targetSpec);
          impliedVol = volatility;
        }
      } else {
        impliedVol = volatility;      
      }
    }
    
    
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    return Collections.singleton(new ComputedValue(resultSpec, impliedVol));
  }
  
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(ListedEquityOptionRollGeskeWhaleyImpliedVolFunction.class);

}
