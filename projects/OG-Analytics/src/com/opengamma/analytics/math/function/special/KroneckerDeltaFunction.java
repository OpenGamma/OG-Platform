/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function.special;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function2D;

/**
 * Class implementing the Kronecker delta function, defined as:
 * $$
 * \begin{align*}
 * \delta_{i, j}=
 * \begin{cases}
 * 1 & i = j\\
 * 0 & i \neq j
 * \end{cases}
 * \end{align*}
 * $$
 */
public class KroneckerDeltaFunction extends Function2D<Integer, Integer> {

  @Override
  public Integer evaluate(Integer i, Integer j) {
    Validate.notNull(i, "i");
    Validate.notNull(j, "j");
    return i.intValue() == j.intValue() ? 1 : 0;
  }

}
