/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.centralcounterparty;

import com.opengamma.util.ArgumentChecker;

/**
 * Class to define the characteristics of a central counterparty (for clearing of trades) e.g. ICE, LCH or CME
 */
public class CentralCounterpartyDefinition {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Work-in-progress

  // TODO : Add the geographical region
  // TODO : Add the legal domicile
  // TODO : List of the asset classes that the CCP can clear
  // TODO : List of the Obligors who are General Clearing Members of the clearing house

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
