/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.centralcounterparty;

import com.opengamma.util.ArgumentChecker;

/**
 * Class to define the characteristics of a central counterparty (for clearing of trades) e.g. ICE or CME
 */
public class CentralCounterpartyDefinition {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // The name of the Central Counterparty
  private final String _ccpName;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public CentralCounterpartyDefinition(
      final String ccpName) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    ArgumentChecker.notNull(ccpName, "Central Counterparty name");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _ccpName = ccpName;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  public String getCentralCounterpartyName() {
    return _ccpName;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
