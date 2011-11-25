/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.time.Tenor;

/**
 * Generates Bloomberg codes for swaption volatilities given tenors.
 */
public class BloombergSwaptionVolatilitySurfaceInstrumentProvider implements SurfaceInstrumentProvider<Tenor, Tenor> {
  private String _countryPrefix;
  private String _typePrefix;
  private String _postfix;
  private boolean _zeroPadSwapMaturityTenor;
  private boolean _zeroPadSwaptionExpiryTenor;
  private String _dataFieldName; // expecting MarketDataRequirementNames.MARKET_VALUE or PX_LAST

  public BloombergSwaptionVolatilitySurfaceInstrumentProvider(final String countryPrefix, final String typePrefix, final boolean zeroPadSwapMaturityTenor, final boolean zeroPadSwaptionExpiryTenor,
      final String postfix) {
    this(countryPrefix, typePrefix, zeroPadSwapMaturityTenor, zeroPadSwaptionExpiryTenor, postfix, MarketDataRequirementNames.MARKET_VALUE);
  }

  public BloombergSwaptionVolatilitySurfaceInstrumentProvider(final String countryPrefix, final String typePrefix, final boolean zeroPadSwapMaturityTenor, final boolean zeroPadSwaptionExpiryTenor,
      final String postfix, final String dataFieldName) {
    Validate.notNull(countryPrefix);
    Validate.notNull(typePrefix);
    Validate.notNull(postfix);
    _countryPrefix = countryPrefix;
    _typePrefix = typePrefix;
    _zeroPadSwapMaturityTenor = zeroPadSwapMaturityTenor;
    _zeroPadSwaptionExpiryTenor = zeroPadSwaptionExpiryTenor;
    _postfix = postfix;
    _dataFieldName = dataFieldName;
  }

  @Override
  public ExternalId getInstrument(final Tenor swapMaturityTenor, final Tenor swaptionExpiryTenor) {
    final StringBuffer ticker = new StringBuffer();
    ticker.append(_countryPrefix);
    ticker.append(_typePrefix);
    ticker.append(tenorToCode(swaptionExpiryTenor, _zeroPadSwaptionExpiryTenor));
    ticker.append(tenorToCode(swapMaturityTenor, _zeroPadSwapMaturityTenor));
    ticker.append(_postfix);
    return ExternalId.of(SecurityUtils.BLOOMBERG_TICKER_WEAK, ticker.toString());
  }

  @Override
  public ExternalId getInstrument(final Tenor startTenor, final Tenor maturity, final LocalDate surfaceDate) {
    return getInstrument(startTenor, maturity);
  }

  private String tenorToCode(final Tenor tenor, final boolean prepadWithZero) {
    if (tenor.getPeriod().getYears() == 0) {
      final int months = tenor.getPeriod().getMonths();
      if (months > 0) {
        final String[] monthsTable = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
                                      "1A", "1B", "1C", "1D", "1E", "1F", "1G", "1H", "1I", "1J", "1K", "1L"};

        final String result = monthsTable[months - 1];
        if (result.length() == 1 && prepadWithZero) {
          return "0" + result;
        } 
        return result;
      } 
      throw new OpenGammaRuntimeException("Cannot generate encoding for tenor " + tenor);
    } 
    if (tenor.getPeriod().getYears() < 10 && prepadWithZero) {
      return "0" + Integer.toString(tenor.getPeriod().getYears());
    } 
    return Integer.toString(tenor.getPeriod().getYears());    
  }

  /**
   * Gets the countryPrefix field.
   * @return the countryPrefix
   */
  public String getCountryPrefix() {
    return _countryPrefix;
  }

  /**
   * Gets the typePrefix field.
   * @return the typePrefix
   */
  public String getTypePrefix() {
    return _typePrefix;
  }

  /**
   * Gets the postfix field.
   * @return the postfix
   */
  public String getPostfix() {
    return _postfix;
  }

  /**
   * Gets the zeroPadSwaptionExpiryTenor field.
   * @return the zeroPadSwaptionExpiryTenor
   */
  public boolean isZeroPadSwapMaturityTenor() {
    return _zeroPadSwaptionExpiryTenor;
  }

  /**
   * Gets the zeroPadSwaptionExpiryTenor field.
   * @return the zeroPadSwaptionExpiryTenor
   */
  public boolean isZeroPadSwaptionExpiryTenor() {
    return _zeroPadSwaptionExpiryTenor;
  }

  /**
   * @return The data field name - should be PX_LAST
   */
  @Override
  public String getDataFieldName() {
    return _dataFieldName;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof BloombergSwaptionVolatilitySurfaceInstrumentProvider)) {
      return false;
    }
    final BloombergSwaptionVolatilitySurfaceInstrumentProvider other = (BloombergSwaptionVolatilitySurfaceInstrumentProvider) o;
    // we can avoid using ObjectUtil.equals because we validated the strings as not null.
    return getCountryPrefix().equals(other.getCountryPrefix()) &&
           getPostfix().equals(other.getPostfix()) &&
           getTypePrefix().equals(other.getTypePrefix()) &&
           isZeroPadSwapMaturityTenor() == other.isZeroPadSwapMaturityTenor() &&
           isZeroPadSwaptionExpiryTenor() == other.isZeroPadSwaptionExpiryTenor() &&
           getDataFieldName().equals(other.getDataFieldName());
  }

  @Override
  public int hashCode() {
    return getCountryPrefix().hashCode() + getTypePrefix().hashCode() + getPostfix().hashCode() + getDataFieldName().hashCode();
  }
}
