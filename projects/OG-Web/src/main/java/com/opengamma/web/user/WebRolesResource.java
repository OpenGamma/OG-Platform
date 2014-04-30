/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.user;

import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.master.user.ManageableRole;
import com.opengamma.master.user.RoleEventHistoryRequest;
import com.opengamma.master.user.RoleForm;
import com.opengamma.master.user.RoleFormError;
import com.opengamma.master.user.RoleFormException;
import com.opengamma.master.user.RoleSearchRequest;
import com.opengamma.master.user.RoleSearchResult;
import com.opengamma.master.user.RoleSearchSortOrder;
import com.opengamma.master.user.UserMaster;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all roles.
 * <p>
 * The roles resource represents the whole of a role master.
 */
@Path("/roles")
public class WebRolesResource extends AbstractWebRoleResource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(WebRolesResource.class);
  /**
   * The ftl file.
   */
  private static final String ROLES_PAGE = HTML_DIR + "roles.ftl";
  /**
   * The ftl file.
   */
  private static final String ROLE_ADD_PAGE = HTML_DIR + "role-add.ftl";

  /**
   * Creates the resource.
   * @param userMaster  the user master, not null
   */
  public WebRolesResource(final UserMaster userMaster) {
    super(userMaster);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("sort") String sort,
      @QueryParam("rolename") String rolename,
      @QueryParam("name") String name,
      @QueryParam("roleId") List<String> roleIdStrs,
      @Context UriInfo uriInfo) {
    sort = StringUtils.trimToNull(sort);
    rolename = StringUtils.trimToNull(rolename);
    name = StringUtils.trimToNull(name);
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    RoleSearchSortOrder so = buildSortOrder(sort, RoleSearchSortOrder.NAME_ASC);
    FlexiBean out = createSearchResultData(pr, so, rolename, name, roleIdStrs, uriInfo);
    return getFreemarker().build(ROLES_PAGE, out);
  }

  private FlexiBean createSearchResultData(
      PagingRequest pr, RoleSearchSortOrder so,
      String rolename, String name,
      List<String> roleIdStrs, UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    RoleSearchRequest searchRequest = new RoleSearchRequest();
    searchRequest.setPagingRequest(pr);
    searchRequest.setSortOrder(so);
    searchRequest.setRoleName(rolename);
    for (String roleIdStr : roleIdStrs) {
      searchRequest.addObjectId(ObjectId.parse(roleIdStr));
    }
    out.put("searchRequest", searchRequest);
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      RoleSearchResult searchResult = data().getRoleMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    }
    return out;
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postHTML(
      @FormParam("rolename") String roleName,
      @FormParam("description") String description,
      @FormParam("addroles") String addRoles,
      @FormParam("addperms") String addPerms,
      @FormParam("addusers") String addUsers) {
    try {
      RoleForm form = new RoleForm(roleName, description);
      form.setAddRoles(addRoles);
      form.setAddPermissions(addPerms);
      form.setAddUsers(addUsers);
      ManageableRole added = form.add(data().getUserMaster());
      URI uri = WebRoleResource.uri(data(), added.getRoleName());
      return Response.seeOther(uri).build();
      
    } catch (RoleFormException ex) {
      ex.logUnexpected(s_logger);
      FlexiBean out = createRootData();
      out.put("rolename", roleName);
      out.put("description", description);
      out.put("addroles", addRoles);
      out.put("addperms", addPerms);
      out.put("addusers", addUsers);
      for (RoleFormError error : ex.getErrors()) {
        out.put("err_" + error.toLowerCamel(), true);
      }
      return Response.ok(getFreemarker().build(ROLE_ADD_PAGE, out)).build();
    }
  }

  //-------------------------------------------------------------------------
  @Path("name/{roleName}")
  public WebRoleResource findRole(@PathParam("roleName") String roleName) {
    data().setUriRoleName(roleName);
    try {
      ManageableRole role = data().getRoleMaster().getByName(roleName);
      data().setRole(role);
    } catch (DataNotFoundException ex) {
      RoleEventHistoryRequest request = new RoleEventHistoryRequest(roleName);
      try {
        data().getRoleMaster().eventHistory(request);
        ManageableRole role = new ManageableRole(roleName);
        data().setRole(role);
      } catch (DataNotFoundException ex2) {
        throw ex;
      }
    }
    return new WebRoleResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    RoleSearchRequest searchRequest = new RoleSearchRequest();
    out.put("searchRequest", searchRequest);
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for roles.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(WebRoleData data) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebRolesResource.class);
    return builder.build();
  }

}
