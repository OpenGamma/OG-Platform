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
            /* timeseries */
            var timeseries = function (obj) {
                return; // TODO: PLAT-1772
                api.rest.timeseries.get({
                    handler: function (timeseries_data) {
                        console.log(timeseries_data);
                    },
                    id: ''
                });
            };
            api.rest.positions.get({
                dependencies: ['id', 'node'],
                handler: function (result) {
                    if (result.error) return alert(result.message);
                    api.text({module: 'og.views.gadgets.positions', handler: function (template) {
                        var args = routes.current();
                        $(config.selector).html($.tmpl(template, $.extend(result.data, {editable: config.editable})))
                            .hide().fadeIn();
                        timeseries(result);
                        if ((!args.version || args.version === '*') && config.editable) {
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