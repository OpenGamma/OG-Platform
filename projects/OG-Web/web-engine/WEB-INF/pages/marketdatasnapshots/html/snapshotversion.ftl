<#escape x as x?html>
<@page title="MarketDataSnapshot - ${snapshotDoc.name}" jquery=true aceXmlEditor=true>

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
    <div id="ace-xml-editor"></div>
</@subsection>

<#noescape><@xmlEditorScript  xmlValue="${snapshotXml}" readOnly=true></@xmlEditorScript></#noescape>
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
