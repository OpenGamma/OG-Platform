<#escape x as x?html>
<@page title="Update - ${snapshotDoc.name}" jquery=true aceXmlEditor=true>


<#-- SECTION Update MarketDataSnapshot -->
<@section title="Update MarketDataSnapshot">
  <@form method="PUT" action="${uris.snapshot()}" id="updateForm">
  <p>
    <#if err_nameMissing??><div class="err">The name must be entered</div></#if>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${snapshotDoc.name}" /></@rowin>
    <@rowin>
      <div id="ace-xml-editor">${snapshotXml}</div>
    </@rowin>
    <input type="hidden" name="snapshotxml" id="snapshot-xml"/>
    <@rowin><input type="submit" value="Update" /></@rowin>
<script type="text/javascript">
var editor = ace.edit("ace-xml-editor")
editor.getSession().setMode('ace/mode/xml')
$("#ace-xml-editor").show()

$("#updateForm").submit( function(eventObj) {
  $("#snapshot-xml").val(editor.getSession().getValue())
  return true
})
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
