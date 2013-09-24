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
import java.util.TimeZone;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.format.SignStyle;

import com.opengamma.integration.tool.marketdata.SnapshotUtils.VersionInfo;

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
}
