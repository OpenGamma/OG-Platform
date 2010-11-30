/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Tenor;

/**
 * Generates Bloomberg codes for swaption volatilities given tenors.
 */
public class BloombergSwaptionVolatilitySurfaceInstrumentProvider implements SurfaceInstrumentProvider<Tenor, Tenor> {
  private String _countryPrefix;
  private String _typePrefix;
  private String _postfix;
  private boolean _zeroPadFirstTenor;
  private boolean _zeroPadSecondTenor;
  
  public BloombergSwaptionVolatilitySurfaceInstrumentProvider(String countryPrefix, String typePrefix, boolean zeroPadFirstTenor, boolean zeroPadSecondTenor, String postfix) {
    Validate.notNull(countryPrefix);
    Validate.notNull(typePrefix);
    Validate.notNull(postfix);
    _countryPrefix = countryPrefix;
    _typePrefix = typePrefix;
    _zeroPadFirstTenor = zeroPadFirstTenor;
    _zeroPadSecondTenor = zeroPadSecondTenor;
    _postfix = postfix;
  }
  @Override
  public Identifier getInstrument(Tenor startTenor, Tenor maturity) {
    StringBuffer ticker = new StringBuffer();
    ticker.append(_countryPrefix);
    ticker.append(_typePrefix);
    ticker.append(tenorToCode(startTenor, _zeroPadFirstTenor));
    ticker.append(tenorToCode(maturity, _zeroPadSecondTenor));
    ticker.append(_postfix);
    return Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, ticker.toString());
  }
  
  private String tenorToCode(Tenor tenor, boolean prepadWithZero) {
    if (tenor.getPeriod().getYears() == 0) {
      int months = tenor.getPeriod().getMonths();
      if (months > 0) {
        final String[] monthsTable = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", 
                                      "1A", "1B", "1C", "1D", "1E", "1F", "1G", "1H", "1I", "1J", "1K", "1L" };
        
        String result = monthsTable[months - 1];
        if (result.length() == 1 && prepadWithZero) {
          return "0" + result;
        } else {
          return result;
        }
      } else {
        throw new OpenGammaRuntimeException("Cannot generate encoding for tenor " + tenor);
      }
    } else {
      if (tenor.getPeriod().getYears() < 10 && prepadWithZero) {
        return "0" + Integer.toString(tenor.getPeriod().getYears());
      } else {
        return Integer.toString(tenor.getPeriod().getYears());
      }
    }
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
   * Gets the zeroPadFirstTenor field.
   * @return the zeroPadFirstTenor
   */
  public boolean isZeroPadFirstTenor() {
    return _zeroPadFirstTenor;
  }
  /**
   * Gets the zeroPadSecondTenor field.
   * @return the zeroPadSecondTenor
   */
  public boolean isZeroPadSecondTenor() {
    return _zeroPadSecondTenor;
  }

  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof BloombergSwaptionVolatilitySurfaceInstrumentProvider)) {
      return false;
    }
    BloombergSwaptionVolatilitySurfaceInstrumentProvider other = (BloombergSwaptionVolatilitySurfaceInstrumentProvider) o;
    // we can avoid using ObjectUtil.equals because we validated the strings as not null.
    return getCountryPrefix().equals(other.getCountryPrefix()) &&
           getPostfix().equals(other.getPostfix()) &&
           getTypePrefix().equals(other.getTypePrefix()) &&
           isZeroPadFirstTenor() == other.isZeroPadFirstTenor() &&
           isZeroPadSecondTenor() == other.isZeroPadSecondTenor();
  }
  
  public int hashCode() {
    return getCountryPrefix().hashCode() + getTypePrefix().hashCode() + getPostfix().hashCode();
  }
}
