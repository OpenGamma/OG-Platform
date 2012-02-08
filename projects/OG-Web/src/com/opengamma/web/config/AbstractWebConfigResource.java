/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.config;

import java.io.CharArrayReader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.xml.FudgeXMLStreamReader;
import org.fudgemsg.wire.xml.FudgeXMLStreamWriter;
import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMetaDataRequest;
import com.opengamma.master.config.ConfigMetaDataResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.web.AbstractPerRequestWebResource;
import com.opengamma.web.WebHomeUris;
import com.opengamma.web.json.CurveSpecificationBuilderConfigurationJSONBuilder;
import com.opengamma.web.json.FudgeMsgJSONReader;
import com.opengamma.web.json.ViewDefinitionJSONBuilder;
import com.opengamma.web.json.VolatilityCubeDefinitionJSONBuilder;
import com.opengamma.web.json.VolatilitySurfaceDefinitionJSONBuilder;
import com.opengamma.web.json.VolatilitySurfaceSpecificationJSONBuilder;
import com.opengamma.web.json.YieldCurveDefinitionJSONBuilder;

/**
 * Abstract base class for RESTful config resources.
 * 
 */
public abstract class AbstractWebConfigResource extends AbstractPerRequestWebResource {
  
  /**
   * Logger.
   */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractWebConfigResource.class);
  
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();

  /**
   * The backing bean.
   */
  private final WebConfigData _data;

  /**
   * Creates the resource.
   * @param configMaster  the config master, not null
   */
  protected AbstractWebConfigResource(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    _data = new WebConfigData();
    data().setConfigMaster(configMaster);
    
    // init meta-data
    ConfigMetaDataResult metaData = configMaster.metaData(new ConfigMetaDataRequest());
    for (Class<?> configType : metaData.getConfigTypes()) {
      data().getTypeMap().put(configType.getSimpleName(), configType);
    }
    initializeJSONBuilders();
  }

  private void initializeJSONBuilders() {
    data().getJsonBuilderMap().put(ViewDefinition.class, ViewDefinitionJSONBuilder.INSTANCE);
    data().getJsonBuilderMap().put(YieldCurveDefinition.class, YieldCurveDefinitionJSONBuilder.INSTANCE);
    data().getJsonBuilderMap().put(CurveSpecificationBuilderConfiguration.class, CurveSpecificationBuilderConfigurationJSONBuilder.INSTANCE);
    data().getJsonBuilderMap().put(VolatilityCubeDefinition.class, VolatilityCubeDefinitionJSONBuilder.INSTANCE);
    data().getJsonBuilderMap().put(VolatilitySurfaceDefinition.class, VolatilitySurfaceDefinitionJSONBuilder.INSTANCE);
    data().getJsonBuilderMap().put(VolatilitySurfaceSpecification.class, VolatilitySurfaceSpecificationJSONBuilder.INSTANCE);
  }

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  protected AbstractWebConfigResource(final AbstractWebConfigResource parent) {
    super(parent);
    _data = parent._data;
  }
  
  /**
   * Setter used to inject the URIInfo.
   * This is a roundabout approach, because Spring and JSR-311 injection clash.
   * DO NOT CALL THIS METHOD DIRECTLY.
   * @param uriInfo  the URI info, not null
   */
  @Context
  public void setUriInfo(final UriInfo uriInfo) {
    data().setUriInfo(uriInfo);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = getFreemarker().createRootData();
    out.put("homeUris", new WebHomeUris(data().getUriInfo()));
    out.put("uris", new WebConfigUris(data()));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the backing bean.
   * @return the backing bean, not null
   */
  protected WebConfigData data() {
    return _data;
  }
  
  /**
   * Utility method to convert XML to configuration object
   * @param xml the configuration xml
   * @return the configuration object
   */
  protected Object parseXML(String xml) {
    final CharArrayReader car = new CharArrayReader(xml.toCharArray());
    final FudgeMsgReader fmr = new FudgeMsgReader(new FudgeXMLStreamReader(getFudgeContext(), car));
    final FudgeMsg message = fmr.nextMessage();
    return getFudgeContext().fromFudgeMsg(message);
  }
    
  /**
   * Converts JSON to configuration object
   * 
   * @param json the config document in JSON
   * @return the configuration object
   */
  protected Object parseJSON(String json) {
    s_logger.debug("converting JSON to java: " + json);
    FudgeMsgJSONReader fudgeJSONReader = new FudgeMsgJSONReader(getFudgeContext(), new StringReader(json));
    
    FudgeMsg fudgeMsg = fudgeJSONReader.readMessage();
    s_logger.debug("converted FudgeMsg: " + fudgeMsg);
    
    return new FudgeDeserializer(getFudgeContext()).fudgeMsgToObject(fudgeMsg);
    
  }

  protected String createXML(ConfigDocument<?> doc) {
    String configXML = null;
    // get xml and pretty print it
    FudgeMsgEnvelope msg = getFudgeContext().toFudgeMsg(doc.getValue());
    s_logger.debug("config doc {} converted to fudge {}", doc.getUniqueId(), msg);
    StringWriter buf = new StringWriter(1024);  
    FudgeMsgWriter writer = new FudgeMsgWriter(new FudgeXMLStreamWriter(getFudgeContext(), buf));
    writer.writeMessageEnvelope(msg);
    s_logger.debug("config doc {} converted to xmk {}", doc.getUniqueId(), buf.toString());
    try {
      DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document xmlDoc = db.parse(new InputSource(new StringReader(buf.toString())));
      xmlDoc.getDocumentElement().normalize();
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      DOMSource source = new DOMSource(xmlDoc);
      buf = new StringWriter(1024);
      StreamResult result =  new StreamResult(buf);
      transformer.transform(source, result);
      configXML = buf.toString();
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
    return configXML;
  }

  /**
   * Gets the fudgeContext.
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

}
