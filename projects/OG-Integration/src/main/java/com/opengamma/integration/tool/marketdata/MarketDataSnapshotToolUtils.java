/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import static org.threeten.bp.temporal.ChronoField.DAY_OF_MONTH;
import static org.threeten.bp.temporal.ChronoField.HOUR_OF_DAY;
import static org.threeten.bp.temporal.ChronoField.MINUTE_OF_HOUR;
import static org.threeten.bp.temporal.ChronoField.MONTH_OF_YEAR;
import static org.threeten.bp.temporal.ChronoField.SECOND_OF_MINUTE;
import static org.threeten.bp.temporal.ChronoField.YEAR;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.format.SignStyle;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider;
import com.opengamma.integration.copier.snapshot.SnapshotColumns;
import com.opengamma.integration.tool.marketdata.SnapshotUtils.VersionInfo;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Utility methods for the MarketDataSnapshot Import/Export tools
 */
public class MarketDataSnapshotToolUtils {
  private static final String VERSION_FROM = "Version From";
  private static final String VERSION_TO = "Version To";
  private static final String CORRECTION_FROM = "Correction From";
  private static final String CORRECTION_TO = "Correction To";
  private static final String UNIQUE_ID = "UniqueId";
  private static final String NOT_SPECIFIED = "Not Specified";
  /** Snapshot listing option flag */
  private static final String SNAPSHOT_LIST_OPTION = "s";
  /** Snapshot query option flag */
  private static final String SNAPSHOT_QUERY_OPTION = "q";
  /** Snapshot version list option flag */
  private static final String SNAPSHOT_VERSION_LIST_OPTION = "v";
  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataSnapshotToolUtils.class);
  
  public static Option createSnapshotListOption() {
    final Option option = new Option(SNAPSHOT_LIST_OPTION, "snapshot-list", false, "List the snapshots available");
    return option;
  }

  public static Option createSnapshotQueryOption() {
    final Option option = new Option(SNAPSHOT_QUERY_OPTION, "snapshot-query", true, "List the snapshots available according to a glob");
    option.setArgName("snapshot name glob");
    return option;
  }
  
  public static Option createSnapshotVersionListOption() {
    final Option option = new Option(SNAPSHOT_VERSION_LIST_OPTION, "snapshot-versions", true, "List the versions available for a named snapshot");
    option.setArgName("snapshot name");
    return option;
  }
  
  public static boolean handleQueryOptions(SnapshotUtils snapshotUtils, CommandLine commandLine) {
    if (commandLine.hasOption(SNAPSHOT_LIST_OPTION)) {
      printSnapshotList(snapshotUtils);
      return true;
    } else if (commandLine.hasOption(SNAPSHOT_QUERY_OPTION)) {
      printSnapshotQuery(snapshotUtils, commandLine.getOptionValue(SNAPSHOT_QUERY_OPTION));
      return true;      
    } else if (commandLine.hasOption(SNAPSHOT_VERSION_LIST_OPTION)) {
      printVersionListQuery(snapshotUtils, commandLine.getOptionValue(SNAPSHOT_VERSION_LIST_OPTION));
      return true;      
    } else {
      return false;
    }
  }

  private static void printSnapshotQuery(SnapshotUtils snapshotUtils, String query) {
    List<String> snapshotsByGlob = snapshotUtils.snapshotByGlob(query);
    for (String info : snapshotsByGlob) {
      System.out.println(info);
    }
  }

  private static void printSnapshotList(SnapshotUtils snapshotUtils) {
    List<String> allSnapshots = snapshotUtils.allSnapshots();
    for (String info : allSnapshots) {
      System.out.println(info);
    }
  }
  
  private static void printVersionListQuery(SnapshotUtils snapshotUtils, String optionValue) {
    DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
      .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
      .appendLiteral('-')
      .appendValue(MONTH_OF_YEAR, 2)
      .appendLiteral('-')
      .appendValue(DAY_OF_MONTH, 2)
      .appendValue(HOUR_OF_DAY, 2)
      .appendLiteral(':')
      .appendValue(MINUTE_OF_HOUR, 2)
      .optionalStart()
      .appendLiteral(':')
      .appendValue(SECOND_OF_MINUTE, 2)
      .appendOffsetId()
      .toFormatter();
      

    List<VersionInfo> snapshotVersions = snapshotUtils.snapshotVersionsByName(optionValue);
    System.out.println(OffsetDateTime.now().toString(dateTimeFormatter));

    int fieldWidth = OffsetDateTime.now().toString(dateTimeFormatter).length(); // Assumes all offset date times have same width

    header(fieldWidth);
    String id = TimeZone.getDefault().getID();
    for (VersionInfo versionInfo : snapshotVersions) {
      OffsetDateTime versionFrom = versionInfo.getVersionFrom() != null ? OffsetDateTime.ofInstant(versionInfo.getVersionFrom(), ZoneId.of(id)) : null;
      OffsetDateTime versionTo = versionInfo.getVersionTo() != null ? OffsetDateTime.ofInstant(versionInfo.getVersionTo(), ZoneId.of(id)) : null;
      OffsetDateTime correctionFrom = versionInfo.getCorrectionFrom() != null ? OffsetDateTime.ofInstant(versionInfo.getCorrectionFrom(), ZoneId.of(id)) : null;
      OffsetDateTime correctionTo = versionInfo.getCorrectionTo() != null ? OffsetDateTime.ofInstant(versionInfo.getCorrectionTo(), ZoneId.of(id)) : null;
      if (versionFrom != null) {
        System.out.print(versionFrom.toString(dateTimeFormatter));
      } else {
        notSpecified(fieldWidth);
      }
      spaces();
      if (versionTo != null) {
        System.out.print(versionTo.toString(dateTimeFormatter));
      } else {
        notSpecified(fieldWidth);
      }
      spaces();
      if (correctionFrom != null) {
        System.out.print(correctionFrom.toString(dateTimeFormatter));
      } else {
        notSpecified(fieldWidth);
      }
      spaces();
      if (correctionTo != null) {
        System.out.print(correctionTo.toString(dateTimeFormatter));
      } else {
        notSpecified(fieldWidth);
      }
      spaces();
      System.out.println(versionInfo.getUniqueId());
    }
  }
  
  private static void header(int fieldWidth) {
    System.out.print(VERSION_FROM);
    pad(fieldWidth - VERSION_FROM.length());
    spaces();
    System.out.print(VERSION_TO);
    pad(fieldWidth - VERSION_TO.length());
    spaces();
    System.out.print(CORRECTION_FROM);
    pad(fieldWidth - CORRECTION_FROM.length());
    spaces();
    System.out.print(CORRECTION_TO);
    pad(fieldWidth - CORRECTION_TO.length());
    spaces();
    System.out.println(UNIQUE_ID);
  }
  
  private static void spaces() {
    System.out.print("  ");
  }
   
  private static void notSpecified(int fieldWidth) {
    System.out.print(NOT_SPECIFIED);
    pad(fieldWidth - NOT_SPECIFIED.length());
  }
  
  private static void pad(int n) {
    String repeat = org.apache.commons.lang.StringUtils.repeat(" ", n);
    System.out.print(repeat);
  }


  public static ValueSnapshot createValueSnapshot(String market, String override) {
    Object marketValue = null;
    Object overrideValue = null;

    // marketValue can only be Double, LocalDate, empty or (FudgeMsg which is special cased for Market_All)
    if (market != null && !market.isEmpty()) {
      if (NumberUtils.isNumber(market)) {
        marketValue = NumberUtils.createDouble(market);
      } else {
        try {
          marketValue = LocalDate.parse(market);
        } catch (IllegalArgumentException e)  {
          s_logger.error("Market value {} should be a Double, LocalDate or empty.", market);
        }
      }
    }

    //overrideValue can only be Double, LocalDate or empty
    if (override != null && !override.isEmpty()) {
      if (NumberUtils.isNumber(override)) {
        overrideValue = NumberUtils.createDouble(override);
      } else {
        try {
          overrideValue = LocalDate.parse(override);
        } catch (IllegalArgumentException e)  {
          s_logger.error("Override value {} should be a Double, LocalDate or empty.", override);
        }
      }
    }

    return ValueSnapshot.of(marketValue, overrideValue);
  }

  private static Boolean isTenor(String tenor) {
    try {
      Tenor.parse(tenor);
      return true;
    } catch (IllegalArgumentException e)  {
      return false;
    }
  }

  public static Pair<Object, Object> createOrdinatePair(String xValue, String yValue) {
    String[] yValues = yValue.split("\\|");
    Object surfaceX = null;
    Object surfaceY = null;

    if (xValue != null) {
      if (NumberUtils.isNumber(xValue)) {
        surfaceX = NumberUtils.createDouble(xValue);
      } else if (isTenor(xValue)) {
        surfaceX = Tenor.parse(xValue);
      } else {
        s_logger.error("Volatility surface X ordinate {} should be a Double, Tenor or empty.", xValue);
      }
    }

    if (yValues != null) {
      if (yValues.length > 1) {
        try {
          surfaceY = createYOrdinatePair(yValues);
        } catch (IllegalArgumentException e)  {
          s_logger.error("Volatility surface Y ordinate {} should be a Double, Pair<Number, FXVolQuoteType> or empty.", xValue);
        }
      } else if (yValues.length == 1) {
        if (NumberUtils.isNumber(yValues[0])) {
          surfaceY = NumberUtils.createDouble(yValues[0]);
        } else if (isTenor(yValues[0])) {
          surfaceY = Tenor.parse(yValues[0]);
        }
      }
    }

    return Pairs.of(surfaceX, surfaceY);
  }

  // Bloomberg FX option volatility surface codes given a tenor, quote type (ATM, butterfly, risk reversal) and distance from ATM.
  private static Pair<Number, BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType> createYOrdinatePair(String[] yPair) {
    Number firstElement = null;
    BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType secondElement = null;
    if (NumberUtils.isNumber(yPair[0])) {
      firstElement = NumberUtils.createDouble(yPair[0]);
    }
    switch (yPair[1]) {
      case "ATM":
        secondElement = BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType.ATM;
        break;
      case "RISK_REVERSAL":
        secondElement = BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType.RISK_REVERSAL;
        break;
      case "BUTTERFLY":
        secondElement = BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType.BUTTERFLY;
        break;
    }
    return Pairs.of(firstElement, secondElement);
  }

  public static Pair<String, String> ordinalsAsString(Pair<Object, Object> rawOrdinates) {
    String surfaceX;
    if (rawOrdinates.getFirst() instanceof Tenor) {
      surfaceX = ((Tenor) rawOrdinates.getFirst()).toFormattedString();
    } else {
      surfaceX = rawOrdinates.getFirst().toString();
    }

    String surfaceY;
    if (rawOrdinates.getSecond() instanceof Pair) {
      surfaceY = ((Pair) rawOrdinates.getSecond()).getFirst() + "|" + ((Pair) rawOrdinates.getSecond()).getSecond();
    } else if (rawOrdinates.getSecond() instanceof Tenor) {
      surfaceY = ((Tenor) rawOrdinates.getSecond()).toFormattedString();
    } else {
      surfaceY = rawOrdinates.getSecond().toString();
    }

    return ObjectsPair.of(surfaceX, surfaceY);
  }


}
