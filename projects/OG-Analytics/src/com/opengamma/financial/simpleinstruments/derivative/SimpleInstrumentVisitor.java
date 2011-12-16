/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.simpleinstruments.derivative;

/**
 * 
 * @param <S> The type of the data
 * @param <T> The return type of the calculation
 */
public interface SimpleInstrumentVisitor<S, T> {

  T visit(final SimpleInstrument derivative, final S data);
  
  T visitSimpleFuture(final SimpleFuture future, final S data);
  
  T visitSimpleFXFuture(final SimpleFXFuture future, final S data);
  
  T visit(final SimpleInstrument derivative);
  
  T visitSimpleFuture(final SimpleFuture future);
  
  T visitSimpleFXFuture(final SimpleFXFuture future);
}
