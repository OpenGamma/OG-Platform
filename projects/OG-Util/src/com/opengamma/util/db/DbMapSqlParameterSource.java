/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.sql.Types;
import java.util.Map;

import javax.time.InstantProvider;
import javax.time.calendar.DateProvider;
import javax.time.calendar.TimeProvider;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.opengamma.util.ArgumentChecker;

/**
 * Parameter source for Spring JDBC templating.
 */
public class DbMapSqlParameterSource extends MapSqlParameterSource {
  // TODO: Could add methods taking Joda-Beans Property

  /**
   * Restrictive constructor.
   */
  public DbMapSqlParameterSource() {
  }

  //-------------------------------------------------------------------------
  /**
   * Adds an instant to this source.
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
   * @param name  the name, not null
   * @param dateProvider  the date, not null
   * @return this, for chaining, not null
   */
  public DbMapSqlParameterSource addDate(final String name, final DateProvider dateProvider) {
    ArgumentChecker.notNull(name, "name");
    addValue(name, DbDateUtils.toSqlDate(dateProvider));
    return this;
  }

  /**
   * Adds a time to this source.
   * @param name  the name, not null
   * @param timeProvider  the time, not null
   * @return this, for chaining, not null
   */
  public DbMapSqlParameterSource addTime(final String name, final TimeProvider timeProvider) {
    ArgumentChecker.notNull(name, "name");
    addValue(name, DbDateUtils.toSqlTimestamp(timeProvider));
    return this;
  }

  /**
   * Adds an object to this source.
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
   * Adds a map of parameters to this source.
   * @param map  a Map holding existing parameter values (can be <code>null</code>)
   * @return this, for chaining, not null
   */
  @SuppressWarnings("unchecked")
  @Override
  public DbMapSqlParameterSource addValues(final Map map) {
    super.addValues(map);
    return this;
  }

  //-------------------------------------------------------------------------
  /**
   * Adds an instant to this source unless the object is null.
   * @param name  the name, not null
   * @param instantProvider  the instant, not null
   * @return this, for chaining, not null
   */
  public DbMapSqlParameterSource addTimestampNullIgnored(final String name, final InstantProvider instantProvider) {
    if (instantProvider != null) {
      addTimestamp(name, instantProvider);
    } else {
      addValue(name, null, Types.TIMESTAMP);
    }
    return this;
  }

  /**
   * Adds an date to this source unless the object is null.
   * @param name  the name, not null
   * @param dateProvider  the date, not null
   * @return this, for chaining, not null
   */
  public DbMapSqlParameterSource addDateNullIgnored(final String name, final DateProvider dateProvider) {
    if (dateProvider != null) {
      addDate(name, dateProvider);
    } else {
      addValue(name, null, Types.DATE);
    }
    return this;
  }

  /**
   * Adds an time to this source unless the object is null.
   * @param name  the name, not null
   * @param timeProvider  the time, not null
   * @return this, for chaining, not null
   */
  public DbMapSqlParameterSource addTimeNullIgnored(final String name, final TimeProvider timeProvider) {
    if (timeProvider != null) {
      addTime(name, timeProvider);
    } else {
      addValue(name, null, Types.TIME);
    }
    return this;
  }

  /**
   * Adds an object to this source unless the object is null.
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
  @Override
  public String toString() {
    return "Parameters" + getValues();
  }

}
