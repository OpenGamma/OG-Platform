/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 * Custom Hibernate type for trivial enums.
 * 
 * @author Andrew
 */
public abstract class EnumUserType<E> implements UserType {
  
  private final Class<E> _clazz;
  
  protected EnumUserType (final Class<E> clazz) {
    _clazz = clazz;
  }

  @Override
  public Object assemble(Serializable arg0, Object arg1)
      throws HibernateException {
    return arg0;
  }

  @Override
  public Object deepCopy(Object arg0) throws HibernateException {
    return arg0;
  }

  @Override
  public Serializable disassemble(Object arg0) throws HibernateException {
    return (Serializable)arg0;
  }

  @Override
  public boolean equals(Object x, Object y) throws HibernateException {
    if (x == y) return true;
    if ((x == null) || (y == null)) return false;
    return x.equals (y);
  }

  @Override
  public int hashCode(Object arg0) throws HibernateException {
    return arg0.hashCode ();
  }

  @Override
  public boolean isMutable() {
    return false;
  }
  
  protected abstract E stringToEnum (final String string);

  @Override
  public Object nullSafeGet(ResultSet resultSet, String[] columnNames, Object owner)
      throws HibernateException, SQLException {
    String databaseValue = resultSet.getString (columnNames[0]);
    if (resultSet.wasNull ()) return null;
    return stringToEnum (databaseValue);
  }
  
  protected abstract String enumToString (final E value);

  @SuppressWarnings("unchecked")
  @Override
  public void nullSafeSet(PreparedStatement stmt, Object value, int index)
      throws HibernateException, SQLException {
    if (value == null) {
      stmt.setNull (index, Hibernate.STRING.sqlType ());
    } else {
      stmt.setString (index, enumToString ((E)value));
    }
  }

  @Override
  public Object replace(Object original, Object target, Object owner)
      throws HibernateException {
    return original;
  }

  @Override
  public Class<?> returnedClass() {
    return _clazz;
  }

  @Override
  public int[] sqlTypes() {
    return new int[] { Hibernate.STRING.sqlType () };
  }
}