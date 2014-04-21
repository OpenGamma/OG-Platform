/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.generator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.google.common.collect.Sets;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.LeafPortfolioNodeGenerator;
import com.opengamma.financial.generator.NameGenerator;
import com.opengamma.financial.generator.PortfolioGenerator;
import com.opengamma.financial.generator.PortfolioNodeGenerator;
import com.opengamma.financial.generator.PositionGenerator;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.generator.SimplePositionGenerator;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.financial.security.irs.Rate;
import com.opengamma.financial.security.swap.EquityTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Generates a portfolio of equity TRS.
 */
public class EquityTotalReturnSwapPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {
  /** The list of funding legs */
  private static final List<FloatingInterestRateSwapLeg> FUNDING_LEGS = new ArrayList<>();
  /** The list of equities */
  private static final List<EquitySecurity> EQUITIES = new ArrayList<>();
  /** The list of notionals */
  private static final List<Double> NOTIONALS = new ArrayList<>();
  /** The currency of the equities */
  private static final Currency CURRENCY = Currency.USD;
  /** The funding leg payment frequency */
  private static final Frequency FREQUENCY = PeriodFrequency.QUARTERLY;
  /** The funding leg holiday calendar */
  private static final Set<ExternalId> HOLIDAY = Sets.newHashSet(ExternalSchemes.countryRegionId(Country.US).toBundle());
  /** The funding leg ibor rate */
  private static final ExternalId IBOR_RATE = ExternalSchemes.syntheticSecurityId("USDLIBORP3M");
  /** The rate formatter */
  private static final DecimalFormat FORMATTER = new DecimalFormat("###.###");
  /** The tickers */
  private static final List<String> TICKERS = Arrays.asList("HD", "ARG", "IPG", "RSG", "M");
  /** The equity price */
  private static final List<Double> PRICES = Arrays.asList(35.625, 68.5, 12.1, 29.53, 29.19);

  static {
    final Random rng = new Random(131);
    for (int i = 0; i < 20; i++) {
      final String ticker = TICKERS.get(rng.nextInt(TICKERS.size()));
      final EquitySecurity equity = new EquitySecurity("UQ", "UQ", ticker, CURRENCY);
      equity.setExternalIdBundle(ExternalIdBundle.of(ExternalSchemes.syntheticSecurityId(ticker)));
      final double notional = 1000000. * (1 + rng.nextInt(10));
      final double spread = 0.002 + rng.nextInt(100) / 10000.;
      final FloatingInterestRateSwapLeg leg = new FloatingInterestRateSwapLeg();
      leg.setNotional(new InterestRateSwapNotional(CURRENCY, notional));
      leg.setDayCountConvention(DayCounts.ACT_360);
      leg.setPaymentDateFrequency(FREQUENCY);
      leg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
      leg.setPaymentDateCalendars(HOLIDAY);
      leg.setAccrualPeriodFrequency(FREQUENCY);
      leg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
      leg.setAccrualPeriodCalendars(HOLIDAY);
      leg.setResetPeriodFrequency(FREQUENCY);
      leg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
      leg.setResetPeriodCalendars(HOLIDAY);
      leg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
      leg.setFixingDateCalendars(HOLIDAY);
      leg.setFixingDateOffset(-2);
      leg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
      leg.setMaturityDateCalendars(HOLIDAY);
      leg.setFloatingRateType(FloatingRateType.IBOR);
      leg.setFloatingReferenceRateId(IBOR_RATE);
      leg.setPayReceiveType(PayReceiveType.PAY);
      leg.setRollConvention(RollConvention.NONE);
      leg.setSpreadSchedule(Rate.builder().rates(new double[] {spread }).build());
      FUNDING_LEGS.add(leg);
      EQUITIES.add(equity);
      NOTIONALS.add(notional);
    }
  }

  @Override
  public final PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final SecurityGenerator<ManageableSecurity> securities = createEquityTRSSecurityGenerator(FUNDING_LEGS.size());
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("Equity Total Return Swaps"), positions, FUNDING_LEGS.size());
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

  @Override
  public final PortfolioNodeGenerator createPortfolioNodeGenerator(final int size) {
    final SecurityGenerator<ManageableSecurity> securities = createEquityTRSSecurityGenerator(FUNDING_LEGS.size());
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    return new LeafPortfolioNodeGenerator(new StaticNameGenerator("Equity Total Return Swaps"), positions, FUNDING_LEGS.size());
  }

  /**
   * Creates a security generator that loops over the components of the equity TRS.
   * @param size The expected size of the portfolio
   * @return The security generator
   */
  private SecurityGenerator<ManageableSecurity> createEquityTRSSecurityGenerator(final int size) {
    final SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    final Random rng = new Random(111);
    final SecurityGenerator<ManageableSecurity> securities = new SecurityGenerator<ManageableSecurity>() {
      private int _count;

      @SuppressWarnings("synthetic-access")
      @Override
      public ManageableSecurity createSecurity() {
        if (_count > size - 1) {
          throw new IllegalStateException("Should not ask for more than " + size + " securities");
        }
        final SecurityDocument toAddDoc = new SecurityDocument();
        final FloatingInterestRateSwapLeg fundingLeg = FUNDING_LEGS.get(_count);
        final EquitySecurity equity = EQUITIES.get(_count);
        toAddDoc.setSecurity(equity);
        securityMaster.add(toAddDoc);
        final LocalDate startDate = DateUtils.previousWeekDay().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        final LocalDate maturityDate = startDate.plusYears(1 + rng.nextInt(10));
        final ExternalIdBundle assetId = getSecurityPersister().storeSecurity(equity);
        final double spread = fundingLeg.getSpreadSchedule().getRate(0);
        final Double price = PRICES.get(TICKERS.indexOf(equity.getCompanyName()));
        final Double notional = NOTIONALS.get(_count);
        final long numberOfShares = Math.round(notional / price);
        final EquityTotalReturnSwapSecurity security = new EquityTotalReturnSwapSecurity(fundingLeg, assetId, startDate,
            maturityDate, (double) numberOfShares, CURRENCY, notional, 2, BusinessDayConventions.MODIFIED_FOLLOWING,
            PeriodFrequency.QUARTERLY, RollConvention.NONE);
        final StringBuilder sb = new StringBuilder(Long.toString(numberOfShares));
        sb.append(" x ");
        sb.append(equity.getCompanyName());
        sb.append(", 6m USD Libor + ");
        sb.append(FORMATTER.format(spread * 10000.));
        sb.append("bp");
        security.setName(sb.toString());
        _count++;
        return security;
      }

    };
    configure(securities);
    return securities;
  }
}
