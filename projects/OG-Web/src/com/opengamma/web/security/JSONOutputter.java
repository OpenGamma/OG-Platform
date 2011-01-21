/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.io.CharArrayWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgWriter;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.json.FudgeJSONStreamWriter;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * Outputs JSON representation of security object
 */
public class JSONOutputter {
  private static final Logger s_logger = LoggerFactory.getLogger(JSONOutputter.class);
  
  private static final List<String> DATA_FIELDS = Lists.newArrayList("id", "name");
  private static final String DATA_FIELDS_KEY = "dataFields";
  private static final String HEADER_KEY = "header";
  private static final String DATA_KEY = "data";
  
  private static final Map<String, List<String>> s_dataFieldsMap = new HashMap<String, List<String>>();
  static {
    s_dataFieldsMap.put(DATA_FIELDS_KEY, DATA_FIELDS);
  }
  
  private final FudgeContext _fudgeContext;
 
  /**
   * Creates JsonOutputter
   * 
   * @param fudgeContext the fudge context
   */
  public JSONOutputter(FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }
  
  /**
   * Creates JsonOutputter
   * 
   */
  public JSONOutputter() {
    this(OpenGammaFudgeContext.getInstance());
  }

  public String buildSecuity(ManageableSecurity security) {
    ArgumentChecker.notNull(security, "ManageableSecurity");
    
    FudgeFieldContainer fudgeMsg = security.toFudgeMsg(_fudgeContext);
    
    s_logger.debug("converting message to JSON: {}", security.toString());
    String result = null;
    final CharArrayWriter caw = new CharArrayWriter();
    final FudgeMsgWriter fmw = new FudgeMsgWriter(new FudgeJSONStreamWriter(_fudgeContext, caw));
    
    fmw.writeMessage(removeIdentifiersField(fudgeMsg));
    
    try {
      JSONObject templatedata = new JSONObject(caw.toString());
      JSONObject identifiers = convertIdentierToJSON(fudgeMsg);
      
      JSONObject jsonObject = new JSONObject().put("templateData", templatedata);
      jsonObject.put("identifiers", identifiers);
      result = jsonObject.toString();
    } catch (JSONException ex) {
      throw new OpenGammaRuntimeException("Error creating JSON from FudgeMessage", ex);
    }
    return result;
    
  }
  
  public String buildSecuritySearchResult(SecuritySearchResult searchResult) {
    Map<String, Object> jsonMap = new HashMap<String, Object>();
    
    List<String> dataList = new ArrayList<String>();
    for (SecurityDocument securityDocument : searchResult.getDocuments()) {
      String name = securityDocument.getSecurity().getName();
      String uniqueId = securityDocument.getUniqueId().getValue();
      dataList.add(uniqueId + "|" + name);
    }
    
    jsonMap.put(HEADER_KEY, s_dataFieldsMap);
    jsonMap.put(DATA_KEY, dataList);
    return new JSONObject(jsonMap).toString();
  }
  
  private JSONObject convertIdentierToJSON(FudgeFieldContainer fudgeMsg) {
    Map<String, String> jsonMap = new HashMap<String, String>();
    FudgeDeserializationContext fudgeDeserializationContext = new FudgeDeserializationContext(_fudgeContext);
    FudgeField fudgeField = fudgeMsg.getByName(ManageableSecurity.IDENTIFIERS_KEY);
    IdentifierBundle identifierBundle = fudgeDeserializationContext.fieldValueToObject(IdentifierBundle.class, fudgeField);
    for (Identifier identifier : identifierBundle) {
      jsonMap.put(identifier.getScheme().getName(), formatIdentierOutput(identifier));
    }
    return new JSONObject(jsonMap);
  }

  private String formatIdentierOutput(Identifier identifier) {
    StringBuffer buf = new StringBuffer();
    buf.append(identifier.getScheme().getName());
    buf.append("-");
    buf.append(identifier.getValue());
    return buf.toString();
  }

  private FudgeFieldContainer removeIdentifiersField(FudgeFieldContainer fudgeMsg) {
    MutableFudgeFieldContainer result = _fudgeContext.newMessage();
    List<FudgeField> allFields = fudgeMsg.getAllFields();
    for (FudgeField fudgeField : allFields) {
      if (!fudgeField.getName().equals(ManageableSecurity.IDENTIFIERS_KEY)) {
        result.add(fudgeField);
      }
    }
    return result;
  }
}
