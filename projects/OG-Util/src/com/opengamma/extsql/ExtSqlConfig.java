/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.extsql;

/**
 * Configuration to allow details of the substitutions to be replaced.
 * <p>
 * Implementations must not be thread-safe.
 */
public class ExtSqlConfig {

  /**
   * A constant for the standard set of config.
   */
  public static final ExtSqlConfig DEFAULT = new ExtSqlConfig("Default");
  /**
   * A constant for the config needed for Postgres.
   */
  public static final ExtSqlConfig POSTGRES = new ExtSqlConfig("Postgres");
  /**
   * A constant for the config needed for HSQL.
   */
  public static final ExtSqlConfig HSQL = new HsqlExtSqlConfig();
  /**
   * A constant for the config needed for MySQL.
   */
  public static final ExtSqlConfig MYSQL = new MySqlExtSqlConfig();

  /**
   * The descriptive name.
   */
  private final String _name;

  /**
   * Creates an instance.
   * 
   * @param name  a descriptive name for the config, not null
   */
  public ExtSqlConfig(String name) {
    _name = name;
  }

  /**
   * Gets the config name.
   * 
   * @return the config name, not null
   */
  public final String getName() {
    return _name;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the value contains a wildcard.
   * <p>
   * The default implementation matches % and _, using backslash as an escape character.
   * This matches Postgres and other databases.
   * 
   * @param value  the value to check, not null
   * @return true if the value contains wildcards
   */
  public boolean isLikeWildcard(String value) {
    boolean escape = false;
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      if (escape) {
        escape = false;
      } else {
        if (ch == '\\') {
          escape = true;
        } else if (ch == '%' || ch == '_') {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Gets the suffix to add after LIKE, which would typically be an ESCAPE clause.
   * <p>
   * The default implementation returns an empty string.
   * This matches Postgres and other databases.
   * <p>
   * The returned SQL must be end in a space if non-empty.
   * 
   * @return the suffix to add after LIKE, not null
   */
  public String getLikeSuffix() {
    return "";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the paging SQL, such as OFFSET-FETCH.
   * <p>
   * The default implementation uses 'FETCH FIRST n ROWS ONLY' or
   * 'OFFSET n ROWS FETCH NEXT n ROWS ONLY'.
   * This matches Postgres, HSQL and other databases.
   * <p>
   * The returned SQL must be end in a space if non-empty.
   * 
   * @param offset  the OFFSET amount
   * @param fetchLimit  the FETCH/LIMIT amount
   * @return the SQL to use, not null
   */
  public String getPaging(int offset, int fetchLimit) {
    if (fetchLimit == 0 || fetchLimit == Integer.MAX_VALUE) {
      if (offset > 0) {
        return "OFFSET " + offset + " ROWS ";
      }
      return "";
    }
    if (offset == 0) {
      return "FETCH FIRST " + fetchLimit + " ROWS ONLY ";
    }
    return "OFFSET " + offset + " ROWS FETCH NEXT " + fetchLimit + " ROWS ONLY ";
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "ExtSqlConfig[" + _name + "]";
  }

  //-------------------------------------------------------------------------
  /**
   * Class for HSQL.
   */
  private static class HsqlExtSqlConfig extends ExtSqlConfig {
    public HsqlExtSqlConfig() {
      super("HSQL");
    }
    @Override
    public String getLikeSuffix() {
      return "ESCAPE '\\' ";
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Class for MySQL.
   */
  private static class MySqlExtSqlConfig extends ExtSqlConfig {
    public MySqlExtSqlConfig() {
      super("MySql");
    }
    @Override
    public String getPaging(int offset, int fetchLimit) {
      if (fetchLimit == 0 || fetchLimit == Integer.MAX_VALUE) {
        if (offset > 0) {
          return "OFFSET " + offset + " ";
        }
        return "";
      }
      if (offset == 0) {
        return "LIMIT " + fetchLimit + " ";
      }
      return "LIMIT " + fetchLimit + " OFFSET " + offset + " ";
    }
  }

}
