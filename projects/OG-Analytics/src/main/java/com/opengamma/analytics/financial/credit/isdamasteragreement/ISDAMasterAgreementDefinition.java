/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdamasteragreement;

import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to specify the terms of a ISDA Master Agreement between two counterparties
 */
public class ISDAMasterAgreementDefinition {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Specify the two counterparties the Master Agreement is between

  private final Obligor _counterpartyA;
  private final Obligor _counterpartyB;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public ISDAMasterAgreementDefinition(
      final Obligor counterpartyA,
      final Obligor counterpartyB) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    ArgumentChecker.notNull(counterpartyA, "Counterparty A");
    ArgumentChecker.notNull(counterpartyB, "Counterparty B");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _counterpartyA = counterpartyA;
    _counterpartyB = counterpartyB;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public Obligor getCounterpartyA() {
    return _counterpartyA;
  }

  public Obligor getCounterpartyB() {
    return _counterpartyB;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
