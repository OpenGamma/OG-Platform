package com.opengamma.financial.security.swap;

public interface SwapSecurityVisitor<T> {

  T visitForwardSwapSecurity(ForwardSwapSecurity security);

  T visitSwapSecurity(SwapSecurity security);

}
