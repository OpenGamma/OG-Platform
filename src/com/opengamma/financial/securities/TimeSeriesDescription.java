package com.opengamma.financial.securities;

import java.util.Collections;
import java.util.List;

import javax.time.InstantProvider;
import javax.time.calendar.Clock;

import com.opengamma.util.CompareUtils;

public class TimeSeriesDescription {
  private DataSource _dataSource;
  private DataSourceInternalSource _internalSource;
  private SnapshotTime _snapshotTime;
  private Field _field;
  // this is a list so that there is a definite ordering - if the first one fails to return a code or
  // is not valid on this date, then move on.
  private List<CodeProvider> _codes;

  public TimeSeriesDescription(DataSource dataSource, 
                               DataSourceInternalSource internalSource, 
                               SnapshotTime snapshotTime, 
                               Field field, 
                               List<CodeProvider> codes) {
    _dataSource = CompareUtils.checkForNull(dataSource);
    _internalSource = CompareUtils.checkForNull(internalSource);
    _snapshotTime = CompareUtils.checkForNull(snapshotTime);
    _field = CompareUtils.checkForNull(field);
    _codes = CompareUtils.checkForNull(codes);
  }
  
  public TimeSeriesDescription(DataSource dataSource, 
      DataSourceInternalSource internalSource, 
      SnapshotTime snapshotTime, 
      Field field, 
      CodeProvider code) {
    this(dataSource, internalSource, snapshotTime, field, Collections.singletonList(code));
  }

  public DataSource getDataSource() {
    return _dataSource;
  }

  public DataSourceInternalSource getInternalSource() {
    return _internalSource;
  }

  public SnapshotTime getSnapshotTime() {
    return _snapshotTime;
  }

  public Field getField() {
    return _field;
  }

  public List<CodeProvider> getCodes() {
    return _codes;
  }
  
  // may need to care about time zone in here, or pass in the Clock.
  public String getActiveCodeOn(InstantProvider instant) {
    for (CodeProvider codeProvider : _codes) {
      if (codeProvider.isValidOn(instant)) {
        return codeProvider.getCode();
      }
    }
    return null;
  }
  
  public String getActiveCodeToday() {
    return getActiveCodeOn(Clock.systemDefaultZone().instant());
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof TimeSeriesDescription)) {
      return false;
    }
    TimeSeriesDescription other = (TimeSeriesDescription)o;
    if (!getDataSource().equals(other.getDataSource())) {
      return false;
    }
    if (!getField().equals(other.getField())) {
      return false;
    }
    if (!getInternalSource().equals(other.getInternalSource())) {
      return false;
    }
    if (!getSnapshotTime().equals(other.getSnapshotTime())) {
      return false;
    }
    if (!getCodes().equals(other.getCodes())) {
      return false;
    }
    return true;
  }
  
  public int hashCode() {
    return getDataSource().hashCode() ^ getCodes().hashCode(); 
  }
}
