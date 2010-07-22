package com.opengamma.financial.security.swap;

/**
 * Visitor for the SwapSecurity subclasses.
 * 
 * @param <T> visitor method return type
 */
public interface SwapSecurityVisitor<T> {

  T visitForwardSwapSecurity(ForwardSwapSecurity security);

  T visitSwapSecurity(SwapSecurity security);

}
