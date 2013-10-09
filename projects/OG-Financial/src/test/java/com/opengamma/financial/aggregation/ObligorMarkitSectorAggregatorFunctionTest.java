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
import com.opengamma.core.obligor.CreditRating;
import com.opengamma.core.obligor.CreditRatingFitch;
import com.opengamma.core.obligor.CreditRatingMoodys;
import com.opengamma.core.obligor.CreditRatingStandardAndPoors;
import com.opengamma.core.obligor.Region;
import com.opengamma.core.obligor.Sector;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.id.ExternalId;
import com.opengamma.master.organization.impl.MasterOrganizationSource;
import com.opengamma.master.orgs.ManageableOrganization;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.master.orgs.impl.InMemoryOrganizationMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class ObligorMarkitSectorAggregatorFunctionTest {

  private AggregationFunction<String> _aggregator;
  private InMemorySecurityMaster _securityMaster;
  private InMemoryOrganizationMaster _organizationMaster;

  @BeforeMethod
  public void setup() {

    _securityMaster = new InMemorySecurityMaster();
    _organizationMaster = new InMemoryOrganizationMaster();

    _aggregator = new ObligorMarkitSectorAggregationFunction(
        new MasterSecuritySource(_securityMaster), new MasterOrganizationSource(_organizationMaster));
  }

  @Test
  public void testNameIsDefined() {
    assertEquals(_aggregator.getName(), "Markit Sectors");
  }

  @Test
  public void testNoRequiredEntries() {
    assertEquals(_aggregator.getRequiredEntries().isEmpty(), true);
  }

  @Test
  public void testSimpleStringComparisonIsUsed() {
    assertEquals(_aggregator.compare("CONSUMERSERVICES", "TELECOMMUNICATIONSSERVICES") < 0, true);
    assertEquals(_aggregator.compare("CONSUMERSERVICES", "CONSUMERSERVICES"), 0);
    assertEquals(_aggregator.compare("TELECOMMUNICATIONSSERVICES", "CONSUMERSERVICES") > 0, true);
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
                                             DayCountFactory.INSTANCE.getDayCount("Actual/360"),
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
  public void testObligorWithMatchingRedCodeIsUsed() {

    ManageableOrganization org = new ManageableOrganization("ShortName", "39FF64", "Ticker", Region.NORTHAMERICA,
                                                            "US", Sector.FINANCIALS, CreditRating.NR, CreditRating.NR,
                                                            CreditRatingFitch.NR, CreditRatingMoodys.NR,
                                                            CreditRatingStandardAndPoors.NR, false);

    _organizationMaster.add(new OrganizationDocument(org));

    SecurityDocument document = new SecurityDocument();
    ManageableSecurity cds = createCdsWithRedCode("39FF64");
    ExternalId secId = ExternalId.of("SEC_ID", "12345");
    cds.addExternalId(secId);
    document.setSecurity(cds);
    _securityMaster.add(document);

    Position posn = new SimplePosition(BigDecimal.ONE, secId);

    assertEquals(_aggregator.classifyPosition(posn), "FINANCIALS");
  }

  private StandardVanillaCDSSecurity createCdsWithRedCode(String redcode) {
    return new StandardVanillaCDSSecurity(true, ExternalId.of("EXTERNAL_CODE", "ProtBuyer"),
                                                            ExternalId.of("EXTERNAL_CODE" ,"ProtSeller"), ExternalSchemes.markItRedCode(redcode),
                                                            DebtSeniority.SNRFOR, RestructuringClause.MM, ExternalSchemes.financialRegionId("US"),
                                                            createZdt(2013, 3, 20), createZdt(2013, 3, 21), createZdt(2014,3,20), StubType.SHORT_START,
                                                            SimpleFrequency.SEMI_ANNUAL, DayCountFactory.INSTANCE.getDayCount("Actual/360"),
                                                            BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"),
                                                            true, true, true, new InterestRateNotional(Currency.USD, 10000000), true, true, 500,
                                                            new InterestRateNotional(Currency.USD, 500000), 500, createZdt(2013,3,21), true);
  }

  private ZonedDateTime createZdt(int year, int month, int day) {
    return ZonedDateTime.of(LocalDate.of(year, month, day).atStartOfDay(), ZoneOffset.UTC);
  }


}