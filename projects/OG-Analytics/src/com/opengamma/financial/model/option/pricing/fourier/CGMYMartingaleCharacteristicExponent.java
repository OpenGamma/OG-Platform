/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

/**
 * This class represents the characteristic function of the Carr-Madan-Geman-Yor (CGMY) process, but drift corrected to be an exponential Martingale. 
 * This process is a pure jump process (i.e.
 * there is no Brownian component).
 * <p>
 * The characteristic function is given by:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * \\phi(u; C, G, M, Y) = \\exp\\left(C \\Gamma(-Y)\\left[(M - iu)^Y - M^Y + (G + iu)^Y - G^Y\\right]\\right)
 * \\end{align*}
 * }
 */
public class CGMYMartingaleCharacteristicExponent extends MeanCorrection {


  public CGMYMartingaleCharacteristicExponent(final double c, final double g, final double m, final double y) {
    super(new CGMYCharacteristicExponent(c, g, m, y));
  }

}
