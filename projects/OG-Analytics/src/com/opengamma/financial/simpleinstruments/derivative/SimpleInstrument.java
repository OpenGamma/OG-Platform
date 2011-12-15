/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.simpleinstruments.derivative;

/**
 * 
 */
public interface SimpleInstrument {

  <S, T> T accept(final SimpleInstrumentVisitor<S, T> visitor, final S data);
  
  <S, T> T accept(final SimpleInstrumentVisitor<S, T> visitor);
}
