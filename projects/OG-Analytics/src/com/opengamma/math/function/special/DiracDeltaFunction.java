/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.CompareUtils;

/**
 * Class representing the Dirac delta function, defined as:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * \\delta(x)=
 * \\begin{cases}
 * \\infty & \\text{ when } x = 0\\\\
 * 0       & \\text{ otherwise}
 * \\end{cases}
 * \\end{align*}
 * }
 */
public class DiracDeltaFunction extends Function1D<Double, Double> {

  @Override
  public Double evaluate(final Double x) {
    Validate.notNull(x, "x");
    return CompareUtils.closeEquals(x, 0, 1e-16) ? Double.POSITIVE_INFINITY : 0;
  }

}
