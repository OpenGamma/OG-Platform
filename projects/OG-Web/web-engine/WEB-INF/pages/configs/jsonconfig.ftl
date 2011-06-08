{
    "templateData": {
<#if configJSON??>
      "configJSON":${configJSON},
</#if>
<#if configXML??>
      "configXML":"${configXML}",
</#if>
<#if deleted>
      "deleted":"${configDoc.versionToInstant}",
</#if>
      "name":"${configDoc.name}",
      "objectId":"${configDoc.uniqueId.objectId}",
      "versionId":"${configDoc.uniqueId.version}",
<#-- deprecated -->
      "uniqueId":{"Value":"${configDoc.uniqueId.value}","Scheme":"${configDoc.uniqueId.scheme}","Version":"${configDoc.uniqueId.version}"},
      "type":"${type}"
    }
}