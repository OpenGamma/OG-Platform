/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierTemplate;

/**
 * Tests {@link UserResourceDetails} 
 */
public class UserUniqueIdentifierUtilsTest {
  
  @Test
  public void testGenerateParseCycle() {
    String username = "testUser";
    String clientId = "testClient";
    UserResourceDetails resourceDetails = new UserResourceDetails(username, clientId, "testResource");
    UniqueIdentifierTemplate template = UserUniqueIdentifierUtils.getTemplate(resourceDetails);
    UniqueIdentifier uid = template.uid("testValue");
    assertEquals("testUser/testClient/testResource/testValue", uid.getValue());
    UserResourceDetails parsedDetails = UserUniqueIdentifierUtils.getDetails(uid);
    assertEquals(username, parsedDetails.getUsername());
    assertEquals(clientId, parsedDetails.getClientId());
  }
  
}
