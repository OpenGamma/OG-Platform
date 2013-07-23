/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import java.util.Arrays;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

/**
 * 
 */
public abstract class IMMDateLogic {

  private static final int IMM_DAY = 20;
  private static final int[] IMM_MONTHS = new int[] {3, 6, 9, 12 };

  /**
   * IMM dates are 20th March, June, September and December 
   * @param date the date
   * @return true is date is an IMM date
   */
  public static boolean isIMMDate(final LocalDate date) {
    if (date.getDayOfMonth() != IMM_DAY) {
      return false;
    }
    if ((date.getMonthValue() % 3) != 0) {
      return false;
    }
    return true;
  }

  /**
   * Get a set of IMM dates fixed periods from the stepin date. One first rolls to the next IMM date (this is true even if
   * the stepin date is an IMM date), then counts periods forward from that date.  
   * @param stepinDate The stepin date (this is normally the trade date plus 1 day)
   * @param tenors The periods (typically this would look like 6M, 1Y, 2Y, 3Y, 5Y, 10Y) 
   * @return Set of IMM dates 
   */
  public static LocalDate[] getIMMDateSet(final LocalDate stepinDate, final Period[] tenors) {
    final int n = tenors.length;
    final LocalDate nextIMM = getNextIMMDate(stepinDate);
    final LocalDate[] res = new LocalDate[n];
    for (int i = 0; i < n; i++) {
      res[i] = nextIMM.plus(tenors[i]);
    }
    return res;
  }

  /**
   * IMM dates are 20th March, June, September and December. This returns the next IMM date from the given date - if the date
   * is an IMM date the next IMM date (i.e. 3 months on) is returned.  
   * @param date a given date
   * @return the next IMM date
   */
  public static LocalDate getNextIMMDate(final LocalDate date) {

    final int day = date.getDayOfMonth();
    final int month = date.getMonthValue();
    final int year = date.getYear();
    if (isIMMDate(date)) { //on an IMM date 
      if (month != 12) {
        return LocalDate.of(year, month + 3, IMM_DAY);
      } else {
        return LocalDate.of(year + 1, IMM_MONTHS[0], IMM_DAY);
      }
    } else {
      final int index = Arrays.binarySearch(IMM_MONTHS, month);
      if (index >= 0) { //in an IMM month
        if (day < IMM_DAY) {
          return LocalDate.of(year, month, IMM_DAY);
        } else {
          if (month != 12) {
            return LocalDate.of(year, month + 3, IMM_DAY);
          } else {
            return LocalDate.of(year + 1, IMM_MONTHS[0], IMM_DAY);
          }
        }
      } else {
        final int i = -(index + 1);
        if (i < 4) {
          return LocalDate.of(year, IMM_MONTHS[i], IMM_DAY);
        } else {
          return LocalDate.of(year + 1, IMM_MONTHS[0], IMM_DAY);
        }
      }
    }
  }

}
