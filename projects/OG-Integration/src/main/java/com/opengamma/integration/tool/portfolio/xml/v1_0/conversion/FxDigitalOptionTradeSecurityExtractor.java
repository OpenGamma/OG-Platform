package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.BuySell;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.CallPut;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.FxDigitalOptionTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

public class FxDigitalOptionTradeSecurityExtractor extends TradeSecurityExtractor<FxDigitalOptionTrade> {

  @Override
  public ManageableSecurity extractSecurity(FxDigitalOptionTrade trade) {
    CurrencyPair cp = CurrencyPair.parse(trade.getCurrencyPair());

    Currency optionCurrency = Currency.of(trade.getOptionCurrency());

    if (optionCurrency.equals(cp.getBase()) || optionCurrency.equals(cp.getCounter())) {

      Currency callCurrency;
      Currency putCurrency;
      BigDecimal callAmount;
      BigDecimal putAmount;

      BigDecimal notional = trade.getPayout();
      BigDecimal strike = trade.getStrike();
      Expiry expiry = new Expiry(trade.getExpiryDate().atStartOfDay(ZoneOffset.UTC));

      if (trade.getCallPut() == CallPut.Call) {
        callCurrency = optionCurrency;

        // Get the other currency in the pair
        putCurrency = cp.getCounter().equals(optionCurrency) ? cp.getBase() : cp.getCounter();

        callAmount = notional;

        // The ordering of the currency pair indicates the structure of the strike price.
        // We therefore use this to determine whether we multiply or divide by the strike
        putAmount =  cp.getBase().equals(optionCurrency) ?
            notional.multiply(strike) :
            notional.divide(strike);

      } else {
        callCurrency = cp.getCounter().equals(optionCurrency) ? cp.getBase() : cp.getCounter();
        putCurrency = optionCurrency;
        callAmount = cp.getBase().equals(optionCurrency) ?
            notional.multiply(strike) :
            notional.divide(strike);

        putAmount = notional;
      }

      ZonedDateTime settlementDate = trade.getPremiumSettlementDate().atStartOfDay(ZoneOffset.UTC);
      boolean isLong = trade.getBuySell() == BuySell.Buy;

      ManageableSecurity security = new FXDigitalOptionSecurity(putCurrency, callCurrency, putAmount.doubleValue(), callAmount.doubleValue(),
                               Currency.of(trade.getPayoutCurrency()), expiry, settlementDate, isLong);

      // Generate the loader SECURITY_ID (should be uniquely identifying)
      security.addExternalId(ExternalId.of("XML_LOADER", Integer.toHexString(
          new HashCodeBuilder()
              .append(security.getClass())
              .append(isLong)
              .append(callCurrency)
              .append(callAmount)
              .append(putCurrency)
              .append(putAmount)
              .append(notional)
              .append(expiry).toHashCode()
      )));

      return security;

    } else {
      throw new OpenGammaRuntimeException("Option currency: [" + optionCurrency +
                                              "] does not match either of the currencies in the currency pair: [" + cp +
                                              "]");
    }
  }

}
