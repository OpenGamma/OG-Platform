/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.security.swap.CommodityNotional;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FixedVarianceSwapLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.FloatingVarianceSwapLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.NotionalVisitor;
import com.opengamma.financial.security.swap.SecurityNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.VarianceSwapNotional;
import com.opengamma.id.ExternalId;

/**
 * 
 */
public class SwapSummaryFactory implements SummaryFactory<SwapSecurity> {

  private static final NotionalVisitor<Double> s_notionalVisitor = new NotionalVisitor<Double>() {

    @Override
    public Double visitCommodityNotional(CommodityNotional notional) {
      return null;
    }

    @Override
    public Double visitInterestRateNotional(InterestRateNotional notional) {
      return notional.getAmount();
    }

    @Override
    public Double visitSecurityNotional(SecurityNotional notional) {
      return null;
    }

    @Override
    public Double visitVarianceSwapNotional(VarianceSwapNotional notional) {
      return notional.getAmount();
    }

  };
  
  private static final SwapLegVisitor<String> s_legNameVisitor = new SwapLegVisitor<String>() {

    @Override
    public String visitFixedInterestRateLeg(FixedInterestRateLeg swapLeg) {
      return "Fixed";
    }

    @Override
    public String visitFloatingInterestRateLeg(FloatingInterestRateLeg swapLeg) {
      return "Float";
    }

    @Override
    public String visitFloatingSpreadIRLeg(FloatingSpreadIRLeg swapLeg) {
      return "Float Spread";
    }

    @Override
    public String visitFloatingGearingIRLeg(FloatingGearingIRLeg swapLeg) {
      return "Float Gearing";
    }

    @Override
    public String visitFixedVarianceSwapLeg(FixedVarianceSwapLeg swapLeg) {
      return "Variance Fixed";
    }

    @Override
    public String visitFloatingVarianceSwapLeg(FloatingVarianceSwapLeg swapLeg) {
      return "Variance Floating";
    }
  };
  
  @Override
  public String getSecurityType() {
    return SwapSecurity.SECURITY_TYPE;
  }

  @Override
  public Summary getSummary(SwapSecurity security) {
    return append(SummaryBuilder.create(security), security).build();
  }
  
  //-------------------------------------------------------------------------
  public static SummaryBuilder append(SummaryBuilder builder, SwapSecurity security) {
    Double notional = getNotional(security);
    String direction = notional != null ? (notional >= 0 ? "Pay" : "Receive") : null;
    Double rate = getRate(security);
    return builder
      .with(SummaryField.START, security.getEffectiveDate())
      .with(SummaryField.MATURITY, security.getMaturityDate())
      .with(SummaryField.DESCRIPTION, getLegName(security.getPayLeg()) + "/" + getLegName(security.getReceiveLeg()))
      .with(SummaryField.NOTIONAL, notional)
      .with(SummaryField.STRIKE, rate)
      .with(SummaryField.DIRECTION, direction)
      .with(SummaryField.FREQUENCY, security.getPayLeg().getFrequency().getConventionName() + "/" + security.getReceiveLeg().getFrequency().getConventionName())
      .with(SummaryField.UNDERLYING, getUnderlying(security));
  }
  
  private static String getLegName(final SwapLeg leg) {
    return leg.accept(s_legNameVisitor);
  }
  
  private static Double getNotional(final SwapSecurity security) {
    if (SwapSecurityUtils.isFloatFloat(security)) {
      return security.getPayLeg().getNotional().accept(s_notionalVisitor);
    }
    
    boolean isPayFixed = isFixedLeg(security.getPayLeg());
    SwapLeg fixedLeg = isPayFixed ? security.getPayLeg() : security.getReceiveLeg();
    Double notional = fixedLeg.getNotional().accept(s_notionalVisitor);
    if (notional != null && !isPayFixed) {
      notional *= -1;
    }
    return notional;
  }
  
  private static Double getRate(final SwapSecurity security) {
    FixedInterestRateLeg fixedLeg = isFixedLeg(security.getPayLeg()) ? (FixedInterestRateLeg) security.getPayLeg()
                                  : isFixedLeg(security.getReceiveLeg()) ? (FixedInterestRateLeg) security.getReceiveLeg()
                                  : null;
    if (fixedLeg == null) {
      return null;
    }
    return fixedLeg.getRate();
  }
  
  private static String getUnderlying(final SwapSecurity security) {
    if (SwapSecurityUtils.isFloatFloat(security)) {
      ExternalId payRefRate = getReferenceRate(security.getPayLeg());
      ExternalId receiveRefRate = getReferenceRate(security.getReceiveLeg());
      return payRefRate.getValue() + "/" + receiveRefRate.getValue();
    } else {
      SwapLeg floatingLeg = isFixedLeg(security.getPayLeg()) ? security.getReceiveLeg() : security.getPayLeg();
      return getReferenceRate(floatingLeg).getValue();
    }
  }
  
  private static ExternalId getReferenceRate(final SwapLeg leg) {
    return leg.accept(new SwapLegVisitor<ExternalId>() {

      @Override
      public ExternalId visitFixedInterestRateLeg(FixedInterestRateLeg swapLeg) {
        return null;
      }

      @Override
      public ExternalId visitFloatingInterestRateLeg(FloatingInterestRateLeg swapLeg) {
        return swapLeg.getFloatingReferenceRateId();
      }

      @Override
      public ExternalId visitFloatingSpreadIRLeg(FloatingSpreadIRLeg swapLeg) {
        return swapLeg.getFloatingReferenceRateId();
      }

      @Override
      public ExternalId visitFloatingGearingIRLeg(FloatingGearingIRLeg swapLeg) {
        return swapLeg.getFloatingReferenceRateId();
      }

      @Override
      public ExternalId visitFixedVarianceSwapLeg(FixedVarianceSwapLeg swapLeg) {
        return null;
      }

      @Override
      public ExternalId visitFloatingVarianceSwapLeg(FloatingVarianceSwapLeg swapLeg) {
        swapLeg.getUnderlyingId();
        return null;
      }

    });
  }

  public static boolean isFixedLeg(final SwapLeg leg) {
    return leg.accept(new SwapLegVisitor<Boolean>() {

      @Override
      public Boolean visitFixedInterestRateLeg(FixedInterestRateLeg swapLeg) {
        return true;
      }

      @Override
      public Boolean visitFloatingInterestRateLeg(FloatingInterestRateLeg swapLeg) {
        return false;
      }

      @Override
      public Boolean visitFloatingSpreadIRLeg(FloatingSpreadIRLeg swapLeg) {
        return false;
      }

      @Override
      public Boolean visitFloatingGearingIRLeg(FloatingGearingIRLeg swapLeg) {
        return false;
      }

      @Override
      public Boolean visitFixedVarianceSwapLeg(FixedVarianceSwapLeg swapLeg) {
        return true;
      }

      @Override
      public Boolean visitFloatingVarianceSwapLeg(FloatingVarianceSwapLeg swapLeg) {
        return false;
      }

    });
  }

}
