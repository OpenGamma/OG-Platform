/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;

/**
 * Repository of summary factories.
 */
public class SummaryProvider {

  private final Map<String, SummaryFactory<?>> _factoriesBySecurityType = new HashMap<String, SummaryFactory<?>>();
  
  public SummaryProvider(SecuritySource securitySource) {
    register(new BondSummaryFactory());
    register(new CapFloorCMSSpreadSummaryFactory());
    register(new CapFloorSummaryFactory());
    register(new EquityBarrierOptionSummaryFactory());
    register(new EquityIndexOptionSummaryFactory());
    register(new EquityOptionSummaryFactory(securitySource));
    register(new EquitySummaryFactory());
    register(new EquityVarianceSwapSummaryFactory());
    register(new FRASummaryFactory());
    register(new FutureSummaryFactory());
    register(new FXBarrierOptionSummaryFactory());
    register(new FXForwardSummaryFactory(securitySource));
    register(new FXOptionSummaryFactory());
    register(new IRFutureOptionSummaryFactory());
    register(new NonDeliverableFXOptionSummaryFactory());
    register(new SwapSummaryFactory());
    register(new SwaptionSummaryFactory(securitySource));
  }

  private void register(SummaryFactory<?> summaryFactory) {
    _factoriesBySecurityType.put(summaryFactory.getSecurityType(), summaryFactory);
  }
  
  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  public Summary getSummary(Security security) {
    SummaryFactory<Security> factory = (SummaryFactory<Security>) _factoriesBySecurityType.get(security.getSecurityType());
    if (factory == null) {
      return null;
    }
    return factory.getSummary(security);
  }
  
}
