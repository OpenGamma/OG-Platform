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
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 * 
 * @param <T>
 */
public abstract class AnalyticOptionModel<T extends OptionDefinition<?>, U extends StandardOptionDataBundle> implements OptionModel<T, U> {

  public abstract Function1D<U, Double> getPricingFunction(T definition);

  public GreekVisitor<GreekResult<?>> getGreekVisitor(Function1D<U, Double> pricingFunction, U vars, T definition) {
    return new AnalyticOptionModelFiniteDifferenceGreekVisitor<U, T>(pricingFunction, vars, definition);
  }

  @Override
  public GreekResultCollection getGreeks(T definition, U vars, List<Greek> requiredGreeks) {
    Function1D<U, Double> pricingFunction = getPricingFunction(definition);
    GreekResultCollection results = new GreekResultCollection();
    GreekVisitor<GreekResult<?>> visitor = getGreekVisitor(pricingFunction, vars, definition);
    for (Greek greek : requiredGreeks) {
      GreekResult<?> result = greek.accept(visitor);
      results.put(greek, result);
    }
    return results;
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
  protected class AnalyticOptionModelFiniteDifferenceGreekVisitor<S extends StandardOptionDataBundle, R extends OptionDefinition> implements GreekVisitor<GreekResult<?>> {
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
    public GreekResult<?> visitDelta() {
      Double spot = _vars.getSpot();
      S upVars = (S) new StandardOptionDataBundle(_vars.getDiscountCurve(), _vars.getCostOfCarry(), _vars.getVolatilitySurface(), spot + EPS, _vars.getDate());
      S downVars = (S) new StandardOptionDataBundle(_vars.getDiscountCurve(), _vars.getCostOfCarry(), _vars.getVolatilitySurface(), spot - EPS, _vars.getDate());
      double upPrice = _pricingFunction.evaluate(upVars);
      double downPrice = _pricingFunction.evaluate(downVars);
      return new SingleGreekResult((upPrice - downPrice) / (2 * EPS));
    }

    @Override
    public GreekResult<?> visitGamma() {
      Double spot = _vars.getSpot();
      S upVars = (S) new StandardOptionDataBundle(_vars.getDiscountCurve(), _vars.getCostOfCarry(), _vars.getVolatilitySurface(), spot + EPS, _vars.getDate());
      S downVars = (S) new StandardOptionDataBundle(_vars.getDiscountCurve(), _vars.getCostOfCarry(), _vars.getVolatilitySurface(), spot - EPS, _vars.getDate());
      double price = _pricingFunction.evaluate(_vars);
      double upPrice = _pricingFunction.evaluate(upVars);
      double downPrice = _pricingFunction.evaluate(downVars);
      return new SingleGreekResult((upPrice + downPrice - 2 * price) / (EPS * EPS));
    }

    @Override
    public GreekResult<?> visitPrice() {
      return new SingleGreekResult(_pricingFunction.evaluate(_vars));
    }

    @Override
    public GreekResult<?> visitRho() {
      ZonedDateTime date = _vars.getDate();
      double t = _definition.getTimeToExpiry(date);
      double r = _vars.getInterestRate(t);
      DiscountCurve upCurve = new ConstantInterestRateDiscountCurve(r + EPS);
      DiscountCurve downCurve = new ConstantInterestRateDiscountCurve(r - EPS);
      S upVars, downVars;
      double upPrice, downPrice;
      upVars = (S) new StandardOptionDataBundle(upCurve, _vars.getCostOfCarry(), _vars.getVolatilitySurface(), _vars.getSpot(), _vars.getDate());
      downVars = (S) new StandardOptionDataBundle(downCurve, _vars.getCostOfCarry(), _vars.getVolatilitySurface(), _vars.getSpot(), _vars.getDate());
      upPrice = _pricingFunction.evaluate(upVars);
      downPrice = _pricingFunction.evaluate(downVars);
      return new SingleGreekResult((upPrice - downPrice) / (2 * EPS));
    }

    @Override
    public GreekResult<?> visitTheta() {
      return new SingleGreekResult(-34.);
    }

    @Override
    public GreekResult<?> visitTimeBucketedRho() {
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
        partialGreeks.put(Greek.RHO.name() + "(" + i + ")", (upPrice - downPrice) / (2 * EPS));
      }
      return new MultipleGreekResult(partialGreeks);
    }
  }
}
