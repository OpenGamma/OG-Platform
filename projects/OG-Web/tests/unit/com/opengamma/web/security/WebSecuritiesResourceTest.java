/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import static com.opengamma.web.security.WebSecuritiesResourceTestUtils.loadJson;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Collections;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.testng.annotations.Test;

/**
 * Test {@link WebSecuritiesResource}.
 */
@Test
public class WebSecuritiesResourceTest extends AbstractWebSecurityResourceTestCase {
  
  public void testGetMetaDataJSON() throws Exception {
    String metaDataJSON = _webSecuritiesResource.getMetaDataJSON();
    assertNotNull(metaDataJSON);
    JSONObject actualJson = new JSONObject(metaDataJSON); 
    assertEquals(loadJson("securitiesMetaDataJson.txt").toString(), actualJson.toString());
  }
  
  public void testGetAllSecurities() throws Exception {
    MultivaluedMap<String, String> queryParameters = _uriInfo.getQueryParameters();
    queryParameters.putSingle("name", StringUtils.EMPTY);
    queryParameters.putSingle("identifier", StringUtils.EMPTY);
    queryParameters.putSingle("type", StringUtils.EMPTY);
    queryParameters.put("securityId", Collections.<String>emptyList());
    
    String resultJson = _webSecuritiesResource.getJSON(null, null, null, null, 
        queryParameters.getFirst("name"), queryParameters.getFirst("identifier"), queryParameters.getFirst("type"), queryParameters.get("securityId"), _uriInfo);
    assertNotNull(resultJson);
    assertEquals(loadJson("allSecuritiesJson.txt").toString(), new JSONObject(resultJson).toString());
    
  }
  
}
