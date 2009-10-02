package com.opengamma.financial.model.option.pricing.analytic;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.greeks.GreekResult;
import com.opengamma.financial.greeks.GreekVisitor;
import com.opengamma.financial.greeks.SingleGreekResult;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.InterpolationException;
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * @author emcleod
 * 
 * 
 *         Generalised Black-Scholes-Merton option pricing. Inputs will give:<br>
 *         <em>b = r</em> Black-Scholes stock option pricing model<br>
 *         <em>b = r - q</em> Merton stock option model with continuous dividend
 *         yield <em>q</em><br>
 *         <em>b = 0</em> Black future option model<br>
 *         <em>b = 0, r = 0</em> Asay margined future option model<br>
 *         <em>b = r - r<sub>f</sub></em> Garman-Kohlhagen FX option model, with
 *         foreign risk-free rate <em>r<sub>f</sub></em>
 */
public class BlackScholesMertonModel extends AnalyticOptionModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> {
  ProbabilityDistribution<Double> _normalProbabilityDistribution = new NormalProbabilityDistribution(0, 1);

  @Override
  public GreekVisitor<GreekResult<?>> getGreekVisitor(Function1D<StandardOptionDataBundle, Double> pricingFunction, StandardOptionDataBundle vars,
      EuropeanVanillaOptionDefinition definition) {
    return new BlackScholesMertonGreekVisitor(vars, pricingFunction, definition);
  }

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final EuropeanVanillaOptionDefinition definition) {
    Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @Override
      public Double evaluate(StandardOptionDataBundle data) {
        try {
          ZonedDateTime date = data.getDate();
          double s = data.getSpot();
          double k = definition.getStrike();
          double t = definition.getTimeToExpiry(date);
          double r = data.getInterestRate(t);
          double b = data.getCostOfCarry();
          double sigma = data.getVolatility(t, k);
          double d1 = getD1(s, k, t, sigma, b);
          double d2 = getD2(d1, sigma, t);
          int sign = definition.isCall() ? 1 : -1;
          return sign * Math.exp(-r * t) * (s * Math.exp(b * t) * _normalProbabilityDistribution.getCDF(sign * d1) - k * _normalProbabilityDistribution.getCDF(sign * d2));
        } catch (InterpolationException e) {
          throw new OptionPricingException(e);
        }
      }
    };
    return pricingFunction;
  }

  protected class BlackScholesMertonGreekVisitor extends AnalyticOptionModelFiniteDifferenceGreekVisitor<StandardOptionDataBundle, EuropeanVanillaOptionDefinition> {
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

    public BlackScholesMertonGreekVisitor(StandardOptionDataBundle vars, Function1D<StandardOptionDataBundle, Double> pricingFunction, EuropeanVanillaOptionDefinition definition) {
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
    public GreekResult<Double> visitDelta() {
      double value = _isCall ? _df * _normalProbabilityDistribution.getCDF(_d1) : _df * (_normalProbabilityDistribution.getCDF(_d1) - 1);
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitGamma() {
      double value = _df * _normalProbabilityDistribution.getPDF(_d1) / (_s * _sigma * Math.sqrt(_t));
      return new SingleGreekResult(value);
    }

    @Override
    public GreekResult<Double> visitPrice() {
      return new SingleGreekResult(_price);
    }

    @Override
    public GreekResult<Double> visitRho() {
      int sign = _isCall ? 1 : -1;
      double value = sign * _t * _k * Math.exp(-_r * _t) * _normalProbabilityDistribution.getCDF(sign * _d2);
      return new SingleGreekResult(value);
    }
  }
}
