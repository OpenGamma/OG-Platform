/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.master.DocumentVisibility;
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
  private List<ObjectId> _portfolioObjectIds;
  /**
   * The set of node object identifiers, null to not limit by node object identifiers.
   * Each returned portfolio will contain at least one of these nodes.
   * Note that an empty list will return no portfolio.
   */
  @PropertyDefinition(set = "manual")
  private List<ObjectId> _nodeObjectIds;
  /**
   * The portfolio name, wildcards allowed, null to not match on name.
   */
  @PropertyDefinition
  private String _name;
  /**
   * The sort order to use.
   */
  @PropertyDefinition(validate = "notNull")
  private PortfolioSearchSortOrder _sortOrder = PortfolioSearchSortOrder.OBJECT_ID_ASC;
  /**
   * The depth of nodes to return.
   * A value of zero returns the root node, one returns the root node with immediate children, and so on.
   * A negative value, such as -1, returns the full tree.
   * By default this is -1 returning all the data.
   */
  @PropertyDefinition
  private int _depth = -1;
  /**
   * The flag to determine whether to include positions.
   * True will return any positions on the requested nodes, false will not include the positions.
   * By default this is true returning all the positions, set to false to enhance performance.
   */
  @PropertyDefinition
  private boolean _includePositions = true;
  /**
   * The lowest visibility level to return.  
   */
  @PropertyDefinition(validate = "notNull")
  private DocumentVisibility _visibility = DocumentVisibility.VISIBLE;

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
  public void addPortfolioObjectId(ObjectIdentifiable portfolioId) {
    ArgumentChecker.notNull(portfolioId, "portfolioId");
    if (_portfolioObjectIds == null) {
      _portfolioObjectIds = new ArrayList<ObjectId>();
    }
    _portfolioObjectIds.add(portfolioId.getObjectId());
  }

  /**
   * Sets the set of portfolio object identifiers, null to not limit by portfolio object identifiers.
   * Note that an empty set will return no portfolios.
   * 
   * @param portfolioIds  the new portfolio identifiers, null clears the position id search
   */
  public void setPortfolioObjectIds(Iterable<? extends ObjectIdentifiable> portfolioIds) {
    if (portfolioIds == null) {
      _portfolioObjectIds = null;
    } else {
      _portfolioObjectIds = new ArrayList<ObjectId>();
      for (ObjectIdentifiable portfolioId : portfolioIds) {
        _portfolioObjectIds.add(portfolioId.getObjectId());
      }
    }
  }

  /**
   * Adds a single node object identifier to the set.
   * 
   * @param nodeId  the node object identifier to add, not null
   */
  public void addNodeObjectId(ObjectIdentifiable nodeId) {
    ArgumentChecker.notNull(nodeId, "nodeId");
    if (_nodeObjectIds == null) {
      _nodeObjectIds = new ArrayList<ObjectId>();
    }
    _nodeObjectIds.add(nodeId.getObjectId());
  }

  /**
   * Sets the set of node object identifiers, null to not limit by node object identifiers.
   * Each returned portfolio will contain at least one of these nodes.
   * Note that an empty set will return no portfolios.
   * 
   * @param nodeIds  the new node identifiers, null clears the position id search
   */
  public void setNodeObjectIds(Iterable<? extends ObjectIdentifiable> nodeIds) {
    if (nodeIds == null) {
      _nodeObjectIds = null;
    } else {
      _nodeObjectIds = new ArrayList<ObjectId>();
      for (ObjectIdentifiable nodeId : nodeIds) {
        _nodeObjectIds.add(nodeId.getObjectId());
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
    if (getVisibility().getVisibilityLevel() < document.getVisibility().getVisibilityLevel()) {
      return false;
    }
    final ManageablePortfolio portfolio = document.getPortfolio();
    if (getPortfolioObjectIds() != null && getPortfolioObjectIds().contains(document.getObjectId()) == false) {
      return false;
    }
    if (getNodeObjectIds() != null && portfolio.getRootNode().matchesAnyNode(getNodeObjectIds()) == false) {
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

  //-----------------------------------------------------------------------
  /**
   * Gets the set of portfolio object identifiers, null to not limit by portfolio object identifiers.
   * Note that an empty set will return no portfolios.
   * @return the value of the property
   */
  public List<ObjectId> getPortfolioObjectIds() {
    return _portfolioObjectIds;
  }

  /**
   * Gets the the {@code portfolioObjectIds} property.
   * Note that an empty set will return no portfolios.
   * @return the property, not null
   */
  public final Property<List<ObjectId>> portfolioObjectIds() {
    return metaBean().portfolioObjectIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of node object identifiers, null to not limit by node object identifiers.
   * Each returned portfolio will contain at least one of these nodes.
   * Note that an empty list will return no portfolio.
   * @return the value of the property
   */
  public List<ObjectId> getNodeObjectIds() {
    return _nodeObjectIds;
  }

  /**
   * Gets the the {@code nodeObjectIds} property.
   * Each returned portfolio will contain at least one of these nodes.
   * Note that an empty list will return no portfolio.
   * @return the property, not null
   */
  public final Property<List<ObjectId>> nodeObjectIds() {
    return metaBean().nodeObjectIds().createProperty(this);
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
   * Gets the sort order to use.
   * @return the value of the property, not null
   */
  public PortfolioSearchSortOrder getSortOrder() {
    return _sortOrder;
  }

  /**
   * Sets the sort order to use.
   * @param sortOrder  the new value of the property, not null
   */
  public void setSortOrder(PortfolioSearchSortOrder sortOrder) {
    JodaBeanUtils.notNull(sortOrder, "sortOrder");
    this._sortOrder = sortOrder;
  }

  /**
   * Gets the the {@code sortOrder} property.
   * @return the property, not null
   */
  public final Property<PortfolioSearchSortOrder> sortOrder() {
    return metaBean().sortOrder().createProperty(this);
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
   * Gets the flag to determine whether to include positions.
   * True will return any positions on the requested nodes, false will not include the positions.
   * By default this is true returning all the positions, set to false to enhance performance.
   * @return the value of the property
   */
  public boolean isIncludePositions() {
    return _includePositions;
  }

  /**
   * Sets the flag to determine whether to include positions.
   * True will return any positions on the requested nodes, false will not include the positions.
   * By default this is true returning all the positions, set to false to enhance performance.
   * @param includePositions  the new value of the property
   */
  public void setIncludePositions(boolean includePositions) {
    this._includePositions = includePositions;
  }

  /**
   * Gets the the {@code includePositions} property.
   * True will return any positions on the requested nodes, false will not include the positions.
   * By default this is true returning all the positions, set to false to enhance performance.
   * @return the property, not null
   */
  public final Property<Boolean> includePositions() {
    return metaBean().includePositions().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the lowest visibility level to return.
   * @return the value of the property, not null
   */
  public DocumentVisibility getVisibility() {
    return _visibility;
  }

  /**
   * Sets the lowest visibility level to return.
   * @param visibility  the new value of the property, not null
   */
  public void setVisibility(DocumentVisibility visibility) {
    JodaBeanUtils.notNull(visibility, "visibility");
    this._visibility = visibility;
  }

  /**
   * Gets the the {@code visibility} property.
   * @return the property, not null
   */
  public final Property<DocumentVisibility> visibility() {
    return metaBean().visibility().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public PortfolioSearchRequest clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      PortfolioSearchRequest other = (PortfolioSearchRequest) obj;
      return JodaBeanUtils.equal(getPortfolioObjectIds(), other.getPortfolioObjectIds()) &&
          JodaBeanUtils.equal(getNodeObjectIds(), other.getNodeObjectIds()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getSortOrder(), other.getSortOrder()) &&
          (getDepth() == other.getDepth()) &&
          (isIncludePositions() == other.isIncludePositions()) &&
          JodaBeanUtils.equal(getVisibility(), other.getVisibility()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getPortfolioObjectIds());
    hash = hash * 31 + JodaBeanUtils.hashCode(getNodeObjectIds());
    hash = hash * 31 + JodaBeanUtils.hashCode(getName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSortOrder());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDepth());
    hash = hash * 31 + JodaBeanUtils.hashCode(isIncludePositions());
    hash = hash * 31 + JodaBeanUtils.hashCode(getVisibility());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("PortfolioSearchRequest{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("portfolioObjectIds").append('=').append(JodaBeanUtils.toString(getPortfolioObjectIds())).append(',').append(' ');
    buf.append("nodeObjectIds").append('=').append(JodaBeanUtils.toString(getNodeObjectIds())).append(',').append(' ');
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("sortOrder").append('=').append(JodaBeanUtils.toString(getSortOrder())).append(',').append(' ');
    buf.append("depth").append('=').append(JodaBeanUtils.toString(getDepth())).append(',').append(' ');
    buf.append("includePositions").append('=').append(JodaBeanUtils.toString(isIncludePositions())).append(',').append(' ');
    buf.append("visibility").append('=').append(JodaBeanUtils.toString(getVisibility())).append(',').append(' ');
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
     * The meta-property for the {@code portfolioObjectIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ObjectId>> _portfolioObjectIds = DirectMetaProperty.ofReadWrite(
        this, "portfolioObjectIds", PortfolioSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code nodeObjectIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ObjectId>> _nodeObjectIds = DirectMetaProperty.ofReadWrite(
        this, "nodeObjectIds", PortfolioSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", PortfolioSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code sortOrder} property.
     */
    private final MetaProperty<PortfolioSearchSortOrder> _sortOrder = DirectMetaProperty.ofReadWrite(
        this, "sortOrder", PortfolioSearchRequest.class, PortfolioSearchSortOrder.class);
    /**
     * The meta-property for the {@code depth} property.
     */
    private final MetaProperty<Integer> _depth = DirectMetaProperty.ofReadWrite(
        this, "depth", PortfolioSearchRequest.class, Integer.TYPE);
    /**
     * The meta-property for the {@code includePositions} property.
     */
    private final MetaProperty<Boolean> _includePositions = DirectMetaProperty.ofReadWrite(
        this, "includePositions", PortfolioSearchRequest.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code visibility} property.
     */
    private final MetaProperty<DocumentVisibility> _visibility = DirectMetaProperty.ofReadWrite(
        this, "visibility", PortfolioSearchRequest.class, DocumentVisibility.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "portfolioObjectIds",
        "nodeObjectIds",
        "name",
        "sortOrder",
        "depth",
        "includePositions",
        "visibility");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -397882735:  // portfolioObjectIds
          return _portfolioObjectIds;
        case 2068534039:  // nodeObjectIds
          return _nodeObjectIds;
        case 3373707:  // name
          return _name;
        case -26774448:  // sortOrder
          return _sortOrder;
        case 95472323:  // depth
          return _depth;
        case 81400994:  // includePositions
          return _includePositions;
        case 1941332754:  // visibility
          return _visibility;
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
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code portfolioObjectIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ObjectId>> portfolioObjectIds() {
      return _portfolioObjectIds;
    }

    /**
     * The meta-property for the {@code nodeObjectIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ObjectId>> nodeObjectIds() {
      return _nodeObjectIds;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code sortOrder} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PortfolioSearchSortOrder> sortOrder() {
      return _sortOrder;
    }

    /**
     * The meta-property for the {@code depth} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> depth() {
      return _depth;
    }

    /**
     * The meta-property for the {@code includePositions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> includePositions() {
      return _includePositions;
    }

    /**
     * The meta-property for the {@code visibility} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DocumentVisibility> visibility() {
      return _visibility;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -397882735:  // portfolioObjectIds
          return ((PortfolioSearchRequest) bean).getPortfolioObjectIds();
        case 2068534039:  // nodeObjectIds
          return ((PortfolioSearchRequest) bean).getNodeObjectIds();
        case 3373707:  // name
          return ((PortfolioSearchRequest) bean).getName();
        case -26774448:  // sortOrder
          return ((PortfolioSearchRequest) bean).getSortOrder();
        case 95472323:  // depth
          return ((PortfolioSearchRequest) bean).getDepth();
        case 81400994:  // includePositions
          return ((PortfolioSearchRequest) bean).isIncludePositions();
        case 1941332754:  // visibility
          return ((PortfolioSearchRequest) bean).getVisibility();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -397882735:  // portfolioObjectIds
          ((PortfolioSearchRequest) bean).setPortfolioObjectIds((List<ObjectId>) newValue);
          return;
        case 2068534039:  // nodeObjectIds
          ((PortfolioSearchRequest) bean).setNodeObjectIds((List<ObjectId>) newValue);
          return;
        case 3373707:  // name
          ((PortfolioSearchRequest) bean).setName((String) newValue);
          return;
        case -26774448:  // sortOrder
          ((PortfolioSearchRequest) bean).setSortOrder((PortfolioSearchSortOrder) newValue);
          return;
        case 95472323:  // depth
          ((PortfolioSearchRequest) bean).setDepth((Integer) newValue);
          return;
        case 81400994:  // includePositions
          ((PortfolioSearchRequest) bean).setIncludePositions((Boolean) newValue);
          return;
        case 1941332754:  // visibility
          ((PortfolioSearchRequest) bean).setVisibility((DocumentVisibility) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((PortfolioSearchRequest) bean)._sortOrder, "sortOrder");
      JodaBeanUtils.notNull(((PortfolioSearchRequest) bean)._visibility, "visibility");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
