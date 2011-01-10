/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import com.opengamma.financial.model.tree.RecombiningBinomialTree;

/**
 * 
 * @param <T>
 * @param <U>
 */
public abstract class BinomialOptionModelDefinition<T extends OptionDefinition, U extends StandardOptionDataBundle> {

  public abstract double getUpFactor(T option, U data, int n, int j);

  public abstract double getDownFactor(T option, U data, int n, int j);

  public abstract RecombiningBinomialTree<Double> getUpProbabilityTree(T option, U data, int n, int j);
}
