/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.gadgets.securities_identifiers',
    dependencies: ['og.common.util.ui.dialog'],
    obj: function () {
        var ui = og.common.util.ui, api = og.api, dependencies = ['id'];
        return function (config) {
            var version = config.version !== '*' ? config.version : void 0,
                height = config.height || 400, selector = config.selector,
                tbody = '.og-js-gadgets-securities-identifiers';
            handler = function (result, template) {
                var identifiers = result.data.identifiers, id, html = [];
                $(selector).html(template);
                if (!Object.keys(identifiers)[0])
                    $(tbody).html('<tr><td><span>' + ''.lang() + '</span></td><td></td></tr>');
                else for (id in identifiers) {
                    if (identifiers.hasOwnProperty(id)) html.push('<tr><td><span>', id.lang(),
                        '<span></td><td>', identifiers[id].replace(id + '-', ''), '</td></tr>');
                    $(tbody).html(html.join(''));
                }
            };
            $.when(
                api.rest.securities.get({dependencies: dependencies, id: config.id, cache_for: 500, version: version}),
                api.text({module: 'og.views.gadgets.securities_identifiers'})
            ).then(handler);
        };
    }
});