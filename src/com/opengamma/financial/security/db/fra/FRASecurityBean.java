/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.security.db.fra;

import java.util.Date;

import com.opengamma.financial.security.db.SecurityBean;
import com.opengamma.financial.security.fra.FRASecurity;

/**
 * A bean representation of {@link FRASecurity}.
 */
public class FRASecurityBean extends SecurityBean {

  private Date _startDate;
  private Date _endDate;

  public void setStartDate(final Date startDate) {
    _startDate = startDate;
  }

  public Date getStartDate() {
    return _startDate;
  }

  public void setEndDate(final Date endDate) {
    _endDate = endDate;
  }

  public Date getEndDate() {
    return _endDate;
  }

}
