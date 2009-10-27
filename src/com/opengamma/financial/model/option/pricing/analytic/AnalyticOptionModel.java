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

/**
 * 
 * @author emcleod
 * 
 * @param <T>
 */
public abstract class AnalyticOptionModel<T extends OptionDefinition<?>, U extends StandardOptionDataBundle> implements OptionModel<T, U> {

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
    private final S _vars;
    private final R _definition;

    public AnalyticOptionModelFiniteDifferenceGreekVisitor(final Function1D<S, Double> pricingFunction, final S vars, final R definition) {
      _pricingFunction = pricingFunction;
      _vars = vars;
      _definition = definition;
    }

    @Override
    public GreekResult<?> visitDelta() {
      final Double spot = _vars.getSpot();
      final S upVars = (S) new StandardOptionDataBundle(_vars.getDiscountCurve(), _vars.getCostOfCarry(), _vars.getVolatilitySurface(), spot + EPS, _vars.getDate());
      final S downVars = (S) new StandardOptionDataBundle(_vars.getDiscountCurve(), _vars.getCostOfCarry(), _vars.getVolatilitySurface(), spot - EPS, _vars.getDate());
      final double upPrice = _pricingFunction.evaluate(upVars);
      final double downPrice = _pricingFunction.evaluate(downVars);
      return new SingleGreekResult((upPrice - downPrice) / (2 * EPS));
    }

    @Override
    public GreekResult<?> visitGamma() {
      final Double spot = _vars.getSpot();
      final S upVars = (S) new StandardOptionDataBundle(_vars.getDiscountCurve(), _vars.getCostOfCarry(), _vars.getVolatilitySurface(), spot + EPS, _vars.getDate());
      final S downVars = (S) new StandardOptionDataBundle(_vars.getDiscountCurve(), _vars.getCostOfCarry(), _vars.getVolatilitySurface(), spot - EPS, _vars.getDate());
      final double price = _pricingFunction.evaluate(_vars);
      final double upPrice = _pricingFunction.evaluate(upVars);
      final double downPrice = _pricingFunction.evaluate(downVars);
      return new SingleGreekResult((upPrice + downPrice - 2 * price) / (EPS * EPS));
    }

    @Override
    public GreekResult<?> visitVega() {
      final ZonedDateTime date = _vars.getDate();
      final double t = _definition.getTimeToExpiry(date);
      final Double sigma = _vars.getVolatility(t, _definition.getStrike());
      final VolatilitySurface upSurface = new ConstantVolatilitySurface(sigma + EPS);
      final VolatilitySurface downSurface = new ConstantVolatilitySurface(sigma - EPS);
      final S upVars = (S) new StandardOptionDataBundle(_vars.getDiscountCurve(), _vars.getCostOfCarry(), upSurface, _vars.getSpot(), date);
      final S downVars = (S) new StandardOptionDataBundle(_vars.getDiscountCurve(), _vars.getCostOfCarry(), downSurface, _vars.getSpot(), date);
      final double upPrice = _pricingFunction.evaluate(upVars);
      final double downPrice = _pricingFunction.evaluate(downVars);
      return new SingleGreekResult((upPrice - downPrice) / 2 * EPS);
    }

    @Override
    public GreekResult<?> visitPrice() {
      return new SingleGreekResult(_pricingFunction.evaluate(_vars));
    }

    @Override
    public GreekResult<?> visitRho() {
      final ZonedDateTime date = _vars.getDate();
      final double t = _definition.getTimeToExpiry(date);
      final double r = _vars.getInterestRate(t);
      final DiscountCurve upCurve = new ConstantInterestRateDiscountCurve(r + EPS);
      final DiscountCurve downCurve = new ConstantInterestRateDiscountCurve(r - EPS);
      final S upVars = (S) new StandardOptionDataBundle(upCurve, _vars.getCostOfCarry(), _vars.getVolatilitySurface(), _vars.getSpot(), _vars.getDate());
      final S downVars = (S) new StandardOptionDataBundle(downCurve, _vars.getCostOfCarry(), _vars.getVolatilitySurface(), _vars.getSpot(), _vars.getDate());
      final double upPrice = _pricingFunction.evaluate(upVars);
      final double downPrice = _pricingFunction.evaluate(downVars);
      return new SingleGreekResult((upPrice - downPrice) / (2 * EPS));
    }

    @Override
    public GreekResult<?> visitTheta() {
      return new SingleGreekResult(-34.);
    }

    @Override
    public GreekResult<?> visitTimeBucketedRho() {
      final DiscountCurve curve = _vars.getDiscountCurve();
      final Map<String, Double> partialGreeks = new TreeMap<String, Double>();
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
