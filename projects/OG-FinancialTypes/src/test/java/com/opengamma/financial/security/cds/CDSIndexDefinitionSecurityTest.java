/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.cds;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.credit.IndexCDSDefinitionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test CDSIndexDefinitionSecurity
 */
@Test(groups = TestGroup.UNIT)
public class CDSIndexDefinitionSecurityTest {

  public static final String IG_INDEX = "CDX.NA.IG.23-V1";
  private static final Currency USD = Currency.USD;
  private static final ExternalIdBundle CDXD_BUNDLE = ExternalIdBundle.of("Sample", IG_INDEX);
  private static final Set<ExternalId> USNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));

  @Test
  public void testBadIndexFactor() {

    try {
        new IndexCDSDefinitionSecurity(CDXD_BUNDLE,
                                       IG_INDEX,
                                       LocalDate.of(2014, 9, 20),
                                       "V1",
                                       "23",
                                       "IG",
                                       USD,
                                       0.4,
                                       SimpleFrequency.QUARTERLY,
                                       0.01,
                                       CDSIndexTerms.of(Tenor.FIVE_YEARS),
                                       USNY,
                                       BusinessDayConventions.MODIFIED_FOLLOWING,
                                       2d);
    } catch(Exception e) {
      assertThat(e instanceof IllegalArgumentException, is(true));
    }

  }

  @Test
  public void testGoodIndexFactor() {

    IndexCDSDefinitionSecurity security = new IndexCDSDefinitionSecurity(CDXD_BUNDLE,
                                                                         IG_INDEX,
                                                                         LocalDate.of(2014, 9, 20),
                                                                         "V1",
                                                                         "23",
                                                                         "IG",
                                                                         USD,
                                                                         0.4,
                                                                         SimpleFrequency.QUARTERLY,
                                                                         0.01,
                                                                         CDSIndexTerms.of(Tenor.FIVE_YEARS),
                                                                         USNY,
                                                                         BusinessDayConventions.MODIFIED_FOLLOWING,
                                                                         .98d);

    ImmutableSortedSet<CreditDefaultSwapIndexComponent> components = security.getComponents().getComponents();
    assertThat(components.size(), is(1));
    assertThat(components.first().getWeight(), is(.98d));
    assertThat(components.first().getName(), is(IG_INDEX));
    assertThat(components.first().getObligorRedCode(), is(CDXD_BUNDLE.getExternalIds().first()));
  }

}
