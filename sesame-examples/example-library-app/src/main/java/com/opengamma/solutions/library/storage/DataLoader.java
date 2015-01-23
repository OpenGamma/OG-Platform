/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.library.storage;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.FilenameUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.xml.FudgeXMLStreamReader;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.marketdatasnapshot.NamedSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.isda.credit.CreditCurveDataSnapshot;
import com.opengamma.financial.analytics.isda.credit.YieldCurveDataSnapshot;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.sesame.credit.config.RestructuringSettings;
import com.opengamma.solutions.library.tool.CreditPricerExample;
import com.opengamma.solutions.library.tool.CurveBundleProviderExample;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.JodaBeanSerialization;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Load config and market data to masters.
 */
public class DataLoader {

  private final String _path;
  private final SecurityMaster _securityMaster;
  private final ConfigMaster _configMaster;
  private final HolidayMaster _holidayMaster;
  private final MarketDataSnapshotMaster _snapshotMaster;
  private final ConventionMaster _conventionMaster;

  /**
   * Initialise the DataLoadModule
   * @param path the full path to the data resources
   * @param securityMaster security master
   * @param configMaster config master
   * @param holidayMaster holiday master
   * @param snapshotMaster snapshot master
   * @param conventionMaster convention master
   */
  public DataLoader(String path,
                    SecurityMaster securityMaster,
                    ConfigMaster configMaster,
                    HolidayMaster holidayMaster,
                    MarketDataSnapshotMaster snapshotMaster,
                    ConventionMaster conventionMaster) {
    _path = ArgumentChecker.notNull(path, "path");
    _securityMaster = ArgumentChecker.notNull(securityMaster, "securityMaster");
    _configMaster = ArgumentChecker.notNull(configMaster, "configMaster");
    _holidayMaster = ArgumentChecker.notNull(holidayMaster, "holidayMaster");
    _snapshotMaster = ArgumentChecker.notNull(snapshotMaster, "snapshotMaster");
    _conventionMaster = ArgumentChecker.notNull(conventionMaster, "conventionMaster");
  }

  /**
   * Load sample credit data for use in the {@link CreditPricerExample} example
   */
  public void populateCreditData() {

    _configMaster.add(loadConfig(RestructuringSettings.class, "SampleRestructuringMap.xml", false));
    _snapshotMaster.add(loadSnapshots(CreditCurveDataSnapshot.class, "SampleCreditCurve.xml", false));
    _snapshotMaster.add(loadSnapshots(YieldCurveDataSnapshot.class, "SampleYieldCurve.xml", false));
  }

  /**
   * Load sample multicurve data for use in the {@link CurveBundleProviderExample} example
   */
  public void populateMulticurveData() {

    _holidayMaster.add(loadHoliday(ManageableHoliday.class, "USNY.xml", false));

    _snapshotMaster.add(loadSnapshots(ManageableMarketDataSnapshot.class, "USD.xml", true));

    _securityMaster.add(loadSecurity("USDFEDFUNDS.xml"));
    _securityMaster.add(loadSecurity("USDLIBOR1M.xml"));
    _securityMaster.add(loadSecurity("USDLIBOR3M.xml"));
    _securityMaster.add(loadSecurity("USDLIBOR6M.xml"));

    _conventionMaster.add(loadConvention("USDLIBOR3M.xml"));
    _conventionMaster.add(loadConvention("USDFixed6M.xml"));
    _conventionMaster.add(loadConvention("USDFixed1Y.xml"));
    _conventionMaster.add(loadConvention("USDDepoON.xml"));
    _conventionMaster.add(loadConvention("USDLIBOR1MCmp3M_Flat.xml"));
    _conventionMaster.add(loadConvention("USDFEDFUNDSAA3M.xml"));
    _conventionMaster.add(loadConvention("USDLIBOR.xml"));
    _conventionMaster.add(loadConvention("USDFixed1Y_PayLag.xml"));
    _conventionMaster.add(loadConvention("USDFEDFUNDSCmp1Y.xml"));
    _conventionMaster.add(loadConvention("USDFEDFUNDS.xml"));
    _conventionMaster.add(loadConvention("USDLIBOR1M.xml"));
    _conventionMaster.add(loadConvention("USDLIBOR3MCmp6M_Flat.xml"));
    _conventionMaster.add(loadConvention("USDLIBOR6M.xml"));

    _configMaster.add(loadConfig(CurrencyMatrix.class, "CurrencyMatrix.xml", true));
    _configMaster.add(loadConfig(CurveNodeIdMapper.class, "USD-OIS-BBG.xml", true));
    _configMaster.add(loadConfig(CurveNodeIdMapper.class, "USD-LIBOR1M-BBG.xml", true));
    _configMaster.add(loadConfig(CurveNodeIdMapper.class, "USD-LIBOR3M-BBG.xml", true));
    _configMaster.add(loadConfig(CurveNodeIdMapper.class, "USD-Depo-BBG.xml", true));
    _configMaster.add(loadConfig(CurveNodeIdMapper.class, "USD-LIBOR-BS-1M-3M-BBG.xml", true));
    _configMaster.add(loadConfig(CurveNodeIdMapper.class, "USD-LIBOR-BS-3M-6M-BBG.xml", true));
    _configMaster.add(loadConfig(CurveNodeIdMapper.class, "USD-FFF-FFS-BBG.xml", true));
    _configMaster.add(loadConfig(CurveNodeIdMapper.class, "USD-LIBOR6M-BBG.xml", true));
    _configMaster.add(loadConfig(InterpolatedCurveDefinition.class, "USD-OIS-FFS-NCS.xml", false));
    _configMaster.add(loadConfig(InterpolatedCurveDefinition.class, "USD-FRAL3M-IRSL3M-NCS.xml", false));
    _configMaster.add(loadConfig(InterpolatedCurveDefinition.class, "USD-FRAL6M-BSL3ML6M-NCS.xml", false));
    _configMaster.add(loadConfig(InterpolatedCurveDefinition.class, "USD-IRSL1M-BSL1ML3M-NCS.xml", false));
    _configMaster.add(loadConfig(CurveConstructionConfiguration.class,
                                 "USD_FF_DSCON-OISFFS_L3M-FRAIRS_L1M-BS_L6M-BS.xml",
                                 false));

  }

  private SecurityDocument loadSecurity(String path) {
    return new SecurityDocument(loadBean(ManageableSecurity.class, "/securities/" + path, false));
  }

  private HolidayDocument loadHoliday(Class clazz, String file, boolean isFudge) {
    return new HolidayDocument((Holiday) loadBean(clazz, "/holidays/" + file, isFudge));
  }

  private MarketDataSnapshotDocument loadSnapshots(Class clazz, String file, boolean isFudge) {
    return new MarketDataSnapshotDocument((NamedSnapshot) loadBean(clazz, "/snapshots/" + file, isFudge));
  }

  private ConfigDocument loadConfig(Class clazz, String file, boolean isFudge) {
    String name = FilenameUtils.removeExtension(file);
    return new ConfigDocument(ConfigItem.of(loadBean(clazz, "/configs/" + file, isFudge), name, clazz));
  }

  private ConventionDocument loadConvention(String file) {
    return new ConventionDocument(loadBean(ManageableConvention.class, "/conventions/" + file, false));
  }

  private <T> T loadBean(Class<T> clazz, String path, boolean isFudge) {
    InputStream resource = ClassLoader.getSystemResourceAsStream(_path + path);
    if (!isFudge) {
      return JodaBeanSerialization.deserializer().xmlReader().read(resource, clazz);
    } else {
      InputStreamReader reader = new InputStreamReader(resource);
      FudgeContext context = OpenGammaFudgeContext.getInstance();
      FudgeXMLStreamReader streamReader = new FudgeXMLStreamReader(context, reader);
      // Don't close fudgeMsgReader; the caller will close the stream later
      @SuppressWarnings("resource")
      FudgeMsgReader fudgeMsgReader = new FudgeMsgReader(streamReader);
      FudgeMsg msg = fudgeMsgReader.nextMessage();
      return new FudgeDeserializer(context).fudgeMsgToObject(clazz, msg);
    }
  }
}

