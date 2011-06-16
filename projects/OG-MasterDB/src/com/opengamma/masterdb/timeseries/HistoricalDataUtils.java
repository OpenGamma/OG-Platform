/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.timeseries;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.IdentifierWithDates;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility methods used by DbTimeseries Master
 */
/* package */final class HistoricalDataUtils {
  
  
  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalDataUtils.class);
  
  /**
   * Restricted constructor
   */
  private HistoricalDataUtils() {
  }

  /**
   * Checks if two IdentifierWithDates dates intersects
   * 
   * @param first the first identifier, not null
   * @param second the second identifier, not null
   * @return true if they intersects or false otherwise
   */
  protected static boolean intersects(IdentifierWithDates first, IdentifierWithDates second) {
    ArgumentChecker.notNull(first, "first identifier");
    ArgumentChecker.notNull(second, "second identifier");
        
    s_logger.debug("checking if {} and {} intersects", first, second);
    LocalDate firstStart = first.getValidFrom();
    LocalDate secondStart = second.getValidFrom();
    LocalDate firstEnd = first.getValidTo();
    LocalDate secondEnd = second.getValidTo();
    
    if (firstStart == null && firstEnd == null && secondStart == null && secondEnd == null) {
      return true;
    }
    if (firstStart == null) {
      if (secondStart == null) {
        if (firstEnd.isBefore(secondEnd)) {
          firstStart = firstEnd.minusDays(1);
          secondStart = firstEnd.minusDays(1);
        } else {
          firstStart = secondEnd.minusDays(1);
          secondStart = secondEnd.minusDays(1);
        }
      } else {
        firstStart = secondStart;
      }
    }
    if (firstEnd == null) {
      if (secondEnd == null) {
        if (firstStart.isAfter(secondStart)) {
          firstEnd = firstStart.plusDays(1);
          secondEnd = firstStart.plusDays(1);
        } else {
          firstEnd = secondStart.plusDays(1);
          secondEnd = secondStart.plusDays(1);
        }
      } else {
        firstEnd = secondEnd;
      }
    }
    if (secondStart == null) {
      secondStart = firstStart;
    }
    if (secondEnd == null) {
      secondEnd = firstEnd;
    }
    
    if (firstStart.equals(secondStart) || firstStart.isBefore(secondStart)) {
      return (firstEnd.isAfter(secondStart));
    } else {
      return (firstStart.isBefore(secondEnd));
    }
  }
  
  /**
   * Checks if two IdentifierWithDates dates are identical
   * 
   * @param first the first identifier, not null
   * @param second the second identifier, not null
   * @return true if they are identical or false otherwise
   */
  protected static boolean isIdenticalRange(final IdentifierWithDates first, final IdentifierWithDates second) {
    ArgumentChecker.notNull(first, "first identifier");
    ArgumentChecker.notNull(second, "second identifier");
        
    s_logger.debug("checking if {} and {} are identical", first, second);
    LocalDate firstStart = first.getValidFrom();
    LocalDate secondStart = second.getValidFrom();
    LocalDate firstEnd = first.getValidTo();
    LocalDate secondEnd = second.getValidTo();
    
    if (firstStart == null && secondStart == null) {
      if (firstEnd != null && secondEnd != null) {
        return firstEnd.equals(secondEnd);
      }
    }
    
    if (firstEnd == null && secondEnd == null) {
      if (firstStart != null && secondStart != null) {
        return firstStart.equals(secondStart);
      }
    }
    
    if (firstStart != null && secondStart != null) {
      if (firstEnd != null && secondEnd != null) {
        return firstStart.equals(secondStart) && firstEnd.equals(secondEnd);
      }
    }
    return false;
  }

}
