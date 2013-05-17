/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.centralcounterparty;

import java.util.ArrayList;

import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
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

  // An array of Obligor objects representing the General Clearing Members of this CCP
  private final ArrayList<Obligor> _generalClearingMembers;

  // The value of the reserve fund (all assets)
  private final double _reserveFundAmount;

  // The cashflow waterfall for the reserve fund
  private final ReserveFundWaterfall _reserveFundWaterfall;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public CentralCounterpartyDefinition(
      final String ccpName,
      final ArrayList<Obligor> generalClearingMembers,
      final double reserveFundAmount,
      final ReserveFundWaterfall reserveFundWaterfall) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    ArgumentChecker.notNull(ccpName, "Central Counterparty name");
    ArgumentChecker.notNull(generalClearingMembers, "General Clearing Members");
    ArgumentChecker.notNegative(reserveFundAmount, "Reserve fund amount");
    ArgumentChecker.notNull(reserveFundWaterfall, "Reserve fund waterfall");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _ccpName = ccpName;

    _generalClearingMembers = generalClearingMembers;

    _reserveFundAmount = reserveFundAmount;

    _reserveFundWaterfall = reserveFundWaterfall;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  public String getCentralCounterpartyName() {
    return _ccpName;
  }

  public ArrayList<Obligor> getGeneralClearingMembers() {
    return _generalClearingMembers;
  }

  public double getReserveFundAmount() {
    return _reserveFundAmount;
  }

  public ReserveFundWaterfall getReserveFundWaterfall() {
    return _reserveFundWaterfall;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
