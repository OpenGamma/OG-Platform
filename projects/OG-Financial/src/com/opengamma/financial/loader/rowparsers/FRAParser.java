package com.opengamma.financial.loader.rowparsers;

import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.core.region.RegionUtils;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.loader.RowParser;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.money.Currency;

public class FRAParser extends RowParser {

  private static final String ID_SCHEME = "FRA_LOADER";

  public static final String CURRENCY = "currency";
  public static final String REGION = "region";
  public static final String START_DATE = "start date";
  public static final String END_DATE = "end date";
  public static final String RATE = "rate";
  public static final String AMOUNT = "amount";
  public static final String BBG_ID = "bloomberg identifier";

  @Override
  public ManageableSecurity[] constructSecurity(Map<String, String> fraDetails) {
    Currency ccy = Currency.of(getWithException(fraDetails, CURRENCY));
    ExternalId region = ExternalId.of(RegionUtils.ISO_COUNTRY_ALPHA2, REGION);
    LocalDateTime startDate = LocalDateTime.of(LocalDate.parse(
        getWithException(fraDetails, START_DATE), CSV_DATE_FORMATTER),
        LocalTime.MIDNIGHT);
    LocalDateTime endDate = LocalDateTime.of(LocalDate.parse(
        getWithException(fraDetails, END_DATE), CSV_DATE_FORMATTER),
        LocalTime.MIDNIGHT);
    double rate = Double.parseDouble(getWithException(fraDetails, RATE));
    double amount = Double.parseDouble(getWithException(fraDetails, AMOUNT));
    ZonedDateTime zonedStartDate = startDate.atZone(TimeZone.UTC);
    ZonedDateTime zonedEndDate = endDate.atZone(TimeZone.UTC);
    if (!zonedEndDate.isAfter(zonedStartDate)) {
      throw new IllegalArgumentException("Start date must be before end date");
    }
    ZonedDateTime zonedFixingDate = zonedStartDate.minusDays(2);
    String bbgId = getWithException(fraDetails, BBG_ID);
    ExternalId underlyingID = ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, bbgId);
    FRASecurity fra = new FRASecurity(ccy, region, zonedStartDate, endDate.atZone(TimeZone.UTC), rate, 1000000 * amount, underlyingID, zonedFixingDate);
    fra.setName("FRA " + ccy.getCode() + " " + NOTIONAL_FORMATTER.format(amount) + " @ "
        + RATE_FORMATTER.format(rate) + ", from "
        + startDate.toString(OUTPUT_DATE_FORMATTER) + " to "
        + endDate.toString(OUTPUT_DATE_FORMATTER));
    fra.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));

    ManageableSecurity[] result = {fra};
    return result;
  }

}
