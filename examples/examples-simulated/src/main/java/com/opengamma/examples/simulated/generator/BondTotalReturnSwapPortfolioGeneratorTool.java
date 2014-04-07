/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.generator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.DateRelativeTo;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.FloatingInterestRateSwapLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.LeafPortfolioNodeGenerator;
import com.opengamma.financial.generator.NameGenerator;
import com.opengamma.financial.generator.PortfolioGenerator;
import com.opengamma.financial.generator.PortfolioNodeGenerator;
import com.opengamma.financial.generator.PositionGenerator;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.generator.SimplePositionGenerator;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.financial.security.irs.Rate;
import com.opengamma.financial.security.swap.BondTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Generates a portfolio of UGX bond TRS.
 */
public class BondTotalReturnSwapPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {
  /** The list of funding legs */
  private static final List<FloatingInterestRateSwapLeg> FUNDING_LEGS = new ArrayList<>();
  /** The list of bonds */
  private static final List<BondSecurity> BONDS = new ArrayList<>();
  /** The list of notionals */
  private static final List<Double> NOTIONALS = new ArrayList<>();
  /** The currency of the bonds */
  private static final Currency CURRENCY = Currency.of("UGX");
  /** The rate formatter */
  private static final DecimalFormat FORMATTER = new DecimalFormat("###.###");

  static {
    final Set<ExternalId> holiday = Sets.newHashSet(ExternalSchemes.countryRegionId(Country.US).toBundle());
    final FloatingInterestRateSwapLegConvention leg = new FloatingInterestRateSwapLegConvention("USD Floating Swap Leg", ExternalIdBundle.of("CONVENTION", "USD 6M IBOR INDEX"));
    leg.setDayCountConvention(DayCounts.ACT_360);
    leg.setCalculationCalendars(holiday);
    leg.setMaturityCalendars(holiday);
    leg.setPaymentCalendars(holiday);
    leg.setPaymentFrequency(SimpleFrequency.SEMI_ANNUAL);
    leg.setPaymentRelativeTo(DateRelativeTo.END);
    leg.setSettlementDays(0);
    leg.setPaymentDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    leg.setCalculationBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    leg.setCalculationFrequency(SimpleFrequency.SEMI_ANNUAL);
    leg.setMaturityBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    leg.setFixingCalendars(holiday);
    leg.setFixingBusinessDayConvention(BusinessDayConventions.NONE);
    leg.setResetFrequency(SimpleFrequency.SEMI_ANNUAL);
    leg.setResetCalendars(holiday);
    leg.setResetBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    leg.setResetRelativeTo(DateRelativeTo.START);
    leg.setRollConvention(RollConvention.EOM);
    leg.setRateType(FloatingRateType.IBOR);
    leg.setCompoundingMethod(CompoundingMethod.NONE);
    final Random rng = new Random(131);
    for (int i = 0; i < 20; i++) {
      final String issuerName = "UGANDA";
      final String issuerType = "Sovereign";
      final String issuerDomicile = "UG";
      final String market = "UGANDA";
      final YieldConvention yieldConvention = YieldConventionFactory.INSTANCE.getYieldConvention("US street");
      final String couponType = "FIXED";
      final Frequency couponFrequency = PeriodFrequency.SEMI_ANNUAL;
      final DayCount dayCountConvention = DayCounts.ACT_365;
      final double totalAmountIssued = 1000000000.;
      final double minimumAmount = 100;
      final double minimumIncrement = 100;
      final double parAmount = 100;
      final double redemptionValue = 100;
      final ZonedDateTime baseDate = DateUtils.previousWeekDay().atStartOfDay(ZoneOffset.UTC);
      final ZonedDateTime bondStartDate = baseDate;
      final int months = (int) ((i + 1) / 2. * 12);
      final ZonedDateTime maturityDate = baseDate.plusMonths(months);
      final double coupon = 6 + (rng.nextInt(10) / 8. + (rng.nextBoolean() ? -0.5 : 0.5));
      final double issuancePrice = 100;
      final GovernmentBondSecurity bond = new GovernmentBondSecurity(issuerName, issuerType, issuerDomicile, market, CURRENCY,
          yieldConvention, new Expiry(maturityDate), couponType, coupon, couponFrequency, dayCountConvention, bondStartDate,
          bondStartDate, bondStartDate.plusMonths(6), issuancePrice, totalAmountIssued, minimumAmount,
          minimumIncrement, parAmount, redemptionValue);
      String suffix;
      if (months < 10) {
        suffix = "00" + Integer.toString(months);
      } else if (months < 100) {
        suffix = "0" + Integer.toString(months);
      } else {
        suffix = Integer.toString(months);
      }
      bond.setExternalIdBundle(ExternalIdBundle.of(ExternalSchemes.isinSecurityId("UG0000000" + suffix)));
      final StringBuilder bondName = new StringBuilder("Uganda ");
      bondName.append(FORMATTER.format(coupon));
      bondName.append("% ");
      bondName.append(maturityDate.toLocalDate());
      bond.setName(bondName.toString());
      final double notional = 1000000. * (1 + rng.nextInt(10));
      final double spread = 0.002 + rng.nextInt(100) / 10000.;
      FUNDING_LEGS.add(leg.toLeg(new InterestRateSwapNotional(CURRENCY, notional), PayReceiveType.PAY,
          Rate.builder().rates(new double[] {spread }).build()));
      BONDS.add(bond);
      NOTIONALS.add(notional);
    }
  }

  @Override
  public final PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final SecurityGenerator<ManageableSecurity> securities = createBondTRSSecurityGenerator(FUNDING_LEGS.size());
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("Bond Total Return Swaps"), positions, FUNDING_LEGS.size());
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

  @Override
  public final PortfolioNodeGenerator createPortfolioNodeGenerator(final int size) {
    final SecurityGenerator<ManageableSecurity> securities = createBondTRSSecurityGenerator(FUNDING_LEGS.size());
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    return new LeafPortfolioNodeGenerator(new StaticNameGenerator("Bond Total Return Swaps"), positions, FUNDING_LEGS.size());
  }

  /**
   * Creates a security generator that loops over the components of the bond TRS.
   * @param size The expected size of the portfolio
   * @return The security generator
   */
  private SecurityGenerator<ManageableSecurity> createBondTRSSecurityGenerator(final int size) {
    final SecurityMaster securityMaster = getToolContext().getSecurityMaster();
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
        final BondSecurity bond = BONDS.get(_count);
        toAddDoc.setSecurity(bond);
        securityMaster.add(toAddDoc);
        final ExternalIdBundle assetId = getSecurityPersister().storeSecurity(bond);
        final double spread = fundingLeg.getSpreadSchedule().getRate(0);
        final BondTotalReturnSwapSecurity security = new BondTotalReturnSwapSecurity(fundingLeg, assetId, bond.getSettlementDate().toLocalDate(),
            bond.getLastTradeDate().getExpiry().toLocalDate(), CURRENCY, NOTIONALS.get(_count), 0, BusinessDayConventions.MODIFIED_FOLLOWING,
            PeriodFrequency.SEMI_ANNUAL, RollConvention.NONE);
        final StringBuilder sb = new StringBuilder(bond.getName());
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
