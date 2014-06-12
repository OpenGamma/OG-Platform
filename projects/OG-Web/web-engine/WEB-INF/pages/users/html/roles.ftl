<#escape x as x?html>
<@page title="Roles">


<#-- SECTION Role search -->
<@section title="Role search" if=searchRequest??>
  <@form method="GET" action="${uris.roles()}">
  <p>
    <@rowin label="Role name"><input type="text" size="30" name="rolename" value="${searchRequest.roleName}" /></@rowin>
    <@rowin><input type="submit" value="Search" /></@rowin>
  </p>
  </@form>

<#-- SUBSECTION Role results -->
<#if searchResult??>
<@subsection title="Results">
  <@table items=searchResult.roles paging=paging empty="No role found" headers=["Role Name","Description","Reference","Actions"]; item>
      <td><a href="${uris.role(item)}">${item.roleName}</a></td>
      <td>${item.description}</td>
      <td>${item.uniqueId.value}</td>
      <td><a href="${uris.role(item)}">View</a></td>
  </@table>
</@subsection>
</#if>
</@section>

<#-- SECTION Add role -->
<@section title="Add role">
  <@form method="POST" action="${uris.roles()}">
  <p>
    <@rowin label="Role name"><input type="text" size="40" maxlength="20" name="rolename" id="rolename" /></@rowin>
    <@rowin label="Description"><input type="text" size="40" maxlength="200" name="description" id="description" /></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section>

<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
