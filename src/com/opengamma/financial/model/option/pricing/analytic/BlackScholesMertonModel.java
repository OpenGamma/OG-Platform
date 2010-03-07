/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.greeks.GreekResult;
import com.opengamma.financial.greeks.GreekVisitor;
import com.opengamma.financial.greeks.SingleGreekResult;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * Generalised Black-Scholes-Merton option pricing. Inputs will give:<br>
 * <em>b = r</em> Black-Scholes stock option pricing model<br>
 * <em>b = r - q</em> Merton stock option model with continuous dividend
 * yield <em>q</em><br>
 * <em>b = 0</em> Black future option model<br>
 * <em>b = 0, r = 0</em> Asay margined future option model<br>
 * <em>b = r - r<sub>f</sub></em> Garman-Kohlhagen FX option model, with
 * foreign risk-free rate <em>r<sub>f</sub></em>
 * 
 * @author emcleod
 */
public class BlackScholesMertonModel extends AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> {
  ProbabilityDistribution<Double> _normal = new NormalDistribution(0, 1);

  @Override
  public GreekVisitor<GreekResult<?>> getGreekVisitor(final Function1D<StandardOptionDataBundle, Double> pricingFunction, final StandardOptionDataBundle vars,
      final OptionDefinition definition) {
    return new BlackScholesMertonGreekVisitor(vars, pricingFunction, definition);
  }

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final OptionDefinition definition) {
    if (definition == null)
      throw new IllegalArgumentException("Null option definition");
    final Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        if (data == null)
          throw new IllegalArgumentException("Null data bundle");
        final ZonedDateTime date = data.getDate();
        final double s = data.getSpot();
        final double k = definition.getStrike();
        final double t = definition.getTimeToExpiry(date);
        final double r = data.getInterestRate(t);
        final double b = data.getCostOfCarry();
        final double sigma = data.getVolatility(t, k);
        final double d1 = getD1(s, k, t, sigma, b);
        final double d2 = getD2(d1, sigma, t);
        final int sign = definition.isCall() ? 1 : -1;
        return sign * Math.exp(-r * t) * (s * Math.exp(b * t) * _normal.getCDF(sign * d1) - k * _normal.getCDF(sign * d2));
      }
    };
    return pricingFunction;
  }

  protected class BlackScholesMertonGreekVisitor extends AnalyticOptionModelFiniteDifferenceGreekVisitor<StandardOptionDataBundle, OptionDefinition> {
    private final double _s;
    private final double _k;
    private final double _sigma;
    private final double _t;
    private final double _b;
    private final double _r;
    private final boolean _isCall;
    private final double _df;
    private final double _d1;
    private final double _d2;
    private final double _price;

    public BlackScholesMertonGreekVisitor(final StandardOptionDataBundle vars, final Function1D<StandardOptionDataBundle, Double> pricingFunction, final OptionDefinition definition) {
      super(pricingFunction, vars, definition);
      _s = vars.getSpot();
      _k = definition.getStrike();
      _t = definition.getTimeToExpiry(vars.getDate());
      _r = vars.getInterestRate(_t);
      _sigma = vars.getVolatility(_t, _k);
      _b = vars.getCostOfCarry();
      _isCall = definition.isCall();
      _df = getDF(_r, _b, _t);
      _d1 = getD1(_s, _k, _t, _sigma, _b);
      _d2 = getD2(_d1, _sigma, _t);
      _price = pricingFunction.evaluate(vars);
    }

    @Override
    public GreekResult<Double> visitCarryRho() {
      final int sign = _isCall ? 1 : -1;
      final double value = sign * _t * _s * _df * _normal.getCDF(sign * _d1);
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitDelta() {
      final double value = _df * (_isCall ? _normal.getCDF(_d1) : _normal.getCDF(_d1) - 1);
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitDeltaBleed() {
      final int sign = _isCall ? 1 : -1;
      final double value = -_df * (_normal.getPDF(_d1) * (_b / (_sigma * Math.sqrt(_t)) - _d2 / (2 * _t)) + sign * (_b - _r) * _normal.getCDF(sign * _d1));
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitDriftlessTheta() {
      final double value = -_s * _normal.getPDF(_d1) * _sigma / (2 * Math.sqrt(_t));
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitDVannaDVol() {
      final double value = visitVanna().getResult() * (_d1 * _d2 - _d1 / _d2 - 1) / _sigma;
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitDZetaDVol() {
      final double value = (_isCall ? -1 : 1) * _normal.getPDF(_d2) * _d1 / _sigma;
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitElasticity() {
      final double value = _df * (_isCall ? _normal.getCDF(_d1) : _normal.getCDF(_d1) - 1) * _s / _price;
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitGamma() {
      final double value = _df * _normal.getPDF(_d1) / (_s * _sigma * Math.sqrt(_t));
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitGammaBleed() {
      final double value = visitGamma().getResult() * (_r - _b + _b * _d1 / (_sigma * Math.sqrt(_t)) + (1 - _d1 * _d2) / (2 * _t));
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitGammaP() {
      return new SingleGreekResult(visitGamma().getResult() * _s / 100);
    }

    @Override
    public GreekResult<Double> visitGammaPBleed() {
      final double value = visitGammaP().getResult() * (_r - _b + _b * _d1 / (_sigma * Math.sqrt(_t)) + (1 - _d1 * _d2) / (2 * _t));
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitPhi() {
      final int sign = _isCall ? 1 : -1;
      final double value = -sign * _t * _s * _df * _normal.getCDF(_d1 * sign);
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitPrice() {
      return new SingleGreekResult(_price);
    }

    @Override
    public GreekResult<Double> visitRho() {
      final int sign = _isCall ? 1 : -1;
      final double value = sign * _t * _k * Math.exp(-_r * _t) * _normal.getCDF(sign * _d2);
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitSpeed() {
      final double value = -visitGamma().getResult() * (1 + _d1 / (_sigma * Math.sqrt(_t))) / _s;
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitSpeedP() {
      final double value = -visitGamma().getResult() * _d1 / (100 * _sigma * Math.sqrt(_t));
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitStrikeDelta() {
      final int sign = _isCall ? 1 : -1;
      final double value = -sign * Math.exp(-_r * _t) * _normal.getCDF(sign * _d2);
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitStrikeGamma() {
      final double value = _normal.getPDF(_d2) * Math.exp(-_r * _t) / (_k * _sigma * Math.sqrt(_t));
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitTheta() {
      final int sign = _isCall ? 1 : -1;
      final double value = -_s * _df * _normal.getPDF(_d1) * _sigma / (2 * Math.sqrt(_t)) - sign * (_b - _r) * _s * _df * _normal.getCDF(sign * _d1) - sign * _r * _k
          * Math.exp(-_r * _t) * _normal.getCDF(sign * _d2);
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitVanna() {
      final double value = -_df * _d2 * _normal.getPDF(_d1) / _sigma;
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitVarianceUltima() {
      final double value = _s * _df * Math.sqrt(_t) / (8 * Math.pow(_sigma, 5)) * _normal.getPDF(_d1) * ((_d1 * _d2 - 1) * (_d1 * _d2 - 3) - (_d1 * _d1 + _d2 * _d2));
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitVarianceVanna() {
      final double value = -_s * _df * _normal.getPDF(_d1) * _d2 / (2 * _sigma * _sigma);
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitVarianceVega() {
      final double value = _s * _df * _normal.getPDF(_d1) * Math.sqrt(_t) / (2 * _sigma);
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitVarianceVomma() {
      final double value = _s * _df * Math.sqrt(_t) / (4 * Math.pow(_sigma, 3)) * _normal.getPDF(_d1) * (_d1 * _d2 - 1);
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitVega() {
      final double value = _s * _df * _normal.getPDF(_d1) * Math.sqrt(_t);
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitVegaBleed() {
      final double value = visitVega().getResult() * (_r - _b + _b * _d1 / (_sigma * Math.sqrt(_t)) - (1 + _d1 * _d2) / (2 * _t));
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitVegaP() {
      final double value = visitVega().getResult() * _sigma / 10;
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitUltima() {
      final double value = visitVomma().getResult() * (_d1 * _d2 - _d1 / _d2 - _d2 / _d1 - 1) / _sigma;
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitVomma() {
      final double value = visitVega().getResult() * _d1 * _d2 / _sigma;
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitVommaP() {
      final double value = visitVegaP().getResult() * _d1 * _d2 / _sigma;
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitZeta() {
      final double value = _normal.getCDF(_isCall ? _d2 : -_d2);
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitZetaBleed() {
      final double value = (_isCall ? 1 : -1) * _normal.getPDF(_d2) * (_b / (_sigma * Math.sqrt(_t)) - _d1 / (2 * _t));
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitZomma() {
      final double value = visitGamma().getResult() * (_d1 * _d2 - 1) / _sigma;
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitZommaP() {
      final double value = visitGammaP().getResult() * (_d1 * _d2 - 1) / _sigma;
      return new SingleGreekResult(value);
    }
  }
}
