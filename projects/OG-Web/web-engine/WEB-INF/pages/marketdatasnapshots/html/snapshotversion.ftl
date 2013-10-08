<#escape x as x?html>
<@page title="MarketDataSnapshot - ${snapshotDoc.name}">

<@section css="info" if=deleted>
  <p>This MarketDataSnapshot has been deleted</p>
</@section>


<#-- SECTION MarketDataSnapshot output -->
<@section title="MarketDataSnapshot">
  <p>
    <@rowout label="Name">${snapshotDoc.name}</@rowout>
    <@rowout label="Reference">${snapshotDoc.uniqueId.value}, version ${snapshotDoc.uniqueId.version}
</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail">
    <div style="border:1px solid black;padding:2px;"><textarea rows="30" cols="120" name="snapshotxml" id="xmltextarea">${snapshotXml}</textarea></div>
</@subsection>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.snapshotVersions()}">All versions</a><br />
    <a href="${uris.snapshot()}">Latest version - ${latestSnapshotDoc.uniqueId.version}</a><br />
    <a href="${uris.snapshots()}">MarketDataSnapshot home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
