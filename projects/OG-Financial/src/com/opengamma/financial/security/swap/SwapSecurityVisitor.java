/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

/**
 * Visitor for the {@code SwapSecurity} subclasses.
 * 
 * @param <T> visitor method return type
 */
public interface SwapSecurityVisitor<T> {

  T visitForwardSwapSecurity(ForwardSwapSecurity security);

  T visitSwapSecurity(SwapSecurity security);

}
