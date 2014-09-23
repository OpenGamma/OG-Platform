/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.analytics.conversion;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

//TODO: REQS-554 Holidays and calendars

/**
 * Convert fees on a trade to an instrument definition.
 */
public class TradeFeeConverter implements TradeConverter {

  private static final TradeFeeConverter INSTANCE = new TradeFeeConverter();
  private static final String FEE_DATE = "FEE_%d_DATE";
  private static final String FEE_CURRENCY = "FEE_%d_CURRENCY";
  private static final String FEE_AMOUNT = "FEE_%d_AMOUNT";
  private static final String FEE_DIRECTION = "FEE_%d_DIRECTION";

  /**
   * Get an instance
   *
   * @return the instance
   */
  public static final TradeFeeConverter getInstance() {
    return INSTANCE;
  }

  /**
   * Construct a definition from the fees on a trade.
   *
   * @param trade the trade, not null
   * @return the definition, null if no fees
   */
  public InstrumentDefinition<?> convert(final Trade trade) {
    ArgumentChecker.notNull(trade, "trade");
    List<CouponFixedDefinition> fees = new ArrayList<>();
    Security security = trade.getSecurity();
    Currency securityCcy = FinancialSecurityUtils.getCurrency(security);
    for (int i = 1;; i++) {
      if (!trade.getAttributes().containsKey(String.format(FEE_DATE, i))) {
        break;
      }
      final LocalDate feeDate = LocalDate.parse(trade.getAttributes().get(String.format(FEE_DATE, i)));
      final ZonedDateTime feeTime = feeDate.atStartOfDay(ZoneId.systemDefault());
      final Currency ccy = Currency.of(trade.getAttributes().get(String.format(FEE_CURRENCY, i)));
      ArgumentChecker.isTrue(securityCcy.equals(ccy), "Fee must be in security currency {} got {}", securityCcy, ccy);
      final Double amount = Double.parseDouble(trade.getAttributes().get(String.format(FEE_AMOUNT, i)));
      final PayReceiveType payOrReceive = PayReceiveType.valueOf(trade.getAttributes().get(String.format(FEE_DIRECTION, i)));
      final CouponFixedDefinition payment = new CouponFixedDefinition(ccy, feeTime, feeTime, feeTime, 1, payOrReceive == PayReceiveType.PAY ? -amount : amount, 1);
      fees.add(payment);
    }
    if (!fees.isEmpty()) {
      return new AnnuityDefinition<>(fees.toArray(new CouponFixedDefinition[fees.size()]), new MondayToFridayCalendar(""));
    } else {
      return null;
    }
  }


}
