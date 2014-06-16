/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.FloatingRateNoteSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.InflationBondSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.financial.security.cds.LegacyFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.LegacyRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.StandardRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
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
 * Exposure function that returns the security type and region for a given trade.
 */
public class SecurityAndRegionExposureFunction implements ExposureFunction {
  
  /**
   * The name of the exposure function.
   */
  public static final String NAME = "Security / Region";
  
  private final SecurityAndRegionVisitor _visitor;

  public SecurityAndRegionExposureFunction(final SecuritySource securitySource) {
    _visitor = new SecurityAndRegionVisitor(ArgumentChecker.notNull(securitySource, "security source"));
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
  
  private static final class SecurityAndRegionVisitor extends FinancialSecurityVisitorSameValueAdapter<List<ExternalId>> {
    
    private final SecuritySource _securitySource;
    
    public SecurityAndRegionVisitor(SecuritySource securitySource) {
      super(null);
      _securitySource = ArgumentChecker.notNull(securitySource, "securitySource");
    }
    
    @Override
    public List<ExternalId> visitCashSecurity(final CashSecurity security) {
      final ExternalId regionId = security.getRegionId();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + regionId.getValue()));
    }

    @Override
    public List<ExternalId> visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
      final ExternalId regionId = security.getRegion();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + regionId.getValue()));
    }

    @Override
    public List<ExternalId> visitCorporateBondSecurity(final CorporateBondSecurity security) {
      final String region = security.getIssuerDomicile();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + region));
    }

    @Override
    public List<ExternalId> visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
      final ExternalId regionId = security.getRegionId();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + regionId.getValue()));
    }

    @Override
    public List<ExternalId> visitFRASecurity(final FRASecurity security) {
      final ExternalId regionId = security.getRegionId();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + regionId.getValue()));
    }

    @Override
    public List<ExternalId> visitForwardSwapSecurity(final ForwardSwapSecurity security) {
      final List<ExternalId> result = new ArrayList<>();
      final SwapLeg payLeg = security.getPayLeg();
      final SwapLeg receiveLeg = security.getReceiveLeg();
      final String securityType = security.getSecurityType();
      if (payLeg.getRegionId().equals(receiveLeg.getRegionId())) {
        return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + payLeg.getRegionId().getValue()));
      }
      result.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + payLeg.getRegionId().getValue()));
      result.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + receiveLeg.getRegionId().getValue()));
      return result;
    }

    @Override
    public List<ExternalId> visitBillSecurity(final BillSecurity security) {
      final String region = security.getRegionId().getValue();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + region));
    }

    @Override
    public List<ExternalId> visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
      final String region = security.getIssuerDomicile();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + region));
    }

    @Override
    public List<ExternalId> visitInflationBondSecurity(final InflationBondSecurity security) {
      final String region = security.getIssuerDomicile();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + region));
    }

    @Override
    public List<ExternalId> visitSwapSecurity(final SwapSecurity security) {
      final List<ExternalId> result = new ArrayList<>();
      final SwapLeg payLeg = security.getPayLeg();
      final SwapLeg receiveLeg = security.getReceiveLeg();
      final String securityType = security.getSecurityType();
      if (payLeg.getRegionId().equals(receiveLeg.getRegionId())) {
        return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + payLeg.getRegionId().getValue()));
      }
      result.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + payLeg.getRegionId().getValue()));
      result.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + receiveLeg.getRegionId().getValue()));
      return result;
    }

    @Override
    public List<ExternalId> visitSwaptionSecurity(final SwaptionSecurity security) {
      final List<ExternalId> result = new ArrayList<>();
      final SwapSecurity underlyingSwap = (SwapSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId())); //TODO version
      final SwapLeg payLeg = underlyingSwap.getPayLeg();
      final SwapLeg receiveLeg = underlyingSwap.getReceiveLeg();
      final String securityType = security.getSecurityType();
      if (payLeg.getRegionId().equals(receiveLeg.getRegionId())) {
        return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + "_" + payLeg.getRegionId().getValue()));
      }
      result.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + "_" + payLeg.getRegionId().getValue()));
      result.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + "_" + receiveLeg.getRegionId().getValue()));
      return result;
    }

    @Override
    public List<ExternalId> visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
      final ExternalId regionId = security.getRegionId();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + regionId.getValue()));
    }

    @Override
    public List<ExternalId> visitStandardFixedRecoveryCDSSecurity(final StandardFixedRecoveryCDSSecurity security) {
      final ExternalId regionId = security.getRegionId();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + regionId.getValue()));
    }

    @Override
    public List<ExternalId> visitStandardRecoveryLockCDSSecurity(final StandardRecoveryLockCDSSecurity security) {
      final ExternalId regionId = security.getRegionId();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + regionId.getValue()));
    }

    @Override
    public List<ExternalId> visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
      final ExternalId regionId = security.getRegionId();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + regionId.getValue()));
    }

    @Override
    public List<ExternalId> visitLegacyFixedRecoveryCDSSecurity(final LegacyFixedRecoveryCDSSecurity security) {
      final ExternalId regionId = security.getRegionId();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + regionId.getValue()));
    }

    @Override
    public List<ExternalId> visitLegacyRecoveryLockCDSSecurity(final LegacyRecoveryLockCDSSecurity security) {
      final ExternalId regionId = security.getRegionId();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + regionId.getValue()));
    }

    @Override
    public List<ExternalId> visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
      final CreditDefaultSwapSecurity underlyingCDS = (CreditDefaultSwapSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId())); //TODO version
      final ExternalId regionId = underlyingCDS.getRegionId();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + regionId.getValue()));
    }

    @Override
    public List<ExternalId> visitZeroCouponInflationSwapSecurity(final ZeroCouponInflationSwapSecurity security) {
      final List<ExternalId> result = new ArrayList<>();
      final SwapLeg payLeg = security.getPayLeg();
      final SwapLeg receiveLeg = security.getReceiveLeg();
      final String securityType = security.getSecurityType();
      if (payLeg.getRegionId().equals(receiveLeg.getRegionId())) {
        return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + payLeg.getRegionId().getValue()));
      }
      result.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + payLeg.getRegionId().getValue()));
      result.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + receiveLeg.getRegionId().getValue()));
      return result;
    }

    @Override
    public List<ExternalId> visitYearOnYearInflationSwapSecurity(final YearOnYearInflationSwapSecurity security) {
      final List<ExternalId> result = new ArrayList<>();
      final SwapLeg payLeg = security.getPayLeg();
      final SwapLeg receiveLeg = security.getReceiveLeg();
      final String securityType = security.getSecurityType();
      if (payLeg.getRegionId().equals(receiveLeg.getRegionId())) {
        return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + payLeg.getRegionId().getValue()));
      }
      result.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + payLeg.getRegionId().getValue()));
      result.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + receiveLeg.getRegionId().getValue()));
      return result;
    }

    @Override
    public List<ExternalId> visitFloatingRateNoteSecurity(final FloatingRateNoteSecurity security) {
      final String region = security.getRegionId().getValue();
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + region));
    }
  }
}
