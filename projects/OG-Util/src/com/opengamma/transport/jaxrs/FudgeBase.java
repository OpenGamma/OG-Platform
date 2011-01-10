/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import org.fudgemsg.FudgeContext;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

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
