/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit test for ConfigDBInstrumentExposuresProvider.
 */
@Test(groups = TestGroup.UNIT)
public class ConfigDBInstrumentExposuresProviderTest {

  @Test
  public void testEmptyCurveConfigs() {
    ConfigMaster configMaster = new InMemoryConfigMaster();
    ConfigSource configSource = new MasterConfigSource(configMaster);
    SecuritySource securitySource = new MasterSecuritySource(new InMemorySecurityMaster());

    String name = "test";
    List<String> exposureFunctions = Lists.newArrayList(CurrencyExposureFunction.NAME);
    Map<ExternalId, String> idsToNames = Maps.newHashMap();
    ExposureFunctions exposures = new ExposureFunctions(name, exposureFunctions, idsToNames);
    ConfigMasterUtils.storeByName(configMaster, ConfigItem.of(exposures));

    ConfigDBInstrumentExposuresProvider provider = new ConfigDBInstrumentExposuresProvider(configSource, securitySource, VersionCorrection.LATEST);

    FRASecurity security = ExposureFunctionTestHelper.getFRASecurity();
    Trade trade = new SimpleTrade(security, BigDecimal.ONE, new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "TEST")), LocalDate.now(), OffsetTime.now());
    try {
      provider.getCurveConstructionConfigurationsForConfig(name, trade);
      fail("Expected exception for empty curve configs");
    } catch (OpenGammaRuntimeException e) {
      // test has passed
    }
  }

  @Test
  public void testMultipleCurveConfigs() {
    ConfigMaster configMaster = new InMemoryConfigMaster();
    ConfigSource configSource = new MasterConfigSource(configMaster);
    SecuritySource securitySource = new MasterSecuritySource(new InMemorySecurityMaster());

    FRASecurity security = ExposureFunctionTestHelper.getFRASecurity();
    
    String name = "test";
    List<String> exposureFunctions = Lists.newArrayList(SecurityTypeExposureFunction.NAME, CurrencyExposureFunction.NAME);
    Map<ExternalId, String> idsToNames = new HashMap<>();
    String securityTypeCurveConfig = "SecurityTypeConfig";
    idsToNames.put(ExternalId.of(ExposureFunction.SECURITY_IDENTIFIER, security.getSecurityType()), securityTypeCurveConfig);
    String currencyCurveConfig = "CurrencyConfig";
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, security.getCurrency().getCode()), currencyCurveConfig);
    ExposureFunctions exposures = new ExposureFunctions(name, exposureFunctions, idsToNames);
    ConfigMasterUtils.storeByName(configMaster, ConfigItem.of(exposures));
    
    ConfigDBInstrumentExposuresProvider provider = new ConfigDBInstrumentExposuresProvider(configSource, securitySource, VersionCorrection.LATEST);
    
    Trade trade = new SimpleTrade(security, BigDecimal.ONE, new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "TEST")), LocalDate.now(), OffsetTime.now());
    Set<String> curveConfigs = provider.getCurveConstructionConfigurationsForConfig(name, trade);
    assertEquals("Expected single curve config", 1, curveConfigs.size());
    assertTrue("Expected configs to contain security type config", curveConfigs.contains(securityTypeCurveConfig));
    assertFalse("Expected configs to not contain currency config", curveConfigs.contains(currencyCurveConfig));
  }
}
