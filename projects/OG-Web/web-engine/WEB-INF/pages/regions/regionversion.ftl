<#escape x as x?html>
<@page title="Region - ${region.name}">

<@section css="info" if=deleted>
  <p>This region has been deleted</p>
</@section>


<#-- SECTION Region output -->
<@section title="Region">
  <p>
    <@rowout label="Name">${region.name}</@rowout>
    <@rowout label="Reference">${region.uniqueId.value}, version ${region.uniqueId.version}, <a href="${uris.regionVersions()}">view history</a>
</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail">
    <@rowout label="Full name">${region.fullName}</@rowout>
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


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.regionVersions()}">All versions</a><br />
    <a href="${uris.region()}">Latest version - ${latestRegion.uniqueId.version}</a><br />
    <a href="${uris.regions()}">Region search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
