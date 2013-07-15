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
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.type.StringType;
import org.hibernate.usertype.EnhancedUserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.VersionCorrection;


/**
 * Persist {@link com.opengamma.id.VersionCorrection} via hibernate as a String.
 *
 */
public class PersistentVersionCorrection implements EnhancedUserType {

  /**
   * Singleton instance.
   */
  public static final PersistentVersionCorrection INSTANCE = new PersistentVersionCorrection();

  private static final Logger s_logger = LoggerFactory.getLogger(PersistentVersionCorrection.class);

  private static final int[] SQL_TYPES = new int[]{Types.VARCHAR};

  public int[] sqlTypes() {
    return SQL_TYPES;
  }

  public Class<?> returnedClass() {
    return VersionCorrection.class;
  }

  public boolean equals(Object x, Object y) throws HibernateException {
    if (x == y) {
      return true;
    }
    if (x == null || y == null) {
      return false;
    }
    VersionCorrection ix = (VersionCorrection) x;
    VersionCorrection iy = (VersionCorrection) y;
    return ix.equals(iy);
  }

  public int hashCode(Object object) throws HibernateException {
    return object.hashCode();
  }

  public Object nullSafeGet(ResultSet resultSet, String[] names, Object object) throws HibernateException, SQLException {
    return nullSafeGet(resultSet, names[0]);
  }

  @SuppressWarnings("deprecation")
  public Object nullSafeGet(ResultSet resultSet, String name) throws SQLException {
    String value = (String) (new StringType()).nullSafeGet(resultSet, name);
    if (value == null) {
      return null;
    }
    return VersionCorrection.parse(value);
  }

  @SuppressWarnings("deprecation")
  public void nullSafeSet(PreparedStatement preparedStatement, Object value, int index) throws HibernateException, SQLException {
    if (value == null) {
      s_logger.debug("VersionCorrection -> String : NULL -> NULL");
      (new StringType()).nullSafeSet(preparedStatement, null, index);
    } else {
      s_logger.debug("VersionCorrection -> String : {}   ->  {}", value, value);
      (new StringType()).nullSafeSet(preparedStatement, value.toString(), index);
    }
  }

  public Object deepCopy(Object value) throws HibernateException {
    return value;
  }

  public boolean isMutable() {
    return false;
  }

  public Serializable disassemble(Object value) throws HibernateException {
    return (Serializable) value;
  }

  public Object assemble(Serializable serializable, Object value) throws HibernateException {
    return serializable;
  }

  public Object replace(Object original, Object target, Object owner) throws HibernateException {
    return original;
  }

  // __________ EnhancedUserType ____________________

  public String objectToSQLString(Object object) {
    throw new UnsupportedOperationException();
  }

  public String toXMLString(Object object) {
    return object.toString();
  }

  public Object fromXMLString(String string) {
    return VersionCorrection.parse(string);
  }

}
