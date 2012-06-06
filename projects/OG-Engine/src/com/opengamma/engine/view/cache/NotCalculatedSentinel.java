/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import com.opengamma.engine.view.calcnode.MissingInput;

/**
 * Instances of this class are saved in the computation cache for outputs that aren't calculated because of an error. Exact details of the failure can be found from the calculation job result that is
 * routed back to the view processor
 */
public final class NotCalculatedSentinel implements MissingInput {

  private static final NotCalculatedSentinel INSTANCE = new NotCalculatedSentinel();

  private NotCalculatedSentinel() {
  }

  public static NotCalculatedSentinel getInstance() {
    return INSTANCE;
  }

}
