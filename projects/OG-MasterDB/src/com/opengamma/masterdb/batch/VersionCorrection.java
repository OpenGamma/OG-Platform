/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.opengamma.util.db.DbDateUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.time.Instant;
import java.sql.Timestamp;

/**
 * Hibernate bean.
 */
public class VersionCorrection {

  private int _id;
  private Timestamp _asOf;
  private Timestamp _correctedTo;

  public int getId() {
    return _id;
  }

  public void setId(int id) {
    _id = id;
  }

  public Timestamp getAsOf() {
    return _asOf;
  }

  public void setAsOf(Timestamp asOf) {
    this._asOf = asOf;
  }

  public Timestamp getCorrectedTo() {
    return _correctedTo;
  }

  public void setCorrectedTo(Timestamp correctedTo) {
    this._correctedTo = correctedTo;
  }

  public com.opengamma.id.VersionCorrection getBaseType(){
    return com.opengamma.id.VersionCorrection.of(DbDateUtils.fromSqlTimestamp(_asOf), DbDateUtils.fromSqlTimestamp(_correctedTo));
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toString() {
    return "VersionCorrection as of: " + _asOf + " corrected to: " + _correctedTo;
  }

}
