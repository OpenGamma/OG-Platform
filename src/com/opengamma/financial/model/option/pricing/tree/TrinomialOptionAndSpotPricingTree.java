package com.opengamma.financial.model.option.pricing.tree;

import com.opengamma.financial.model.tree.RecombiningTrinomialTree;

/**
 * 
 * @author emcleod
 * 
 */

public class TrinomialOptionAndSpotPricingTree {
  private static final int DEFAULT_N = 1000;
  private RecombiningTrinomialTree<Double> _spotPrices;
  private RecombiningTrinomialTree<Double> _optionPrices;
}
