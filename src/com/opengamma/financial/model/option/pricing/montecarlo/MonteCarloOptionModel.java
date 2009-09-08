package com.opengamma.financial.model.option.pricing.montecarlo;

import java.util.Map;

import com.opengamma.financial.greeks.Greek.GreekType;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionModel;
import com.opengamma.financial.model.stochastic.StochasticProcess;
import com.opengamma.financial.montecarlo.MonteCarlo;

public abstract class MonteCarloOptionModel<U extends MonteCarlo<StochasticProcess>, T extends OptionDefinition, V extends StandardOptionDataBundle> implements OptionModel<T, V> {
  private final int _n;

  public MonteCarloOptionModel(int n) {
    _n = n;
  }

  public Map<GreekType, Double> getGreeks(T definition, V vars) {
    // TODO Auto-generated method stub
    return null;
  }

  public double getPrice(T definition, V vars) {
    /*
     * double r = vars.getInterestRate(); double t =
     * definition.getTimeToExpiry(date); double[] underlying =
     * getProcess(definition, spot, vars); double sum = 0; for(double s :
     * underlying) { sum += definition.getPayoff(s); } return sum / _n *
     * Math.exp(-r * t);
     */
    return 0;
  }

  public abstract double[] getProcess(T definition, V vars);
}
