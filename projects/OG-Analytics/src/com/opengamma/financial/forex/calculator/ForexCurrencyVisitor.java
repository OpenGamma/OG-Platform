/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.forex.derivative.ForexSwap;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class ForexCurrencyVisitor extends AbstractForexDerivativeVisitor<Object, Pair<Currency, Currency>> {
  private static final ForexCurrencyVisitor s_instance = new ForexCurrencyVisitor();
  
  public static ForexCurrencyVisitor getInstance() {
    return s_instance;
  }
  
  @Override
  public Pair<Currency, Currency> visitForex(final Forex derivative) {
    return Pair.of(derivative.getCurrency1(), derivative.getCurrency2());
  }
  
  @Override
  public Pair<Currency, Currency> visitForexSwap(final ForexSwap derivative) {
    return Pair.of(derivative.getNearLeg().getCurrency1(), derivative.getNearLeg().getCurrency2());
  }
  
  @Override
  public Pair<Currency, Currency> visitForexOptionVanilla(final ForexOptionVanilla derivative) {
    return Pair.of(derivative.getUnderlyingForex().getCurrency1(), derivative.getUnderlyingForex().getCurrency2());
  }
  
  @Override
  public Pair<Currency, Currency> visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative) {
    return Pair.of(derivative.getUnderlyingOption().getUnderlyingForex().getCurrency1(), derivative.getUnderlyingOption().getUnderlyingForex().getCurrency2());
  }
}
