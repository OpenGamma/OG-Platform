/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.security.swap.CommodityNotional;
import com.opengamma.financial.security.swap.FixedInflationSwapLeg;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FixedVarianceSwapLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.FloatingVarianceSwapLeg;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
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
    public Double visitCommodityNotional(final CommodityNotional notional) {
      return null;
    }

    @Override
    public Double visitInterestRateNotional(final InterestRateNotional notional) {
      return notional.getAmount();
    }

    @Override
    public Double visitSecurityNotional(final SecurityNotional notional) {
      return null;
    }

    @Override
    public Double visitVarianceSwapNotional(final VarianceSwapNotional notional) {
      return notional.getAmount();
    }

  };

  private static final SwapLegVisitor<String> s_legNameVisitor = new SwapLegVisitor<String>() {

    @Override
    public String visitFixedInterestRateLeg(final FixedInterestRateLeg swapLeg) {
      return "Fixed";
    }

    @Override
    public String visitFloatingInterestRateLeg(final FloatingInterestRateLeg swapLeg) {
      return "Float";
    }

    @Override
    public String visitFloatingSpreadIRLeg(final FloatingSpreadIRLeg swapLeg) {
      return "Float Spread";
    }

    @Override
    public String visitFloatingGearingIRLeg(final FloatingGearingIRLeg swapLeg) {
      return "Float Gearing";
    }

    @Override
    public String visitFixedVarianceSwapLeg(final FixedVarianceSwapLeg swapLeg) {
      return "Variance Fixed";
    }

    @Override
    public String visitFloatingVarianceSwapLeg(final FloatingVarianceSwapLeg swapLeg) {
      return "Variance Floating";
    }

    @Override
    public String visitFixedInflationSwapLeg(final FixedInflationSwapLeg swapLeg) {
      return "Fixed Inflation";
    }

    @Override
    public String visitInflationIndexSwapLeg(final InflationIndexSwapLeg swapLeg) {
      return "Inflation Index";
    }
  };

  @Override
  public String getSecurityType() {
    return SwapSecurity.SECURITY_TYPE;
  }

  @Override
  public Summary getSummary(final SwapSecurity security) {
    return append(SummaryBuilder.create(security), security).build();
  }

  //-------------------------------------------------------------------------
  public static SummaryBuilder append(final SummaryBuilder builder, final SwapSecurity security) {
    final Double notional = getNotional(security);
    final String direction = notional != null ? (notional >= 0 ? "Pay" : "Receive") : null;
    final Double rate = getRate(security);
    return builder
        .with(SummaryField.START, security.getEffectiveDate())
        .with(SummaryField.MATURITY, security.getMaturityDate())
        .with(SummaryField.DESCRIPTION, getLegName(security.getPayLeg()) + "/" + getLegName(security.getReceiveLeg()))
        .with(SummaryField.NOTIONAL, notional)
        .with(SummaryField.STRIKE, rate)
        .with(SummaryField.DIRECTION, direction)
        .with(SummaryField.FREQUENCY, security.getPayLeg().getFrequency().getName() + "/" + security.getReceiveLeg().getFrequency().getName())
        .with(SummaryField.UNDERLYING, getUnderlying(security));
  }

  private static String getLegName(final SwapLeg leg) {
    return leg.accept(s_legNameVisitor);
  }

  private static Double getNotional(final SwapSecurity security) {
    if (SwapSecurityUtils.isFloatFloat(security)) {
      return security.getPayLeg().getNotional().accept(s_notionalVisitor);
    }

    final boolean isPayFixed = isFixedLeg(security.getPayLeg());
    final SwapLeg fixedLeg = isPayFixed ? security.getPayLeg() : security.getReceiveLeg();
    Double notional = fixedLeg.getNotional().accept(s_notionalVisitor);
    if (notional != null && !isPayFixed) {
      notional *= -1;
    }
    return notional;
  }

  //TODO this method does not handle a swap with two fixed legs correctly - it only returns the pay leg
  private static Double getRate(final SwapSecurity security) {
    final Double rate = getFixedRate(security.getPayLeg());
    if (rate != null) {
      return rate;
    }
    return getFixedRate(security.getReceiveLeg());
  }

  private static String getUnderlying(final SwapSecurity security) {
    if (SwapSecurityUtils.isFloatFloat(security)) {
      final ExternalId payRefRate = getReferenceRate(security.getPayLeg());
      final ExternalId receiveRefRate = getReferenceRate(security.getReceiveLeg());
      return payRefRate.getValue() + "/" + receiveRefRate.getValue();
    } else {
      final SwapLeg floatingLeg = isFixedLeg(security.getPayLeg()) ? security.getReceiveLeg() : security.getPayLeg();
      return getReferenceRate(floatingLeg).getValue();
    }
  }

  private static Double getFixedRate(final SwapLeg leg) {
    return leg.accept(new SwapLegVisitor<Double>() {

      @Override
      public Double visitFixedInterestRateLeg(final FixedInterestRateLeg swapLeg) {
        return swapLeg.getRate();
      }

      @Override
      public Double visitFloatingInterestRateLeg(final FloatingInterestRateLeg swapLeg) {
        return null;
      }

      @Override
      public Double visitFloatingSpreadIRLeg(final FloatingSpreadIRLeg swapLeg) {
        return null;
      }

      @Override
      public Double visitFloatingGearingIRLeg(final FloatingGearingIRLeg swapLeg) {
        return null;
      }

      @Override
      public Double visitFixedVarianceSwapLeg(final FixedVarianceSwapLeg swapLeg) {
        return null;
      }

      @Override
      public Double visitFloatingVarianceSwapLeg(final FloatingVarianceSwapLeg swapLeg) {
        return null;
      }

      @Override
      public Double visitFixedInflationSwapLeg(final FixedInflationSwapLeg swapLeg) {
        return swapLeg.getRate();
      }

      @Override
      public Double visitInflationIndexSwapLeg(final InflationIndexSwapLeg swapLeg) {
        return null;
      }

    });
  }

  private static ExternalId getReferenceRate(final SwapLeg leg) {
    return leg.accept(new SwapLegVisitor<ExternalId>() {

      @Override
      public ExternalId visitFixedInterestRateLeg(final FixedInterestRateLeg swapLeg) {
        return null;
      }

      @Override
      public ExternalId visitFloatingInterestRateLeg(final FloatingInterestRateLeg swapLeg) {
        return swapLeg.getFloatingReferenceRateId();
      }

      @Override
      public ExternalId visitFloatingSpreadIRLeg(final FloatingSpreadIRLeg swapLeg) {
        return swapLeg.getFloatingReferenceRateId();
      }

      @Override
      public ExternalId visitFloatingGearingIRLeg(final FloatingGearingIRLeg swapLeg) {
        return swapLeg.getFloatingReferenceRateId();
      }

      @Override
      public ExternalId visitFixedVarianceSwapLeg(final FixedVarianceSwapLeg swapLeg) {
        return null;
      }

      @Override
      public ExternalId visitFloatingVarianceSwapLeg(final FloatingVarianceSwapLeg swapLeg) {
        //TODO
        return null;
      }

      @Override
      public ExternalId visitFixedInflationSwapLeg(final FixedInflationSwapLeg swapLeg) {
        return null;
      }

      @Override
      public ExternalId visitInflationIndexSwapLeg(final InflationIndexSwapLeg swapLeg) {
        return swapLeg.getIndexId();
      }
    });
  }

  public static boolean isFixedLeg(final SwapLeg leg) {
    return leg.accept(new SwapLegVisitor<Boolean>() {

      @Override
      public Boolean visitFixedInterestRateLeg(final FixedInterestRateLeg swapLeg) {
        return true;
      }

      @Override
      public Boolean visitFloatingInterestRateLeg(final FloatingInterestRateLeg swapLeg) {
        return false;
      }

      @Override
      public Boolean visitFloatingSpreadIRLeg(final FloatingSpreadIRLeg swapLeg) {
        return false;
      }

      @Override
      public Boolean visitFloatingGearingIRLeg(final FloatingGearingIRLeg swapLeg) {
        return false;
      }

      @Override
      public Boolean visitFixedVarianceSwapLeg(final FixedVarianceSwapLeg swapLeg) {
        return true;
      }

      @Override
      public Boolean visitFloatingVarianceSwapLeg(final FloatingVarianceSwapLeg swapLeg) {
        return false;
      }

      @Override
      public Boolean visitFixedInflationSwapLeg(final FixedInflationSwapLeg swapLeg) {
        return true;
      }

      @Override
      public Boolean visitInflationIndexSwapLeg(final InflationIndexSwapLeg swapLeg) {
        return false;
      }

    });
  }

}
