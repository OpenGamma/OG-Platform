/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import static org.threeten.bp.temporal.ChronoUnit.MONTHS;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.CommodityNotional;
import com.opengamma.financial.security.swap.FixedInflationSwapLeg;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FixedVarianceSwapLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.FloatingVarianceSwapLeg;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.NotionalVisitor;
import com.opengamma.financial.security.swap.SecurityNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.VarianceSwapNotional;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Source of random, but reasonable, swaption security instances.
 */
public class SwaptionSecurityGenerator extends SecurityGenerator<SwaptionSecurity> {

  private static final int[] OPTION_LENGTH = new int[] {1, 2, 3, 6, 9, 12, 24, 36, 60 };

  private final SwapSecurityGenerator _underlying;
  private final SecurityPersister _securityPersister;

  public SwaptionSecurityGenerator(final SwapSecurityGenerator underlying, final SecurityPersister securityPersister) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(securityPersister, "securityPersister");
    _underlying = underlying;
    _securityPersister = securityPersister;
  }

  protected SwapSecurityGenerator getUnderlyingGenerator() {
    return _underlying;
  }

  protected SecurityPersister getSecurityPersister() {
    return _securityPersister;
  }

  protected SwapSecurity createUnderlying(final ZonedDateTime earliestMaturity, final ZonedDateTime swaptionExpiry) {
    SwapSecurity security;
    do {
      do {
        getUnderlyingGenerator().setSwationExpiry(swaptionExpiry.toLocalDate());
        security = getUnderlyingGenerator().createSecurity();
      } while (security == null);
    } while ((FinancialSecurityUtils.getCurrency(security) == null) || security.getMaturityDate().isBefore(earliestMaturity));
    return security;
  }

  private String lengthString(final int months) {
    if ((months % 12) == 0) {
      return (months / 12) + "Y";
    } else {
      return months + "M";
    }
  }

  protected String createName(final Currency currency, final int optionLength, final int swapLength, final double notional, final double rate) {
    final StringBuilder sb = new StringBuilder();
    sb.append("Vanilla swaption, ").append(lengthString(optionLength)).append(" x ").append(lengthString(swapLength)).append(", ");
    sb.append(currency.getCode()).append(" ").append(NOTIONAL_FORMATTER.format(notional)).append(" @ ").append(RATE_FORMATTER.format(rate));
    return sb.toString();
  }

  private Double getRate(final SwapLeg leg) {
    return leg.accept(new SwapLegVisitor<Double>() {

      @Override
      public Double visitFixedInterestRateLeg(final FixedInterestRateLeg swapLeg) {
        return swapLeg.getRate();
      }

      @Override
      public Double visitFloatingInterestRateLeg(final FloatingInterestRateLeg swapLeg) {
        return null;
      }

      @Override
      public Double visitFloatingSpreadIRLeg(final FloatingSpreadIRLeg swapLeg) {
        return null;
      }

      @Override
      public Double visitFloatingGearingIRLeg(final FloatingGearingIRLeg swapLeg) {
        return null;
      }

      @Override
      public Double visitFixedVarianceSwapLeg(final FixedVarianceSwapLeg swapLeg) {
        return swapLeg.getStrike();
      }

      @Override
      public Double visitFloatingVarianceSwapLeg(final FloatingVarianceSwapLeg swapLeg) {
        return null;
      }

      @Override
      public Double visitFixedInflationSwapLeg(final FixedInflationSwapLeg swapLeg) {
        return swapLeg.getRate();
      }

      @Override
      public Double visitInflationIndexSwapLeg(final InflationIndexSwapLeg swapLeg) {
        return null;
      }

    });
  }

  @Override
  public SwaptionSecurity createSecurity() {
    final int optionLength = getRandom(OPTION_LENGTH);
    ZonedDateTime expiry = ZonedDateTime.now().plusMonths(optionLength);
    final SwapSecurity underlying = createUnderlying(expiry.plusMonths(2), expiry);
    final Currency currency = FinancialSecurityUtils.getCurrency(underlying);
    expiry = nextWorkingDay(expiry, currency);
    final boolean isPayer = getRandom().nextBoolean();
    final boolean isLong = getRandom().nextBoolean();
    final boolean isCashSettled = getRandom().nextBoolean();
    final ZonedDateTime settlementDate = nextWorkingDay(expiry.plusDays(2), currency);
    final Double notional = underlying.getPayLeg().getNotional().accept(new NotionalVisitor<Double>() {

      @Override
      public Double visitCommodityNotional(final CommodityNotional notional) {
        return null;
      }

      @Override
      public Double visitInterestRateNotional(final InterestRateNotional notional) {
        return notional.getAmount();
      }

      @Override
      public Double visitSecurityNotional(final SecurityNotional notional) {
        return null;
      }

      @Override
      public Double visitVarianceSwapNotional(final VarianceSwapNotional notional) {
        return notional.getAmount();
      }

    });
    if (notional == null) {
      return null;
    }
    Double rate = getRate(underlying.getPayLeg());
    if (rate == null) {
      rate = getRate(underlying.getReceiveLeg());
      if (rate == null) {
        return null;
      }
    }
    final SwaptionSecurity security = new SwaptionSecurity(isPayer, getSecurityPersister().storeSecurity(underlying).iterator().next(), isLong, new Expiry(expiry), isCashSettled, currency, notional,
        new EuropeanExerciseType(), settlementDate);
    security.setName(createName(currency, optionLength, (int) MONTHS.between(underlying.getEffectiveDate(), underlying.getMaturityDate()), notional, rate));
    return security;
  }

}
