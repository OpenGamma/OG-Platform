package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.BuySell;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.CallPut;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.FxOptionTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

public class FxOptionTradeSecurityExtractor extends TradeSecurityExtractor<FxOptionTrade> {

  @Override
  public ManageableSecurity extractSecurity(FxOptionTrade fxOptionTrade) {
    CurrencyPair cp = CurrencyPair.parse(fxOptionTrade.getCurrencyPair());

    Currency optionCurrency = Currency.of(fxOptionTrade.getOptionCurrency());

    if (optionCurrency.equals(cp.getBase()) || optionCurrency.equals(cp.getCounter())) {

      Currency callCurrency;
      Currency putCurrency;
      BigDecimal callAmount;
      BigDecimal putAmount;

      BigDecimal notional = fxOptionTrade.getNotional();
      BigDecimal strike = fxOptionTrade.getStrike();
      Expiry expiry = new Expiry(fxOptionTrade.getExpiryDate().atStartOfDay(ZoneOffset.UTC));

      if (fxOptionTrade.getCallPut() == CallPut.Call) {
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

      ZonedDateTime settlementDate = fxOptionTrade.getPremiumSettlementDate().atStartOfDay(ZoneOffset.UTC);
      boolean isLong = fxOptionTrade.getBuySell() == BuySell.Buy;
      ExerciseType exerciseType = fxOptionTrade.getExerciseType() == FxOptionTrade.ExerciseType.American ?
          new AmericanExerciseType() : new EuropeanExerciseType();

      ManageableSecurity security = fxOptionTrade.getSettlementType() == FxOptionTrade.SettlementType.Physical ?
          new FXOptionSecurity(putCurrency, callCurrency, putAmount.doubleValue(), callAmount.doubleValue(),
                               expiry, settlementDate, isLong, exerciseType) :
          new NonDeliverableFXOptionSecurity(putCurrency,
                                             callCurrency,
                                             putAmount.doubleValue(),
                                             callAmount.doubleValue(),
                                             expiry,
                                             settlementDate,
                                             isLong,
                                             exerciseType,
                                             fxOptionTrade.getSettlementCurrency().equals(callCurrency.getCode()));

      // Generate the loader SECURITY_ID (should be uniquely identifying)
      security.addExternalId(ExternalId.of("XML_LOADER", Integer.toHexString(
          new HashCodeBuilder()
              .append(security.getClass())
              .append(isLong)
              .append(callCurrency)
              .append(callAmount)
              .append(putCurrency)
              .append(putAmount)
              .append(expiry)
              .append(exerciseType).toHashCode()
      )));

      return security;

    } else {
      throw new OpenGammaRuntimeException("Option currency: [" + optionCurrency +
                                              "] does not match either of the currencies in the currency pair: [" + cp +
                                              "]");
    }
  }
}
