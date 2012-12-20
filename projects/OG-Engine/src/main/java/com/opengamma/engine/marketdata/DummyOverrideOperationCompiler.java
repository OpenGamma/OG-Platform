/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

/**
 * Instance of {@link OverrideOperationCompiler} that fails on all inputs.
 */
public class DummyOverrideOperationCompiler implements OverrideOperationCompiler {

  @Override
  public OverrideOperation compile(final String operation) {
    throw new IllegalArgumentException("Can't compile " + operation);
  }

}
