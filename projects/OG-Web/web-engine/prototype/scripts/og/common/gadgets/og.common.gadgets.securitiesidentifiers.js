/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.gadgets.SecuritiesIdentifiers',
    dependencies: [],
    obj: function () {
        var api = og.api, dependencies = ['id'], template, empty = ''.lang(),
            prefix = 'SecuritiesIdentifiers_', counter = 1;
        return function (config) {
            var gadget = this, height = config.height || 150, render, alive = prefix + counter++,
                version = config.version !== '*' ? config.version : void 0;
            gadget.alive = function () {return !!$('.' + alive).length;};
            gadget.resize = $.noop;
            render = function (result, html_template) {
                var ids = result.data.identifiers, keys = Object.keys(ids), data = {
                    alive: alive, empty: empty,
                    ids: keys.map(function (key) {return {key: key.lang(), value: ids[key].replace(key + '-', '')};})
                };
                $(config.selector).html((template || (template = Handlebars.compile(html_template)))(data))
                    .find('table').awesometable({height: height});
                og.common.gadgets.manager.register(gadget);
            };
            $.when(
                api.rest.securities.get({dependencies: dependencies, id: config.id, cache_for: 500, version: version}),
                api.text({module: 'og.views.gadgets.securitiesidentifiers_tash'})
            ).then(render);
        };
    }
});