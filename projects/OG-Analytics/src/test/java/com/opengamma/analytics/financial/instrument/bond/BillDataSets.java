package com.opengamma.analytics.financial.instrument.bond;

import java.util.Collections;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarTarget;
import com.opengamma.analytics.financial.legalentity.CreditRating;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

public class BillDataSets {

  private final static Currency EUR = Currency.EUR;
  /** A holiday calendar */
  private static final Calendar CALENDAR = new CalendarTarget("TARGET");
  /** The day count */
  private static final DayCount ACT360 = DayCounts.ACT_360;
  /** The number of settlement days */
  private static final int SETTLEMENT_DAYS = 2;
  /** The yield convention */
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("INTEREST@MTY");
  /** Belgian government name */
  private final static String ISSUER_BEL_NAME = "BELGIUM GOVT";
  /** German government name */
  private final static String ISSUER_GER_NAME = "GERMANY GOVT";
  /** Belgian government entity */
  private final static LegalEntity ISSUER_BEL = new LegalEntity(null, ISSUER_BEL_NAME, Collections.singleton(CreditRating.of("A", "Custom", true)), Sector.of("Government"), Region.of("Belgium",
      Country.BE, Currency.EUR));
  /** German government entity */
  private final static LegalEntity ISSUER_GER = new LegalEntity(null, ISSUER_GER_NAME, Collections.singleton(CreditRating.of("AA", "Custom", true)), Sector.of("Government"), Region.of("Germany",
      Country.DE, Currency.EUR));
  /** The maturity: BE0312710792 */
  private final static ZonedDateTime END_DATE_BEL = DateUtils.getUTCDate(2014, 12, 18);
  /** The maturity: DE0001119261  */
  private final static ZonedDateTime END_DATE_GER = DateUtils.getUTCDate(2014, 12, 10);
  /** The notional */
  private final static double NOTIONAL = 1;

  /** Bill definition: Belgium*/
  private final static BillSecurityDefinition BILL_BEL_SEC_DEFINITION =
      new BillSecurityDefinition(EUR, END_DATE_BEL, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL);
  /** Bill definition: Germany */
  private final static BillSecurityDefinition BILL_GER_SEC_DEFINITION =
      new BillSecurityDefinition(EUR, END_DATE_GER, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_GER);

  /**
   * Returns the definition of the Belgium Treasury Certificate 18-Dec-2014 - ISIN-BE0312710792
   * Default notional of 1.
   * @return The bill.
   */
  public static BillSecurityDefinition billBel_20141218() {
    return BILL_BEL_SEC_DEFINITION;
  }

  /**
   * Returns the definition of the German Treasury Bill 10-Dec-2014 - ISIN-DE0001119261
   * Default notional of 1.
   * @return The bill.
   */
  public static BillSecurityDefinition billGer_20141210() {
    return BILL_GER_SEC_DEFINITION;
  }

}
