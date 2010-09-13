/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.security.master.db.hibernate.fra;

import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.master.db.hibernate.SecurityBean;
import com.opengamma.financial.security.master.db.hibernate.ZonedDateTimeBean;

/**
 * A bean representation of {@link FRASecurity}.
 */
public class FRASecurityBean extends SecurityBean {

  private ZonedDateTimeBean _startDate;
  private ZonedDateTimeBean _endDate;

  public void setStartDate(final ZonedDateTimeBean startDate) {
    _startDate = startDate;
  }

  public ZonedDateTimeBean getStartDate() {
    return _startDate;
  }

  public void setEndDate(final ZonedDateTimeBean endDate) {
    _endDate = endDate;
  }

  public ZonedDateTimeBean getEndDate() {
    return _endDate;
  }

}
