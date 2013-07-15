/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.obligor.definition;

import com.opengamma.analytics.financial.credit.obligor.CreditRating;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.obligor.Region;
import com.opengamma.analytics.financial.credit.obligor.Sector;
import com.opengamma.util.ArgumentChecker;

/**
 * Class for defining the characteristics of an obligor in a derivative contract
 * In the credit derivative context obligors can be protection buyers, protection sellers or the reference entity
 * More generally an obligor is someone to whom one has counterparty risk
 */
public class Obligor {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to be able to allow the user to add user-defined fields to the definition of an obligor on an ad-hoc basis (each user will have different ways of representing an obligor)
  // TODO : Continue development of Obligor concept

  // NOTE : There should be no market data within this objects definition (should only have the obligor characteristics)

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Private member variables

  // The obligor identifiers
  private final String _obligorTicker;
  private final String _obligorShortName;
  private final String _obligorREDCode;

  // The obligor credit rating (MarkIt fields)
  private final CreditRating _compositeRating;
  private final CreditRating _impliedRating;

  // The obligor credit rating (Moodys, S&P and Fitch classifications)
  private final CreditRatingMoodys _moodysCreditRating;
  private final CreditRatingStandardAndPoors _standardAndPoorsCreditRating;
  private final CreditRatingFitch _fitchCreditRating;

  // Explicit flag to determine if the obligor has already defaulted prior to the current time
  private final boolean _hasDefaulted;

  // The obligor industrial sector classification
  private final Sector _sector;

  // The regional domicile of the obligor
  private final Region _region;

  // The country of domicile of the obligor
  private final String _country;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Obligor constructor

  public Obligor(final String obligorTicker,
      final String obligorShortName,
      final String obligorREDCode,
      final CreditRating compositeRating,
      final CreditRating impliedRating,
      final CreditRatingMoodys moodysCreditRating,
      final CreditRatingStandardAndPoors standardAndPoorsCreditRating,
      final CreditRatingFitch fitchCreditRating,
      final boolean hasDefaulted,
      final Sector sector,
      final Region region,
      final String country) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check the validity of the input arguments

    ArgumentChecker.notNull(obligorTicker, "Obligor ticker");
    ArgumentChecker.isFalse(obligorTicker.isEmpty(), "Obligor ticker");

    ArgumentChecker.notNull(obligorShortName, "Obligor short name");
    ArgumentChecker.isFalse(obligorShortName.isEmpty(), "Obligor short name");

    ArgumentChecker.notNull(obligorREDCode, "Obligor RED code");
    ArgumentChecker.isFalse(obligorREDCode.isEmpty(), "Obligor RED code");

    ArgumentChecker.notNull(compositeRating, "Composite rating field is null");
    ArgumentChecker.notNull(impliedRating, "Implied rating field is null");

    ArgumentChecker.notNull(moodysCreditRating, "Moodys credit rating");
    ArgumentChecker.notNull(standardAndPoorsCreditRating, "S&P credit rating");
    ArgumentChecker.notNull(fitchCreditRating, "Fitch credit rating");

    ArgumentChecker.notNull(sector, "Sector field");
    ArgumentChecker.notNull(region, "Region field");

    ArgumentChecker.notNull(country, "Country field");
    ArgumentChecker.isFalse(country.isEmpty(), "Country field");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Assign the member variables for the obligor object

    _obligorTicker = obligorTicker;
    _obligorShortName = obligorShortName;
    _obligorREDCode = obligorREDCode;

    _compositeRating = compositeRating;
    _impliedRating = impliedRating;

    _moodysCreditRating = moodysCreditRating;
    _standardAndPoorsCreditRating = standardAndPoorsCreditRating;
    _fitchCreditRating = fitchCreditRating;

    _hasDefaulted = hasDefaulted;

    _sector = sector;
    _region = region;
    _country = country;

    // ----------------------------------------------------------------------------------------------------------------------------------------
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
    return _standardAndPoorsCreditRating;
  }

  public CreditRatingFitch getFitchCreditRating() {
    return _fitchCreditRating;
  }

  public boolean getHasDefaulted() {
    return _hasDefaulted;
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
    result = prime * result + ((_standardAndPoorsCreditRating == null) ? 0 : _standardAndPoorsCreditRating.hashCode());
    return result;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Obligor other = (Obligor) obj;
    if (_compositeRating != other._compositeRating) {
      return false;
    }
    if (_country == null) {
      if (other._country != null) {
        return false;
      }
    } else if (!_country.equals(other._country)) {
      return false;
    }
    if (_fitchCreditRating != other._fitchCreditRating) {
      return false;
    }
    if (_impliedRating != other._impliedRating) {
      return false;
    }
    if (_moodysCreditRating != other._moodysCreditRating) {
      return false;
    }
    if (_obligorREDCode == null) {
      if (other._obligorREDCode != null) {
        return false;
      }
    } else if (!_obligorREDCode.equals(other._obligorREDCode)) {
      return false;
    }
    if (_obligorShortName == null) {
      if (other._obligorShortName != null) {
        return false;
      }
    } else if (!_obligorShortName.equals(other._obligorShortName)) {
      return false;
    }
    if (_obligorTicker == null) {
      if (other._obligorTicker != null) {
        return false;
      }
    } else if (!_obligorTicker.equals(other._obligorTicker)) {
      return false;
    }
    if (_region != other._region) {
      return false;
    }
    if (_sector != other._sector) {
      return false;
    }
    if (_standardAndPoorsCreditRating != other._standardAndPoorsCreditRating) {
      return false;
    }
    return true;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
