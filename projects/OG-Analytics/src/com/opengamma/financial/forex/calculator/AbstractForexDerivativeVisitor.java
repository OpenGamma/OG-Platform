/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.forex.derivative.ForexSwap;

/**
 * Abstract visitor for Forex derivatives.
 * @param <S> The type of the data
 * @param <T> The return type of the calculation
 */
public class AbstractForexDerivativeVisitor<S, T> implements ForexDerivativeVisitor<S, T> {

  @Override
  public T visit(final ForexDerivative derivative, final S data) {
    Validate.notNull(derivative, "derivative");
    Validate.notNull(data, "data");
    return derivative.accept(this, data);
  }

  @Override
  public T visit(final ForexDerivative derivative) {
    Validate.notNull(derivative, "derivative");
    return derivative.accept(this);
  }

  @Override
  public T[] visit(final ForexDerivative[] derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visit(derivative[], data)");
  }

  @Override
  public T[] visit(final ForexDerivative[] derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visit(derivative[])");
  }

  @Override
  public T visitForex(final Forex derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForex()");
  }

  @Override
  public T visitForex(final Forex derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForex()");
  }

  @Override
  public T visitForexSwap(final ForexSwap derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexSwap()");
  }

  @Override
  public T visitForexSwap(final ForexSwap derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexSwap()");
  }

  @Override
  public T visitForexOptionVanilla(final ForexOptionVanilla derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionVanilla()");
  }

  @Override
  public T visitForexOptionVanilla(final ForexOptionVanilla derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionVanilla()");
  }

  @Override
  public T visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionSingleBarrier()");
  }

  @Override
  public T visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionSingleBarrier()");
  }

  @Override
  public T visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexNonDeliverableForward()");
  }

  @Override
  public T visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexNonDeliverableForward()");
  }

  @Override
  public T visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexNonDeliverableOption()");
  }

  @Override
  public T visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexNonDeliverableOption()");
  }

}
