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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Custom Hibernate type for trivial enums.
 * 
 */
public abstract class EnumUserType<E extends Enum<E>> implements UserType {
  
  private final Class<E> _clazz;
  private final Map<String, E> _stringToEnum;
  private final Map<E, String> _enumToString;
  
  protected EnumUserType(final Class<E> clazz, final E[] values) {
    _clazz = clazz;
    _stringToEnum = new HashMap<String, E>();
    _enumToString = new EnumMap<E, String>(clazz);
    for (final E value : values) {
      final String string = enumToStringNoCache(value);
      _stringToEnum.put(string, value);
      _enumToString.put(value, string);
    }
  }

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
    // Is this first check necessary?
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
  
  protected abstract String enumToStringNoCache(E value); 
  
  protected E stringToEnum(final String string) {
    final E value = _stringToEnum.get(string);
    if (value == null) {
      throw new OpenGammaRuntimeException("unexpected value: " + string);
    }
    return value;
  }

  @Override
  public Object nullSafeGet(ResultSet resultSet, String[] columnNames, Object owner) throws HibernateException, SQLException {
    String databaseValue = resultSet.getString(columnNames[0]);
    if (resultSet.wasNull()) {
      return null;
    }
    return stringToEnum(databaseValue);
  }
  
  protected String enumToString(final E value) {
    return _enumToString.get(value);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void nullSafeSet(PreparedStatement stmt, Object value, int index) throws HibernateException, SQLException {
    if (value == null) {
      stmt.setNull(index, Hibernate.STRING.sqlType());
    } else {
      stmt.setString(index, enumToString((E) value));
    }
  }

  @Override
  public Object replace(Object original, Object target, Object owner) throws HibernateException {
    return original;
  }

  @Override
  public Class<?> returnedClass() {
    return _clazz;
  }

  @Override
  public int[] sqlTypes() {
    return new int[] {Hibernate.STRING.sqlType()};
  }
}