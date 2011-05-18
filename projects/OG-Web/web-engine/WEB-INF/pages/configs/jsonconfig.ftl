{
    "templateData": {
      "name":"${configDoc.name}",
      "objectId":"${configDoc.uniqueId.objectId}",
      "versionId":"${configDoc.uniqueId.version}",
<#-- deprecated -->
      "uniqueId":{"Value":"${configDoc.uniqueId.value}","Scheme":"${configDoc.uniqueId.scheme}","Version":"${configDoc.uniqueId.version}"},
<#if deleted>
      "deleted":"${configDoc.versionToInstant}",
</#if>
      "type":"${type}",
      "configData" : ${configData}
   	}
}