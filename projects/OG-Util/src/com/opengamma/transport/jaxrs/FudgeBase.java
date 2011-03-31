/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import org.fudgemsg.FudgeContext;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Base class for the Fudge JAX-RS objects.
 */
/* package */abstract class FudgeBase {

  /**
   * The Fudge context.
   */
  private FudgeContext _fudgeContext;

  /**
   * Creates an instance.
   */
  protected FudgeBase() {
    setFudgeContext(OpenGammaFudgeContext.getInstance());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Fudge context.
   * @return the context, not null
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Sets the Fudge context.
   * @param fudgeContext  the context to use, not null
   */
  public void setFudgeContext(final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }

}
