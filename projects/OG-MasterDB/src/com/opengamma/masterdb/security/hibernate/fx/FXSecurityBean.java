/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.fx;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.masterdb.security.hibernate.CurrencyBean;
import com.opengamma.masterdb.security.hibernate.ExternalIdBean;
import com.opengamma.masterdb.security.hibernate.SecurityBean;

/**
 * A bean representation of {@link FXSecurity}.
 */
public class FXSecurityBean extends SecurityBean {
  private double _payAmount;
  private CurrencyBean _payCurrency;
  private double _receiveAmount;
  private CurrencyBean _receiveCurrency;
  private ExternalIdBean _region;
  
  /**
   * Gets the payAmount.
   * @return the payAmount
   */
  public double getPayAmount() {
    return _payAmount;
  }

  /**
   * Sets the payAmount.
   * @param payAmount  the payAmount
   */
  public void setPayAmount(double payAmount) {
    _payAmount = payAmount;
  }

  /**
   * Gets the payCurrency.
   * @return the payCurrency
   */
  public CurrencyBean getPayCurrency() {
    return _payCurrency;
  }

  /**
   * Sets the payCurrency.
   * @param payCurrency  the payCurrency
   */
  public void setPayCurrency(CurrencyBean payCurrency) {
    _payCurrency = payCurrency;
  }

  /**
   * Gets the receiveAmount.
   * @return the receiveAmount
   */
  public double getReceiveAmount() {
    return _receiveAmount;
  }

  /**
   * Sets the receiveAmount.
   * @param receiveAmount  the receiveAmount
   */
  public void setReceiveAmount(double receiveAmount) {
    _receiveAmount = receiveAmount;
  }

  /**
   * Gets the receiveCurrency.
   * @return the receiveCurrency
   */
  public CurrencyBean getReceiveCurrency() {
    return _receiveCurrency;
  }

  /**
   * Sets the receiveCurrency.
   * @param receiveCurrency  the receiveCurrency
   */
  public void setReceiveCurrency(CurrencyBean receiveCurrency) {
    _receiveCurrency = receiveCurrency;
  }
  
  /**
   * Gets the region.
   * @return the region
   */
  public ExternalIdBean getRegion() {
    return _region;
  }

  /**
   * Sets the region.
   * @param region  the region
   */
  public void setRegion(ExternalIdBean region) {
    _region = region;
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof FXSecurityBean)) {
      return false;
    }
    FXSecurityBean fx = (FXSecurityBean) other;
    return new EqualsBuilder()
      .append(getId(), fx.getId())
      .append(getPayAmount(), fx.getPayAmount())
      .append(getReceiveAmount(), fx.getReceiveAmount())
      .append(getPayCurrency(), fx.getPayCurrency())
      .append(getReceiveCurrency(), fx.getReceiveCurrency())
      .append(getRegion(), fx.getRegion())
      .isEquals();
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append(getPayAmount())
      .append(getReceiveAmount())
      .append(getPayCurrency())
      .append(getReceiveCurrency())
      .append(getRegion())
      .toHashCode();
  }

}
