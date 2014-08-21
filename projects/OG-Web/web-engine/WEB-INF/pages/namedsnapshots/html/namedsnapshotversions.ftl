<#escape x as x?html>
<@page title="Snapshot history  - ${snapshotDoc.name}">

<@section css="info" if=deleted>
  <p>This snapshot has been deleted</p>
</@section>


<#-- SECTION Snapshot output -->
<@section title="Snapshot">
  <p>
    <@rowout label="Name">${snapshotDoc.name}</@rowout>
    <@rowout label="Type">${snapshotDescription}</@rowout>
    <@rowout label="Reference">${snapshotDoc.uniqueId.value}</@rowout>
  </p>


<#-- SUBSECTION Main data -->
<@subsection title="History">
  <@table items=versionsResult.documents empty="No versions" headers=["Name","Reference","Valid from", "Valid to","Actions"]; item>
      <td><a href="${uris.snapshotVersion(item.uniqueId)}">${item.name}</a></td>
      <td>${item.uniqueId.value} v${item.uniqueId.version}</td>
      <td>${item.versionFromInstant}</td>
      <td>${item.versionToInstant}</td>
      <td><a href="${uris.snapshotVersion(item.uniqueId)}">View</a></td>
  </@table>
</@subsection>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.snapshot()}">Latest version - ${snapshotDoc.uniqueId.version}</a><br />
    <a href="${uris.snapshots()}">Snapshot home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
