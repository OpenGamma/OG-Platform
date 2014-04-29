<#escape x as x?html>
<@page title="User - ${user.userName}">

<@section css="info" if=deleted>
  <p>This user has been deleted</p>
</@section>


<#-- SECTION User output -->
<@section title="User">
  <p>
    <@rowout label="User name">${user.userName}</@rowout>
<#if !deleted>
    <@rowout label="Reference">${user.uniqueId.value}, version ${user.uniqueId.version}</@rowout>
</#if>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail" if=!deleted>
<#if user.alternateIds?has_content>
<#list user.alternateIds.externalIds as item>
    <@rowout label="Alternate id">${item.scheme.name} - ${item.value}</@rowout>
</#list>
</#if>
    <@rowout label="Status">${user.status}</@rowout>
    <@rowout label="Email address">${user.emailAddress}</@rowout>
    <@rowout label="Display name">${user.profile.displayName}</@rowout>
    <@rowout label="Locale">${user.profile.locale}</@rowout>
    <@rowout label="Time zone">${user.profile.zone}</@rowout>
    <@rowout label="Date style">${user.profile.dateStyle}</@rowout>
    <@rowout label="Time style">${user.profile.timeStyle}</@rowout>
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


<#-- SECTION Update user -->
<@section title="Update user" if=!deleted>
  <@form method="PUT" action="${uris.user()}">
  <p>
    <@rowin label="User name"><input type="text" size="40" maxlength="20" name="username" value="${user.userName}" id="username" /></@rowin>
    <@rowin label="Email address"><input type="email" size="40" maxlength="200" name="email" value="${user.emailAddress}" id="email" /></@rowin>
    <@rowin label="Display name"><input type="text" size="40" maxlength="200" name="displayname" value="${user.profile.displayName}" id="displayname" /></@rowin>
    <@rowin label="Bloomberg EMRS User"><input type="text" size="40" maxlength="100" name="idBloombergEmrs" value="${idBloombergEmrs}" id="idBloombergEmrs" /></@rowin>
    <@rowin label="Windows User"><input type="text" size="40" maxlength="100" name="idWindows" value="${idWindows}" id="idWindows" /></@rowin>
    <@rowin><input type="submit" value="Update" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Reset password -->
<@section title="Reset password" if=!deleted>
  <@form method="POST" action="${uris.userResetPassword()}">
  <p>
    <@rowin label="Password"><input type="password" size="40" maxlength="255" name="password" id="password" /></@rowin>
    <@rowin><input type="submit" value="Reset" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Disable user -->
<#if user.userName != 'admin'>
<#if user.status == 'ENABLED'>
<@section title="Disable user" if=!deleted>
  <@form method="POST" action="${uris.userStatus()}">
  <p>
    <@rowin>
      <input type="hidden" name="status" value="DISABLED" />
      <input type="submit" value="Disable" />
    </@rowin>
  </p>
  </@form>
</@section>
</#if>
<#if user.status != 'ENABLED'>
<@section title="Enable user" if=!deleted>
  <@form method="POST" action="${uris.userStatus()}">
  <p>
    <@rowin>
      <input type="hidden" name="status" value="ENABLED" />
      <input type="submit" value="Enable" />
    </@rowin>
  </p>
  </@form>
</@section>
</#if>
</#if>


<#-- SECTION Delete user -->
<#if user.userName != 'admin'>
<@section title="Delete user" if=!deleted>
  <@form method="DELETE" action="${uris.user()}">
  <p>
    <@rowin><input type="submit" value="Delete" /></@rowin>
  </p>
  </@form>
</@section>
</#if>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.users()}">User search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
