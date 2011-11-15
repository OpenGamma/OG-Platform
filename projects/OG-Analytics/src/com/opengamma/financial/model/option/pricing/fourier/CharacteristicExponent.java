/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 * By definition, the characteristic function {@latex.inline $\\phi(u)$} of a distribution {@latex.inline $X$} is the Fourier transform of 
 * the probability density function {@latex.inline $\\rho(x)$}:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * \\phi_X(u) = E\\left[e^{iuX}\\right] = \\int_{-\\infty}^{\\infty} e^{iux}\\rho(x)dx
 * \\end{align*}
 * }
 * <p>
 * The cumulant characteristic function (characteristic exponent) is defined as {@latex.inline $\\psi(u) = \\ln(\\phi(u))$} and is given by the Levy-Khintchine formula
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * \\psi(u) = i\\gamma u - \\tfrac{1}{2}\\sigma^2 u^2 + \\int_{-\\infty}^{\\infty}\\left(e^{iux} - 1 - iux\\mathbf{1}_{|x|<1}\\right)\\nu(dx)
 * \\end{align*}
 * }
 * where {@latex.inline $\\gamma$} is a pure drift term, {@latex.inline $\\sigma^2$} is the Brownian volatility and {@latex.inline $\\nu(dx)$} is the Levy measure and 
 * controls how jumps occur.
 */
public interface CharacteristicExponent {

  /**
   * Returns the characteristic exponent function
   * @param t The time
   * @return A function to calculate the characteristic exponent
   */
  Function1D<ComplexNumber, ComplexNumber> getFunction(final double t);

  /**
   * Evaluates the characteristic exponent at a particular (complex) u, and time t 
   * @param u complex value 
   * @param t time
   * @return the characteristic exponent 
   */
  ComplexNumber getValue(final ComplexNumber u, final double t);

  /**
   *  Evaluates the characteristic exponent at a particular (complex) u, and time t, and the first derivative of the characteristic exponent wrt each of its parameters 
   *  (also at u and t)
   * @param u The complex value that the characteristic exponent is evaluated at
   * @param t The time that the characteristic exponent is evaluated at
   * @return Array of ComplexNumbers with the first entry being the value of the characteristic exponent, and subsequent entries being derivatives WRT each parameter 
   */
  ComplexNumber[] getCharacteristicExponentAdjoint(final ComplexNumber u, final double t);

  /**
   * Returns the characteristic exponent ajoint function
   * @param t The time
   * @return A function to calculate the characteristic exponent
   */
  Function1D<ComplexNumber, ComplexNumber[]> getAdjointFunction(final double t);

  /**
   * Returns the largest allowable value of {@latex.inline $\\alpha$}, the contour along which the characteristic function is integrated.
   * @return Returns the largest allowable value of {@latex.inline $\\alpha$}  
   */
  double getLargestAlpha();

  /**
   * Returns the largest allowable value of {@latex.inline $\\alpha$}, the contour along which the characteristic function is integrated.
   * @return Returns the smallest allowable value of {@latex.inline $\\alpha$}
   */
  double getSmallestAlpha();

}
