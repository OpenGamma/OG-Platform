/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.math.BigDecimal;

import com.opengamma.engine.security.Security;

/**
 * 
 *
 * @author kirk
 */
public interface Position {
  
  BigDecimal getQuantity();
  
  Security getSecurity();
  
}
