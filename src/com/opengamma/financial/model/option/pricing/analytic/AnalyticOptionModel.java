/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResult;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.GreekVisitor;
import com.opengamma.financial.greeks.MultipleGreekResult;
import com.opengamma.financial.greeks.SingleGreekResult;
import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.interestrate.curve.DiscountCurveTransformation;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionModel;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.time.DateUtil;

/**
 * 
 * @author emcleod
 * 
 * @param <T>
 */
public abstract class AnalyticOptionModel<T extends OptionDefinition, U extends StandardOptionDataBundle> implements OptionModel<T, U> {

  public abstract Function1D<U, Double> getPricingFunction(T definition);

  public GreekVisitor<GreekResult<?>> getGreekVisitor(final Function1D<U, Double> pricingFunction, final U vars, final T definition) {
    return new AnalyticOptionModelFiniteDifferenceGreekVisitor<U, T>(pricingFunction, vars, definition);
  }

  @Override
  public GreekResultCollection getGreeks(final T definition, final U vars, final List<Greek> requiredGreeks) {
    final Function1D<U, Double> pricingFunction = getPricingFunction(definition);
    final GreekResultCollection results = new GreekResultCollection();
    final GreekVisitor<GreekResult<?>> visitor = getGreekVisitor(pricingFunction, vars, definition);
    for (final Greek greek : requiredGreeks) {
      final GreekResult<?> result = greek.accept(visitor);
      results.put(greek, result);
    }
    return results;
  }

  protected double getD1(final double s, final double k, final double t, final double sigma, final double b) {
    return (Math.log(s / k) + t * (b + sigma * sigma / 2)) / (sigma * Math.sqrt(t));
  }

  protected double getD2(final double d1, final double sigma, final double t) {
    return d1 - sigma * Math.sqrt(t);
  }

  protected double getDF(final double r, final double b, final double t) {
    return Math.exp(t * (b - r));
  }

  // TODO doesn't work with things that don't use StandardOptionDataBundles -
  // need to have a mutable ? extends StandardOptionDataBundle to replace
  // appropriate variable
  @SuppressWarnings("unchecked")
  protected class AnalyticOptionModelFiniteDifferenceGreekVisitor<S extends StandardOptionDataBundle, R extends OptionDefinition> implements GreekVisitor<GreekResult<?>> {
    private static final double EPS = 1e-3;
    private final Function1D<S, Double> _pricingFunction;
    private final S _data;
    private final R _definition;
    private final double _d1;
    private final double _d2;
    private final ProbabilityDistribution<Double> _normal = new NormalProbabilityDistribution(0, 1);

    public AnalyticOptionModelFiniteDifferenceGreekVisitor(final Function1D<S, Double> pricingFunction, final S vars, final R definition) {
      _pricingFunction = pricingFunction;
      _data = vars;
      _definition = definition;
      final double t = _definition.getTimeToExpiry(vars.getDate());
      final double sigma = _data.getVolatility(t, _definition.getStrike());
      _d1 = getD1(_data.getSpot(), _definition.getStrike(), t, _data.getVolatility(t, _definition.getStrike()), _data.getCostOfCarry());
      _d2 = getD2(_d1, sigma, t);
    }

    @Override
    public GreekResult<?> visitDelta() {
      final S dataUp = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot() + EPS, _data.getDate());
      final S dataDown = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot() - EPS, _data.getDate());
      return new SingleGreekResult(getFirstDerivative(dataUp, dataDown));
    }

    @Override
    public GreekResult<?> visitGamma() {
      final Double spot = _data.getSpot();
      final S dataUp = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), spot + EPS, _data.getDate());
      final S dataDown = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), spot - EPS, _data.getDate());
      return new SingleGreekResult(getSecondDerivative(dataUp, dataDown, _data));
    }

    @Override
    public GreekResult<?> visitVega() {
      final ZonedDateTime date = _data.getDate();
      final double t = _definition.getTimeToExpiry(date);
      final Double sigma = _data.getVolatility(t, _definition.getStrike());
      final VolatilitySurface upSurface = new ConstantVolatilitySurface(sigma + EPS);
      final VolatilitySurface downSurface = new ConstantVolatilitySurface(sigma - EPS);
      final S dataUp = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), upSurface, _data.getSpot(), date);
      final S dataDown = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), downSurface, _data.getSpot(), date);
      return new SingleGreekResult(getFirstDerivative(dataUp, dataDown));
    }

    @Override
    public GreekResult<?> visitPrice() {
      return new SingleGreekResult(_pricingFunction.evaluate(_data));
    }

    @Override
    public GreekResult<?> visitRho() {
      final ZonedDateTime date = _data.getDate();
      final double t = _definition.getTimeToExpiry(date);
      final double r = _data.getInterestRate(t);
      final DiscountCurve upCurve = new ConstantInterestRateDiscountCurve(r + EPS);
      final DiscountCurve downCurve = new ConstantInterestRateDiscountCurve(r - EPS);
      final S dataUp = (S) new StandardOptionDataBundle(upCurve, _data.getCostOfCarry() + EPS, _data.getVolatilitySurface(), _data.getSpot(), _data.getDate());
      final S dataDown = (S) new StandardOptionDataBundle(downCurve, _data.getCostOfCarry() - EPS, _data.getVolatilitySurface(), _data.getSpot(), _data.getDate());
      return new SingleGreekResult(getFirstDerivative(dataUp, dataDown));
    }

    @Override
    public GreekResult<?> visitTheta() {
      final ZonedDateTime date = _data.getDate();
      final ZonedDateTime offset = DateUtil.getDateOffsetWithYearFraction(date, EPS);
      final S dataUp = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot(), offset);
      return new SingleGreekResult(getForwardFirstDerivative(dataUp, _data));
    }

    @Override
    public GreekResult<?> visitTimeBucketedRho() {
      final DiscountCurve curve = _data.getDiscountCurve();
      final Map<String, Double> partialGreeks = new TreeMap<String, Double>();
      DiscountCurve upCurve, downCurve;
      S upVars, downVars;
      double upPrice, downPrice;
      for (int i = 0; i < curve.getData().size(); i++) {
        upCurve = DiscountCurveTransformation.getSingleShiftedDataPointCurve(curve, i, EPS);
        downCurve = DiscountCurveTransformation.getSingleShiftedDataPointCurve(curve, i, -EPS);
        upVars = (S) new StandardOptionDataBundle(upCurve, _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot(), _data.getDate());
        downVars = (S) new StandardOptionDataBundle(downCurve, _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot(), _data.getDate());
        upPrice = _pricingFunction.evaluate(upVars);
        downPrice = _pricingFunction.evaluate(downVars);
        // TODO make a better string than this
        partialGreeks.put(Greek.RHO.name() + "(" + i + ")", (upPrice - downPrice) / (2 * EPS));
      }
      return new MultipleGreekResult(partialGreeks);
    }

    @Override
    public GreekResult<?> visitCarryRho() {
      final S dataUp = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry() + EPS, _data.getVolatilitySurface(), _data.getSpot(), _data.getDate());
      final S dataDown = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry() - EPS, _data.getVolatilitySurface(), _data.getSpot(), _data.getDate());
      return new SingleGreekResult(getFirstDerivative(dataUp, dataDown));
    }

    @Override
    public GreekResult<?> visitDZetaDVol() {
      final double s = _data.getSpot();
      final double k = _definition.getStrike();
      final double t = _definition.getTimeToExpiry(_data.getDate());
      final double b = _data.getCostOfCarry();
      final double sigma = _data.getVolatility(t, k);
      final int sign = _definition.isCall() ? 1 : -1;
      final double nUp = _normal.getCDF(sign * getD2(getD1(s, k, t, sigma + EPS, b), sigma + EPS, t));
      final double nDown = _normal.getCDF(sign * getD2(getD1(s, k, t, sigma - EPS, b), sigma - EPS, t));
      return new SingleGreekResult((nUp - nDown) / (2 * EPS));
    }

    // TODO need to use forward differencing for dt?
    @Override
    public GreekResult<?> visitDeltaBleed() {
      final ZonedDateTime dateUp = DateUtil.getDateOffsetWithYearFraction(_data.getDate(), EPS);
      final ZonedDateTime dateDown = DateUtil.getDateOffsetWithYearFraction(_data.getDate(), -EPS);
      final S dataUp1Up2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot() + EPS, dateUp);
      final S dataUp1Down2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot() + EPS, dateDown);
      final S dataDown1Up2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot() - EPS, dateUp);
      final S dataDown1Down2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot() - EPS, dateDown);
      return new SingleGreekResult(getMixedSecondDerivative(dataUp1Up2, dataUp1Down2, dataDown1Up2, dataDown1Down2));
    }

    @Override
    public GreekResult<?> visitDriftlessTheta() {
      return null;
    }

    @Override
    public GreekResult<?> visitElasticity() {
      final double delta = ((SingleGreekResult) visitDelta()).getResult();
      final double price = ((SingleGreekResult) visitPrice()).getResult();
      return new SingleGreekResult(_data.getSpot() * delta / price);
    }

    @Override
    public GreekResult<?> visitGammaBleed() {
      final ZonedDateTime dateUp = DateUtil.getDateOffsetWithYearFraction(_data.getDate(), EPS);
      final ZonedDateTime dateDown = DateUtil.getDateOffsetWithYearFraction(_data.getDate(), -EPS);
      final S dataUp1Up1 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot() + EPS, dateUp);
      final S dataUp2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot(), dateUp);
      final S dataDown1Up2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot() - EPS, dateUp);
      final S dataUp1Down2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot() + EPS, dateDown);
      final S dataDown2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot(), dateDown);
      final S dataDown1Down2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot() - EPS, dateDown);
      return new SingleGreekResult(getMixedThirdDerivative(dataUp1Up1, dataUp2, dataDown1Up2, dataUp1Down2, dataDown2, dataDown1Down2));
    }

    @Override
    public GreekResult<?> visitGammaP() {
      return new SingleGreekResult(getGammaP(0, 0));
    }

    @Override
    public GreekResult<?> visitGammaPBleed() {
      final double gammaPUp = getGammaP(0, EPS);
      final double gammaPDown = getGammaP(0, -EPS);
      return new SingleGreekResult((gammaPUp - gammaPDown) / (2 * EPS));
    }

    @Override
    public GreekResult<?> visitPhi() {
      return new SingleGreekResult(-((SingleGreekResult) visitCarryRho()).getResult());
    }

    @Override
    public GreekResult<?> visitSpeed() {
      final S dataUpUp = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot() + 2 * EPS, _data
          .getDate());
      final S dataUp = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot() + EPS, _data.getDate());
      final S dataDown = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot() - EPS, _data.getDate());
      return new SingleGreekResult(getThirdDerivative(dataUpUp, dataUp, _data, dataDown));
    }

    @Override
    public GreekResult<?> visitSpeedP() {
      final double gammaPUp = getGammaP(EPS, 0);
      final double gammaPDown = getGammaP(-EPS, 0);
      return new SingleGreekResult((gammaPUp - gammaPDown) / (2 * EPS));
    }

    @Override
    public GreekResult<?> visitStrikeDelta() {
      return null;
    }

    @Override
    public GreekResult<?> visitStrikeGamma() {
      return null;
    }

    @Override
    public GreekResult<?> visitUltima() {
      final double t = _definition.getTimeToExpiry(_data.getDate());
      final double sigma = _data.getVolatility(t, _definition.getStrike());
      final S dataUpUp = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), new ConstantVolatilitySurface(sigma + 2 * EPS), _data.getSpot(), _data
          .getDate());
      final S dataUp = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), new ConstantVolatilitySurface(sigma + EPS), _data.getSpot(), _data
          .getDate());
      final S dataDown = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), new ConstantVolatilitySurface(sigma - EPS), _data.getSpot(), _data
          .getDate());
      return new SingleGreekResult(getThirdDerivative(dataUpUp, dataUp, _data, dataDown));
    }

    @Override
    public GreekResult<?> visitVanna() {
      final double t = _definition.getTimeToExpiry(_data.getDate());
      final double sigma = _data.getVolatility(t, _definition.getStrike());
      final VolatilitySurface upSurface = new ConstantVolatilitySurface(sigma + EPS);
      final VolatilitySurface downSurface = new ConstantVolatilitySurface(sigma - EPS);
      final S dataUp1Up2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), upSurface, _data.getSpot() + EPS, _data.getDate());
      final S dataUp1Down2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), upSurface, _data.getSpot() - EPS, _data.getDate());
      final S dataDown1Up2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), downSurface, _data.getSpot() + EPS, _data.getDate());
      final S dataDown1Down2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), downSurface, _data.getSpot() - EPS, _data.getDate());
      return new SingleGreekResult(getMixedSecondDerivative(dataUp1Up2, dataUp1Down2, dataDown1Up2, dataDown1Down2));
    }

    @Override
    public GreekResult<?> visitVarianceUltima() {
      final double t = _definition.getTimeToExpiry(_data.getDate());
      final double sigma = _data.getVolatility(t, _definition.getStrike());
      final double variance = sigma * sigma;
      final S dataUpUp = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), new ConstantVolatilitySurface(Math.sqrt(variance + 2 * EPS)), _data
          .getSpot(), _data.getDate());
      final S dataUp = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), new ConstantVolatilitySurface(Math.sqrt(variance + EPS)),
          _data.getSpot(), _data.getDate());
      final S dataDown = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), new ConstantVolatilitySurface(Math.sqrt(variance - EPS)), _data
          .getSpot(), _data.getDate());
      return new SingleGreekResult(getThirdDerivative(dataUpUp, dataUp, _data, dataDown));
    }

    @Override
    public GreekResult<?> visitVarianceVanna() {
      final double t = _definition.getTimeToExpiry(_data.getDate());
      final double sigma = _data.getVolatility(t, _definition.getStrike());
      final double variance = sigma * sigma;
      final VolatilitySurface upSurface = new ConstantVolatilitySurface(Math.sqrt(variance + EPS));
      final VolatilitySurface downSurface = new ConstantVolatilitySurface(Math.sqrt(variance - EPS));
      final S dataUp1Up2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), upSurface, _data.getSpot() + EPS, _data.getDate());
      final S dataUp1Down2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), upSurface, _data.getSpot() - EPS, _data.getDate());
      final S dataDown1Up2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), downSurface, _data.getSpot() + EPS, _data.getDate());
      final S dataDown1Down2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), downSurface, _data.getSpot() - EPS, _data.getDate());
      return new SingleGreekResult(_data.getSpot() * getMixedSecondDerivative(dataUp1Up2, dataUp1Down2, dataDown1Up2, dataDown1Down2));
    }

    @Override
    public GreekResult<?> visitVarianceVega() {
      final double t = _definition.getTimeToExpiry(_data.getDate());
      final double sigma = _data.getVolatility(t, _definition.getStrike());
      final double variance = sigma * sigma;
      final double varianceUp = variance + EPS;
      final double varianceDown = variance - EPS;
      final S dataUp = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), new ConstantVolatilitySurface(Math.sqrt(varianceUp)), _data.getSpot(),
          _data.getDate());
      final S dataDown = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), new ConstantVolatilitySurface(Math.sqrt(varianceDown)),
          _data.getSpot(), _data.getDate());
      return new SingleGreekResult(getFirstDerivative(dataUp, dataDown));
    }

    @Override
    public GreekResult<?> visitVarianceVomma() {
      final double t = _definition.getTimeToExpiry(_data.getDate());
      final double sigma = _data.getVolatility(t, _definition.getStrike());
      final double variance = sigma * sigma;
      final S dataUp = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), new ConstantVolatilitySurface(Math.sqrt(variance + EPS)),
          _data.getSpot(), _data.getDate());
      final S dataDown = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), new ConstantVolatilitySurface(Math.sqrt(variance - EPS)), _data
          .getSpot(), _data.getDate());
      return new SingleGreekResult(getSecondDerivative(dataUp, dataDown, _data));
    }

    @Override
    public GreekResult<?> visitVegaBleed() {
      final ZonedDateTime dateUp = DateUtil.getDateOffsetWithYearFraction(_data.getDate(), EPS);
      final ZonedDateTime dateDown = DateUtil.getDateOffsetWithYearFraction(_data.getDate(), -EPS);
      final double sigma = _data.getVolatility(_definition.getTimeToExpiry(_data.getDate()), _definition.getStrike());
      final VolatilitySurface upSurface = new ConstantVolatilitySurface(sigma + EPS);
      final VolatilitySurface downSurface = new ConstantVolatilitySurface(sigma - EPS);
      final S dataUp1Up2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), upSurface, _data.getSpot(), dateUp);
      final S dataUp1Down2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), upSurface, _data.getSpot(), dateDown);
      final S dataDown1Up2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), downSurface, _data.getSpot(), dateUp);
      final S dataDown1Down2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), downSurface, _data.getSpot(), dateDown);
      return new SingleGreekResult(getMixedSecondDerivative(dataUp1Up2, dataUp1Down2, dataDown1Up2, dataDown1Down2));
    }

    @Override
    public GreekResult<?> visitVegaP() {
      final double t = _definition.getTimeToExpiry(_data.getDate());
      final double sigma = _data.getVolatility(t, _definition.getStrike());
      return new SingleGreekResult(((SingleGreekResult) visitVega()).getResult() * sigma / 10);
    }

    @Override
    public GreekResult<?> visitVomma() {
      final double t = _definition.getTimeToExpiry(_data.getDate());
      final double sigma = _data.getVolatility(t, _definition.getStrike());
      final S dataUp = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), new ConstantVolatilitySurface(sigma + EPS), _data.getSpot(), _data
          .getDate());
      final S dataDown = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), new ConstantVolatilitySurface(sigma - EPS), _data.getSpot(), _data
          .getDate());
      return new SingleGreekResult(getSecondDerivative(dataUp, dataDown, _data));
    }

    @Override
    public GreekResult<?> visitVommaP() {
      final double t = _definition.getTimeToExpiry(_data.getDate());
      final double sigma = _data.getVolatility(t, _definition.getStrike());
      return new SingleGreekResult(((SingleGreekResult) visitVomma()).getResult() * sigma / 10);
    }

    @Override
    public GreekResult<?> visitZeta() {
      return new SingleGreekResult(_definition.isCall() ? _normal.getCDF(_d2) : _normal.getCDF(-_d2));
    }

    @Override
    public GreekResult<?> visitZetaBleed() {
      final double k = _definition.getStrike();
      final double t = _definition.getTimeToExpiry(_data.getDate());
      final double sigma = _data.getVolatility(t, k);
      final double b = _data.getCostOfCarry();
      final double s = _data.getSpot();
      final int sign = _definition.isCall() ? 1 : -1;
      final double nUp = _normal.getCDF(sign * getD2(getD1(s, k, t + EPS, sigma, b), sigma, t + EPS));
      final double n = _normal.getCDF(sign * getD2(getD1(s, k, t, sigma, b), sigma, t));
      return new SingleGreekResult((nUp - n) / EPS);
    }

    @Override
    public GreekResult<?> visitZomma() {
      final double t = _definition.getTimeToExpiry(_data.getDate());
      final double sigma = _data.getVolatility(t, _definition.getStrike());
      final VolatilitySurface upSurface = new ConstantVolatilitySurface(sigma + EPS);
      final VolatilitySurface downSurface = new ConstantVolatilitySurface(sigma - EPS);
      final S dataUp1Up1 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), upSurface, _data.getSpot() + EPS, _data.getDate());
      final S dataUp2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), upSurface, _data.getSpot(), _data.getDate());
      final S dataDown1Up2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), upSurface, _data.getSpot() - EPS, _data.getDate());
      final S dataUp1Down2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), downSurface, _data.getSpot() + EPS, _data.getDate());
      final S dataDown2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), downSurface, _data.getSpot(), _data.getDate());
      final S dataDown1Down2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), downSurface, _data.getSpot() - EPS, _data.getDate());
      return new SingleGreekResult(getMixedThirdDerivative(dataUp1Up1, dataUp2, dataDown1Up2, dataUp1Down2, dataDown2, dataDown1Down2));
    }

    @Override
    public GreekResult<?> visitZommaP() {
      return new SingleGreekResult(((SingleGreekResult) visitZomma()).getResult() * _data.getSpot() / 100);
    }

    @Override
    public GreekResult<?> visitDVannaDVol() {
      final double t = _definition.getTimeToExpiry(_data.getDate());
      final double sigma = _data.getVolatility(t, _definition.getStrike());
      final VolatilitySurface upSurface = new ConstantVolatilitySurface(sigma + EPS);
      final VolatilitySurface downSurface = new ConstantVolatilitySurface(sigma - EPS);
      final S dataUp1Up1 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), upSurface, _data.getSpot() + EPS, _data.getDate());
      final S dataUp2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot() + EPS, _data.getDate());
      final S dataDown1Up2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), downSurface, _data.getSpot() + EPS, _data.getDate());
      final S dataUp1Down2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), upSurface, _data.getSpot() - EPS, _data.getDate());
      final S dataDown2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), _data.getSpot() - EPS, _data.getDate());
      final S dataDown1Down2 = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), downSurface, _data.getSpot() - EPS, _data.getDate());
      return new SingleGreekResult(getMixedThirdDerivative(dataUp1Up1, dataUp2, dataDown1Up2, dataUp1Down2, dataDown2, dataDown1Down2));
    }

    private double getFirstDerivative(final S dataUp, final S dataDown) {
      return (_pricingFunction.evaluate(dataUp) - _pricingFunction.evaluate(dataDown)) / (2 * EPS);
    }

    private double getForwardFirstDerivative(final S dataUp, final S data) {
      return (_pricingFunction.evaluate(dataUp) - _pricingFunction.evaluate(data)) / EPS;
    }

    private double getSecondDerivative(final S dataUp, final S dataDown, final S data) {
      return (_pricingFunction.evaluate(dataUp) + _pricingFunction.evaluate(dataDown) - 2 * _pricingFunction.evaluate(data)) / (EPS * EPS);
    }

    private double getMixedSecondDerivative(final S dataUp1Up2, final S dataUp1Down2, final S dataDown1Up2, final S dataDown1Down2) {
      return (_pricingFunction.evaluate(dataUp1Up2) - _pricingFunction.evaluate(dataUp1Down2) - _pricingFunction.evaluate(dataDown1Up2) + _pricingFunction.evaluate(dataDown1Down2))
          / (4 * EPS * EPS);
    }

    private double getThirdDerivative(final S dataUpUp, final S dataUp, final S data, final S dataDown) {
      return (_pricingFunction.evaluate(dataUpUp) - 3 * _pricingFunction.evaluate(dataUp) + 3 * _pricingFunction.evaluate(data) - _pricingFunction.evaluate(dataDown))
          / (EPS * EPS * EPS);
    }

    private double getMixedThirdDerivative(final S dataUp1Up1, final S dataUp2, final S dataDown1Up2, final S dataUp1Down2, final S dataDown2, final S dataDown1Down2) {
      return (_pricingFunction.evaluate(dataUp1Up1) - 2 * _pricingFunction.evaluate(dataUp2) + _pricingFunction.evaluate(dataDown1Up2) - _pricingFunction.evaluate(dataUp1Down2)
          + 2 * _pricingFunction.evaluate(dataDown2) - _pricingFunction.evaluate(dataDown1Down2))
          / (2 * EPS * EPS * EPS);
    }

    private double getGammaP(final double spotOffset, final double tOffset) {
      final double spot = _data.getSpot() + spotOffset;
      final ZonedDateTime date = DateUtil.getDateOffsetWithYearFraction(_data.getDate(), tOffset);
      final S dataUp = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), spot + EPS, date);
      final S dataDown = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), spot - EPS, date);
      final S data = (S) new StandardOptionDataBundle(_data.getDiscountCurve(), _data.getCostOfCarry(), _data.getVolatilitySurface(), spot, date);
      final double gamma = getSecondDerivative(dataUp, dataDown, data);
      return gamma * spot / 100;
    }
  }
}
