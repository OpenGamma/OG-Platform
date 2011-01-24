/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.util;

import java.io.CharArrayWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgWriter;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.json.FudgeJSONStreamWriter;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * Outputs JSON representation of security object
 */
public class JSONOutputter {
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(JSONOutputter.class);
  
  private static final String TYPE_KEY = "type";
  private static final String DATA_FIELDS_KEY = "dataFields";
  private static final String HEADER_KEY = "header";
  private static final String DATA_KEY = "data";
  private static final String TEMPLATE_DATA_KEY = "templateData";
  private static final String IDENTIFIERS_KEY = "identifiers";
    
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
    
    Map<String, Object> jsonMap = new HashMap<String, Object>();
    
    FudgeFieldContainer fudgeMsg = security.toFudgeMsg(_fudgeContext);
    final CharArrayWriter caw = new CharArrayWriter();
    final FudgeMsgWriter fmw = new FudgeMsgWriter(new FudgeJSONStreamWriter(_fudgeContext, caw));
    FudgeFieldContainer processedMsg = removeClassHeaders(removeIdentifiersField(fudgeMsg));
    fmw.writeMessage(processedMsg);
    String result = null;
    try {
      jsonMap.put(TEMPLATE_DATA_KEY, new JSONObject(caw.toString()));
      jsonMap.put(IDENTIFIERS_KEY, convertIdentierToJSON(fudgeMsg));
      result = new JSONObject(jsonMap).toString();
    } catch (JSONException ex) {
      throw new OpenGammaRuntimeException("Error creating JSON from FudgeMessage", ex);
    }
    return result;  
  }
  
  /**
   * Build JSON document for the search result page
   * 
   * @param type the web resource type
   * @param dataFields the list of datafields headers
   * @param data the formatted list of search results
   * @return the build JSON result from search result
   */
  public String buildJSONSearchResult(String type, List<String> dataFields, List<String> data) {
    
    Map<String, Object> searchResultMap = new HashMap<String, Object>();
    searchResultMap.put(HEADER_KEY, createHeaderMap(type, dataFields));
    
    searchResultMap.put(DATA_KEY, data);
    
    return new JSONObject(searchResultMap).toString();
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
  
  private FudgeFieldContainer removeClassHeaders(FudgeFieldContainer fudgeMsg) {
    //remove fields with ordinal zero
    MutableFudgeFieldContainer result = _fudgeContext.newMessage();
    for (FudgeField fudgeField : fudgeMsg.getAllFields()) {
      if (fudgeField.getType().getTypeId() == FudgeTypeDictionary.FUDGE_MSG_TYPE_ID) {
        if (fudgeField.getValue() != null) {
          FudgeFieldContainer withoutClassHeaders = removeClassHeaders((FudgeFieldContainer) fudgeField.getValue());
          result.add(fudgeField.getName(), withoutClassHeaders);
        }
      } else {
        if (fudgeField.getOrdinal() == null || fudgeField.getOrdinal() != 0) {
          result.add(fudgeField);
        }
      }
    }
    return result;
  }
  
  private Map<String, Object> createHeaderMap(String type, List<String> dataFields) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put(TYPE_KEY, type);
    result.put(DATA_FIELDS_KEY, dataFields);
    return result;
  }

}
