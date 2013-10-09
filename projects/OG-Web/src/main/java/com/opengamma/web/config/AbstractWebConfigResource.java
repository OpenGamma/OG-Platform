/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.config;

import java.io.CharArrayReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map.Entry;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

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

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveSpecification;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.web.AbstractPerRequestWebResource;
import com.opengamma.web.WebHomeUris;
import com.opengamma.web.json.CurveSpecificationBuilderConfigurationJSONBuilder;
import com.opengamma.web.json.FXForwardCurveDefinitionJSONBuilder;
import com.opengamma.web.json.FXForwardCurveSpecificationJSONBuilder;
import com.opengamma.web.json.FudgeMsgJSONReader;
import com.opengamma.web.json.MultiCurveCalculationConfigJSONBuilder;
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
   * HTML ftl directory
   */
  protected static final String HTML_DIR = "configs/html/";
  /**
   * JSON ftl directory
   */
  protected static final String JSON_DIR = "configs/json/";
  
  private static final int INDENTATION_SIZE = 4;
  
  /**
   * Logger.
   */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractWebConfigResource.class);
  
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();
  
  /**
   * The Config Types provider
   */
  private final ConfigTypesProvider _configTypesProvider = ConfigTypesProvider.getInstance();
  
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
    initializeMetaData();
    initializeJSONBuilders();
  }

  //init meta-data
  private void initializeMetaData() {
    for (Entry<String, Class<?>> entry : _configTypesProvider.getConfigTypeMap().entrySet()) {
      data().getTypeMap().put(entry.getKey(), entry.getValue());
    }
  }
  
  private void initializeJSONBuilders() {
    data().getJsonBuilderMap().put(ViewDefinition.class, ViewDefinitionJSONBuilder.INSTANCE);
    data().getJsonBuilderMap().put(YieldCurveDefinition.class, YieldCurveDefinitionJSONBuilder.INSTANCE);
    data().getJsonBuilderMap().put(CurveSpecificationBuilderConfiguration.class, CurveSpecificationBuilderConfigurationJSONBuilder.INSTANCE);
    data().getJsonBuilderMap().put(VolatilityCubeDefinition.class, VolatilityCubeDefinitionJSONBuilder.INSTANCE);
    data().getJsonBuilderMap().put(VolatilitySurfaceDefinition.class, VolatilitySurfaceDefinitionJSONBuilder.INSTANCE);
    data().getJsonBuilderMap().put(VolatilitySurfaceSpecification.class, VolatilitySurfaceSpecificationJSONBuilder.INSTANCE);
    data().getJsonBuilderMap().put(FXForwardCurveDefinition.class, FXForwardCurveDefinitionJSONBuilder.INSTANCE);
    data().getJsonBuilderMap().put(FXForwardCurveSpecification.class, FXForwardCurveSpecificationJSONBuilder.INSTANCE);
    data().getJsonBuilderMap().put(MultiCurveCalculationConfig.class, MultiCurveCalculationConfigJSONBuilder.INSTANCE);
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
   * Utility method to convert XML to configuration object.
   * 
   * @param <T> the type to parse to
   * @param xml  the configuration xml, not null
   * @param type  the type to parse to, not null
   * @return the configuration object
   */
  protected <T> T parseXML(String xml, Class<T> type) {
    final CharArrayReader car = new CharArrayReader(xml.toCharArray());
    @SuppressWarnings("resource")
    final FudgeMsgReader fmr = new FudgeMsgReader(new FudgeXMLStreamReader(getFudgeContext(), car));
    final FudgeMsg message = fmr.nextMessage();
    return getFudgeContext().fromFudgeMsg(type, message);
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

  protected String createXML(ConfigDocument doc) {
    // get xml and pretty print it
    FudgeMsgEnvelope msg = getFudgeContext().toFudgeMsg(doc.getConfig().getValue());
    s_logger.debug("config doc {} converted to fudge {}", doc.getUniqueId(), msg);
    StringWriter buf = new StringWriter(1024);  
    @SuppressWarnings("resource")
    FudgeMsgWriter writer = new FudgeMsgWriter(new FudgeXMLStreamWriter(getFudgeContext(), buf));
    writer.writeMessageEnvelope(msg);
    s_logger.debug("config doc {} converted to xmk {}", doc.getUniqueId(), buf.toString());
    try {
      return prettyXML(buf.toString(), INDENTATION_SIZE);
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }
  
  private static String prettyXML(String input, int indent) throws TransformerException {
    Source xmlInput = new StreamSource(new StringReader(input));
    StreamResult xmlOutput = new StreamResult(new StringWriter());
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    transformerFactory.setAttribute("indent-number", indent);
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(xmlInput, xmlOutput);
    return xmlOutput.getWriter().toString();
  }

  /**
   * Gets the fudgeContext.
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Gets the configTypesProvider.
   * @return the configTypesProvider
   */
  public ConfigTypesProvider getConfigTypesProvider() {
    return _configTypesProvider;
  }
  
}
