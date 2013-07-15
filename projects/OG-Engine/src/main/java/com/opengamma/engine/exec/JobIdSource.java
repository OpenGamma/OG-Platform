/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 */
public class JobIdSource {
  
  private static final AtomicLong CURRENT_ID = new AtomicLong(0);
  
  public static long getId() {
    return CURRENT_ID.getAndIncrement();    
  }

}
