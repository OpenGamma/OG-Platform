/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.obligor.definition;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.credit.obligor.CreditRating;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.obligor.Region;
import com.opengamma.analytics.financial.credit.obligor.Sector;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Class for defining the characteristics of an obligor in a derivative contract.
 * In the credit derivative context obligors can be protection buyers, protection
 * sellers or the reference entity. More generally, an obligor is someone to whom
 * one has counterparty risk.
 * <p>
 * @deprecated The concept of an obligor is useful for more than credit products
 * and so this version has been deprected. The newer equivalent is in
 * {@link com.opengamma.analytics.financial.legalentity.LegalEntityWithREDCode}
 */
@Deprecated
public class Obligor {
  /** The ticker */
  private final String _obligorTicker;
  /** The short name */
  private final String _obligorShortName;
  /** The RED code */
  private final String _obligorREDCode;
  /** The Markit composite rating */
  private final CreditRating _compositeRating;
  /** The Markit implied rating */
  private final CreditRating _impliedRating;
  /** The Moody's rating */
  private final CreditRatingMoodys _moodysCreditRating;
  /** The S&P rating */
  private final CreditRatingStandardAndPoors _standardAndPoorsCreditRating;
  /** The Fitch rating */
  private final CreditRatingFitch _fitchCreditRating;
  /** Has the obligor defaulted */
  private final boolean _hasDefaulted;
  /** The sector */
  private final Sector _sector;
  /** The region */
  private final Region _region;
  /** The country */
  private final String _country;

  /**
   * @param obligorTicker The ticker, not null or empty
   * @param obligorShortName The short name, not null or empty
   * @param obligorREDCode The RED code, not null
   * @param compositeRating The Markit composite rating, not null
   * @param impliedRating The Markit implied rating, not null
   * @param moodysCreditRating The Moody's rating, not null
   * @param standardAndPoorsCreditRating The S&P rating, not null
   * @param fitchCreditRating The Fitch rating, not null
   * @param hasDefaulted True if the obligor has defaulted
   * @param sector The sector, not null
   * @param region The region, not null
   * @param country The country, not null
   */
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
    ArgumentChecker.notNull(obligorTicker, "Obligor ticker");
    ArgumentChecker.notEmpty(obligorTicker, "Obligor ticker");
    ArgumentChecker.notNull(obligorShortName, "Obligor short name");
    ArgumentChecker.notEmpty(obligorShortName, "Obligor short name");
    ArgumentChecker.notNull(obligorREDCode, "Obligor RED code");
    ArgumentChecker.notEmpty(obligorREDCode, "Obligor RED code");
    ArgumentChecker.notNull(compositeRating, "Composite rating field is null");
    ArgumentChecker.notNull(impliedRating, "Implied rating field is null");
    ArgumentChecker.notNull(moodysCreditRating, "Moodys credit rating");
    ArgumentChecker.notNull(standardAndPoorsCreditRating, "S&P credit rating");
    ArgumentChecker.notNull(fitchCreditRating, "Fitch credit rating");
    ArgumentChecker.notNull(sector, "Sector field");
    ArgumentChecker.notNull(region, "Region field");
    ArgumentChecker.notNull(country, "Country field");
    ArgumentChecker.notEmpty(country, "Country field");
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
  }

  /**
   * Constructs an equivalent non-deprecated object. The internal delegates for the
   * {@link CreditRating}, {@link CreditRatingMoodys}, {@link CreditRatingStandardAndPoors},
   * {@link CreditRatingFitch}, {@link Sector} and {@link Region} enums are used.
   * @return A {@link com.opengamma.analytics.financial.legalentity.LegalEntityWithREDCode}
   */
  public com.opengamma.analytics.financial.legalentity.LegalEntity toObligor() {
    final Set<com.opengamma.analytics.financial.legalentity.CreditRating> creditRatings = new HashSet<>();
    creditRatings.add(_compositeRating.toCreditRating());
    creditRatings.add(_impliedRating.toCreditRating());
    creditRatings.add(_moodysCreditRating.toCreditRating());
    creditRatings.add(_standardAndPoorsCreditRating.toCreditRating());
    creditRatings.add(_fitchCreditRating.toCreditRating());
    final com.opengamma.analytics.financial.legalentity.Region region = com.opengamma.analytics.financial.legalentity.Region.of(_region.name(), Country.of(_country), (Currency) null);
    return new com.opengamma.analytics.financial.legalentity.LegalEntityWithREDCode(_obligorTicker, _obligorShortName, creditRatings,
        _sector.toSector(), region, _obligorREDCode);
  }

  /**
   * Gets the ticker.
   * @return The ticker
   */
  public String getObligorTicker() {
    return _obligorTicker;
  }

  /**
   * Gets the short name.
   * @return The short name
   */
  public String getObligorShortName() {
    return _obligorShortName;
  }

  /**
   * Gets the RED code.
   * @return The RED code
   */
  public String getObligorREDCode() {
    return _obligorREDCode;
  }

  /**
   * Gets the Markit composite rating.
   * @return The Markit composite rating
   */
  public CreditRating getCompositeRating() {
    return _compositeRating;
  }

  /**
   * Gets the implied rating.
   * @return The implied rating
   */
  public CreditRating getImpliedRating() {
    return _impliedRating;
  }

  /**
   * Gets the Moody's rating.
   * @return The Moody's rating
   */
  public CreditRatingMoodys getMoodysCreditRating() {
    return _moodysCreditRating;
  }

  /**
   * Gets the S&P rating.
   * @return The S&P rating
   */
  public CreditRatingStandardAndPoors getStandardAdPoorsCreditRating() {
    return _standardAndPoorsCreditRating;
  }

  /**
   * Gets the Fitch rating.
   * @return The Fitch rating
   */
  public CreditRatingFitch getFitchCreditRating() {
    return _fitchCreditRating;
  }

  /**
   * Returns true if the obligor has defaulted.
   * @return True if the obligor has defaulted
   */
  public boolean getHasDefaulted() {
    return _hasDefaulted;
  }

  /**
   * Gets the sector.
   * @return The sector
   */
  public Sector getSector() {
    return _sector;
  }

  /**
   * Gets the region.
   * @return The region
   */
  public Region getRegion() {
    return _region;
  }

  /**
   * Gets the country.
   * @return The country
   */
  public String getCountry() {
    return _country;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _compositeRating.hashCode();
    result = prime * result + _country.hashCode();
    result = prime * result + _fitchCreditRating.hashCode();
    result = prime * result + (_hasDefaulted ? 1231 : 1237);
    result = prime * result + _impliedRating.hashCode();
    result = prime * result + _moodysCreditRating.hashCode();
    result = prime * result + _obligorREDCode.hashCode();
    result = prime * result + _obligorShortName.hashCode();
    result = prime * result + _obligorTicker.hashCode();
    result = prime * result + _region.hashCode();
    result = prime * result + _sector.hashCode();
    result = prime * result + _standardAndPoorsCreditRating.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Obligor)) {
      return false;
    }
    final Obligor other = (Obligor) obj;
    if (!ObjectUtils.equals(_obligorTicker, other._obligorTicker)) {
      return false;
    }
    if (!ObjectUtils.equals(_obligorShortName, other._obligorShortName)) {
      return false;
    }
    if (!ObjectUtils.equals(_obligorREDCode, other._obligorREDCode)) {
      return false;
    }
    if (_sector != other._sector) {
      return false;
    }
    if (_region != other._region) {
      return false;
    }
    if (!ObjectUtils.equals(_country, other._country)) {
      return false;
    }
    if (_compositeRating != other._compositeRating) {
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
    if (_standardAndPoorsCreditRating != other._standardAndPoorsCreditRating) {
      return false;
    }
    if (_hasDefaulted != other._hasDefaulted) {
      return false;
    }
    return true;
  }

}
