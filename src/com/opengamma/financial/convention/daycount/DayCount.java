package com.opengamma.financial.convention.daycount;

import javax.time.Instant;

/**
 * 
 * @author emcleod
 * 
 */

public interface DayCount {

  public double getDayCountFraction(Instant firstDate, Instant secondDate);
}
