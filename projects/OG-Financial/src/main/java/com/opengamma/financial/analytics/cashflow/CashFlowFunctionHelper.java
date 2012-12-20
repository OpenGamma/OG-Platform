/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.cashflow;

import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * 
 */
public class CashFlowFunctionHelper {
  private static final ReferenceIndexVisitor REFERENCE_INDEX = new ReferenceIndexVisitor();
  
  public static ReferenceIndexVisitor getReferenceIndexVisitor() {
    return REFERENCE_INDEX;
  }
  
  private static final class ReferenceIndexVisitor extends FinancialSecurityVisitorAdapter<String> {
    private static final String STRING = " + ";

    public ReferenceIndexVisitor() {
    }

    @Override
    public String visitSwapSecurity(final SwapSecurity security) {
      if (security.getPayLeg() instanceof FixedInterestRateLeg) {
        return null;
      }
      final FloatingInterestRateLeg floatingLeg = (FloatingInterestRateLeg) security.getPayLeg();
      final StringBuilder sb = new StringBuilder(floatingLeg.getFloatingReferenceRateId().getValue());
      if (floatingLeg instanceof FloatingSpreadIRLeg) {
        sb.append(STRING);
        sb.append(((FloatingSpreadIRLeg) floatingLeg).getSpread());
      }
      return sb.toString();
    }
  }

  public String visitFRASecurity(final FRASecurity security) {
    return security.getUnderlyingId().getValue();
  }
}
