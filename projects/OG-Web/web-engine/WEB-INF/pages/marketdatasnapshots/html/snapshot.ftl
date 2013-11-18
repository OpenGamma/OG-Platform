<#escape x as x?html>
<@page title="MarketDataSnapshot - ${snapshotDoc.name}">

<@section css="info" if=deleted>
  <p>This snapshot has been deleted</p>
</@section>


<#-- SECTION MarketDataSnapshot output -->
<@section title="MarketDataSnapshot">
  <p>
    <@rowout label="Name">${snapshotDoc.name}</@rowout>
    <@rowout label="Reference">${snapshotDoc.uniqueId.value}, version ${snapshotDoc.uniqueId.version}, <a href="${uris.snapshotVersions()}">view history</a>
</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail" if=deleted>
    <@rowout label="Data"><textarea readonly style="width:650px;height:300px;">${snapshotXml}</textarea></@rowout>
</@subsection>
</@section>


<#-- SECTION Update snapshot -->
<@section title="Update snapshot" if=!deleted>
  <@form method="PUT" action="${uris.snapshot()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${snapshotDoc.name}" /></@rowin>
    <@rowin label="Market Data Snapshot (XML)">
      <div style="border:1px solid black;padding:2px;"><textarea rows="30" cols="120" name="snapshotxml" id="xmltextarea">${snapshotXml}</textarea></div>
    </@rowin>
    <@rowin><input type="submit" value="Update" /></@rowin>
<script type="text/javascript" src="/js/lib/codemirror/codemirror.js"></script>
<script type="text/javascript">
var editor = CodeMirror.fromTextArea("xmltextarea", {
  parserfile: ["parsexml.js"],
  path: "/js/lib/codemirror/",
  stylesheet: "/css/lib/codemirror/xmlcolors.css",
  width: "650px",
  height: "dynamic",
  minHeight: "300",
  reindentOnLoad: true,
  iframeClass: "xmleditor"
});
</script>
  </p>
  </@form>
</@section>


<#-- SECTION Delete snapshot -->
<@section title="Delete snapshot" if=!deleted>
  <@form method="DELETE" action="${uris.snapshot()}">
  <p>
    <@rowin><input type="submit" value="Delete" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.snapshotVersions()}">History of this snapshot</a><br />
    <a href="${uris.snapshots()}">MarketDataSnapshot home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
