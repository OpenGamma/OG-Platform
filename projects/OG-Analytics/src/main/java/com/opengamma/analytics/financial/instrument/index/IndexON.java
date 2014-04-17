/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing an OIS-like index. The fixing period is always one business day.
 */
public class IndexON extends IndexDeposit {

  /**
   * The day count convention associated to the overnight rate. Not null.
   */
  private final DayCount _dayCount;
  /**
   * The number of days between start of the fixing period and the publication of the index value. It is usually 0 day (EUR) or 1 day (USD).
   * It does not represent the standard number of days between the trade date and the settlement date.
   */
  private final int _publicationLag;

  /**
   * Index constructor from all the details.
   * @param name The name of the index. Not null.
   * @param currency The index currency. Not null.
   * @param dayCount The day count convention associated to the overnight rate. Not null.
   * @param publicationLag The number of days between start of the fixing period and the publication of the index value.
   */
  public IndexON(final String name, final Currency currency, final DayCount dayCount, final int publicationLag) {
    super(name, currency);
    ArgumentChecker.notNull(dayCount, "OIS index: day count");
    ArgumentChecker.isTrue(publicationLag == 0 || publicationLag == 1, "Attempted to construct an IndexON with publicationLag other than 0 or 1");
    _publicationLag = publicationLag;
    _dayCount = dayCount;
  }

  /**
   * Gets the day count convention associated to the overnight rate.
   * @return The day count convention.
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Gets the number of days between start of the fixing period and the publication of the index value.
   * @return The number of lag days.
   */
  public int getPublicationLag() {
    return _publicationLag;
  }

  @Override
  public String toString() {
    return super.toString() + "-" + _dayCount.getName();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _dayCount.hashCode();
    result = prime * result + _publicationLag;
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final IndexON other = (IndexON) obj;
    if (!ObjectUtils.equals(_dayCount, other._dayCount)) {
      return false;
    }
    if (_publicationLag != other._publicationLag) {
      return false;
    }
    return true;
  }

}
