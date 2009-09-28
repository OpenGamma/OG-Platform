package com.opengamma.financial.securities;

/* 
 * This just says what the sample time is, by name.  The actual time series samples should reflect the
 * sample time if it's required.
 */
public class SnapshotTime extends Dimension {
  public static final String LONDON_CLOSE="LONDON_CLOSE";
  public static final String NEW_YORK_CLOSE="NEW_YORK_CLOSE";
  public static final String TOKYO_CLOSE="TOKYO_CLOSE";
  public static final String LONDON_FIXING="LONDON_FIXING";
  public static final String INTRADAY="INTRADAY";
  public static final String LIVE="LIVE";
  public static final String UNKNOWN="UNKNOWN";
  
  public SnapshotTime(String name) {
    super(name);
  }

}
