/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.definition;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.Obligor;

/**
 * 
 */
public class TestCDS {

  private final BuySellProtection _buySellProtection;

  private final Obligor _protectionBuyer;
  private final Obligor _protectionSeller;
  private final Obligor _referenceEntity;

  public TestCDS(BuySellProtection buySellProtection, Obligor protectionBuyer, Obligor protectionSeller, Obligor referenceEntity) {

    _buySellProtection = buySellProtection;

    _protectionBuyer = protectionBuyer;
    _protectionSeller = protectionSeller;
    _referenceEntity = referenceEntity;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public BuySellProtection getBuySellProtection() {
    return _buySellProtection;
  }

  public Obligor getProtectionBuyer() {
    return _protectionBuyer;
  }

  public Obligor getProtectionSeller() {
    return _protectionSeller;
  }

  public Obligor getReferenceEntity() {
    return _referenceEntity;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

}
