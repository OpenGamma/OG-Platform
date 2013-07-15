<#escape x as x?html>
{
    "template_data": {
        "obligorShortName": "${organization.obligor.obligorShortName}",
        "object_id": "${organization.uniqueId.objectId}",
        "version_id": "${organization.uniqueId.version}",
        <#if deleted>
        "deleted": "${organizationDoc.versionToInstant}",
        </#if>
        "obligorRedCode": "${organization.obligor.obligorREDCode}",
        "obligorTicker": "${organization.obligor.obligorTicker}",
        "region": "${organization.obligor.region.name()}",
        "country": "${organization.obligor.country}",
        "sector": "${organization.obligor.sector.name()}",
        "compositeRating": "${organization.obligor.compositeRating.name()}",
        "impliedRating": "${organization.obligor.impliedRating.name()}",
        "moodysCreditRating": "${organization.obligor.moodysCreditRating.name()}",
        "standardAndPoorsCreditRating": "${organization.obligor.standardAndPoorsCreditRating.name()}",
        "fitchCreditRating": "${organization.obligor.fitchCreditRating.name()}",
        "hasDefaulted": "${organization.obligor.hasDefaulted?string}"
    }
}
</#escape>