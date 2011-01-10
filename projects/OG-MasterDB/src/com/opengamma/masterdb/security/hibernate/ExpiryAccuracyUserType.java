/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Hibernate user type for ExpiryAccuracy.
 */
public class ExpiryAccuracyUserType implements UserType {

  // TODO: should this be in the same package as Expiry?

  @Override
  public Object assemble(Serializable arg0, Object arg1) throws HibernateException {
    return arg0;
  }

  @Override
  public Object deepCopy(Object arg0) throws HibernateException {
    return arg0;
  }

  @Override
  public Serializable disassemble(Object arg0) throws HibernateException {
    return (Serializable) arg0;
  }

  @Override
  public boolean equals(Object x, Object y) throws HibernateException {
    // Check for either being null for database null semantics which ObjectUtils won't give us
    if ((x == null) || (y == null)) {
      return false;
    }
    return ObjectUtils.equals(x, y);
  }

  @Override
  public int hashCode(Object arg0) throws HibernateException {
    return arg0.hashCode();
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public Object nullSafeGet(ResultSet resultSet, String[] columnNames, Object owner) throws HibernateException, SQLException {
    Integer databaseValue = resultSet.getInt(columnNames[0]);
    if (resultSet.wasNull()) {
      return null;
    }
    switch (databaseValue) {
      case 1:
        return ExpiryAccuracy.YEAR;
      case 2:
        return ExpiryAccuracy.MONTH_YEAR;
      case 3:
        return ExpiryAccuracy.DAY_MONTH_YEAR;
      case 4:
        return ExpiryAccuracy.HOUR_DAY_MONTH_YEAR;
      case 5:
        return ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR;
      default:
        return null;
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public void nullSafeSet(PreparedStatement stmt, Object value, int index) throws HibernateException, SQLException {
    if (value == null) {
      stmt.setNull(index, Hibernate.INTEGER.sqlType());
    } else {
      switch ((ExpiryAccuracy) value) {
        case MIN_HOUR_DAY_MONTH_YEAR:
          stmt.setInt(index, 5);
          break;
        case HOUR_DAY_MONTH_YEAR:
          stmt.setInt(index, 4);
          break;
        case DAY_MONTH_YEAR:
          stmt.setInt(index, 3);
          break;
        case MONTH_YEAR:
          stmt.setInt(index, 2);
          break;
        case YEAR:
          stmt.setInt(index, 1);
          break;
      }
    }
  }

  @Override
  public Object replace(Object original, Object target, Object owner) throws HibernateException {
    return original;
  }

  @Override
  public Class<?> returnedClass() {
    return ExpiryAccuracy.class;
  }

  @SuppressWarnings("deprecation")
  @Override
  public int[] sqlTypes() {
    return new int[] {Hibernate.INTEGER.sqlType()};
  }
}
