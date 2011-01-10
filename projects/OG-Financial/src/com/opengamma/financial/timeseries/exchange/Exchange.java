/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.exchange;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * A simple definition of an exchange.
 */
public class Exchange {

  private String _mic;
  private String _description;
  private String _countryCode;
  private String _country;
  private String _city;
  private String _acr;
  private String _status;

  /**
   * @param mic the mic to set
   */
  protected void setMic(String mic) {
    _mic = mic;
  }
  
  /**
   * @param description the description to set
   */
  protected void setDescription(String description) {
    _description = description;
  }
  
  /**
   * @param countryCode the countryCode to set
   */
  protected void setCountryCode(String countryCode) {
    _countryCode = countryCode;
  }
  
  /**
   * @param country the country to set
   */
  protected void setCountry(String country) {
    _country = country;
  }
  
  /**
   * @param city the city to set
   */
  protected void setCity(String city) {
    _city = city;
  }
  
  /**
   * @param acr the acr to set
   */
  protected void setAcr(String acr) {
    _acr = acr;
  }
  
  /**
   * @param status the status to set
   */
  protected void setStatus(String status) {
    _status = status;
  }
  
  /**
   * @return the mic
   */
  public String getMic() {
    return _mic;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return _description;
  }

  /**
   * @return the countryCode
   */
  public String getCountryCode() {
    return _countryCode;
  }

  /**
   * @return the country
   */
  public String getCountry() {
    return _country;
  }

  /**
   * @return the city
   */
  public String getCity() {
    return _city;
  }

  /**
   * @return the acr
   */
  public String getAcr() {
    return _acr;
  }

  /**
   * @return the status
   */
  public String getStatus() {
    return _status;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((_countryCode == null) ? 0 : _countryCode.hashCode());
    result = prime * result + ((_mic == null) ? 0 : _mic.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Exchange other = (Exchange) obj;
    if (_countryCode == null) {
      if (other._countryCode != null) {
        return false;
      }
    } else if (!_countryCode.equals(other._countryCode)) {
      return false;
    }
    if (_mic == null) {
      if (other._mic != null) {
        return false;
      }
    } else if (!_mic.equals(other._mic)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.reflectionToString(this);
  }
  
}
