/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.frequency;

import org.joda.convert.FromStringFactory;
import org.joda.convert.ToString;

import com.opengamma.financial.convention.NamedInstance;

/**
 * Convention for frequency.
 * <p>
 * Some financial products have a specific event every so often.
 * This convention defines the frequency of those events relative to a year.
 */
@FromStringFactory(factory = SimpleFrequencyFactory.class)
public interface Frequency extends NamedInstance {

  //TODO: Improve frequency to have a "toPeriod" and a "paymentPerYear".

  /**
   * Never convention name.
   */
  String NEVER_NAME = "Never";
  /**
   * Annual convention name
   */
  String ANNUAL_NAME = "Annual";
  /**
   * Semi-annual convention name
   */
  String SEMI_ANNUAL_NAME = "Semi-annual";
  /**
   * Quarterly convention name
   */
  String QUARTERLY_NAME = "Quarterly";
  /**
   * Bi-monthly convention name
   */
  String BIMONTHLY_NAME = "Bi-monthly";
  /**
   * Monthly convention name
   */
  String MONTHLY_NAME = "Monthly";
  /**
   * Twenty Eight days convention name
   */
  String TWENTY_EIGHT_DAYS_NAME = "Twenty Eight Days";
  /**
   * Three-weekly convention name
   */
  String THREE_WEEK_NAME = "Three week";
  /**
   * Bi-weekly convention name
   */
  String BIWEEKLY_NAME = "Bi-weekly";
  /**
   * Weekly convention name
   */
  String WEEKLY_NAME = "Weekly";
  /**
   * Daily convention name
   */
  String DAILY_NAME = "Daily";
  /**
   * Continuous convention name
   */
  String CONTINUOUS_NAME = "Continuous";
  /** 
   * Four month convention name
   */
  String FOUR_MONTH_NAME = "Four Month";
  /** 
   * Five month convention name
   */
  String FIVE_MONTH_NAME = "Five Month";
  /** 
   * Four month convention name
   */
  String SEVEN_MONTH_NAME = "Seven Month";
  /** 
   * Eight month convention name
   */
  String EIGHT_MONTH_NAME = "Eight Month";
  /** 
   * Four month convention name
   */
  String NINE_MONTH_NAME = "Nine Month";
  /** 
   * Ten month convention name
   */
  String TEN_MONTH_NAME = "Ten Month";
  /** 
   * Eleven month convention name
   */
  String ELEVEN_MONTH_NAME = "Eleven Month";
  /**
   * Eighteen month convention name
   */
  String EIGHTEEN_MONTH_NAME = "Eighteen Month";

  /**
   * Gets the name of the convention.
   * 
   * @return the name, not null
   * @deprecated use getName()
   */
  @Deprecated
  String getConventionName();

  /**
   * Gets the name of the convention.
   * 
   * @return the name, not null
   */
  @ToString
  String getName();

}
