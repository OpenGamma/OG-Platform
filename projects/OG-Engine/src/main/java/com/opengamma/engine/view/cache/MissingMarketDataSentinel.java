/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import com.opengamma.engine.view.calcnode.MissingInput;

/**
 * Instances of this class are saved in the computation cache for each item of missing market data.
 * See [PLAT-1262].
 */
public final class MissingMarketDataSentinel implements MissingInput {

  private static final MissingMarketDataSentinel INSTANCE = new MissingMarketDataSentinel();

  private MissingMarketDataSentinel() {
  }

  public static MissingMarketDataSentinel getInstance() {
    return INSTANCE;
  }
  
}
