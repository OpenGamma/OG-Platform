/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import static com.opengamma.web.WebResourceTestUtils.assertJSONObjectEquals;
import static com.opengamma.web.WebResourceTestUtils.loadJson;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link WebSecuritiesResource}.
 */
@Test(groups = TestGroup.UNIT)
public class WebSecuritiesResourceTest extends AbstractWebSecurityResourceTestCase {

  public void testGetMetaDataJSON() throws Exception {
    String metaDataJSON = _webSecuritiesResource.getMetaDataJSON(null);
    assertNotNull(metaDataJSON);
    JSONObject actualJson = new JSONObject(metaDataJSON); 
    assertJSONObjectEquals(expectedSecurityTypes(), actualJson);
  }

  private JSONObject expectedSecurityTypes() {
    SecurityTypesDescriptionProvider s_secTypesProvider = SecurityTypesDescriptionProvider.getInstance();
    Map<String, Object> result = new HashMap<>();
    result.put("description2type", s_secTypesProvider.getDescription2Type());
    result.put("types", s_secTypesProvider.getDescription2Type().values());
    return new JSONObject(result);
  }

  public void testGetAllSecurities() throws Exception {
    MultivaluedMap<String, String> queryParameters = _uriInfo.getQueryParameters();
    queryParameters.putSingle("name", StringUtils.EMPTY);
    queryParameters.putSingle("identifier", StringUtils.EMPTY);
    queryParameters.putSingle("type", StringUtils.EMPTY);
    queryParameters.put("securityId", Collections.<String>emptyList());
    
    String resultJson = _webSecuritiesResource.getJSON(null, null, null, null, null, 
        queryParameters.getFirst("name"), queryParameters.getFirst("identifier"), 
        queryParameters.getFirst("type"), queryParameters.get("securityId"), _uriInfo);
    assertNotNull(resultJson);
    assertJSONObjectEquals(loadJson("com/opengamma/web/security/allSecuritiesJson.txt"), new JSONObject(resultJson));
  }

}
