/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudge;

import org.fudgemsg.FudgeContext;

/**
 * A singleton holding the shared Fudge Context that should be used throughout
 * OpenGamma code. 
 */
public final class OpenGammaFudgeContext {
  private OpenGammaFudgeContext() {
  }
  
  private static volatile FudgeContext s_instance;
  
  public static FudgeContext getInstance() {
    if (s_instance == null) {
      synchronized (OpenGammaFudgeContext.class) {
        if (s_instance == null) {
          s_instance = constructContext();
        }
      }
    }
    return s_instance;
  }
  
  @Deprecated
  public static FudgeContext constructContext() {
    FudgeContext fudgeContext = new FudgeContext();
    ExtendedFudgeBuilderFactory.init(fudgeContext.getObjectDictionary());
    fudgeContext.getObjectDictionary().addAllClasspathBuilders();
    fudgeContext.getTypeDictionary().addAllAnnotatedSecondaryTypes();
    return fudgeContext;
  }

}
