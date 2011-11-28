/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.gadgets.positions',
    dependencies: [],
    obj: function () {
        var common = og.common, views = og.views, routes = common.routes, api = og.api;
        return function (config) {
            api.rest.positions.get({
                handler: function (result) {
                    console.log(config);
                    if (result.error) return alert(result.message);
                    api.text({module: 'og.views.gadgets.positions', handler: function (template) {
                        $(config.selector).html($.tmpl(template, $.extend(result.data, {editable: config.editable})));
                        var args = routes.current();
                        if (!args.version || args.version === '*') {
                            if (!config.editable) return;
                            common.util.ui.content_editable({
                                attribute: 'data-og-editable',
                                handler: function () {
                                    views.positions.search(args), routes.handler();
                                }
                            });
                        }
                    }});
                },
                id: config.id,
                cache_for: 10000,
                loading: function () {}
            });
        }
    }
});