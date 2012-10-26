/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.sql.Types;
import java.util.Map;

import javax.time.InstantProvider;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.opengamma.util.ArgumentChecker;

/**
 * Parameter source for Spring JDBC templating.
 * <p>
 * This class extends {@link MapSqlParameterSource} from Spring to provide
 * additional support for types.
 */
public class DbMapSqlParameterSource extends MapSqlParameterSource {

  /**
   * Restrictive constructor.
   */
  public DbMapSqlParameterSource() {
  }

  //-------------------------------------------------------------------------
  /**
   * Adds an instant to this source.
   * 
   * @param name  the name, not null
   * @param instantProvider  the instant, not null
   * @return this, for chaining, not null
   */
  public DbMapSqlParameterSource addTimestamp(final String name, final InstantProvider instantProvider) {
    ArgumentChecker.notNull(name, "name");
    addValue(name, DbDateUtils.toSqlTimestamp(instantProvider));
    return this;
  }

  /**
   * Adds an instant to this source treating null as far future.
   * 
   * @param name  the name, not null
   * @param instantProvider  the instant, null is far future
   * @return this, for chaining, not null
   */
  public DbMapSqlParameterSource addTimestampNullFuture(final String name, final InstantProvider instantProvider) {
    ArgumentChecker.notNull(name, "name");
    if (instantProvider == null) {
      addValue(name, DbDateUtils.MAX_SQL_TIMESTAMP);
    } else {
      addValue(name, DbDateUtils.toSqlTimestamp(instantProvider));
    }
    return this;
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a date to this source.
   * 
   * @param name  the name, not null
   * @param date  the date, not null
   * @return this, for chaining, not null
   */
  public DbMapSqlParameterSource addDate(final String name, final LocalDate date) {
    ArgumentChecker.notNull(name, "name");
    addValue(name, DbDateUtils.toSqlDate(date));
    return this;
  }

  /**
   * Adds a time to this source.
   * 
   * @param name  the name, not null
   * @param time  the time, not null
   * @return this, for chaining, not null
   */
  public DbMapSqlParameterSource addTime(final String name, final LocalTime time) {
    ArgumentChecker.notNull(name, "name");
    addValue(name, DbDateUtils.toSqlTimestamp(time));
    return this;
  }

  //-------------------------------------------------------------------------
  /**
   * Adds an object to this source.
   * 
   * @param name  the name, not null
   * @param object  the object, not null
   * @return this, for chaining, not null
   */
  @Override
  public DbMapSqlParameterSource addValue(final String name, final Object object) {
    super.addValue(name, object);
    return this;
  }

  /**
   * Adds an object to this source, specifying the SQL type.
   * 
   * @param name  the name, not null
   * @param object  the object, not null
   * @param sqlType  the SQL type
   * @return this, for chaining, not null
   */
  @Override
  public DbMapSqlParameterSource addValue(final String name, final Object object, int sqlType) {
    super.addValue(name, object, sqlType);
    return this;
  }

  /**
   * Adds an object to this source, specifying the SQL type and type name.
   * 
   * @param name  the name, not null
   * @param object  the object, not null
   * @param sqlType  the SQL type
   * @param typeName  the type name of the parameter
   * @return this, for chaining, not null
   */
  @Override
  public DbMapSqlParameterSource addValue(final String name, final Object object, final int sqlType, final String typeName) {
    super.addValue(name, object, sqlType, typeName);
    return this;
  }

  /**
   * Adds a map of parameters to this source.
   * 
   * @param values  a map holding existing parameter values which may be null
   * @return this, for chaining, not null
   */
  @Override
  public DbMapSqlParameterSource addValues(final Map<String, ?> values) {
    super.addValues(values);
    return this;
  }

  //-------------------------------------------------------------------------
  /**
   * Adds an instant to this source unless the object is null.
   * 
   * @param name  the name, not null
   * @param instantProvider  the instant, not null
   * @return this, for chaining, not null
   */
  public DbMapSqlParameterSource addTimestampAllowNull(final String name, final InstantProvider instantProvider) {
    if (instantProvider != null) {
      addTimestamp(name, instantProvider);
    } else {
      addValue(name, null, Types.TIMESTAMP);
    }
    return this;
  }

  /**
   * Adds an date to this source unless the object is null.
   * 
   * @param name  the name, not null
   * @param date  the date, not null
   * @return this, for chaining, not null
   */
  public DbMapSqlParameterSource addDateAllowNull(final String name, final LocalDate date) {
    if (date != null) {
      addDate(name, date);
    } else {
      addValue(name, null, Types.DATE);
    }
    return this;
  }

  /**
   * Adds an time to this source unless the object is null.
   * 
   * @param name  the name, not null
   * @param time  the time, not null
   * @return this, for chaining, not null
   */
  public DbMapSqlParameterSource addTimeAllowNull(final String name, final LocalTime time) {
    if (time != null) {
      addTime(name, time);
    } else {
      addValue(name, null, Types.TIME);
    }
    return this;
  }

  //-------------------------------------------------------------------------
  /**
   * Adds an instant to this source unless the object is null.
   * 
   * @param name  the name, not null
   * @param instantProvider  the instant, not null
   * @return this, for chaining, not null
   */
  public DbMapSqlParameterSource addTimestampNullIgnored(final String name, final InstantProvider instantProvider) {
    if (instantProvider != null) {
      addTimestamp(name, instantProvider);
    }
    return this;
  }

  /**
   * Adds an date to this source unless the object is null.
   * 
   * @param name  the name, not null
   * @param date  the date, not null
   * @return this, for chaining, not null
   */
  public DbMapSqlParameterSource addDateNullIgnored(final String name, final LocalDate date) {
    if (date != null) {
      addDate(name, date);
    }
    return this;
  }

  /**
   * Adds an time to this source unless the object is null.
   * 
   * @param name  the name, not null
   * @param time  the time, not null
   * @return this, for chaining, not null
   */
  public DbMapSqlParameterSource addTimeNullIgnored(final String name, final LocalTime time) {
    if (time != null) {
      addTime(name, time);
    }
    return this;
  }

  /**
   * Adds an object to this source unless the object is null.
   * 
   * @param name  the name, not null
   * @param object  the object, not null
   * @return this, for chaining, not null
   */
  public DbMapSqlParameterSource addValueNullIgnored(final String name, final Object object) {
    if (object != null) {
      addValue(name, object);
    }
    return this;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a description of this object suitable for debugging.
   * 
   * @return the description, not null
   */
  @Override
  public String toString() {
    return "Parameters" + getValues();
  }

}
