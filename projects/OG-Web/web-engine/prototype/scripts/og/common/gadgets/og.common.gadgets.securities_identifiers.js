/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.gadgets.securities_identifiers',
    dependencies: [],
    obj: function () {
        var api = og.api, dependencies = ['id'], template, empty = ''.lang();
        return function (config) {
            var height = config.height || 150, version = config.version !== '*' ? config.version : void 0;
            handler = function (result, html_template) {
                var ids = result.data.identifiers, keys = Object.keys(ids), data = {
                    empty: empty,
                    ids: keys.map(function (key) {return {key: key.lang(), value: ids[key].replace(key + '-', '')};})
                };
                $(config.selector).html((template || (template = Handlebars.compile(html_template)))(data))
                    .find('table').awesometable({height: height});
            };
            $.when(
                api.rest.securities.get({dependencies: dependencies, id: config.id, cache_for: 500, version: version}),
                api.text({module: 'og.views.gadgets.securities_identifiers_tash'})
            ).then(handler);
        };
    }
});