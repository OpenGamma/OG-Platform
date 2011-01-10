/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

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
