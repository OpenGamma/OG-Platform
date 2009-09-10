package com.opengamma.financial.model.option.pricing.tree;

import java.util.Map;

import com.opengamma.financial.greeks.Greek.GreekType;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;

/**
 * 
 * @author emcleod
 * 
 */
public class BinomialOptionModel extends TreeOptionModel<OptionDefinition, StandardOptionDataBundle> {
  private static final int N = 1000;

  @Override
  public Map<GreekType, Double> getGreeks(OptionDefinition definition, StandardOptionDataBundle vars) {
    // TODO see below
    // TrigeorgisBinomialOptionAndSpotPricingTree trees = new
    // TrigeorgisBinomialOptionAndSpotPricingTree(N, definition, vars);
    // Lattice<Double> spotTree = trees.getSpotTree();
    // Lattice<Double> optionTree = trees.getOptionTree();
    return null;
  }

  @Override
  public double getPrice(OptionDefinition definition, StandardOptionDataBundle vars) {
    // TODO for some options, such as barrier options, it is best to let the
    // definition decide what n should be
    try {
      TrigeorgisBinomialOptionAndSpotPricingTree trees = new TrigeorgisBinomialOptionAndSpotPricingTree(N, definition, vars);
      return trees.getOptionTree().getNode(0, 0);
    } catch (Exception e) {
      return 0;
    }
  }
}
