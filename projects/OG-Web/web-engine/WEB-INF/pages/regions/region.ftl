<#escape x as x?html>
<@page title="Region - ${region.name}">

<@section css="info" if=deleted>
  <p>This region has been deleted</p>
</@section>


<#-- SECTION Region output -->
<@section title="Region">
  <p>
    <@rowout label="Name">${region.name}</@rowout>
    <@rowout label="Reference">${region.uniqueId.value}, version ${region.uniqueId.version}, <a href="${uris.regionVersions()}">view history</a></@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail">
    <@rowout label="Full name">${region.fullName}</@rowout>
    <@rowout label="Classification">${region.classification}</@rowout>
    <@rowout label="Country code" if=region.country??>${region.country}</@rowout>
<#list region.externalIdBundle.externalIds as item>
    <@rowout label="Key">${item.scheme.name} - ${item.value}</@rowout>
</#list>
</@subsection>

<@subsection title="Parent regions">
  <@table items=regionParents paging=paging empty="No parents" headers=["Name","Reference","Version valid from","Actions"]; item>
      <td><a href="${uris.region(item.region)}">${item.region.name}</a></td>
      <td>${item.uniqueId.value}</td>
      <td>${item.versionFromInstant}</td>
      <td><a href="${uris.region(item.region)}">View</a></td>
  </@table>
</@subsection>

<@subsection title="Child regions">
  <@table items=regionChildren paging=paging empty="No children" headers=["Name","Reference","Version valid from","Actions"]; item>
      <td><a href="${uris.region(item.region)}">${item.region.name}</a></td>
      <td>${item.uniqueId.value}</td>
      <td>${item.versionFromInstant}</td>
      <td><a href="${uris.region(item.region)}">View</a></td>
  </@table>
</@subsection>
</@section>


<#-- SECTION Update region -->
<@section title="Update region" if=!deleted>
  <@form method="PUT" action="${uris.region()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${region.name}" /></@rowin>
    <@rowin label="Full name"><input type="text" size="30" maxlength="80" name="fullname" value="${region.fullName}" /></@rowin>
    <@rowin label="Classification"><input type="text" size="30" maxlength="80" name="classification" value="${region.classification}" /></@rowin>
    <@rowin label="Country ISO"><input type="text" size="30" maxlength="80" name="country" value="" /></@rowin>
    <@rowin label="Currency ISO"><input type="text" size="30" maxlength="80" name="currency" value="" /></@rowin>
    <@rowin label="Time zone"><input type="text" size="30" maxlength="80" name="timezone" value="" /></@rowin>
    <@rowin><input type="submit" value="Update" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Add region -->
<@section title="Add region" if=!deleted>
  <@form method="POST" action="${uris.region()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="" /></@rowin>
    <@rowin label="Full name"><input type="text" size="30" maxlength="80" name="fullname" value="" /></@rowin>
    <@rowin label="Classification"><input type="text" size="30" maxlength="80" name="classification" value="" /></@rowin>
    <@rowin label="Country ISO"><input type="text" size="30" maxlength="80" name="country" value="" /></@rowin>
    <@rowin label="Currency ISO"><input type="text" size="30" maxlength="80" name="currency" value="" /></@rowin>
    <@rowin label="Time zone"><input type="text" size="30" maxlength="80" name="timezone" value="" /></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Delete region -->
<@section title="Delete region" if=!deleted>
  <@form method="DELETE" action="${uris.region()}">
  <p>
    <@rowin><input type="submit" value="Delete" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.regionVersions()}">History of this region</a><br />
    <a href="${uris.regions()}">Region search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
