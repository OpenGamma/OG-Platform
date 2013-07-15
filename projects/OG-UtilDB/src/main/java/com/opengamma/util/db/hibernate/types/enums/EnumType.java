/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.hibernate.types.enums;

import static com.opengamma.util.db.hibernate.types.enums.EnumReflect.getNameFromValue;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.ParameterizedType;

//Please notice the calls to getNameFromValue *************************
/**
 * An enum type for Hibernate.
 * 
 * @param <T> the enum type
 */
public class EnumType<T extends Enum<T>>
    implements EnhancedUserType, ParameterizedType {

  /**
   * Enum class for this particular user type.
   */
  private Class<T> _enumClass;

  /**
   * Value to use if null.
   */
  private String _defaultValue;

  /** Creates a new instance of ActiveStateEnumType */
  public EnumType() {
  }

  @SuppressWarnings("unchecked")
  public void setParameterValues(Properties parameters) {
    String enumClassName = parameters.getProperty("enum");
    try {
      _enumClass = (Class<T>) Class.forName(enumClassName).asSubclass(Enum.class);
    } catch (ClassNotFoundException cnfe) {
      throw new HibernateException("Enum class not found", cnfe);
    }

    setDefaultValue(parameters.getProperty("defaultValue"));
  }

  public String getDefaultValue() {
    return _defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this._defaultValue = defaultValue;
  }

  /**
   * The class returned by <tt>nullSafeGet()</tt>.
   * @return Class
   */
  public Class<?> returnedClass() {
    return _enumClass;
  }

  public int[] sqlTypes() {
    return new int[] {Types.VARCHAR };
  }

  public boolean isMutable() {
    return false;
  }

  /**
   * Retrieve an instance of the mapped class from a JDBC resultset. Implementors
   * should handle possibility of null values.
   *
   * @param rs a JDBC result set
   * @param names the column names
   * @param owner the containing entity
   * @return Object
   * @throws HibernateException
   * @throws SQLException
   */
  public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
    String value = rs.getString(names[0]);
    if (value == null) {
      value = getDefaultValue();
      if (value == null) { //no default value
        return null;
      }
    }
    String name = getNameFromValue(_enumClass, value);
    Object res = rs.wasNull() ? null : Enum.valueOf(_enumClass, name);

    return res;
  }

  /**
   * Write an instance of the mapped class to a prepared statement. Implementors
   * should handle possibility of null values. A multi-column type should be written
   * to parameters starting from <tt>index</tt>.
   *
   * @param st a JDBC prepared statement
   * @param value the object to write
   * @param index statement parameter index
   * @throws HibernateException
   * @throws SQLException
   */
  @SuppressWarnings("unchecked")
  public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
    if (value == null) {
      st.setNull(index, Types.VARCHAR);
    } else {
      st.setString(index, ((T) value).name());
    }
  }

  public Object assemble(Serializable cached, Object owner) throws HibernateException {
    return cached;
  }

  @SuppressWarnings("rawtypes")
  public Serializable disassemble(Object value) throws HibernateException {
    return (Enum) value;
  }

  public Object deepCopy(Object value) throws HibernateException {
    return value;
  }

  public boolean equals(Object x, Object y) throws HibernateException {
    return x == y;
  }

  public int hashCode(Object x) throws HibernateException {
    return x.hashCode();
  }

  public Object replace(Object original, Object target, Object owner) throws HibernateException {
    return original;
  }

  @SuppressWarnings("unchecked")
  public String objectToSQLString(Object value) {
    return '\'' + ((T) value).name() + '\'';
  }

  @SuppressWarnings("unchecked")
  public String toXMLString(Object value) {
    return ((T) value).name();
  }

  public Object fromXMLString(String xmlValue) {
    String name = getNameFromValue(_enumClass, xmlValue);
    return Enum.valueOf(_enumClass, name);
  }

}
