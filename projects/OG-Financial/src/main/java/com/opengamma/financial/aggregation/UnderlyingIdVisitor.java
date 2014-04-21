/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Visitor that returns the value of a security's underlying ID.
 */
public class UnderlyingIdVisitor extends FinancialSecurityVisitorAdapter<String> {

  /**
   * Code to use for not applicable.
   */
  public static final String NOT_APPLICABLE = "N/A";

  private final ExternalScheme _preferredScheme;
  private final SecuritySource _securitySource;

  /**
   * Creates an instance.
   * 
   * @param preferredScheme  the preferred scheme, not null
   * @param securitySource  the security source, not null
   */
  public UnderlyingIdVisitor(ExternalScheme preferredScheme, SecuritySource securitySource) {
    ArgumentChecker.notNull(securitySource, "secSource");
    ArgumentChecker.notNull(preferredScheme, "preferredScheme");
    _preferredScheme = preferredScheme;
    _securitySource = securitySource;
  }

  //-------------------------------------------------------------------------
  @Override
  public String visitCommodityFutureOptionSecurity(CommodityFutureOptionSecurity security) {
    if (security.getUnderlyingId().isScheme(_preferredScheme)) {
      String identifier = security.getUnderlyingId().getValue();
      return identifier != null ? identifier : NOT_APPLICABLE;
    } else {
      Security underlying = _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      if (underlying != null) {
        String identifier = underlying.getExternalIdBundle().getValue(_preferredScheme);
        return identifier != null ? identifier : NOT_APPLICABLE;
      } else {
        String identifier = security.getUnderlyingId() != null ? security.getUnderlyingId().getValue() : null;
        return identifier != null ? identifier : NOT_APPLICABLE;
      }
    }
  }

  @Override
  public String visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
    String identifier = security.getUnderlyingId().getValue();
    return identifier != null ? identifier : NOT_APPLICABLE;
  }

  @Override
  public String visitEquityIndexFutureOptionSecurity(EquityIndexFutureOptionSecurity security) {
    String identifier = security.getUnderlyingId().getValue();
    return identifier != null ? identifier : NOT_APPLICABLE;
  }

  @Override
  public String visitEquityOptionSecurity(EquityOptionSecurity security) {
    if (security.getUnderlyingId().isScheme(_preferredScheme)) {
      String identifier = security.getUnderlyingId().getValue();
      return identifier != null ? identifier : NOT_APPLICABLE;
    } else {
      Security underlying = _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      if (underlying != null) {
        String identifier = underlying.getExternalIdBundle().getValue(_preferredScheme);
        return identifier != null ? identifier : NOT_APPLICABLE;
      } else {
        String identifier = security.getUnderlyingId() != null ? security.getUnderlyingId().getValue() : null;
        return identifier != null ? identifier : NOT_APPLICABLE;
      }
    }
  }

  @Override
  public String visitEquityBarrierOptionSecurity(EquityBarrierOptionSecurity security) {
    if (security.getUnderlyingId().isScheme(_preferredScheme)) {
      String identifier = security.getUnderlyingId().getValue();
      return identifier != null ? identifier : NOT_APPLICABLE;
    } else {
      Security underlying = _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      if (underlying != null) {
        String identifier = underlying.getExternalIdBundle().getValue(_preferredScheme);
        return identifier != null ? identifier : NOT_APPLICABLE;
      } else {
        String identifier = security.getUnderlyingId() != null ? security.getUnderlyingId().getValue() : null;
        return identifier != null ? identifier : NOT_APPLICABLE;
      }
    }
  }

  @Override
  public String visitFXOptionSecurity(FXOptionSecurity fxOptionSecurity) {
    UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxOptionSecurity.getCallCurrency(),
                                                                   fxOptionSecurity.getPutCurrency());
    return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
  }

  @Override
  public String visitEquitySecurity(EquitySecurity equitySecurity) {
    String ticker = equitySecurity.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
    String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitMetalFutureSecurity(MetalFutureSecurity security) {
    String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitBondFutureSecurity(BondFutureSecurity security) {
    String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitEnergyFutureSecurity(EnergyFutureSecurity security) {
    String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitDeliverableSwapFutureSecurity(DeliverableSwapFutureSecurity security) {
    String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitEquityFutureSecurity(EquityFutureSecurity security) {
    String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitEquityIndexDividendFutureSecurity(EquityIndexDividendFutureSecurity security) {
    String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitFXFutureSecurity(FXFutureSecurity security) {
    String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitIndexFutureSecurity(IndexFutureSecurity security) {
    String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
    String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitStockFutureSecurity(StockFutureSecurity security) {
    String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitNonDeliverableFXOptionSecurity(NonDeliverableFXOptionSecurity fxOptionSecurity) {
    UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxOptionSecurity.getCallCurrency(),
                                                                   fxOptionSecurity.getPutCurrency());
    return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
  }

  @Override
  public String visitFXDigitalOptionSecurity(FXDigitalOptionSecurity fxOptionSecurity) {
    UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxOptionSecurity.getCallCurrency(),
                                                                   fxOptionSecurity.getPutCurrency());
    return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
  }

  @Override
  public String visitNonDeliverableFXDigitalOptionSecurity(NonDeliverableFXDigitalOptionSecurity fxOptionSecurity) {
    UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxOptionSecurity.getCallCurrency(),
                                                                   fxOptionSecurity.getPutCurrency());
    return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
  }

  @Override
  public String visitFXBarrierOptionSecurity(FXBarrierOptionSecurity fxBarrierOptionSecurity) {
    UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxBarrierOptionSecurity.getCallCurrency(),
                                                                   fxBarrierOptionSecurity.getPutCurrency());
    return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
  }

  @Override
  public String visitFXForwardSecurity(FXForwardSecurity fxForwardSecurity) {
    UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxForwardSecurity.getPayCurrency(),
                                                                   fxForwardSecurity.getReceiveCurrency());
    return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
  }

  @Override
  public String visitNonDeliverableFXForwardSecurity(NonDeliverableFXForwardSecurity ndfFxForwardSecurity) {
    UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(ndfFxForwardSecurity.getPayCurrency(),
                                                                   ndfFxForwardSecurity.getReceiveCurrency());
    return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
  }

  @Override
  public String visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
    if (security.getUnderlyingId().isScheme(_preferredScheme)) {
      String identifier = security.getUnderlyingId().getValue();
      return identifier != null ? identifier : NOT_APPLICABLE;
    }
    Security underlying = _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
    String identifier = underlying.getExternalIdBundle().getValue(_preferredScheme);
    return identifier != null ? identifier : NOT_APPLICABLE;
  }

  @Override
  public String visitSwaptionSecurity(SwaptionSecurity security) {
    SwapSecurity underlying = (SwapSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
    String name = underlying.getName();
    return (name != null && name.length() > 0) ? name : NOT_APPLICABLE;
  }

  @Override
  public String visitCreditDefaultSwapIndexSecurity(CreditDefaultSwapIndexSecurity security) {
    if (security.getReferenceEntity().isScheme(_preferredScheme)) {
      String identifier = security.getReferenceEntity().getValue();
      return identifier != null ? identifier : NOT_APPLICABLE;
    }
    Security underlying = _securitySource.getSingle(ExternalIdBundle.of(security.getReferenceEntity()));
    String identifier = underlying.getExternalIdBundle().getValue(_preferredScheme);
    return identifier != null ? identifier : NOT_APPLICABLE;
  }

  @Override
  public String visitCreditDefaultSwapOptionSecurity(CreditDefaultSwapOptionSecurity security) {
    if (security.getUnderlyingId().isScheme(_preferredScheme)) {
      String identifier = security.getUnderlyingId().getValue();
      return identifier != null ? identifier : NOT_APPLICABLE;
    }
    Security underlying = _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
    String identifier = underlying.getExternalIdBundle().getValue(_preferredScheme);
    return identifier != null ? identifier : NOT_APPLICABLE;
  }

}
