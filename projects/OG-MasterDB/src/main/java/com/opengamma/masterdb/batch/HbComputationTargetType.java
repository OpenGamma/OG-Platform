/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.EnhancedUserType;

import com.opengamma.engine.target.ComputationTargetType;

/**
 * A Hibernate type wrapper for {@link ComputationTargetType} that uses the string form of the type for storage.
 */
public class HbComputationTargetType implements EnhancedUserType {

  public HbComputationTargetType() {
  }

  @Override
  public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
    return cached;
  }

  @Override
  public Object deepCopy(final Object value) throws HibernateException {
    return value;
  }

  @Override
  public Serializable disassemble(final Object value) throws HibernateException {
    return (Serializable) value;
  }

  @Override
  public boolean equals(final Object x, final Object y) throws HibernateException {
    return x.equals(y);
  }

  @Override
  public int hashCode(final Object x) throws HibernateException {
    return x.hashCode();
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public Object nullSafeGet(final ResultSet rs, final String[] names, final Object owner) throws HibernateException, SQLException {
    final String value = rs.getString(names[0]);
    if (value == null) {
      return ComputationTargetType.NULL;
    }
    return ComputationTargetType.parse(value);
  }

  @Override
  public void nullSafeSet(final PreparedStatement st, final Object value, final int index) throws HibernateException, SQLException {
    if ((value == null) || ComputationTargetType.NULL.equals(value)) {
      st.setNull(index, Types.VARCHAR);
    } else {
      st.setString(index, value.toString());
    }
  }

  @Override
  public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
    return original;
  }

  @Override
  public Class<?> returnedClass() {
    return ComputationTargetType.class;
  }

  @Override
  public int[] sqlTypes() {
    return new int[] {Types.VARCHAR };
  }

  @Override
  public Object fromXMLString(final String xmlValue) {
    // TODO: should be unescaping any XML reserved characters ?
    return ComputationTargetType.parse(xmlValue);
  }

  @Override
  public String objectToSQLString(final Object value) {
    // TODO: should be escaping any SQL reserved characters
    return "\'" + value.toString() + "\'";
  }

  @Override
  public String toXMLString(final Object value) {
    // TODO: should be escaping any XML reserved characters ?
    return value.toString();
  }

}
