/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.bond.FloatingRateNoteSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.credit.IndexCDSSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.BondTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.EquityTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Exposure function that returns the underlying security of the given trade. If there is no underlying, then null is 
 * returned.
 */
public class UnderlyingExposureFunction implements ExposureFunction {
  
  /**
   * The name of the exposure function.
   */
  public static final String NAME = "Underlying";
  
  private final UnderlyingVisitor _visitor;

  public UnderlyingExposureFunction(final SecuritySource securitySource) {
    _visitor = new UnderlyingVisitor(ArgumentChecker.notNull(securitySource, "security source"));
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
  
  private static final class UnderlyingVisitor extends FinancialSecurityVisitorSameValueAdapter<List<ExternalId>> {
    
    private final SecuritySource _securitySource;
    
    public UnderlyingVisitor(SecuritySource securitySource) {
      super(null);
      _securitySource = ArgumentChecker.notNull(securitySource, "securitySource");
    }
  
    @Override
    public List<ExternalId> visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
      return Arrays.asList(security.getUnderlyingId());
    }
  
    @Override
    public List<ExternalId> visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
      return Arrays.asList(security.getUnderlyingId());
    }
  
    @Override
    public List<ExternalId> visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
      final List<ExternalId> result = new ArrayList<>();
      result.add(security.getLongId());
      result.add(security.getShortId());
      return result;
    }
  
    @Override
    public List<ExternalId> visitCapFloorSecurity(final CapFloorSecurity security) {
      return Arrays.asList(security.getUnderlyingId());
    }
  
    @Override
    public List<ExternalId> visitFRASecurity(final FRASecurity security) {
      return Arrays.asList(security.getUnderlyingId());
    }
  
    @Override
    public List<ExternalId> visitForwardRateAgreementSecurity(final ForwardRateAgreementSecurity security) {
      return Arrays.asList(security.getUnderlyingId());
    }
  
    @Override
    public List<ExternalId> visitForwardSwapSecurity(final ForwardSwapSecurity security) {
      final List<ExternalId> result = new ArrayList<>();
      final SwapLeg payLeg = security.getPayLeg();
      final SwapLeg receiveLeg = security.getReceiveLeg();
      if (payLeg instanceof FloatingInterestRateLeg) {
        result.add(((FloatingInterestRateLeg) payLeg).getFloatingReferenceRateId());
      }
      if (receiveLeg instanceof FloatingInterestRateLeg) {
        result.add(((FloatingInterestRateLeg) receiveLeg).getFloatingReferenceRateId());
      }
      if (result.isEmpty()) {
        return null;
      }
      return result;
    }
  
    @Override
    public List<ExternalId> visitSwapSecurity(final SwapSecurity security) {
      final List<ExternalId> result = new ArrayList<>();
      final SwapLeg payLeg = security.getPayLeg();
      final SwapLeg receiveLeg = security.getReceiveLeg();
      if (payLeg instanceof FloatingInterestRateLeg) {
        result.add(((FloatingInterestRateLeg) payLeg).getFloatingReferenceRateId());
      }
      if (receiveLeg instanceof FloatingInterestRateLeg) {
        result.add(((FloatingInterestRateLeg) receiveLeg).getFloatingReferenceRateId());
      }
      if (result.isEmpty()) {
        return null;
      }
      return result;
    }
  
    @Override
    public List<ExternalId> visitSwaptionSecurity(final SwaptionSecurity security) {
      final List<ExternalId> result = new ArrayList<>();
      final SwapSecurity underlyingSwap = (SwapSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId())); //TODO version
      final SwapLeg payLeg = underlyingSwap.getPayLeg();
      final SwapLeg receiveLeg = underlyingSwap.getReceiveLeg();
      if (payLeg instanceof FloatingInterestRateLeg) {
        result.add(((FloatingInterestRateLeg) payLeg).getFloatingReferenceRateId());
      }
      if (receiveLeg instanceof FloatingInterestRateLeg) {
        result.add(((FloatingInterestRateLeg) receiveLeg).getFloatingReferenceRateId());
      }
      if (result.isEmpty()) {
        return null;
      }
      return result;
    }
  
    @Override
    public List<ExternalId> visitZeroCouponInflationSwapSecurity(final ZeroCouponInflationSwapSecurity security) {
      final List<ExternalId> result = new ArrayList<>();
      final SwapLeg payLeg = security.getPayLeg();
      final SwapLeg receiveLeg = security.getReceiveLeg();
      if (payLeg instanceof InflationIndexSwapLeg) {
        result.add(((InflationIndexSwapLeg) payLeg).getIndexId());
      }
      if (receiveLeg instanceof InflationIndexSwapLeg) {
        result.add(((InflationIndexSwapLeg) receiveLeg).getIndexId());
      }
      if (result.isEmpty()) {
        return null;
      }
      return result;
    }
  
    @Override
    public List<ExternalId> visitYearOnYearInflationSwapSecurity(final YearOnYearInflationSwapSecurity security) {
      final List<ExternalId> result = new ArrayList<>();
      final SwapLeg payLeg = security.getPayLeg();
      final SwapLeg receiveLeg = security.getReceiveLeg();
      if (payLeg instanceof InflationIndexSwapLeg) {
        result.add(((InflationIndexSwapLeg) payLeg).getIndexId());
      }
      if (receiveLeg instanceof InflationIndexSwapLeg) {
        result.add(((InflationIndexSwapLeg) receiveLeg).getIndexId());
      }
      if (result.isEmpty()) {
        return null;
      }
      return result;
    }
  
    @Override
    public List<ExternalId> visitInterestRateSwapSecurity(final InterestRateSwapSecurity security) {
      final List<ExternalId> result = new ArrayList<>();
      for (final InterestRateSwapLeg leg : security.getLegs()) {
        if (leg instanceof FloatingInterestRateSwapLeg) {
          final ExternalIdBundle ids = ((FloatingInterestRateSwapLeg) leg).getFloatingReferenceRateId().toBundle();
          for (final ExternalId id : ids) {
            result.add(id);
            // only add the first id per leg, if multiple ids resolving to the same rate were returned
            // the caller could over estimate their exposure
            break;
          }
        }
      }
      if (result.isEmpty()) {
        return null;
      }
      return result;
    }
  
    @Override
    public List<ExternalId> visitFloatingRateNoteSecurity(final FloatingRateNoteSecurity security) {
      return Collections.singletonList(security.getBenchmarkRateId());
    }
  
    @Override
    public List<ExternalId> visitEquityTotalReturnSwapSecurity(final EquityTotalReturnSwapSecurity security) {
      return Collections.singletonList(security.getFundingLeg().getFloatingReferenceRateId());
    }
  
    @Override
    public List<ExternalId> visitBondTotalReturnSwapSecurity(final BondTotalReturnSwapSecurity security) {
      return Collections.singletonList(security.getFundingLeg().getFloatingReferenceRateId());
    }
  
    @Override
    public List<ExternalId> visitIndexCDSSecurity(IndexCDSSecurity security) {
      return security.getUnderlyingIndex().resolve().getExternalIdBundle().getExternalIds().asList();
    }
  }
}
