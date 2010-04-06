/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import org.fudgemsg.FudgeContext;

/**
 * Base class for the Fudge JAX-RS objects.
 * 
 * @author Andrew Griffin
 */
/* package */ abstract class FudgeBase {

  private FudgeContext _fudgeContext;
  
  protected FudgeBase () {
    setFudgeContext (FudgeContext.GLOBAL_DEFAULT);
  }
  
  public void setFudgeContext (final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }
  
  public FudgeContext getFudgeContext () {
    return _fudgeContext;
  }
  
}