/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.xml.FudgeXMLStreamReader;
import org.joda.beans.Bean;
import org.joda.beans.ser.JodaBeanSer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Loads in a file containing either a single JodaXML or FudgeXML encoded config and updates the config master.
 * Can be provided with a hint type if the JodaXML messages don't contain a type attribute on the bean element.
 */
public class SingleConfigLoader {
  private static final Logger s_logger = LoggerFactory.getLogger(SingleConfigLoader.class);
  private ConfigMaster _configMaster;

  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  public SingleConfigLoader(ConfigMaster configMaster) {
    _configMaster = configMaster;
  }
  
  public <T> void loadConfig(InputStream is, Class<T> hintType) {
    T config = JodaBeanSer.PRETTY.xmlReader().read(is, hintType);
    if (config instanceof Bean) {
      ConfigItem<T> item = ConfigItem.of(config);
      ConfigMasterUtils.storeByName(_configMaster, item);          
    } else {
      s_logger.error("Unsupported type {} is not a JodaBean", config.getClass());
    }
  }
  
  public void loadConfig(InputStream is) {
    Object config = JodaBeanSer.PRETTY.xmlReader().read(is);
    if (config instanceof Bean) {
      ConfigItem<?> item = ConfigItem.of(config);
      ConfigMasterUtils.storeByName(_configMaster, item);          
    } else {
      s_logger.error("Unsupported type {} is not a JodaBean", config.getClass());
    }
  }
  
  public <T> void loadFudgeConfig(InputStream is) {
    @SuppressWarnings("resource")
    final FudgeMsgReader fmr = new FudgeMsgReader(new FudgeXMLStreamReader(s_fudgeContext, new InputStreamReader(is)));
    final FudgeMsg message = fmr.nextMessage();
    Object config = s_fudgeContext.fromFudgeMsg(message);
    ConfigItem<?> item = ConfigItem.of(config);
    ConfigMasterUtils.storeByName(_configMaster, item);          
  }
}
