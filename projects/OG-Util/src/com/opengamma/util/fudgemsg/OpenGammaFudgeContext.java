/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.FudgeContext;

/**
 * Provides a shared singleton {@code FudgeContext} for use throughout OpenGamma.
 * <p>
 * The {@code FudgeContext} is a low-level object necessary to use the Fudge messaging system.
 * Providing the context to Fudge on demand would clutter code and configuration.
 * This class instead provides a singleton that can be used whenever necessary.
 */
public final class OpenGammaFudgeContext {

  /**
   * Singleton instance.
   */
  private static volatile FudgeContext s_instance;

  /**
   * Restricted constructor.
   */
  private OpenGammaFudgeContext() {
  }

  /**
   * Gets the singleton instance of the context, creating it if necessary.
   * @return the singleton instance, not null
   */
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

  /**
   * Creates a new Fudge context, which is an operation that should be avoided.
   * @return the new context, not null
   * @deprecated a warning to indicate that calling this method is bad practice
   */
  @Deprecated
  public static FudgeContext constructContext() {
    FudgeContext fudgeContext = new FudgeContext();
    ExtendedFudgeBuilderFactory.init(fudgeContext.getObjectDictionary());
    fudgeContext.getObjectDictionary().addAllAnnotatedBuilders();
    fudgeContext.getTypeDictionary().addAllAnnotatedSecondaryTypes();
    return fudgeContext;
  }

}
