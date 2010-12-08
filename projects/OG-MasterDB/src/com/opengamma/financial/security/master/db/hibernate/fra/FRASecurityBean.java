/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.security.master.db.hibernate.fra;

import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.master.db.hibernate.CurrencyBean;
import com.opengamma.financial.security.master.db.hibernate.IdentifierBean;
import com.opengamma.financial.security.master.db.hibernate.SecurityBean;
import com.opengamma.financial.security.master.db.hibernate.ZonedDateTimeBean;

/**
 * A bean representation of {@link FRASecurity}.
 */
public class FRASecurityBean extends SecurityBean {
  private CurrencyBean _currency;
  private IdentifierBean _region;
  private ZonedDateTimeBean _startDate;
  private ZonedDateTimeBean _endDate;
  
  /**
   * Gets the currency field.
   * @return the currency
   */
  public CurrencyBean getCurrency() {
    return _currency;
  }

  /**
   * Sets the currency field.
   * @param currency  the currency
   */
  public void setCurrency(CurrencyBean currency) {
    _currency = currency;
  }

  /**
   * Gets the region field.
   * @return the region
   */
  public IdentifierBean getRegion() {
    return _region;
  }

  /**
   * Sets the region field.
   * @param region  the region
   */
  public void setRegion(IdentifierBean region) {
    _region = region;
  }

  /**
   * Gets the startDate field.
   * @return the startDate
   */
  public ZonedDateTimeBean getStartDate() {
    return _startDate;
  }

  /**
   * Sets the startDate field.
   * @param startDate  the startDate
   */
  public void setStartDate(ZonedDateTimeBean startDate) {
    _startDate = startDate;
  }

  /**
   * Gets the endDate field.
   * @return the endDate
   */
  public ZonedDateTimeBean getEndDate() {
    return _endDate;
  }

  /**
   * Sets the endDate field.
   * @param endDate  the endDate
   */
  public void setEndDate(ZonedDateTimeBean endDate) {
    _endDate = endDate;
  }




}
