/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import com.opengamma.financial.model.tree.RecombiningBinomialTree;

/**
 * 
 */
public class EdgeworthSkewKurtosisBinomialOptionModelDefinition extends
    BinomialOptionModelDefinition<OptionDefinition, SkewKurtosisOptionDataBundle> {

  @Override
  public double getDownFactor(final OptionDefinition option, final SkewKurtosisOptionDataBundle data, final int n,
      final int j) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double getUpFactor(final OptionDefinition option, final SkewKurtosisOptionDataBundle data, final int n,
      final int j) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public RecombiningBinomialTree<Double> getUpProbabilityTree(final OptionDefinition option,
      final SkewKurtosisOptionDataBundle data, final int n, final int j) {
    // TODO Auto-generated method stub
    return null;
  }

}
