package com.opengamma.solutions.util;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.sesame.trade.FXForwardTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import java.math.BigDecimal;
import java.util.List;

public class FxForwardViewUtils {

  private FxForwardViewUtils(){/*stuff*/}

  public static final List<Object> FX_TRADE_INPUTS =
      ImmutableList.<Object>of(createFxForwardTrade());

  public static final List<Object> FX_SECURITY_INPUTS =
      ImmutableList.<Object>of(createFxForwardSecurity());


  private static FXForwardTrade createFxForwardTrade(){

    Counterparty counterparty = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "COUNTERPARTY"));
    BigDecimal tradeQuantity = BigDecimal.valueOf(1);
    LocalDate tradeDate = LocalDate.of(2014, 7, 11);
    OffsetTime tradeTime = OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.UTC);
    SimpleTrade trade = new SimpleTrade(createFxForwardSecurity(), tradeQuantity, counterparty, tradeDate, tradeTime);
    trade.setPremium(0.00);
    trade.setPremiumDate(LocalDate.of(2014, 7, 25));
    trade.setPremiumCurrency(Currency.GBP);

    FXForwardTrade fxForwardTrade = new FXForwardTrade(trade);

    return fxForwardTrade;
  }


  private static FXForwardSecurity createFxForwardSecurity(){

    Currency payCurrency = Currency.GBP;
    Currency recCurrency = Currency.USD;

    double payAmount = 1000000;
    double recAmount = 1000000;

    ZonedDateTime forwardDate = DateUtils.getUTCDate(2019, 2, 4);

    ExternalId region = ExternalSchemes.currencyRegionId(Currency.GBP);

    FXForwardSecurity fxForwardSecurity = new FXForwardSecurity(payCurrency,payAmount,recCurrency,recAmount,forwardDate,region);

    return fxForwardSecurity;
  }
}
