/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.currency.CurrencyPairsConfigPopulator;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SamplingFrequency;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;
import com.opengamma.util.tuple.Pairs;

/**
 * Source of random, but reasonable, FX security instances.
 * 
 * @param <T> the sub-type of the security
 */
public abstract class AbstractFXSecurityGenerator<T extends ManageableSecurity> extends SecurityGenerator<T> {

  private static final ExternalId REGION = ExternalSchemes.countryRegionId(Country.US);
  private static final double NOTIONAL = 100000;
  private static final DecimalFormat RATE_FORMATTER = new DecimalFormat("###.######");
  private static final DecimalFormat NOTIONAL_FORMATTER = new DecimalFormat("########.###");
  private static final Boolean[] BOOLEAN_VALUES = {Boolean.TRUE, Boolean.FALSE};
  private CurrencyPairs _currencyPairs = CurrencyPairsConfigPopulator.createCurrencyPairs();

  /**
   * Structured random information for creating the security.
   */
  protected class Bundle {

    private final boolean _long;
    private final boolean _up;
    private final Currency _firstCurrency;
    private final Currency _secondCurrency;
    private final Currency _paymentCurrency;
    private final int _daysOffset;
    private final ZonedDateTime _tradeDate;

    public Bundle() {
      _long = getRandom().nextBoolean();
      _up = getRandom().nextBoolean();
      _firstCurrency = getRandomCurrency();
      Currency secondCurrency;
      do {
        secondCurrency = getRandomCurrency();
      } while (secondCurrency == _firstCurrency);
      _secondCurrency = secondCurrency;
      _daysOffset = 100 + getRandom().nextInt(600);
      _tradeDate = previousWorkingDay(ZonedDateTime.now().minusDays(getRandom(30)), _firstCurrency, _secondCurrency);
      _paymentCurrency = getRandom().nextBoolean() ? _firstCurrency : _secondCurrency;
    }

  }

  protected Bundle createBundle() {
    return new Bundle();
  }

  protected FXBarrierOptionSecurity createFXBarrierOptionSecurity(final Bundle bundle) {
    final Currency putCurrency = bundle._firstCurrency;
    final Currency callCurrency = bundle._secondCurrency;
    final double putAmount = putCurrency.equals(Currency.JPY) ? NOTIONAL * 100 : NOTIONAL;
    final ZonedDateTime settlementDate = nextWorkingDay(bundle._tradeDate.plusDays(bundle._daysOffset), bundle._firstCurrency, bundle._secondCurrency);
    final Expiry expiry = new Expiry(settlementDate, ExpiryAccuracy.DAY_MONTH_YEAR);
    final Double fxRate = getApproxFXRate(settlementDate.toLocalDate(), Pairs.of(bundle._firstCurrency, bundle._secondCurrency));
    if (fxRate == null) {
      return null;
    }
    final double callAmount = NOTIONAL * fxRate;
    final String dateString = settlementDate.toString(DATE_FORMATTER);
    final BarrierType barrierType = bundle._up ? BarrierType.UP : BarrierType.DOWN;
    final BarrierDirection barrierDirection = BarrierDirection.KNOCK_IN;
    final MonitoringType monitoringType = MonitoringType.CONTINUOUS;
    final SamplingFrequency samplingFrequency = SamplingFrequency.DAILY_CLOSE;
    final boolean invertBarrierLevel = !CurrencyPair.of(putCurrency, callCurrency).equals(getCurrencyPair(putCurrency, callCurrency));
    // so if UP and ccy convention order, multiple, if UP and inverted ccy order, divide, if DOWN and ccy convention order multiply, if DOWN and inverted ccy order, divide.
    final double barrierLevel = bundle._up ^ invertBarrierLevel ? fxRate * 1.5 : fxRate / 1.5;
    final FXBarrierOptionSecurity fxBarrierOptionSecurity = new FXBarrierOptionSecurity(putCurrency, callCurrency, putAmount, callAmount, expiry, settlementDate, barrierType, barrierDirection,
        monitoringType, samplingFrequency, barrierLevel, bundle._long);
    final String callAmountString = NOTIONAL_FORMATTER.format(callAmount);
    final String putAmountString = NOTIONAL_FORMATTER.format(putAmount);
    final String barrierLevelString = RATE_FORMATTER.format(barrierLevel);
    final String barrierUnitString = callCurrency + "/" + putCurrency;
    fxBarrierOptionSecurity.setName((bundle._long ? "Long " : "Short ") + (bundle._up ? "up" : "down") + " knock-in at " + barrierLevelString + " " + barrierUnitString + ", put " + putCurrency +
        " " +
        putAmountString + ", call " + callCurrency + " " + callAmountString + " on " + dateString);
    return fxBarrierOptionSecurity;
  }

  protected FXDigitalOptionSecurity createFXDigitalOptionSecurity(final Bundle bundle) {
    final Currency putCurrency = bundle._firstCurrency;
    final Currency callCurrency = bundle._secondCurrency;
    final Currency paymentCurrency = bundle._paymentCurrency;
    final double putAmount = putCurrency.equals(Currency.JPY) ? NOTIONAL * 100 : NOTIONAL;
    final ZonedDateTime expiry = nextWorkingDay(bundle._tradeDate.plusDays(bundle._daysOffset), putCurrency, callCurrency);
    final Double rate = getApproxFXRate(expiry.toLocalDate(), Pairs.of(putCurrency, callCurrency));
    if (rate == null) {
      return null;
    }
    final double callAmount = rate * NOTIONAL;
    final ZonedDateTime settlementDate = nextWorkingDay(expiry.plusDays(2), putCurrency, callCurrency);
    final FXDigitalOptionSecurity security = new FXDigitalOptionSecurity(putCurrency, callCurrency, putAmount, callAmount, paymentCurrency, new Expiry(expiry), settlementDate, bundle._long);
    final StringBuilder sb = new StringBuilder("Digital ");
    sb.append(bundle._long ? "Long" : "Short");
    sb.append(" put ").append(putCurrency).append(' ').append(NOTIONAL_FORMATTER.format(putAmount));
    sb.append(", call ").append(callCurrency).append(' ').append(NOTIONAL_FORMATTER.format(callAmount));
    sb.append(" on ").append(expiry.toString(DATE_FORMATTER));
    security.setName(sb.toString());
    return security;
  }

  protected FXForwardSecurity createFXForwardSecurity(final Bundle bundle) {
    final double putAmount = bundle._firstCurrency.equals(Currency.JPY) ? NOTIONAL * 100 : NOTIONAL;
    final ZonedDateTime forwardDate = nextWorkingDay(bundle._tradeDate.plusDays(bundle._daysOffset), bundle._firstCurrency, bundle._secondCurrency);
    final Double fxRate = getApproxFXRate(forwardDate.toLocalDate(), Pairs.of(bundle._firstCurrency, bundle._secondCurrency));
    if (fxRate == null) {
      return null;
    }
    final double callAmount = NOTIONAL * fxRate;
    final Currency payCurrency = bundle._long ? bundle._secondCurrency : bundle._firstCurrency;
    final Currency receiveCurrency = bundle._long ? bundle._firstCurrency : bundle._secondCurrency;
    final String dateString = forwardDate.toString(DATE_FORMATTER);
    final FXForwardSecurity fxForwardSecurity = new FXForwardSecurity(payCurrency, callAmount, receiveCurrency, putAmount, forwardDate, REGION);
    final String callAmountString = NOTIONAL_FORMATTER.format(callAmount);
    final String putAmountString = NOTIONAL_FORMATTER.format(putAmount);
    fxForwardSecurity.setName("Pay " + payCurrency + " " + callAmountString + ", receive " + receiveCurrency + " " + putAmountString + " on " + dateString);
    return fxForwardSecurity;
  }
  
  protected NonDeliverableFXForwardSecurity createNDFXForwardSecurity(final Bundle bundle) {
    final double putAmount = bundle._firstCurrency.equals(Currency.JPY) ? NOTIONAL * 100 : NOTIONAL;
    final ZonedDateTime forwardDate = nextWorkingDay(bundle._tradeDate.plusDays(bundle._daysOffset), bundle._firstCurrency, bundle._secondCurrency);
    final Double fxRate = getApproxFXRate(forwardDate.toLocalDate(), Pairs.of(bundle._firstCurrency, bundle._secondCurrency));
    if (fxRate == null) {
      return null;
    }
    final double callAmount = NOTIONAL * fxRate;
    final Currency payCurrency = bundle._long ? bundle._secondCurrency : bundle._firstCurrency;
    final Currency receiveCurrency = bundle._long ? bundle._firstCurrency : bundle._secondCurrency;
    final String dateString = forwardDate.toString(DATE_FORMATTER);
    final NonDeliverableFXForwardSecurity security = new NonDeliverableFXForwardSecurity(payCurrency, callAmount, receiveCurrency, putAmount, forwardDate, 
        REGION, getRandom(BOOLEAN_VALUES));
    
    final String callAmountString = NOTIONAL_FORMATTER.format(callAmount);
    final String putAmountString = NOTIONAL_FORMATTER.format(putAmount);
    security.setName("Pay " + payCurrency + " " + callAmountString + ", receive " + receiveCurrency + " " + putAmountString + " on " + dateString);
    return security;
  }

  protected FXOptionSecurity createFXOptionSecurity(final Bundle bundle) {
    final Currency putCurrency = bundle._firstCurrency;
    final Currency callCurrency = bundle._secondCurrency;
    final double putAmount = bundle._firstCurrency.equals(Currency.JPY) ? NOTIONAL * 100 : NOTIONAL;
    final ZonedDateTime settlementDate = bundle._tradeDate.plusDays(bundle._daysOffset);
    final Double fxRate = getApproxFXRate(settlementDate.toLocalDate(), Pairs.of(bundle._firstCurrency, bundle._secondCurrency));
    if (fxRate == null) {
      return null;
    }
    final double callAmount = NOTIONAL * fxRate;
    final Expiry expiry = new Expiry(settlementDate, ExpiryAccuracy.DAY_MONTH_YEAR);
    final String dateString = settlementDate.toString(DATE_FORMATTER);
    final FXOptionSecurity fxOptionSecurity = new FXOptionSecurity(putCurrency, callCurrency, putAmount, callAmount, expiry, settlementDate, bundle._long, new EuropeanExerciseType());
    final String callAmountString = NOTIONAL_FORMATTER.format(callAmount);
    final String putAmountString = NOTIONAL_FORMATTER.format(putAmount);
    fxOptionSecurity.setName((bundle._long ? "Long " : "Short ") + "put " + putCurrency + " " + putAmountString + ", call " + callCurrency + " " + callAmountString + " on " + dateString);
    return fxOptionSecurity;
  }
  
  protected NonDeliverableFXOptionSecurity createNDFXOptionSecurity(final Bundle bundle) {
    final Currency putCurrency = bundle._firstCurrency;
    final Currency callCurrency = bundle._secondCurrency;
    final double putAmount = bundle._firstCurrency.equals(Currency.JPY) ? NOTIONAL * 100 : NOTIONAL;
    final ZonedDateTime settlementDate = bundle._tradeDate.plusDays(bundle._daysOffset);
    final Double fxRate = getApproxFXRate(settlementDate.toLocalDate(), Pairs.of(bundle._firstCurrency, bundle._secondCurrency));
    if (fxRate == null) {
      return null;
    }
    final double callAmount = NOTIONAL * fxRate;
    final Expiry expiry = new Expiry(settlementDate, ExpiryAccuracy.DAY_MONTH_YEAR);
    final String dateString = settlementDate.toString(DATE_FORMATTER);
    
    final NonDeliverableFXOptionSecurity optionSecurity = new NonDeliverableFXOptionSecurity(putCurrency, callCurrency, putAmount, callAmount, expiry, settlementDate, 
        getRandom(BOOLEAN_VALUES), new EuropeanExerciseType(), getRandom(BOOLEAN_VALUES));
    
    final String callAmountString = NOTIONAL_FORMATTER.format(callAmount);
    final String putAmountString = NOTIONAL_FORMATTER.format(putAmount);
    optionSecurity.setName((bundle._long ? "Long " : "Short ") + "put " + putCurrency + " " + putAmountString + ", call " + callCurrency + " " + callAmountString + " on " + dateString);
    return optionSecurity;
  }

  protected ManageableTrade createFXBarrierOptionSecurityTrade(final Bundle bundle, final BigDecimal quantity, final SecurityPersister persister, final NameGenerator counterPartyGenerator) {
    final FXBarrierOptionSecurity security = createFXBarrierOptionSecurity(bundle);
    if (security == null) {
      return null;
    }
    ManageableTrade trade = new ManageableTrade(quantity, persister.storeSecurity(security), bundle._tradeDate.toLocalDate(), bundle._tradeDate.toOffsetDateTime().toOffsetTime(), 
        ExternalId.of(Counterparty.DEFAULT_SCHEME, counterPartyGenerator.createName()));
    trade.setPremium(security.getCallAmount());
    trade.setPremiumCurrency(security.getCallCurrency());
    return trade;
  }

  protected ManageableTrade createFXDigitalOptionSecurityTrade(final Bundle bundle, final BigDecimal quantity, final SecurityPersister persister, final NameGenerator counterPartyGenerator) {
    final FXDigitalOptionSecurity security = createFXDigitalOptionSecurity(bundle);
    if (security == null) {
      return null;
    }
    ManageableTrade trade = new ManageableTrade(quantity, persister.storeSecurity(security), bundle._tradeDate.toLocalDate(), bundle._tradeDate.toOffsetDateTime().toOffsetTime(), 
        ExternalId.of(Counterparty.DEFAULT_SCHEME, counterPartyGenerator.createName()));
    trade.setPremium(security.getCallAmount());
    trade.setPremiumCurrency(security.getCallCurrency());
    return trade;
  }

  protected ManageableTrade createFXForwardSecurityTrade(final Bundle bundle, final BigDecimal quantity, final SecurityPersister persister, final NameGenerator counterPartyGenerator) {
    final FXForwardSecurity security = createFXForwardSecurity(bundle);
    if (security == null) {
      return null;
    }
    ManageableTrade trade = new ManageableTrade(quantity, persister.storeSecurity(security), bundle._tradeDate.toLocalDate(), bundle._tradeDate.toOffsetDateTime().toOffsetTime(), 
        ExternalId.of(Counterparty.DEFAULT_SCHEME, counterPartyGenerator.createName()));
    trade.setPremium(security.getPayAmount());
    trade.setPremiumCurrency(security.getPayCurrency());
    return trade;
    
  }
  
  protected ManageableTrade createNDFXForwardSecurityTrade(final Bundle bundle, final BigDecimal quantity, final SecurityPersister persister, final NameGenerator counterPartyGenerator) {
    final NonDeliverableFXForwardSecurity security = createNDFXForwardSecurity(bundle);
    if (security == null) {
      return null;
    }
    ManageableTrade trade = new ManageableTrade(quantity, persister.storeSecurity(security), bundle._tradeDate.toLocalDate(), bundle._tradeDate.toOffsetDateTime().toOffsetTime(), 
        ExternalId.of(Counterparty.DEFAULT_SCHEME, counterPartyGenerator.createName()));
    trade.setPremium(security.getPayAmount());
    trade.setPremiumCurrency(security.getPayCurrency());
    return trade;
  }

  protected ManageableTrade createFXOptionSecurityTrade(final Bundle bundle, final BigDecimal quantity, final SecurityPersister persister, final NameGenerator counterPartyGenerator) {
    final FXOptionSecurity security = createFXOptionSecurity(bundle);
    if (security == null) {
      return null;
    }
    ManageableTrade trade = new ManageableTrade(quantity, persister.storeSecurity(security), bundle._tradeDate.toLocalDate(), bundle._tradeDate.toOffsetDateTime().toOffsetTime(), 
        ExternalId.of(Counterparty.DEFAULT_SCHEME, counterPartyGenerator.createName()));
    trade.setPremium(security.getCallAmount());
    trade.setPremiumCurrency(security.getCallCurrency());
    return trade;
  }
  
  protected ManageableTrade createNDFXOptionSecurityTrade(final Bundle bundle, final BigDecimal quantity, final SecurityPersister persister, final NameGenerator counterPartyGenerator) {
    final NonDeliverableFXOptionSecurity security = createNDFXOptionSecurity(bundle);
    if (security == null) {
      return null;
    }
    ManageableTrade trade = new ManageableTrade(quantity, persister.storeSecurity(security), bundle._tradeDate.toLocalDate(), bundle._tradeDate.toOffsetDateTime().toOffsetTime(), 
        ExternalId.of(Counterparty.DEFAULT_SCHEME, counterPartyGenerator.createName()));
    trade.setPremium(security.getCallAmount());
    trade.setPremiumCurrency(security.getCallCurrency());
    return trade;
  }
  
  protected CurrencyPair getCurrencyPair(Currency ccy1, Currency ccy2) {
    return _currencyPairs.getCurrencyPair(ccy1, ccy2);
  }
  
}
