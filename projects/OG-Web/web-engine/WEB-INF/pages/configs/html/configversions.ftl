<#escape x as x?html>
<@page title="Configuration history  - ${configDoc.name}">

<@section css="info" if=deleted>
  <p>This configuration has been deleted</p>
</@section>


<#-- SECTION Configuration output -->
<@section title="Configuration">
  <p>
    <@rowout label="Name">${configDoc.name}</@rowout>
    <@rowout label="Type">${configDescription}</@rowout>
    <@rowout label="Reference">${configDoc.uniqueId.value}</@rowout>
  </p>


<#-- SUBSECTION Main data -->
<@subsection title="History">
  <@table items=versionsResult.documents empty="No versions" headers=["Name","Reference","Valid from", "Valid to","Actions"]; item>
      <td><a href="${uris.configVersion(item.uniqueId)}">${item.name}</a></td>
      <td>${item.uniqueId.value} v${item.uniqueId.version}</td>
      <td>${item.versionFromInstant}</td>
      <td>${item.versionToInstant}</td>
      <td><a href="${uris.configVersion(item.uniqueId)}">View</a></td>
  </@table>
</@subsection>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.config()}">Latest version - ${configDoc.uniqueId.version}</a><br />
    <a href="${uris.configs()}">Configuration home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
