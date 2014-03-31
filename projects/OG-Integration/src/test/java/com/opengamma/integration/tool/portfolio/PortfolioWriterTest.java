/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class PortfolioWriterTest {

  private MasterRecorder _recorder;

  @BeforeMethod
  public void setUp() {
    _recorder = new MasterRecorder();
  }


  @Test
  public void testNewPortfolioInsertsSecurityPositionPortfolio() {

    PortfolioWriter persister = new PortfolioWriter(true, mockPortfolioMaster(), mockPositionMaster(),
                                                          mockSecurityMaster());

    String portfolioName = "TestPortfolio";
    SimplePortfolioNode root = new SimplePortfolioNode(portfolioName);
    ExternalIdBundle securityKey = ExternalIdBundle.of("TEST", "1234");
    ManageableSecurity security = new ManageableSecurity("SEC_TYPE_TEST");
    security.setName("TestSec");
    security.setExternalIdBundle(securityKey);
    Set<ManageableSecurity> securities = ImmutableSet.of(security);
    root.addPosition(new SimplePosition(BigDecimal.valueOf(1000), securityKey));
    SimplePortfolio pf = new SimplePortfolio(portfolioName, root);

    persister.write(pf, securities);

    assertThat(_recorder._securities.size(), is(1));
    assertThat(_recorder._securities.get(0).getExternalIdBundle(), is(securityKey));
    assertThat(_recorder._positions.size(), is(1));
    assertThat(_recorder._positions.get(0).getSecurityLink().getExternalId(), is(securityKey));
    assertThat(_recorder._positions.get(0).getQuantity(), is(BigDecimal.valueOf(1000)));
    assertThat(_recorder.countPortfolioAdditions(), is(1));
    assertThat(_recorder._portfolios.get(0).getPortfolio().getName(), is(portfolioName));
  }

  @Test
  public void testNewPortfolioWithExistingSecurity() {

    // Setup the security to appear as if we already have it
    ExternalIdBundle securityKey = ExternalIdBundle.of("TEST", "1234");
    ManageableSecurity security = new ManageableSecurity("SEC_TYPE_TEST");
    security.setName("TestSec");
    security.setExternalIdBundle(securityKey);

    PortfolioWriter persister = new PortfolioWriter(true, mockPortfolioMaster(), mockPositionMaster(),
                                                          mockSecurityMaster(security));

    String portfolioName = "TestPortfolio";
    SimplePortfolioNode root = new SimplePortfolioNode(portfolioName);
    Set<ManageableSecurity> securities = ImmutableSet.of(security);
    root.addPosition(new SimplePosition(BigDecimal.valueOf(1000), securityKey));
    SimplePortfolio pf = new SimplePortfolio(portfolioName, root);

    persister.write(pf, securities);

    // No security inserted as we already have it
    assertThat(_recorder._securities.size(), is(0));
    assertThat(_recorder._positions.size(), is(1));
    assertThat(_recorder._positions.get(0).getSecurityLink().getExternalId(), is(securityKey));
    assertThat(_recorder._positions.get(0).getQuantity(), is(BigDecimal.valueOf(1000)));
    assertThat(_recorder.countPortfolioAdditions(), is(1));
    assertThat(_recorder._portfolios.get(0).getPortfolio().getName(), is(portfolioName));
  }

  @Test
  public void testReplacementOfExistingPortfolio() {

    String portfolioName = "TestPortfolio";
    ManageablePortfolio existing = new ManageablePortfolio(portfolioName);

    PortfolioWriter persister = new PortfolioWriter(true, mockPortfolioMaster(existing), mockPositionMaster(),
                                                          mockSecurityMaster());

    SimplePortfolioNode root = new SimplePortfolioNode(portfolioName);
    ExternalIdBundle securityKey = ExternalIdBundle.of("TEST", "1234");
    ManageableSecurity security = new ManageableSecurity("SEC_TYPE_TEST");
    security.setName("TestSec");
    security.setExternalIdBundle(securityKey);
    Set<ManageableSecurity> securities = ImmutableSet.of(security);
    root.addPosition(new SimplePosition(BigDecimal.valueOf(1000), securityKey));
    SimplePortfolio pf = new SimplePortfolio(portfolioName, root);

    persister.write(pf, securities);

    assertThat(_recorder._securities.size(), is(1));
    assertThat(_recorder._securities.get(0).getExternalIdBundle(), is(securityKey));
    assertThat(_recorder._positions.size(), is(1));
    assertThat(_recorder._positions.get(0).getSecurityLink().getExternalId(), is(securityKey));
    assertThat(_recorder._positions.get(0).getQuantity(), is(BigDecimal.valueOf(1000)));
    assertThat(_recorder.countPortfolioUpdates(), is(1));
    assertThat(_recorder._portfolios.get(0).getPortfolio().getName(), is(portfolioName));
  }

  private PositionMaster mockPositionMaster() {
    final PositionMaster mock = mock(PositionMaster.class);

    when(mock.add(Matchers.<PositionDocument>any())).thenAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        ManageablePosition position = new ManageablePosition();
        position.setUniqueId(UniqueId.of("POSN", "1"));
        _recorder.recordPosition(((PositionDocument) invocation.getArguments()[0]).getPosition());
        return new PositionDocument(position);
      }
    });
    return mock;
  }

  private SecurityMaster mockSecurityMaster(final ManageableSecurity... existingSecurities) {

    // Create a set of securities that are already stored in the master so
    // we respond to queries about them
    final Map<ExternalIdBundle, ManageableSecurity> existing = new HashMap<>();
    for (ManageableSecurity security : existingSecurities) {
      existing.put(security.getExternalIdBundle(), security);
    }

    SecurityMaster mock = mock(SecurityMaster.class);
    when(mock.search(Matchers.<SecuritySearchRequest>any())).thenAnswer(new Answer<SecuritySearchResult>() {
      @Override
      public SecuritySearchResult answer(InvocationOnMock invocation) throws Throwable {
        SecuritySearchRequest request = (SecuritySearchRequest) invocation.getArguments()[0];

        final ExternalIdBundle idBundle = ExternalIdBundle.of(request.getExternalIdSearch());
        return existing.containsKey(idBundle) ?
            new SecuritySearchResult(ImmutableList.of(new SecurityDocument(existing.get(idBundle)))) :
            new SecuritySearchResult();
      }
    });
    when(mock.add(Matchers.<SecurityDocument>any())).thenAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        _recorder.recordSecurity(((SecurityDocument) invocation.getArguments()[0]).getSecurity());
        return new SecurityDocument(new ManageableSecurity());
      }
    });
    return mock;
  }

  private PortfolioMaster mockPortfolioMaster(ManageablePortfolio... existingPortfolios) {
    final Map<String, ManageablePortfolio> existing = new HashMap<>();
    for (ManageablePortfolio pf : existingPortfolios) {
      pf.setUniqueId(UniqueId.of("TEST_PORTFOLIO", pf.getName()));
      existing.put(pf.getName(), pf);
    }
    final PortfolioMaster mock = mock(PortfolioMaster.class);
    when(mock.search(Matchers.<PortfolioSearchRequest>any())).thenAnswer(new Answer<PortfolioSearchResult>() {
      @Override
      public PortfolioSearchResult answer(InvocationOnMock invocation) throws Throwable {
        PortfolioSearchRequest request = (PortfolioSearchRequest) invocation.getArguments()[0];
        return existing.containsKey(request.getName()) ?
            new PortfolioSearchResult(ImmutableList.of(new PortfolioDocument(existing.get(request.getName())))) :
            new PortfolioSearchResult();
      }
    });
    when(mock.add(Matchers.<PortfolioDocument>any())).thenAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        _recorder.recordPortfolio(((PortfolioDocument) invocation.getArguments()[0]));
        return new PortfolioDocument(new ManageablePortfolio());
      }
    });
    return mock;
  }

  private class MasterRecorder {

    private final List<ManageableSecurity> _securities = new ArrayList<>();
    private final List<ManageablePosition> _positions = new ArrayList<>();
    private final List<PortfolioDocument> _portfolios = new ArrayList<>();

    public void recordSecurity(ManageableSecurity security) {
      _securities.add(security);
    }
    public void recordPosition(ManageablePosition position) {
      _positions.add(position);
    }
    public void recordPortfolio(PortfolioDocument portfolio) {
      _portfolios.add(portfolio);
    }

    public int countPortfolioUpdates() {
      int count = 0;
      for (PortfolioDocument portfolioDocument : _portfolios) {
        if (portfolioDocument.getUniqueId() != null) {
          count++;
        }
      }
      return count;
    }

    public int countPortfolioAdditions() {
      return _portfolios.size() - countPortfolioUpdates();
    }


  }



}
