package com.opengamma.integration.tool.portfolio.xml;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;

public class XmlExternalIdValidatorTest {

  XmlExternalIdValidator _validator;

  @BeforeMethod
  public void beforeMethod() {
    _validator = new XmlExternalIdValidator();
  }

  @Test
  public void testValidId() {
    for (int i = 0; i < 2; i++) {
      _validator.validateExternalId(ExternalId.of("scheme1", "id1"), "id1");
      _validator.validateExternalId(ExternalId.of("scheme1", "id2"), "id2");
      _validator.validateExternalId(ExternalId.of("scheme1", "id3"), "id3");
      _validator.validateExternalId(ExternalId.of("scheme2", "id1"), "id1");
      _validator.validateExternalId(ExternalId.of("scheme2", "id2"), "id2");
      _validator.validateExternalId(ExternalId.of("scheme2", "id3"), "id3");
    }
  }

  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void testInvalidId() {
    _validator.validateExternalId(ExternalId.of("scheme1", "id1"), "id1");
    _validator.validateExternalId(ExternalId.of("scheme1", "id1"), "id2");
  }

}
