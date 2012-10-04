/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

/**
 * Class for defining the characteristics of an obligor in a derivative contract
 * In the credit derivative context obligors can be protection buyers, protection sellers or the reference entity
 * More generally an obligor is someone to whom one has counterparty risk
 */
public class Obligor {

  // ---------------------------------------------------------------------------------

  // TODO : Sort out the hashCode and equals methods

  // ---------------------------------------------------------------------------------

  // Private member variables

  private final String _obligorTicker;
  private final String _obligorShortName;
  private final String _obligorREDCode;

  private final CreditRating _compositeRating;
  private final CreditRating _impliedRating;

  private final CreditRatingMoodys _moodysCreditRating;
  private final CreditRatingStandardAndPoors _standardAdPoorsCreditRating;
  private final CreditRatingFitch _fitchCreditRating;

  private final Sector _sector;
  private final Region _region;
  private final String _country;

  // ---------------------------------------------------------------------------------

  // Obligor constructor

  public Obligor(String obligorTicker,
      String obligorShortName,
      String obligorREDCode,
      CreditRating compositeRating,
      CreditRating impliedRating,
      CreditRatingMoodys moodysCreditRating,
      CreditRatingStandardAndPoors standardAdPoorsCreditRating,
      CreditRatingFitch fitchCreditRating,
      Sector sector,
      Region region,
      String country) {

    _obligorTicker = obligorTicker;
    _obligorShortName = obligorShortName;
    _obligorREDCode = obligorREDCode;

    _compositeRating = compositeRating;
    _impliedRating = impliedRating;

    _moodysCreditRating = moodysCreditRating;
    _standardAdPoorsCreditRating = standardAdPoorsCreditRating;
    _fitchCreditRating = fitchCreditRating;

    _sector = sector;
    _region = region;
    _country = country;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Public member accessor methods

  public String getObligorTicker() {
    return _obligorTicker;
  }

  public String getObligorShortName() {
    return _obligorShortName;
  }

  public String getObligorREDCode() {
    return _obligorREDCode;
  }

  public CreditRating getCompositeRating() {
    return _compositeRating;
  }

  public CreditRating getImpliedRating() {
    return _impliedRating;
  }

  public CreditRatingMoodys getMoodysCreditRating() {
    return _moodysCreditRating;
  }

  public CreditRatingStandardAndPoors getStandardAdPoorsCreditRating() {
    return _standardAdPoorsCreditRating;
  }

  public CreditRatingFitch getFitchCreditRating() {
    return _fitchCreditRating;
  }

  public Sector getSector() {
    return _sector;
  }

  public Region getRegion() {
    return _region;
  }

  public String getCountry() {
    return _country;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_compositeRating == null) ? 0 : _compositeRating.hashCode());
    result = prime * result + ((_country == null) ? 0 : _country.hashCode());
    result = prime * result + ((_fitchCreditRating == null) ? 0 : _fitchCreditRating.hashCode());
    result = prime * result + ((_impliedRating == null) ? 0 : _impliedRating.hashCode());
    result = prime * result + ((_moodysCreditRating == null) ? 0 : _moodysCreditRating.hashCode());
    result = prime * result + ((_obligorREDCode == null) ? 0 : _obligorREDCode.hashCode());
    result = prime * result + ((_obligorShortName == null) ? 0 : _obligorShortName.hashCode());
    result = prime * result + ((_obligorTicker == null) ? 0 : _obligorTicker.hashCode());
    result = prime * result + ((_region == null) ? 0 : _region.hashCode());
    result = prime * result + ((_sector == null) ? 0 : _sector.hashCode());
    result = prime * result + ((_standardAdPoorsCreditRating == null) ? 0 : _standardAdPoorsCreditRating.hashCode());
    return result;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Obligor other = (Obligor) obj;
    if (_compositeRating != other._compositeRating)
      return false;
    if (_country == null) {
      if (other._country != null)
        return false;
    } else if (!_country.equals(other._country))
      return false;
    if (_fitchCreditRating != other._fitchCreditRating)
      return false;
    if (_impliedRating != other._impliedRating)
      return false;
    if (_moodysCreditRating != other._moodysCreditRating)
      return false;
    if (_obligorREDCode == null) {
      if (other._obligorREDCode != null)
        return false;
    } else if (!_obligorREDCode.equals(other._obligorREDCode))
      return false;
    if (_obligorShortName == null) {
      if (other._obligorShortName != null)
        return false;
    } else if (!_obligorShortName.equals(other._obligorShortName))
      return false;
    if (_obligorTicker == null) {
      if (other._obligorTicker != null)
        return false;
    } else if (!_obligorTicker.equals(other._obligorTicker))
      return false;
    if (_region != other._region)
      return false;
    if (_sector != other._sector)
      return false;
    if (_standardAdPoorsCreditRating != other._standardAdPoorsCreditRating)
      return false;
    return true;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

}
