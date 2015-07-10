/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.cashflow;

import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.FloatingRateNoteSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 *
 */
public class CashFlowFunctionHelper {
  /** A static instance */
  private static final ReferenceIndexVisitor REFERENCE_INDEX = new ReferenceIndexVisitor();

  /**
   * Gets a static instance of a visitor that returns a reference index string.
   * @return The visitor.
   */
  public static ReferenceIndexVisitor getReferenceIndexVisitor() {
    return REFERENCE_INDEX;
  }

  /**
   * Constructs reference index strings for {@link SwapSecurity}, {@link FRASecurity}
   * and {@link FloatingRateNoteSecurity}
   */
  private static final class ReferenceIndexVisitor extends FinancialSecurityVisitorAdapter<String> {
    /** Sign for spreads */
    private static final String SPREAD = " + ";
    /** Sign for leverage */
    private static final String LEVERAGE = " * ";

    /**
     * Default constructor
     */
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
        sb.append(SPREAD);
        sb.append(((FloatingSpreadIRLeg) floatingLeg).getSpread());
      }
      return sb.toString();
    }

    @Override
    public String visitFRASecurity(final FRASecurity security) {
      return security.getUnderlyingId().getValue();
    }

    @Override
    public String visitFloatingRateNoteSecurity(final FloatingRateNoteSecurity security) {
      final StringBuilder sb = new StringBuilder(security.getBenchmarkRateId().getValue());
      if (Double.compare(security.getLeverageFactor(), 1) != 0) {
        sb.append(LEVERAGE);
        sb.append(security.getLeverageFactor());
      }
      sb.append(SPREAD);
      sb.append(security.getSpread());
      return sb.toString();
    }
  }
}
