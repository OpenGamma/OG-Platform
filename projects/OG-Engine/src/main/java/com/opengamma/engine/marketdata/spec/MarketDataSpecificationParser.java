/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.spec;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeParseException;

import com.opengamma.id.UniqueId;

/**
 * Parses a string to produce a {@link MarketDataSpecification}.
 */
public class MarketDataSpecificationParser {

  private static final String LIVE = "live";
  private static final String SNAPSHOT = "snapshot";
  private static final String FIXED_HISTORICAL = "fixedhistorical";
  private static final String LATEST_HISTORICAL = "latesthistorical";

  private MarketDataSpecificationParser() {/*private constructor*/}

  /**
   * Parses a string to produce a {@link MarketDataSpecification}. Supported formats are
   * 'live', 'fixedhistorical', 'latesthistorical' and 'snapshot'
   * For example:
   * <p><ul>
   * <li>{@code live:Bloomberg}
   * <li>{@code latesthistorical, DEFAULT_HTS_RATING}
   * <li>{@code fixedhistorical:2011-08-03}
   * <li>{@code fixedhistorical:2011-08-03, DEFAULT_HTS_RATING}
   * <li>{@code snapshot:DbSnp~1234}
   * <ul><p>
   * @param specStr String representation of a {@link MarketDataSpecification}
   * @return A {@link MarketDataSpecification} instance built from the string
   * @throws IllegalArgumentException If the string can't be parsed
   */
  public static MarketDataSpecification parse(String specStr) {
    if (specStr.startsWith(LIVE)) {
      return createLiveSpec(removePrefix(specStr, LIVE));
    } else {
      if (specStr.startsWith(SNAPSHOT)) {
        return createSnapshotSpec(removePrefix(specStr, SNAPSHOT));
      } else if (specStr.startsWith(FIXED_HISTORICAL)) {
        return createFixedHistoricalSpec(removePrefix(specStr, FIXED_HISTORICAL));
      } else if (specStr.startsWith(LATEST_HISTORICAL)) {
        return createLatestHistoricalSpec(removePrefix(specStr, LATEST_HISTORICAL));
      } else {
        throw new IllegalArgumentException("Market data must be one of 'live', 'fixedhistorical', 'latesthistorical'" +
                                               " or 'snapshot'");
      }
    }
  }

  private static MarketDataSpecification createLatestHistoricalSpec(String specStr) {
    if (specStr.isEmpty()) {
      return new LatestHistoricalMarketDataSpecification();
    }
    if (!specStr.startsWith(":")) {
      throw new IllegalArgumentException(specStr + " doesn't match 'latesthistorical[:time series rating]'");
    }
    String timeSeriesRating = specStr.substring(1).trim();
    if (timeSeriesRating.isEmpty()) {
      throw new IllegalArgumentException(specStr + " doesn't match 'latesthistorical[:time series rating]'");
    }
    return new LatestHistoricalMarketDataSpecification(timeSeriesRating);
  }

  private static MarketDataSpecification createFixedHistoricalSpec(String specStr) {
    if (!specStr.startsWith(":")) {
      throw new IllegalArgumentException(specStr + " doesn't match 'fixedhistorical:timestamp[,time series rating]'");
    }
    String[] strings = specStr.split(",");
    LocalDate date;
    try {
      date = LocalDate.parse(strings[0].substring(1).trim());
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Unknown date format", e);
    }
    if (strings.length > 1) {
      String timeSeriesRating = strings[1].trim();
      if (timeSeriesRating.isEmpty()) {
        throw new IllegalArgumentException(specStr + " doesn't match 'fixedhistorical:timestamp[,time series rating]'");
      }
      return new FixedHistoricalMarketDataSpecification(timeSeriesRating, date);
    } else {
      return new FixedHistoricalMarketDataSpecification(date);
    }
  }

  // TODO accept 'snapshot name/timestamp'? friendlier but requires looking up data from the server
  private static MarketDataSpecification createSnapshotSpec(String specStr) {
    if (!specStr.startsWith(":")) {
      throw new IllegalArgumentException(specStr + " doesn't match 'snapshot:snapshot ID'");
    }
    String id = specStr.substring(1).trim();
    return UserMarketDataSpecification.of(UniqueId.parse(id));
  }

  private static MarketDataSpecification createLiveSpec(String specStr) {
    if (!specStr.startsWith(":")) {
      throw new IllegalArgumentException(specStr + " doesn't match 'live:source name'");
    }
    String sourceName = specStr.substring(1).trim();
    if (sourceName.isEmpty()) {
      throw new IllegalArgumentException(specStr + " doesn't match 'live:source name'");
    }
    return LiveMarketDataSpecification.of(sourceName);
  }

  private static String removePrefix(String specStr, String prefix) {
    return specStr.substring(prefix.length(), specStr.length());
  }

  /**
   * Example usages of the MarketDataSpecificationParser
   * @return example strings
   */
  public static String getUsageMessage() {
    return "Examples of valid market data strings:\n" +
        "live:Data provider name\n" +
        "latesthistorical\n" +
        "latesthistorical:Time series rating name\n" +
        "fixedhistorical:2011-08-03\n" +
        "fixedhistorical:2011-08-03,Time series rating name\n" +
        "snapshot:DbSnp~1234";
  }
}
