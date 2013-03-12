/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.analytics.financial.equity.future.definition.EquityFutureDefinition;
import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOptionDefinition;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionDefinition;
import com.opengamma.analytics.financial.equity.option.EquityOptionDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.ExerciseTypeAnalyticsVisitorAdapter;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts equity index options, equity options and equity index future options into something that OG-Analytics can use.
 */
public class EquityOptionsConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  private final FutureSecurityConverter _futureSecurityConverter;
  private final SecuritySource _securitySource;

  public EquityOptionsConverter() {
    this(null, null);
  }

  public EquityOptionsConverter(final FutureSecurityConverter futureSecurityConverter, final SecuritySource securitySource) {
    _futureSecurityConverter = futureSecurityConverter;
    _securitySource = securitySource;
  }

  @Override
  public InstrumentDefinition<?> visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final boolean isCall = security.getOptionType() == OptionType.CALL;
    final double strike = security.getStrike();
    final ZonedDateTime expiryDT = security.getExpiry().getExpiry();
    final Currency ccy = security.getCurrency();
    final double unitNotional = security.getPointValue();
    final ExerciseDecisionType exerciseType = security.getExerciseType().accept(ExerciseTypeAnalyticsVisitorAdapter.getInstance());
    // TODO !!! We need to know how long after expiry does settlement occur?
    // IndexOptions are obviously Cash Settled
    final LocalDate settlementDate = expiryDT.toLocalDate(); // FIXME !!! Needs to come from convention !!!
    //TODO settlement type needs to come from trade or convention
    return new EquityIndexOptionDefinition(isCall, strike, ccy, exerciseType, expiryDT, settlementDate, unitNotional, SettlementType.CASH);
  }

  @Override
  public InstrumentDefinition<?> visitEquityOptionSecurity(final EquityOptionSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final boolean isCall = security.getOptionType() == OptionType.CALL;
    final double strike = security.getStrike();
    final ZonedDateTime expiryDT = security.getExpiry().getExpiry();
    final Currency ccy = security.getCurrency();
    final double unitNotional = security.getPointValue();
    final ExerciseDecisionType exerciseType = security.getExerciseType().accept(ExerciseTypeAnalyticsVisitorAdapter.getInstance());
    // TODO !!! We need to know how long after expiry does settlement occur?
    // IndexOptions are obviously Cash Settled
    final LocalDate settlementDate = expiryDT.toLocalDate(); // FIXME !!! Needs to come from convention !!!
    //TODO settlement type needs to come from trade or convention
    return new EquityOptionDefinition(isCall, strike, ccy, exerciseType, expiryDT, settlementDate, unitNotional, SettlementType.PHYSICAL);
  }

  @Override
  public InstrumentDefinition<?> visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
    ArgumentChecker.notNull(security, "security");
    if (_securitySource == null) {
      throw new OpenGammaRuntimeException("Need a security source to convert equity index future option securities");
    }
    if (_futureSecurityConverter == null) {
      throw new OpenGammaRuntimeException("Need a future security converter to convert equity index future option securities");
    }
    final ZonedDateTime expiryDate = security.getExpiry().getExpiry();
    final ExternalId underlyingIdentifier = security.getUnderlyingId();
    // REVIEW Andrew -- This call to getSingle is not correct as the resolution time of the view cycle will not be considered
    final EquityFutureSecurity underlyingSecurity = ((EquityFutureSecurity) _securitySource.getSingle(ExternalIdBundle.of(underlyingIdentifier)));
    if (underlyingSecurity == null) {
      throw new OpenGammaRuntimeException("Underlying security " + underlyingIdentifier + " was not found in database");
    }
    final EquityFutureDefinition underlying = (EquityFutureDefinition) underlyingSecurity.accept(_futureSecurityConverter);
    final double strike = security.getStrike();
    final ExerciseDecisionType exerciseType = security.getExerciseType().accept(ExerciseTypeAnalyticsVisitorAdapter.getInstance());
    final boolean isCall = security.getOptionType() == OptionType.CALL;
    final double pointValue = security.getPointValue();
    return new EquityIndexFutureOptionDefinition(expiryDate, underlying, strike, exerciseType, isCall, pointValue);
  }

}


