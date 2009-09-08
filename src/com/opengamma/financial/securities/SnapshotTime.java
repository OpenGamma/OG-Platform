package com.opengamma.financial.securities;

/* 
 * This just says what the sample time is, by name.  The actual time series samples should relfect the
 * sample time if it's required.
 */
public enum SnapshotTime {
  LONDON_CLOSE, NEW_YORK_CLOSE, TOKYO_CLOSE, LONDON_FIXING, INTRADAY, UNKNOWN
}
