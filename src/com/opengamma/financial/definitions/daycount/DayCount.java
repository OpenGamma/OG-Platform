package com.opengamma.financial.definitions.daycount;

import javax.time.Instant;

/**
 * 
 * @author emcleod
 * 
 */

public interface DayCount {

  public double getDayCountFraction(Instant firstDate, Instant secondDate);
}
