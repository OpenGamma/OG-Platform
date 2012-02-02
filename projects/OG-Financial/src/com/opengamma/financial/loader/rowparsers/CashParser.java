package com.opengamma.financial.loader.rowparsers;

import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;

import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.loader.RowParser;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;

public class CashParser extends RowParser {

  private static final String ID_SCHEME = "CASH_LOADER";

  public static final String CURRENCY = "currency";
  public static final String REGION = "region";
  public static final String MATURITY = "maturity";
  public static final String RATE = "rate";
  public static final String AMOUNT = "amount";

  @Override
  public ManageableSecurity[] constructSecurity(Map<String, String> cashDetails) {
    Currency ccy = Currency.of(getWithException(cashDetails, CURRENCY));
    ExternalId region = ExternalId.of(RegionUtils.ISO_COUNTRY_ALPHA2, REGION);
    LocalDateTime maturity = LocalDateTime.of(
        LocalDate.parse(getWithException(cashDetails, MATURITY), CSV_DATE_FORMATTER),
        LocalTime.MIDNIGHT);
    double rate = Double.parseDouble(getWithException(cashDetails, RATE));
    double amount = Double.parseDouble(getWithException(cashDetails, AMOUNT));
    CashSecurity cash = new CashSecurity(ccy, region, maturity.atZone(TimeZone.UTC), rate, amount);
    cash.setName("Cash " + ccy.getCode() + " " + NOTIONAL_FORMATTER.format(amount) + " @ "
        + RATE_FORMATTER.format(rate) + ", maturity "
        + maturity.toString(OUTPUT_DATE_FORMATTER));
    
    ManageableSecurity[] result = {cash};
    return result;
  }

}
