/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.sesame.trade.fpml;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.daycount.OneOneDayCount;
import com.opengamma.financial.convention.rolldate.RollConvention;

/**
 * Loader of trade data in FpML format.
 * <p>
 * This handles the subset of FpML necessary to populate the trade model.
 */
public final class FpmlConversions {

  /**
   * The day count conversions.
   * 'BUS/252' is not included.
   */
  private static final ImmutableMap<String, DayCount> DAY_COUNTS = ImmutableMap.<String, DayCount>builder()
      .put("1/1", new OneOneDayCount())
      .put("30/360", DayCounts.THIRTY_360_ISDA)
      .put("30E/360", DayCounts.THIRTY_E_360)
      .put("30E/360.ISDA", DayCounts.THIRTY_E_360_ISDA)
      .put("ACT/360", DayCounts.ACT_360)
      .put("ACT/365", DayCounts.ACT_365)
      .put("ACT/365.FIXED", DayCounts.ACT_365)
      .put("ACT/365L", DayCounts.ACT_365L)
      .put("ACT/ACT.AFB", DayCounts.ACT_ACT_AFB)
      .put("ACT/ACT.ICMA", DayCounts.ACT_ACT_ICMA)
      .put("ACT/ACT.ISMA", DayCounts.ACT_ACT_ICMA)
      .put("ACT/ACT.ISDA", DayCounts.ACT_ACT_ISDA)
      .put("ACT/365.ISDA", DayCounts.ACT_ACT_ISDA)
      .build();
  /**
   * The business day convention conversions.
   */
  private static final ImmutableMap<String, BusinessDayConvention> BUSINESS_DAY_CONVS =
      ImmutableMap.<String, BusinessDayConvention>builder()
          .put("NONE", BusinessDayConventions.NONE)
          .put("FOLLOWING", BusinessDayConventions.FOLLOWING)
          .put("MODFOLLOWING", BusinessDayConventions.MODIFIED_FOLLOWING)
          .put("PRECEDING", BusinessDayConventions.PRECEDING)
          .build();
  // FRN definition is dates that on same numerical day of month
  // Use last business day of month if no matching numerical date (eg. 31st June replaced by last business day of June)
  // Non-business days are replaced by following, or preceding to avoid changing the month
  // If last date was last business day of month, then all subsequent dates are last business day of month
  // While close to ModifiedFollowing, it is unclear is that is correct for BusinessDayConvention
  // FpML also has a 'NotApplicable' option, which probably should map to null in the caller
  /**
   * The FpML date parser.
   */
  private static final DateTimeFormatter FPML_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd[XXX]");

  /**
   * Restricted constructor.
   */
  private FpmlConversions() {
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an FpML day count to a {@code DayCount}.
   * 
   * @param fpmlDayCountName  the day count name used by FpML
   * @return the day count
   * @throws FpmlParseException if the day count is not known
   */
  public static DayCount dayCount(String fpmlDayCountName) {
    try {
      DayCount dayCount = DAY_COUNTS.get(fpmlDayCountName);
      if (dayCount == null) {
        throw new FpmlParseException("Unknown FpML day count: " + fpmlDayCountName);
      }
      return dayCount;
    } catch (IllegalArgumentException ex) {
      throw new FpmlParseException("Unknown FpML day count: " + fpmlDayCountName);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an FpML business day convention to a {@code BusinessDayConvention}.
   * 
   * @param fmplBusinessDayConventionName  the business day convention name used by FpML
   * @return the business day convention
   * @throws FpmlParseException if the business day convention is not known
   */
  public static BusinessDayConvention businessDayConvention(String fmplBusinessDayConventionName) {
    BusinessDayConvention businessDayConvention = BUSINESS_DAY_CONVS.get(fmplBusinessDayConventionName);
    if (businessDayConvention == null) {
      throw new FpmlParseException("Unknown FpML business day convention: " + fmplBusinessDayConventionName);
    }
    return businessDayConvention;
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an FpML roll convention to a {@code RollConvention}.
   * 
   * @param fmplRollConventionName  the roll convention name used by FpML
   * @return the roll convention
   * @throws FpmlParseException if the roll convention is not known
   */
  public static RollConvention rollConvention(String fmplRollConventionName) {
    Integer dom = Ints.tryParse(fmplRollConventionName);
    if (dom != null) {
      return RollConvention.dayOfMonth(dom);
    }
    if ("NONE".equals(fmplRollConventionName)) {
      return RollConvention.NONE;
    }
    if ("EOM".equals(fmplRollConventionName)) {
      return RollConvention.EOM;
    }
    if ("IMM".equals(fmplRollConventionName)) {
      return RollConvention.IMM;
    }
    throw new FpmlParseException("Unknown FpML roll convention: " + fmplRollConventionName);
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an FpML date to a {@code LocalDate}.
   * 
   * @param dateStr  the business center name used by FpML
   * @return the holiday calendar
   * @throws DateTimeParseException if the date cannot be parsed
   */
  public static LocalDate date(String dateStr) {
    return LocalDate.parse(dateStr, FPML_DATE_FORMAT);
  }

}
