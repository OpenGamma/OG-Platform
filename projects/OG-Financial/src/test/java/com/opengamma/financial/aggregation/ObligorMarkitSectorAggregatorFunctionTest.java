/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma
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
import com.opengamma.core.legalentity.Rating;
import com.opengamma.core.obligor.CreditRating;
import com.opengamma.core.obligor.CreditRatingFitch;
import com.opengamma.core.obligor.CreditRatingMoodys;
import com.opengamma.core.obligor.CreditRatingStandardAndPoors;
import com.opengamma.core.obligor.Region;
import com.opengamma.core.obligor.Sector;
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
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.master.legalentity.impl.InMemoryLegalEntityMaster;
import com.opengamma.master.legalentity.impl.MasterLegalEntitySource;
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
  private InMemoryLegalEntityMaster _legalEntityMaster;

  @BeforeMethod
  public void setup() {

    _securityMaster = new InMemorySecurityMaster();
    _legalEntityMaster = new InMemoryLegalEntityMaster();

    _aggregator = new ObligorMarkitSectorAggregationFunction(
        new MasterSecuritySource(_securityMaster), new MasterLegalEntitySource(_legalEntityMaster));
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
  public void testObligorWithMatchingRedCodeIsUsed() {

    ExternalIdBundle bundle = ExternalIdBundle.of(ExternalId.of(ExternalSchemes.MARKIT_RED_CODE, "39FF64"), ExternalId.of("TICKEER", "Ticker"));
    ManageableLegalEntity legalEntity = new ManageableLegalEntity("ShortName", bundle);
    legalEntity.getAttributes().put("region", Region.NORTHAMERICA.name());
    legalEntity.getAttributes().put("sector", Sector.FINANCIALS.name());
    legalEntity.getAttributes().put("country", "US");

    _legalEntityMaster.add(new LegalEntityDocument(legalEntity));

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
                                                            SimpleFrequency.SEMI_ANNUAL, DayCounts.ACT_360,
                                                            BusinessDayConventions.FOLLOWING,
                                                            true, true, true, new InterestRateNotional(Currency.USD, 10000000), true, true, 500,
                                                            new InterestRateNotional(Currency.USD, 500000), 500, createZdt(2013,3,21), true);
  }

  private ZonedDateTime createZdt(int year, int month, int day) {
    return ZonedDateTime.of(LocalDate.of(year, month, day).atStartOfDay(), ZoneOffset.UTC);
  }


}