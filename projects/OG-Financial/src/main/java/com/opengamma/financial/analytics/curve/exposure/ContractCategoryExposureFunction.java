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
import com.opengamma.financial.security.forward.AgricultureForwardSecurity;
import com.opengamma.financial.security.forward.CommodityForwardSecurity;
import com.opengamma.financial.security.forward.EnergyForwardSecurity;
import com.opengamma.financial.security.forward.MetalForwardSecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Exposure function that returns the contract type as an {@link ExternalId} for a given trade.
 */
public class ContractCategoryExposureFunction implements ExposureFunction {
  
  /**
   * The name of the exposure function.
   */
  public static final String NAME = "Contract Category";

  /** Contract identifier */
  public static final String CONTRACT_IDENTIFIER = "ContractType";
  
  private final ContractTypeVisitor _visitor;

  /**
   * Default constructor for ContractCategoryExposureFunction.
   * @param securitySource the security source used to look up the underlying.
   */
  public ContractCategoryExposureFunction(final SecuritySource securitySource) {
    _visitor = new ContractTypeVisitor(ArgumentChecker.notNull(securitySource, "securitySource"));
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

  private static List<ExternalId> getContractType(final FutureSecurity security) {
    return Arrays.asList(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, security.getContractCategory()));
  }

  private static List<ExternalId> getContractType(final CommodityForwardSecurity security) {
    return Arrays.asList(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, security.getContractCategory()));
  }
  
  /**
   * Implementation of the FinancialSecurityVisitor that returns the contract type for a security. If the security does 
   * not have a contract type, then null is returned.
   */
  private static final class ContractTypeVisitor extends FinancialSecurityVisitorSameValueAdapter<List<ExternalId>> {
    
    private final SecuritySource _securitySource;
    
    /**
     * Default constructor that initialises the default returned value to null.  
     */
    public ContractTypeVisitor(SecuritySource securitySource) {
      super(null);
      _securitySource = ArgumentChecker.notNull(securitySource, "securitySource");
    }

    @Override
    public List<ExternalId> visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
      return getContractType(security);
    }

    @Override
    public List<ExternalId> visitBondFutureSecurity(final BondFutureSecurity security) {
      return getContractType(security);
    }

    @Override
    public List<ExternalId> visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
      return getContractType(security);
    }

    @Override
    public List<ExternalId> visitFXFutureSecurity(final FXFutureSecurity security) {
      return getContractType(security);
    }

    @Override
    public List<ExternalId> visitStockFutureSecurity(final StockFutureSecurity security) {
      return getContractType(security);
    }

    @Override
    public List<ExternalId> visitEquityFutureSecurity(final EquityFutureSecurity security) {
      return getContractType(security);
    }

    @Override
    public List<ExternalId> visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
      return getContractType(security);
    }

    @Override
    public List<ExternalId> visitIndexFutureSecurity(final IndexFutureSecurity security) {
      return getContractType(security);
    }

    @Override
    public List<ExternalId> visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
      return getContractType(security);
    }

    @Override
    public List<ExternalId> visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
      return getContractType(security);
    }

    @Override
    public List<ExternalId> visitMetalFutureSecurity(final MetalFutureSecurity security) {
      return getContractType(security);
    }

    @Override
    public List<ExternalId> visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
      final FutureSecurity underlyingSecurity = (FutureSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      return getContractType(underlyingSecurity);
    }

    @Override
    public List<ExternalId> visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
      final FutureSecurity underlyingSecurity = (FutureSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      return getContractType(underlyingSecurity);
    }

    @Override
    public List<ExternalId> visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
      final FutureSecurity underlyingSecurity = (FutureSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      return getContractType(underlyingSecurity);
    }

    @Override
    public List<ExternalId> visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
      final FutureSecurity underlyingSecurity = (FutureSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      return getContractType(underlyingSecurity);
    }

    @Override
    public List<ExternalId> visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
      final FutureSecurity underlyingSecurity = (FutureSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      return getContractType(underlyingSecurity);
    }

    @Override
    public List<ExternalId> visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
      final FutureSecurity underlyingSecurity = (FutureSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      return getContractType(underlyingSecurity);
    }

    @Override
    public List<ExternalId> visitEnergyForwardSecurity(final EnergyForwardSecurity security) {
      return getContractType(security);
    }

    @Override
    public List<ExternalId> visitAgricultureForwardSecurity(final AgricultureForwardSecurity security) {
      return getContractType(security);
    }

    @Override
    public List<ExternalId> visitMetalForwardSecurity(final MetalForwardSecurity security) {
      return getContractType(security);
    }
  }
}
