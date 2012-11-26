/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.SecuritiesIdentifiers',
    dependencies: [],
    obj: function () {
        var api = og.api, dependencies = ['id'], template, empty = ''.lang();
        return function (config) {
            var gadget = this, alive = og.common.id('gadget_securitiesidentifiers'),
                version = config.version !== '*' ? config.version : void 0;
            gadget.alive = function () {return !!$('.' + alive).length;};
            gadget.resize = $.noop;
            var render = function (result, html_template) {
                var ids = result.data.identifiers, keys = Object.keys(ids), data = {
                    alive: alive, empty: empty,
                    ids: keys.map(function (key) {return {key: key.lang(), value: ids[key].replace(key + '-', '')};})
                };
                $(config.selector).html((template || (template = Handlebars.compile(html_template)))(data))
                    .find('table').awesometable({height: config.height || 150});
                og.common.gadgets.manager.register(gadget);
            };
            $.when(
                api.rest.securities.get({dependencies: dependencies, id: config.id, cache_for: 500, version: version}),
                api.text({module: 'og.views.gadgets.securitiesidentifiers_tash'})
            ).then(render);
        };
    }
});