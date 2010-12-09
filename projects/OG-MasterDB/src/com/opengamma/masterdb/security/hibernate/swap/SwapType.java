/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.swap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurityVisitor;

/**
 * 
 */
public enum SwapType {
  /**
   * 
   */
  SWAP,
  /**
   * 
   */
  FORWARD;

  public static SwapType identify(final SwapSecurity object) {
    return object.accept(new SwapSecurityVisitor<SwapType>() {

      @Override
      public SwapType visitForwardSwapSecurity(ForwardSwapSecurity security) {
        return FORWARD;
      }

      @Override
      public SwapType visitSwapSecurity(SwapSecurity security) {
        return SWAP;
      }

    });
  }

  public <T> T accept(final SwapSecurityVisitor<T> visitor) {
    switch (this) {
      case SWAP:
        return visitor.visitSwapSecurity(null);
      case FORWARD:
        return visitor.visitForwardSwapSecurity(null);
      default:
        throw new OpenGammaRuntimeException("unexpected SwapType: " + this);
    }
  }

}
