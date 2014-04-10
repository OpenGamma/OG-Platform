<#escape x as x?html>
<@page title="Role - ${role.roleName}">

<@section css="info" if=deleted>
  <p>This role has been deleted</p>
</@section>


<#-- SECTION Role output -->
<@section title="Role">
  <p>
    <@rowout label="Role name">${role.roleName}</@rowout>
<#if !deleted>
    <@rowout label="Reference">${role.uniqueId.value}, version ${role.uniqueId.version}</@rowout>
</#if>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail">
    <@rowout label="Description">${role.description}</@rowout>
<#if role.associatedRoles?has_content>
    <@rowout label="Roles">
<#list role.associatedRoles as item>
<a href="${uris.role(item)}">${item}</a><#if item_has_next>,</#if>
</#list>
    </@rowout>
</#if>
<#if role.associatedPermissions?has_content>
    <@rowout label="Permissions">
<#list role.associatedPermissions as item>
${item}<#if item_has_next>,</#if>
</#list>
    </@rowout>
</#if>
<#if role.associatedUsers?has_content>
    <@rowout label="Users">
<#list role.associatedUsers as item>
<#if security.isPermitted('UserMaster:view')>
<a href="${uris.user(item)}">${item}</a><#if item_has_next>,</#if>
<#else>
${item}<#if item_has_next>,</#if>
</#if>
</#list>
    </@rowout>
</#if>
</@subsection>


<#-- SUBSECTION History data -->
<@subsection title="Event history">
  <@table items=eventHistory.events empty="No history found" headers=["Type","Actioning user","Instant","Changes"]; item>
      <td>${item.type}</td>
      <td>${item.userName}</td>
      <td>${item.instant}</td>
      <td>
<#list item.changes as change>
        ${change}<br />
</#list>
      </td>
  </@table>
</@subsection>
</@section>


<#-- SECTION Update role -->
<@section title="Update role" if=!deleted>
  <@form method="POST" action="${uris.role()}">
  <p>
    Use commas to separate each role, permission and user:
  </p>
  <p>
    <@rowin label="Description"><input type="text" size="40" maxlength="200" name="description" value="${description}" id="description" /></@rowin>
    <@rowin label="Add roles"><input type="text" size="40" maxlength="400" name="addroles" value="" id="addroles" /></@rowin>
    <@rowin label="Remove roles"><input type="text" size="40" maxlength="400" name="removeroles" value="" id="removeroles" /></@rowin>
    <@rowin label="Add permissions"><input type="text" size="40" maxlength="400" name="addperms" value="" id="addperms" /></@rowin>
    <@rowin label="Remove permissions"><input type="text" size="40" maxlength="400" name="removeperms" value="" id="removeperms" /></@rowin>
    <@rowin label="Add users"><input type="text" size="40" maxlength="400" name="addusers" value="" id="addusers" /></@rowin>
    <@rowin label="Remove users"><input type="text" size="40" maxlength="400" name="removeusers" value="" id="removeusers" /></@rowin>
    <@rowin><input type="submit" value="Update" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Delete role -->
<#if role.roleName != 'admin'>
<@section title="Delete role" if=!deleted>
  <@form method="DELETE" action="${uris.role()}">
  <p>
    <@rowin><input type="submit" value="Delete" /></@rowin>
  </p>
  </@form>
</@section>
</#if>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.roles()}">Role search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
