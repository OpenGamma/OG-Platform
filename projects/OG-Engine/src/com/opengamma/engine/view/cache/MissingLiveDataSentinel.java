/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import com.opengamma.engine.view.calcnode.MissingInput;

/**
 * Instances of this class are saved in the computation cache for each
 *  missing live data
 * PLAT-1262
 */
public final class MissingLiveDataSentinel implements MissingInput {

  private static final MissingLiveDataSentinel INSTANCE = new MissingLiveDataSentinel();

  private MissingLiveDataSentinel() {
  }

  public static MissingLiveDataSentinel getInstance() {
    return INSTANCE;
  }
}
