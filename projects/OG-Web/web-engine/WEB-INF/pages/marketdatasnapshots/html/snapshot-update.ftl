<#escape x as x?html>
<@page title="Update - ${snapshotDoc.name}">


<#-- SECTION Update MarketDataSnapshot -->
<@section title="Update MarketDataSnapshot">
  <@form method="PUT" action="${uris.snapshot()}">
  <p>
    <#if err_nameMissing??><div class="err">The name must be entered</div></#if>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${snapshotDoc.name}" /></@rowin>
    <@rowin label="Market Data Snapshot (XML)">
      <div style="border:2px solid black;padding:2px;"><textarea rows="30" cols="120" name="snapshotxml" id="xmltextarea">${snapshotXml}</textarea></div>
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
  reindentOnLoad: true
});
</script>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.snapshots()}">MarketDataSnapshot home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
