package com.opengamma.financial.model.option.pricing.analytic;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.time.InstantProvider;

import com.opengamma.financial.greeks.Delta;
import com.opengamma.financial.greeks.Gamma;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekVisitor;
import com.opengamma.financial.greeks.Price;
import com.opengamma.financial.greeks.Rho;
import com.opengamma.financial.greeks.Theta;
import com.opengamma.financial.greeks.TimeBucketedRho;
import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.interestrate.curve.DiscountCurveTransformation;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionModel;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 * 
 * @param <T>
 */
public abstract class AnalyticOptionModel<T extends OptionDefinition, U extends StandardOptionDataBundle> implements OptionModel<T, U> {
  public abstract Function1D<U, Double> getPricingFunction(T definition);

  public GreekVisitor<Map<String, Double>> getGreekVisitor(Function1D<U, Double> pricingFunction, U vars, T definition) {
    return new AnalyticOptionModelFiniteDifferenceGreekVisitor<U, T>(pricingFunction, vars, definition);
  }

  @Override
  public Map<Greek, Map<String, Double>> getGreeks(T definition, U vars, List<Greek> requiredGreeks) {
    Function1D<U, Double> pricingFunction = getPricingFunction(definition);
    Map<Greek, Map<String, Double>> result = new HashMap<Greek, Map<String, Double>>();
    GreekVisitor<Map<String, Double>> visitor = getGreekVisitor(pricingFunction, vars, definition);
    for (Greek greek : requiredGreeks) {
      result.put(greek, greek.accept(visitor));
    }
    return result;
  }

  protected double getD1(double s, double k, double t, double sigma, double b) {
    return (Math.log(s / k) + t * (b + sigma * sigma / 2)) / (sigma * Math.sqrt(t));
  }

  protected double getD2(double d1, double sigma, double t) {
    return d1 - sigma * Math.sqrt(t);
  }

  protected double getDF(double r, double b, double t) {
    return Math.exp(t * (b - r));
  }

  @SuppressWarnings("unchecked")
  protected class AnalyticOptionModelFiniteDifferenceGreekVisitor<S extends StandardOptionDataBundle, R extends OptionDefinition> implements GreekVisitor<Map<String, Double>> {
    private static final double EPS = 1e-3;
    private final Function1D<S, Double> _pricingFunction;
    private final S _vars;
    private final R _definition;

    public AnalyticOptionModelFiniteDifferenceGreekVisitor(Function1D<S, Double> pricingFunction, S vars, R definition) {
      _pricingFunction = pricingFunction;
      _vars = vars;
      _definition = definition;
    }

    @Override
    public Map<String, Double> visitDelta(Delta delta) {
      Double spot = _vars.getSpot();
      S upVars = (S) new StandardOptionDataBundle(_vars.getDiscountCurve(), _vars.getCostOfCarry(), _vars.getVolatilitySurface(), spot + EPS, _vars.getDate());
      S downVars = (S) new StandardOptionDataBundle(_vars.getDiscountCurve(), _vars.getCostOfCarry(), _vars.getVolatilitySurface(), spot - EPS, _vars.getDate());
      double upPrice = _pricingFunction.evaluate(upVars);
      double downPrice = _pricingFunction.evaluate(downVars);
      return Collections.<String, Double> singletonMap(delta.getName(), (upPrice - downPrice) / (2 * EPS));
    }

    @Override
    public Map<String, Double> visitGamma(Gamma gamma) {
      Double spot = _vars.getSpot();
      S upVars = (S) new StandardOptionDataBundle(_vars.getDiscountCurve(), _vars.getCostOfCarry(), _vars.getVolatilitySurface(), spot + EPS, _vars.getDate());
      S downVars = (S) new StandardOptionDataBundle(_vars.getDiscountCurve(), _vars.getCostOfCarry(), _vars.getVolatilitySurface(), spot - EPS, _vars.getDate());
      double price = _pricingFunction.evaluate(_vars);
      double upPrice = _pricingFunction.evaluate(upVars);
      double downPrice = _pricingFunction.evaluate(downVars);
      return Collections.<String, Double> singletonMap(gamma.getName(), (upPrice + downPrice - 2 * price) / (EPS * EPS));
    }

    @Override
    public Map<String, Double> visitPrice(Price price) {
      return Collections.<String, Double> singletonMap(price.getName(), _pricingFunction.evaluate(_vars));
    }

    @Override
    public Map<String, Double> visitRho(Rho rho) {
      InstantProvider date = _vars.getDate();
      double t = _definition.getTimeToExpiry(date);
      double r = _vars.getInterestRate(t);
      DiscountCurve upCurve = new ConstantInterestRateDiscountCurve(date, r + EPS);
      DiscountCurve downCurve = new ConstantInterestRateDiscountCurve(date, r - EPS);
      S upVars, downVars;
      double upPrice, downPrice;
      upVars = (S) new StandardOptionDataBundle(upCurve, _vars.getCostOfCarry(), _vars.getVolatilitySurface(), _vars.getSpot(), _vars.getDate());
      downVars = (S) new StandardOptionDataBundle(downCurve, _vars.getCostOfCarry(), _vars.getVolatilitySurface(), _vars.getSpot(), _vars.getDate());
      upPrice = _pricingFunction.evaluate(upVars);
      downPrice = _pricingFunction.evaluate(downVars);
      return Collections.<String, Double> singletonMap(rho.getName(), (upPrice - downPrice) / (2 * EPS));
    }

    @Override
    public Map<String, Double> visitTheta(Theta theta) {
      return Collections.<String, Double> singletonMap(theta.getName(), -34.);
    }

    @Override
    public Map<String, Double> visitTimeBucketedRho(TimeBucketedRho rho) {
      DiscountCurve curve = _vars.getDiscountCurve();
      Map<String, Double> partialGreeks = new TreeMap<String, Double>();
      DiscountCurve upCurve, downCurve;
      S upVars, downVars;
      double upPrice, downPrice;
      for (int i = 0; i < curve.getData().size(); i++) {
        upCurve = DiscountCurveTransformation.getSingleShiftedDataPointCurve(curve, i, EPS);
        downCurve = DiscountCurveTransformation.getSingleShiftedDataPointCurve(curve, i, -EPS);
        upVars = (S) new StandardOptionDataBundle(upCurve, _vars.getCostOfCarry(), _vars.getVolatilitySurface(), _vars.getSpot(), _vars.getDate());
        downVars = (S) new StandardOptionDataBundle(downCurve, _vars.getCostOfCarry(), _vars.getVolatilitySurface(), _vars.getSpot(), _vars.getDate());
        upPrice = _pricingFunction.evaluate(upVars);
        downPrice = _pricingFunction.evaluate(downVars);
        // TODO make a better string than this
        partialGreeks.put(rho.getName() + "(" + i + ")", (upPrice - downPrice) / (2 * EPS));
      }
      return partialGreeks;
    }
  }
}
