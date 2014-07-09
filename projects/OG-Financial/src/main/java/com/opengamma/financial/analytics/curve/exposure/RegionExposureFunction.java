/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import java.util.Arrays;
import java.util.List;

import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.financial.security.cds.LegacyFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.LegacyRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.StandardRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Exposure function that returns the regions for a given trade.
 */
public class RegionExposureFunction implements ExposureFunction {
  
  /**
   * The name of the exposure function.
   */
  public static final String NAME = "Region";
  
  private final RegionVisitor _visitor;

  public RegionExposureFunction(final SecuritySource securitySource) {
    ArgumentChecker.notNull(securitySource, "securitySource");
    _visitor = new RegionVisitor(securitySource);
  }

  @Override
  public String getName() {
    return NAME;
  }
  
  @Override
  public List<ExternalId> getIds(Trade trade) {
    Security security = trade.getSecurity();
    if (security instanceof FinancialSecurity) {
      return ((FinancialSecurity) security).accept(_visitor);
    }
    return null;
  }
  
  private static final class RegionVisitor extends FinancialSecurityVisitorSameValueAdapter<List<ExternalId>> {
    
    private final SecuritySource _securitySource;
    
    public RegionVisitor(SecuritySource securitySource) {
      super(null);
      _securitySource = ArgumentChecker.notNull(securitySource, "securitySource");
    }
  
    @Override
    public List<ExternalId> visitCashSecurity(final CashSecurity security) {
      return Arrays.asList(security.getRegionId());
    }
  
    @Override
    public List<ExternalId> visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
      return Arrays.asList(security.getRegion());
    }
  
    @Override
    public List<ExternalId> visitFRASecurity(final FRASecurity security) {
      return Arrays.asList(security.getRegionId());
    }
  
    @Override
    public List<ExternalId> visitForwardSwapSecurity(final ForwardSwapSecurity security) {
      final SwapLeg payLeg = security.getPayLeg();
      final SwapLeg receiveLeg = security.getReceiveLeg();
      if (payLeg.getRegionId().equals(receiveLeg.getRegionId())) {
        return Arrays.asList(payLeg.getRegionId());
      }
      return Arrays.asList(payLeg.getRegionId(), receiveLeg.getRegionId());
    }
  
    @Override
    public List<ExternalId> visitSwapSecurity(final SwapSecurity security) {
      final SwapLeg payLeg = security.getPayLeg();
      final SwapLeg receiveLeg = security.getReceiveLeg();
      if (payLeg.getRegionId().equals(receiveLeg.getRegionId())) {
        return Arrays.asList(payLeg.getRegionId());
      }
      return Arrays.asList(payLeg.getRegionId(), receiveLeg.getRegionId());
    }
  
    @Override
    public List<ExternalId> visitSwaptionSecurity(final SwaptionSecurity security) {
      final SwapSecurity underlyingSwap = (SwapSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId())); //TODO version
      final SwapLeg payLeg = underlyingSwap.getPayLeg();
      final SwapLeg receiveLeg = underlyingSwap.getReceiveLeg();
      if (payLeg.getRegionId().equals(receiveLeg.getRegionId())) {
        return Arrays.asList(payLeg.getRegionId());
      }
      return Arrays.asList(payLeg.getRegionId(), receiveLeg.getRegionId());
    }
  
    @Override
    public List<ExternalId> visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
      return Arrays.asList(security.getRegionId());
    }
  
    @Override
    public List<ExternalId> visitStandardFixedRecoveryCDSSecurity(final StandardFixedRecoveryCDSSecurity security) {
      return Arrays.asList(security.getRegionId());
    }
  
    @Override
    public List<ExternalId> visitStandardRecoveryLockCDSSecurity(final StandardRecoveryLockCDSSecurity security) {
      return Arrays.asList(security.getRegionId());
    }
  
    @Override
    public List<ExternalId> visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
      return Arrays.asList(security.getRegionId());
    }
  
    @Override
    public List<ExternalId> visitLegacyFixedRecoveryCDSSecurity(final LegacyFixedRecoveryCDSSecurity security) {
      return Arrays.asList(security.getRegionId());
    }
  
    @Override
    public List<ExternalId> visitLegacyRecoveryLockCDSSecurity(final LegacyRecoveryLockCDSSecurity security) {
      return Arrays.asList(security.getRegionId());
    }
  
    @Override
    public List<ExternalId> visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
      final CreditDefaultSwapSecurity underlyingCDS = (CreditDefaultSwapSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId())); //TODO version
      return Arrays.asList(underlyingCDS.getRegionId());
    }
  
    @Override
    public List<ExternalId> visitZeroCouponInflationSwapSecurity(final ZeroCouponInflationSwapSecurity security) {
      final SwapLeg payLeg = security.getPayLeg();
      final SwapLeg receiveLeg = security.getReceiveLeg();
      if (payLeg.getRegionId().equals(receiveLeg.getRegionId())) {
        return Arrays.asList(payLeg.getRegionId());
      }
      return Arrays.asList(payLeg.getRegionId(), receiveLeg.getRegionId());
    }
  
    @Override
    public List<ExternalId> visitYearOnYearInflationSwapSecurity(final YearOnYearInflationSwapSecurity security) {
      final SwapLeg payLeg = security.getPayLeg();
      final SwapLeg receiveLeg = security.getReceiveLeg();
      if (payLeg.getRegionId().equals(receiveLeg.getRegionId())) {
        return Arrays.asList(payLeg.getRegionId());
      }
      return Arrays.asList(payLeg.getRegionId(), receiveLeg.getRegionId());
    }

  }
}
