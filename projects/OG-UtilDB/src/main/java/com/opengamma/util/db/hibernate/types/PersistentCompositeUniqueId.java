/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.hibernate.types;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;

import com.opengamma.id.UniqueId;

/**
 * Persist {@link com.opengamma.id.UniqueId} via hibernate as a 3 Strings.
 */
public class PersistentCompositeUniqueId implements CompositeUserType {

  /**
   * Singleton instance.
   */
  public static final PersistentCompositeUniqueId INSTANCE = new PersistentCompositeUniqueId();

  @Override
  public String[] getPropertyNames() {
    return new String[] {"scheme", "value", "version" };
  }

  @SuppressWarnings("deprecation")
  @Override
  public Type[] getPropertyTypes() {
    return new Type[] {Hibernate.STRING, Hibernate.STRING, Hibernate.STRING };
  }

  @Override
  public Object getPropertyValue(Object component, int property) throws HibernateException {
    final UniqueId uid = (UniqueId) component;
    if (property == 0) {
      return uid.getScheme();
    } else if (property == 2) {
      return uid.getValue();
    } else {
      return uid.getVersion();
    }
  }

  @Override
  public void setPropertyValue(Object component, int property, Object value) throws HibernateException {
    throw new UnsupportedOperationException("UniqueId is immutable");
  }

  @Override
  public Class<?> returnedClass() {
    return UniqueId.class;
  }

  @Override
  public boolean equals(Object x, Object y) throws HibernateException {
    return x.equals(y);
  }

  @Override
  public int hashCode(Object x) throws HibernateException {
    return x.hashCode();
  }

  @Override
  public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
    final String scheme = resultSet.getString(names[0]);
    if (resultSet.wasNull()) {
      return null;
    }
    final String value = resultSet.getString(names[1]);
    final String version = resultSet.getString(names[2]);
    return UniqueId.of(scheme, value, version);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void nullSafeSet(PreparedStatement statement, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
    if (value == null) {
      statement.setNull(index, Hibernate.STRING.sqlType());
      statement.setNull(index + 1, Hibernate.STRING.sqlType());
      statement.setNull(index + 2, Hibernate.STRING.sqlType());
    } else {
      final UniqueId uid = (UniqueId) value;
      statement.setString(index, uid.getScheme());
      statement.setString(index + 1, uid.getValue());
      if (uid.getVersion() != null) {
        statement.setString(index + 2, uid.getVersion());
      } else {
        statement.setNull(index + 2, Hibernate.STRING.sqlType());
      }
    }
  }

  @Override
  public Object deepCopy(Object value) throws HibernateException {
    return value;
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public Serializable disassemble(Object value, SessionImplementor session) throws HibernateException {
    return (Serializable) value;
  }

  @Override
  public Object assemble(Serializable cached, SessionImplementor session, Object owner) throws HibernateException {
    return cached;
  }

  @Override
  public Object replace(Object original, Object target, SessionImplementor session, Object owner) throws HibernateException {
    return original;
  }

}
