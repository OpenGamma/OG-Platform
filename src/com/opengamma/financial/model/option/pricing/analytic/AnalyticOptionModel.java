package com.opengamma.financial.model.option.pricing.analytic;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.financial.greeks.Greek.GreekType;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionModel;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 * 
 * @param <T>
 */
public abstract class AnalyticOptionModel<T extends OptionDefinition, U extends StandardOptionDataBundle> implements OptionModel<T, U> {
  private static final double EPS = 1e-3;
  private static Map<PricingFunctionVariables, Integer> VARIABLES = new HashMap<PricingFunctionVariables, Integer>();

  private enum PricingFunctionVariables {
    SPOT, STRIKE, VOLATILITY, TIME, RATE, CARRY
  }

  static {
    VARIABLES.put(PricingFunctionVariables.SPOT, 0);
    VARIABLES.put(PricingFunctionVariables.STRIKE, 1);
    VARIABLES.put(PricingFunctionVariables.VOLATILITY, 2);
    VARIABLES.put(PricingFunctionVariables.TIME, 3);
    VARIABLES.put(PricingFunctionVariables.RATE, 4);
    VARIABLES.put(PricingFunctionVariables.CARRY, 5);
  }

  /**
   * This gets the pricing function for the analytic model. The order that the
   * function expects the variables to be in is: <br/>
   * 1) spot<br/>
   * 2) strike<br/>
   * 3) volatility<br/>
   * 4) time to expiry<br/>
   * 5) interest rate<br/>
   * 6) cost of carry
   * 
   * However, this order only matters if the default finite difference greeks
   * calculation in this class are used: otherwise, any order can be used when
   * writing new models. Any other parameters needed in the calculation can be
   * appended to this array (e.g. the last two elements in the array in
   * JarrowRuddSkewnessKurtosisModel are the skew and kurtosis of the asset
   * price)
   * 
   * @return Pricing function for the model
   */

  protected abstract Function1D<U, Double> getPricingFunction(T definition) throws OptionPricingException;

  public Map<GreekType, Double> getGreeks(T definition, U vars) {
    double strike = definition.getStrike();
    double t = definition.getTimeToExpiry(vars.getDate());
    double spot = vars.getSpot();
    Map<GreekType, Double> greekMap = new HashMap<GreekType, Double>();
    // greekMap.put(GreekType.DELTA, getDelta(definition, pricingFunction,
    // functionVariables));
    // greekMap.put(GreekType.GAMMA, getGamma(definition, pricingFunction,
    // functionVariables));
    return greekMap;
  }

  public double getPrice(T definition, U vars) throws OptionPricingException {
    return getPricingFunction(definition).evaluate(vars);
  }

  /*
   * protected double getDelta(T definition, Function<U, Double>
   * pricingFunction, Double[] functionVariables) { return
   * FiniteDifferenceDifferentiation.getFirstOrder(pricingFunction,
   * functionVariables, VARIABLES.get(PricingFunctionVariables.SPOT), EPS,
   * FiniteDifferenceDifferentiation.DifferenceType.CENTRAL); }
   * 
   * protected double getGamma(T definition, Function<U, Double>
   * pricingFunction, Double[] functionVariables) { return
   * FiniteDifferenceDifferentiation.getSecondOrder(pricingFunction,
   * functionVariables, VARIABLES.get(PricingFunctionVariables.SPOT), EPS); }
   */
  // TODO greeks with respect to volatility and interest rates should use the
  // surface / curve methods to perturb and recompute
  protected double getD1(double s, double k, double t, double sigma, double b) {
    return (Math.log(s / k) + t * (b + sigma * sigma / 2)) / (sigma * Math.sqrt(t));
  }

  protected double getD2(double d1, double sigma, double t) {
    return d1 - sigma * Math.sqrt(t);
  }

  protected double getDF(double r, double b, double t) {
    return Math.exp(t * (b - r));
  }
}
