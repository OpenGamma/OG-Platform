/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class CdsSeniorityAggregationFunctionTest {

  private SecurityMaster _securityMaster;
  private AggregationFunction<String> _aggregator;

  @BeforeMethod
  public void setup() {
    _securityMaster = new InMemorySecurityMaster();
    _aggregator = new CdsSeniorityAggregationFunction(new MasterSecuritySource(_securityMaster));
  }

  @Test
  public void testNameIsDefined() {
    assertEquals(_aggregator.getName(), "Seniority");
  }

  @Test
  public void testNoRequiredEntries() {
    assertEquals(_aggregator.getRequiredEntries().isEmpty(), true);
  }

  @Test
  public void testSimpleStringComparisonIsUsed() {
    assertEquals(_aggregator.compare("39FF64", "6H27C2") < 0, true);
    assertEquals(_aggregator.compare("6H27C2", "6H27C2"), 0);
    assertEquals(_aggregator.compare("6H27C2", "39FF64") > 0, true);
  }

  @Test
  public void testPositionWithUnlocatableSecurityIsIgnored() {

    Position posn = new SimplePosition(BigDecimal.ONE, ExternalId.of("SEC_ID", "9999"));
    assertEquals(_aggregator.classifyPosition(posn), "N/A");
  }

  @Test
  public void testPositionSecurityWithoutObligorIsIgnored() {

    SecurityDocument document = new SecurityDocument();
    CashSecurity security = new CashSecurity(Currency.of("USD"),
                                             ExternalSchemes.financialRegionId("US"),
                                             ZonedDateTime.now(),
                                             ZonedDateTime.now().plusYears(5),
                                             DayCounts.ACT_360,
                                             0.05,
                                             100000);
    ExternalId secId = ExternalId.of("SEC_ID", "12345");
    security.addExternalId(secId);
    document.setSecurity(security);
    _securityMaster.add(document);

    Position posn = new SimplePosition(BigDecimal.ONE, secId);
    assertEquals(_aggregator.classifyPosition(posn), "N/A");
  }

  @Test
  public void testPositionSecurityWithRedCodeIsUsed() {

    SecurityDocument document = new SecurityDocument();
    ManageableSecurity cds = new StandardVanillaCDSSecurity(true, ExternalId.of("EXTERNAL_CODE" ,"ProtBuyer"),
                                                                    ExternalId.of("EXTERNAL_CODE" ,"ProtSeller"), ExternalSchemes.markItRedCode("39FF64"),
                                                                    DebtSeniority.SNRFOR, RestructuringClause.MM, ExternalSchemes.financialRegionId("US"),
                                                                    createZdt(2013, 3, 20), createZdt(2013, 3, 21), createZdt(2014,3,20), StubType.SHORT_START,
                                                                    SimpleFrequency.SEMI_ANNUAL, DayCounts.ACT_360,
                                                                    BusinessDayConventions.FOLLOWING,
                                                                    true, true, true, new InterestRateNotional(Currency.USD, 10000000), true, true, 500,
                                                                    new InterestRateNotional(Currency.USD, 500000), 500, createZdt(2013,3,21), true);
    ExternalId secId = ExternalId.of("SEC_ID", "12345");
    cds.addExternalId(secId);
    document.setSecurity(cds);
    _securityMaster.add(document);

    Position posn = new SimplePosition(BigDecimal.ONE, secId);

    assertEquals(_aggregator.classifyPosition(posn), "SNRFOR");
  }

  private ZonedDateTime createZdt(int year, int month, int day) {
    return ZonedDateTime.of(LocalDate.of(year, month, day).atStartOfDay(), ZoneOffset.UTC);
  }

}