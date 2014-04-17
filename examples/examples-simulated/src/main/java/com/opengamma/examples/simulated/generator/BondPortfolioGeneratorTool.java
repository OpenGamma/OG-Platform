/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.generator;

import java.math.BigDecimal;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.PortfolioNodeGenerator;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.generator.SimplePositionGenerator;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.generator.TreePortfolioNodeGenerator;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Generates a portfolio of AUD swaps.
 */
public class BondPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {
  /** The counterparty */
  private static final String COUNTERPARTY = "Cpty";
  /** Bonds */
  private static final GovernmentBondSecurity[] BONDS = new GovernmentBondSecurity[8];
  /** Amounts of each bond */
  private static final double[] AMOUNTS = new double[8];
  
  static {
    final String issuerName = "US TREASURY N/B";
    final String issuerType = "Sovereign";
    final String issuerDomicile = "US";
    final String market = "US GOVERNMENT";
    final Currency currency = Currency.USD;
    final YieldConvention yieldConvention = YieldConventionFactory.INSTANCE.getYieldConvention("US street");
    final String couponType = "FIXED";
    final Frequency couponFrequency = PeriodFrequency.SEMI_ANNUAL;
    final DayCount dayCountConvention = DayCounts.ACT_ACT_ICMA;
    final double totalAmountIssued = 66000000000.;
    final double minimumAmount = 100;
    final double minimumIncrement = 100;
    final double parAmount = 100;
    final double redemptionValue = 100;
    final GovernmentBondSecurity bond1 = new GovernmentBondSecurity(issuerName, issuerType, issuerDomicile, market, currency, 
        yieldConvention, new Expiry(DateUtils.getUTCDate(2019, 2, 15)), couponType, 2.75, couponFrequency, dayCountConvention, DateUtils.getUTCDate(2009, 2, 15),  
        DateUtils.getUTCDate(2009, 2, 15), DateUtils.getUTCDate(2009, 8, 15), 98.3, totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);
    bond1.setName("T 2 3/4 02/15/19");
    BONDS[0] = bond1;
    AMOUNTS[0] = -140000;
    final GovernmentBondSecurity bond2 = new GovernmentBondSecurity(issuerName, issuerType, issuerDomicile, market, currency, 
        yieldConvention, new Expiry(DateUtils.getUTCDate(2017, 12, 15)), couponType, 2.75, couponFrequency, dayCountConvention, DateUtils.getUTCDate(2010, 12, 31),  
        DateUtils.getUTCDate(2010, 12, 31), DateUtils.getUTCDate(2011, 6, 30), 99.4, totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);
    bond2.setName("T 2 3/4 12/15/17");
    BONDS[1] = bond2;
    AMOUNTS[1] = 350000;
    final GovernmentBondSecurity bond3 = new GovernmentBondSecurity(issuerName, issuerType, issuerDomicile, market, currency, 
        yieldConvention, new Expiry(DateUtils.getUTCDate(2018, 4, 30)), couponType, 2.625, couponFrequency, dayCountConvention, DateUtils.getUTCDate(2011, 4, 30),  
        DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 10, 31), 99.4, totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);
    bond3.setName("T 2 5/8 04/30/18");
    BONDS[2] = bond3;
    AMOUNTS[2] = 650000;
    final GovernmentBondSecurity bond4 = new GovernmentBondSecurity(issuerName, issuerType, issuerDomicile, market, currency, 
        yieldConvention, new Expiry(DateUtils.getUTCDate(2021, 5, 15)), couponType, 3.125, couponFrequency, dayCountConvention, DateUtils.getUTCDate(2011, 5, 15),  
        DateUtils.getUTCDate(2011, 5, 15), DateUtils.getUTCDate(2011, 11, 15), 101.3, totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);
    bond4.setName("T 3 1/8 15/05/21");
    BONDS[3] = bond4;    
    AMOUNTS[3] = -100000;
    final GovernmentBondSecurity bond5 = new GovernmentBondSecurity(issuerName, issuerType, issuerDomicile, market, currency, 
        yieldConvention, new Expiry(DateUtils.getUTCDate(2040, 8, 15)), couponType, 3.875, couponFrequency, dayCountConvention, DateUtils.getUTCDate(2010, 8, 15),  
        DateUtils.getUTCDate(2010, 8, 15), DateUtils.getUTCDate(2011, 2, 15), 100.4, totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);
    bond5.setName("T 3 7/8 08/15/40");
    BONDS[4] = bond5;
    AMOUNTS[4] = -130000;
    final GovernmentBondSecurity bond6 = new GovernmentBondSecurity(issuerName, issuerType, issuerDomicile, market, currency, 
        yieldConvention, new Expiry(DateUtils.getUTCDate(2017, 11, 30)), couponType, 2.5, couponFrequency, dayCountConvention, DateUtils.getUTCDate(2010, 11, 30),  
        DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2011, 5, 31), 100., totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);
    bond6.setName("T 2 1/2 11/30/2017");
    BONDS[5] = bond6;
    AMOUNTS[5] = 120000;
    final GovernmentBondSecurity bond7 = new GovernmentBondSecurity(issuerName, issuerType, issuerDomicile, market, currency, 
        yieldConvention, new Expiry(DateUtils.getUTCDate(2039, 5, 15)), couponType, 4.25, couponFrequency, dayCountConvention, DateUtils.getUTCDate(2009, 5, 15),  
        DateUtils.getUTCDate(2009, 5, 15), DateUtils.getUTCDate(2009, 11, 15), 99.1, totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);
    bond7.setName("T 4 1/4 05/15/39");
    BONDS[6] = bond7;
    AMOUNTS[6] = 230000;
    final GovernmentBondSecurity bond8 = new GovernmentBondSecurity(issuerName, issuerType, issuerDomicile, market, currency, 
        yieldConvention, new Expiry(DateUtils.getUTCDate(2019, 11, 15)), couponType, 3.375, couponFrequency, dayCountConvention, DateUtils.getUTCDate(2009, 11, 15),  
        DateUtils.getUTCDate(2009, 11, 15), DateUtils.getUTCDate(2010, 5, 15), 96.9, totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);
    bond8.setName("T 3 3/8 11/15/19");
    BONDS[7] = bond8;
    AMOUNTS[7] = -250000;
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int portfolioSize) {
    final SecurityGenerator<ManageableSecurity> securities = new GovernmentBondSecurityGenerator<>(BONDS, AMOUNTS, "US Treasuries");
    configure(securities);
    final TreePortfolioNodeGenerator rootNode = new TreePortfolioNodeGenerator(new StaticNameGenerator("Government Bonds"));
    rootNode.addChildNode((PortfolioNodeGenerator) securities);
    return rootNode;
  }

  private class GovernmentBondSecurityGenerator<T extends ManageableSecurity> extends SecurityGenerator<T> implements PortfolioNodeGenerator {
    /** The securities */
    private final GovernmentBondSecurity[] _securities;
    /** The amounts */
    private final double[] _amounts;
    /** The name */
    private final String _name;

    public GovernmentBondSecurityGenerator(final GovernmentBondSecurity[] securities, final double[] amounts, final String name) {
      _securities = securities;
      _amounts = amounts;
      _name = name;
    }

    @Override
    public PortfolioNode createPortfolioNode() {
      final SimplePortfolioNode node = new SimplePortfolioNode(_name);
      for (int i = 0; i < _securities.length; i++) {
        final BigDecimal n = new BigDecimal(_amounts[i]);
        final GovernmentBondSecurity bond = _securities[i];
        final ZonedDateTime tradeDate = bond.getSettlementDate();
        final ManageableTrade trade = new ManageableTrade(n, getSecurityPersister().storeSecurity(bond), tradeDate.toLocalDate(),
            tradeDate.toOffsetDateTime().toOffsetTime(), ExternalId.of(Counterparty.DEFAULT_SCHEME, COUNTERPARTY));
        trade.setPremium(bond.getIssuancePrice());
        trade.setPremiumCurrency(bond.getCurrency());
        final Position position = SimplePositionGenerator.createPositionFromTrade(trade);
        node.addPosition(position);
      }
      return node;
    }

    @Override
    public T createSecurity() {
      return null;
    }
  }
}
