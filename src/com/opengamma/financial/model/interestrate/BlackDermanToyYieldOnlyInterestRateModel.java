/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import com.opengamma.financial.model.interestrate.definition.BlackDermanToyDataBundle;
import com.opengamma.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.Triple;

/**
 * 
 * @author emcleod
 */
public class BlackDermanToyYieldOnlyInterestRateModel {

  public Function1D<BlackDermanToyDataBundle, RecombiningBinomialTree<Triple<Double, Double, Double>>> getTrees() {
    return new Function1D<BlackDermanToyDataBundle, RecombiningBinomialTree<Triple<Double, Double, Double>>>() {

      @Override
      public RecombiningBinomialTree<Triple<Double, Double, Double>> evaluate(final BlackDermanToyDataBundle x) {
        // TODO Auto-generated method stub
        return null;
      }

    };
  }
}
