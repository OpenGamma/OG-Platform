<#escape x as x?html>
{
    "header": {
        "type": "Time Series",
        <#if searchResult??>
        "pgIdx": ${"${paging.firstItem}"?replace(',','')},
        "pgSze": ${"${paging.pagingSize}"?replace(',','')},
        "pgTtl": ${"${paging.totalItems}"?replace(',','')},
        </#if>
        "dataFields": ["id","identifier","data_source","data_provider","data_field","observation_time"]
    },
    "data": [<#if searchResult??><#list searchResult.documents as doc>
        "${doc.info.uniqueId.objectId}|<#list sortedIds[doc.uniqueId] as identifier>${identifier.externalId}<#if identifier_has_next>, </#if></#list>|${doc.info.dataSource}|${doc.info.dataProvider}|${doc.info.dataField}|${doc.info.observationTime}"
    <#if doc_has_next>,</#if></#list> </#if>]
}
</#escape>