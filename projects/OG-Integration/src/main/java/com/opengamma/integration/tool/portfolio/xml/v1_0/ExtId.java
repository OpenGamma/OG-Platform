package com.opengamma.integration.tool.portfolio.xml.v1_0;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.opengamma.id.ExternalId;

/**
 * Represents an external id from an xmlfile which will generally
 * be mapped to an {@link ExternalId}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ExtId {

  /**
   * The scheme for this external id.
   */
  @XmlAttribute(name = "scheme")
  private String _scheme;

  /**
   * The id value.
   */
  @XmlValue
  private String _id;

  public String getScheme() {
    return _scheme;
  }

  public void setScheme(String scheme) {
    this._scheme = scheme;
  }

  public String getId() {
    return _id;
  }

  public void setId(String id) {
    _id = id;
  }
}
