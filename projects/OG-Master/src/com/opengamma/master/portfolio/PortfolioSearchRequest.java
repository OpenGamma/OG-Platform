/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.ObjectId;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.RegexUtils;

/**
 * Request for searching for portfolios.
 * <p>
 * Documents will be returned that match the search criteria.
 * This class provides the ability to page the results and to search
 * as at a specific version and correction instant.
 * See {@link PortfolioHistoryRequest} for more details on how history works.
 */
@PublicSPI
@BeanDefinition
public class PortfolioSearchRequest extends AbstractSearchRequest {

  /**
   * The set of portfolio object identifiers, null to not limit by portfolio object identifiers.
   * Note that an empty set will return no portfolios.
   */
  @PropertyDefinition(set = "manual")
  private List<ObjectId> _portfolioIds;
  /**
   * The set of node object identifiers, null to not limit by node object identifiers.
   * Each returned portfolio will contain at least one of these nodes.
   * Note that an empty list will return no portfolio.
   */
  @PropertyDefinition(set = "manual")
  private List<ObjectId> _nodeIds;
  /**
   * The portfolio name, wildcards allowed, null to not match on name.
   */
  @PropertyDefinition
  private String _name;
  /**
   * The depth of nodes to return.
   * A value of zero returns the root node, one returns the root node with immediate children, and so on.
   * A negative value, such as -1, returns the full tree.
   * By default this is -1 returning all the data.
   */
  @PropertyDefinition
  private int _depth = -1;

  /**
   * Creates an instance.
   */
  public PortfolioSearchRequest() {
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single portfolio object identifier to the set.
   * 
   * @param portfolioId  the portfolio object identifier to add, not null
   */
  public void addPortfolioId(ObjectIdentifiable portfolioId) {
    ArgumentChecker.notNull(portfolioId, "portfolioId");
    if (_portfolioIds == null) {
      _portfolioIds = new ArrayList<ObjectId>();
    }
    _portfolioIds.add(portfolioId.getObjectId());
  }

  /**
   * Sets the set of portfolio object identifiers, null to not limit by portfolio object identifiers.
   * Note that an empty set will return no portfolios.
   * 
   * @param portfolioIds  the new portfolio identifiers, null clears the position id search
   */
  public void setPortfolioIds(Iterable<? extends ObjectIdentifiable> portfolioIds) {
    if (portfolioIds == null) {
      _portfolioIds = null;
    } else {
      _portfolioIds = new ArrayList<ObjectId>();
      for (ObjectIdentifiable portfolioId : portfolioIds) {
        _portfolioIds.add(portfolioId.getObjectId());
      }
    }
  }

  /**
   * Adds a single node object identifier to the set.
   * 
   * @param nodeId  the node object identifier to add, not null
   */
  public void addNodeId(ObjectIdentifiable nodeId) {
    ArgumentChecker.notNull(nodeId, "nodeId");
    if (_nodeIds == null) {
      _nodeIds = new ArrayList<ObjectId>();
    }
    _nodeIds.add(nodeId.getObjectId());
  }

  /**
   * Sets the set of node object identifiers, null to not limit by node object identifiers.
   * Each returned portfolio will contain at least one of these nodes.
   * Note that an empty set will return no portfolios.
   * 
   * @param nodeIds  the new node identifiers, null clears the position id search
   */
  public void setNodeIds(Iterable<? extends ObjectIdentifiable> nodeIds) {
    if (nodeIds == null) {
      _nodeIds = null;
    } else {
      _nodeIds = new ArrayList<ObjectId>();
      for (ObjectIdentifiable nodeId : nodeIds) {
        _nodeIds.add(nodeId.getObjectId());
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean matches(final AbstractDocument obj) {
    if (obj instanceof PortfolioDocument == false) {
      return false;
    }
    final PortfolioDocument document = (PortfolioDocument) obj;
    final ManageablePortfolio portfolio = document.getPortfolio();
    if (getPortfolioIds() != null && getPortfolioIds().contains(document.getObjectId()) == false) {
      return false;
    }
    if (getNodeIds() != null && portfolio.getRootNode().matchesAny(getNodeIds()) == false) {
      return false;
    }
    if (getName() != null && RegexUtils.wildcardMatch(getName(), portfolio.getName()) == false) {
      return false;
    }
    return true;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code PortfolioSearchRequest}.
   * @return the meta-bean, not null
   */
  public static PortfolioSearchRequest.Meta meta() {
    return PortfolioSearchRequest.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(PortfolioSearchRequest.Meta.INSTANCE);
  }

  @Override
  public PortfolioSearchRequest.Meta metaBean() {
    return PortfolioSearchRequest.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -160779184:  // portfolioIds
        return getPortfolioIds();
      case 2114427222:  // nodeIds
        return getNodeIds();
      case 3373707:  // name
        return getName();
      case 95472323:  // depth
        return getDepth();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -160779184:  // portfolioIds
        setPortfolioIds((List<ObjectId>) newValue);
        return;
      case 2114427222:  // nodeIds
        setNodeIds((List<ObjectId>) newValue);
        return;
      case 3373707:  // name
        setName((String) newValue);
        return;
      case 95472323:  // depth
        setDepth((Integer) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      PortfolioSearchRequest other = (PortfolioSearchRequest) obj;
      return JodaBeanUtils.equal(getPortfolioIds(), other.getPortfolioIds()) &&
          JodaBeanUtils.equal(getNodeIds(), other.getNodeIds()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getDepth(), other.getDepth()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getPortfolioIds());
    hash += hash * 31 + JodaBeanUtils.hashCode(getNodeIds());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDepth());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of portfolio object identifiers, null to not limit by portfolio object identifiers.
   * Note that an empty set will return no portfolios.
   * @return the value of the property
   */
  public List<ObjectId> getPortfolioIds() {
    return _portfolioIds;
  }

  /**
   * Gets the the {@code portfolioIds} property.
   * Note that an empty set will return no portfolios.
   * @return the property, not null
   */
  public final Property<List<ObjectId>> portfolioIds() {
    return metaBean().portfolioIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of node object identifiers, null to not limit by node object identifiers.
   * Each returned portfolio will contain at least one of these nodes.
   * Note that an empty list will return no portfolio.
   * @return the value of the property
   */
  public List<ObjectId> getNodeIds() {
    return _nodeIds;
  }

  /**
   * Gets the the {@code nodeIds} property.
   * Each returned portfolio will contain at least one of these nodes.
   * Note that an empty list will return no portfolio.
   * @return the property, not null
   */
  public final Property<List<ObjectId>> nodeIds() {
    return metaBean().nodeIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the portfolio name, wildcards allowed, null to not match on name.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the portfolio name, wildcards allowed, null to not match on name.
   * @param name  the new value of the property
   */
  public void setName(String name) {
    this._name = name;
  }

  /**
   * Gets the the {@code name} property.
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the depth of nodes to return.
   * A value of zero returns the root node, one returns the root node with immediate children, and so on.
   * A negative value, such as -1, returns the full tree.
   * By default this is -1 returning all the data.
   * @return the value of the property
   */
  public int getDepth() {
    return _depth;
  }

  /**
   * Sets the depth of nodes to return.
   * A value of zero returns the root node, one returns the root node with immediate children, and so on.
   * A negative value, such as -1, returns the full tree.
   * By default this is -1 returning all the data.
   * @param depth  the new value of the property
   */
  public void setDepth(int depth) {
    this._depth = depth;
  }

  /**
   * Gets the the {@code depth} property.
   * A value of zero returns the root node, one returns the root node with immediate children, and so on.
   * A negative value, such as -1, returns the full tree.
   * By default this is -1 returning all the data.
   * @return the property, not null
   */
  public final Property<Integer> depth() {
    return metaBean().depth().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code PortfolioSearchRequest}.
   */
  public static class Meta extends AbstractSearchRequest.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code portfolioIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ObjectId>> _portfolioIds = DirectMetaProperty.ofReadWrite(
        this, "portfolioIds", PortfolioSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code nodeIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ObjectId>> _nodeIds = DirectMetaProperty.ofReadWrite(
        this, "nodeIds", PortfolioSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", PortfolioSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code depth} property.
     */
    private final MetaProperty<Integer> _depth = DirectMetaProperty.ofReadWrite(
        this, "depth", PortfolioSearchRequest.class, Integer.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "portfolioIds",
        "nodeIds",
        "name",
        "depth");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -160779184:  // portfolioIds
          return _portfolioIds;
        case 2114427222:  // nodeIds
          return _nodeIds;
        case 3373707:  // name
          return _name;
        case 95472323:  // depth
          return _depth;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends PortfolioSearchRequest> builder() {
      return new DirectBeanBuilder<PortfolioSearchRequest>(new PortfolioSearchRequest());
    }

    @Override
    public Class<? extends PortfolioSearchRequest> beanType() {
      return PortfolioSearchRequest.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code portfolioIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ObjectId>> portfolioIds() {
      return _portfolioIds;
    }

    /**
     * The meta-property for the {@code nodeIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ObjectId>> nodeIds() {
      return _nodeIds;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code depth} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> depth() {
      return _depth;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
