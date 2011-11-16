/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.forex.derivative.ForexSwap;

/**
 * Interface to Forex derivative visitor.
 * @param <S> The type of the data
 * @param <T> The return type of the calculation
 */
public interface ForexDerivativeVisitor<S, T> {

  T visit(ForexDerivative derivative, S data);

  T visit(ForexDerivative derivative);

  T[] visit(ForexDerivative[] derivative, S data);

  T[] visit(ForexDerivative[] derivative);

  T visitForex(Forex derivative, S data);

  T visitForex(Forex derivative);

  T visitForexSwap(ForexSwap derivative, S data);

  T visitForexSwap(ForexSwap derivative);

  T visitForexOptionVanilla(ForexOptionVanilla derivative, S data);

  T visitForexOptionVanilla(ForexOptionVanilla derivative);

  T visitForexOptionSingleBarrier(ForexOptionSingleBarrier derivative, S data);

  T visitForexOptionSingleBarrier(ForexOptionSingleBarrier derivative);

  T visitForexNonDeliverableForward(ForexNonDeliverableForward derivative, S data);

  T visitForexNonDeliverableForward(ForexNonDeliverableForward derivative);

  T visitForexNonDeliverableOption(ForexNonDeliverableOption derivative, S data);

  T visitForexNonDeliverableOption(ForexNonDeliverableOption derivative);

}
