/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.centralcounterparty;

import java.util.ArrayList;

import com.opengamma.analytics.financial.credit.obligor.LegalDomicile;
import com.opengamma.analytics.financial.credit.obligor.Region;
import com.opengamma.analytics.financial.credit.obligor.definition.GeneralClearingMember;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to define the characteristics of a central counterparty (for clearing of trades) e.g. ICE, LCH or CME
 */
public class CentralCounterpartyDefinition {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Work-in-progress

  // TODO : Need to add the IM and VM accounts for each clearing member
  // TODO : Need to include netting rules
  // TODO : Need to include frequency with which reserve fund amounts from GCM's are re-calculated
  // TODO : Add hashCode() and equals() methods

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // The name of the Central Counterparty
  private final String _ccpName;

  // An array of Obligor objects representing the General Clearing Members of this CCP
  private final ArrayList<GeneralClearingMember> _generalClearingMembers;

  // The list of asset classes that the CCP can clear
  private final ArrayList<AssetClasses> _assetClasses;

  // The geographical regions where the CCP operates e.g. US markets
  private final ArrayList<Region> _regions;

  // The legal domicile of the CCP (under whose legal jurisdiction it falls)
  private final LegalDomicile _legalDomicile;

  // The value of the reserve fund (all assets)
  private final double _reserveFundAmount;

  // The cashflow waterfall for the reserve fund
  private final ReserveFundWaterfall _reserveFundWaterfall;

  // The number of days grace a GCM is granted before a failure to honour their obligations results in the CCP liquidating positions
  private final int _gracePeriod;

  // The time in days it takes the CCP to become the legal counterparty between the two GCM's (can be zero to represent intraday novation)
  private final int _novationPeriod;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public CentralCounterpartyDefinition(
      final String ccpName,
      final ArrayList<GeneralClearingMember> generalClearingMembers,
      final ArrayList<AssetClasses> assetClasses,
      final ArrayList<Region> regions,
      final LegalDomicile legalDomicile,
      final double reserveFundAmount,
      final ReserveFundWaterfall reserveFundWaterfall,
      final int gracePeriod,
      final int novationPeriod) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    ArgumentChecker.notNull(ccpName, "Central Counterparty name");
    ArgumentChecker.notNull(generalClearingMembers, "General Clearing Members");
    ArgumentChecker.notNull(assetClasses, "Asset Classes");
    ArgumentChecker.notNull(regions, "Regions");
    ArgumentChecker.notNull(legalDomicile, "Legal Domicile");
    ArgumentChecker.notNegative(reserveFundAmount, "Reserve fund amount");
    ArgumentChecker.notNull(reserveFundWaterfall, "Reserve fund waterfall");
    ArgumentChecker.notNegative(gracePeriod, "Grace Period");
    ArgumentChecker.notNegative(novationPeriod, "Novation Period");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _ccpName = ccpName;

    _generalClearingMembers = generalClearingMembers;

    _assetClasses = assetClasses;

    _regions = regions;
    _legalDomicile = legalDomicile;

    _reserveFundAmount = reserveFundAmount;

    _reserveFundWaterfall = reserveFundWaterfall;

    _gracePeriod = gracePeriod;

    _novationPeriod = novationPeriod;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  public String getCentralCounterpartyName() {
    return _ccpName;
  }

  public ArrayList<GeneralClearingMember> getGeneralClearingMembers() {
    return _generalClearingMembers;
  }

  public ArrayList<AssetClasses> getAssetClasses() {
    return _assetClasses;
  }

  public ArrayList<Region> getRegions() {
    return _regions;
  }

  public LegalDomicile getLegalDomicile() {
    return _legalDomicile;
  }

  public double getReserveFundAmount() {
    return _reserveFundAmount;
  }

  public ReserveFundWaterfall getReserveFundWaterfall() {
    return _reserveFundWaterfall;
  }

  public int getGracePeriod() {
    return _gracePeriod;
  }

  public int getNovationPeriod() {
    return _novationPeriod;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public int getNumberOfGeneralClearingMembers() {
    return _generalClearingMembers.size();
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public double calculateReserveFundValue() {

    double reserveFundValue = 0.0;

    /*
    for (Iterator i = _generalClearingMembers.iterator(); i.hasNext(); ) {
      reserveFundValue += _generalClearingMembers.get(i).getReserveFundContribution();
    }
    */

    for (int i = 0; i < getNumberOfGeneralClearingMembers(); i++) {
      reserveFundValue += _generalClearingMembers.get(i).getReserveFundContribution();
    }

    return reserveFundValue;
  }
  // ----------------------------------------------------------------------------------------------------------------------------------------
}
