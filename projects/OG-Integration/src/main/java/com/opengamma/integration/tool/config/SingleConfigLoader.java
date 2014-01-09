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
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Loads in a file containing either a single JodaXML or FudgeXML encoded config and updates the config master.
 * Can be provided with a hint type if the JodaXML messages don't contain a type attribute on the bean element.
 */
public class SingleConfigLoader {
  private static final Logger s_logger = LoggerFactory.getLogger(SingleConfigLoader.class);
  private ConfigMaster _configMaster;
  private ConventionMaster _conventionMaster;

  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  public SingleConfigLoader(ConfigMaster configMaster, ConventionMaster conventionMaster) {
    _configMaster = configMaster;
    _conventionMaster = conventionMaster;
  }
  
  private ManageableConvention addOrUpdateConvention(ManageableConvention convention) {
    ConventionSearchRequest searchReq = new ConventionSearchRequest(convention.getExternalIdBundle());
    ConventionSearchResult searchResult = _conventionMaster.search(searchReq);
    ConventionDocument match = null;
    for (ConventionDocument doc : searchResult.getDocuments()) {
      if (doc.getConvention().getConventionType().equals(convention.getConventionType())) {
        if (match == null) {
          match = doc;
        } else {
          s_logger.warn("Found more than one match for {} with type {}, changing first one", convention.getExternalIdBundle(), convention.getConventionType());
        }
      }
    }
    if (match != null) {
      s_logger.info("Found existing item, updating it");
      match.setConvention(convention);
      return _conventionMaster.update(match).getConvention();
    } else {
      s_logger.info("No existing convention, creating a new one");
      ConventionDocument doc = new ConventionDocument(convention);
      return _conventionMaster.add(doc).getConvention();
    }
  }
  
  public <T> void loadConfig(InputStream is, Class<T> hintType) {
    T config = JodaBeanSer.PRETTY.xmlReader().read(is, hintType);
    if (config instanceof ManageableConvention) {
      addOrUpdateConvention((ManageableConvention) config);
    } else if (config instanceof Bean) {
      ConfigItem<T> item = ConfigItem.of(config);
      ConfigMasterUtils.storeByName(_configMaster, item);          
    } else {
      s_logger.error("Unsupported type {} is not a JodaBean", config.getClass());
    }
  }
  
  public void loadConfig(InputStream is) {
    Object config = JodaBeanSer.PRETTY.xmlReader().read(is);
    if (config instanceof ManageableConvention) {
      addOrUpdateConvention((ManageableConvention) config);
    } else if (config instanceof Bean) {
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
