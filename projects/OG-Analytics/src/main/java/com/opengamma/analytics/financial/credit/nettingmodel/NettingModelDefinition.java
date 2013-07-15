/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.nettingmodel;

import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to define the netting model used to net trades
 */
public class NettingModelDefinition {

  //----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Work - in - Progress

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Specify the two obligors who the collateral agreement is between
  private final Obligor _counterpartyA;
  private final Obligor _counterpartyB;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public NettingModelDefinition(final Obligor counterpartyA, final Obligor counterpartyB) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check the validity of the input arguments

    ArgumentChecker.notNull(counterpartyA, "Counterparty A");
    ArgumentChecker.notNull(counterpartyB, "Counterparty B");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _counterpartyA = counterpartyA;
    _counterpartyB = counterpartyB;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  public Obligor getCounterpartyA() {
    return _counterpartyA;
  }

  public Obligor getCounterpartyB() {
    return _counterpartyB;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
