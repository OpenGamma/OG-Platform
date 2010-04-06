/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

public interface OTCOptionSecurityVisitor<T> {
  
  public T visitFXOptionSecurity (FXOptionSecurity security);
  
}