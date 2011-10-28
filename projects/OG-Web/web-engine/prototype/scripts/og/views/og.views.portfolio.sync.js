/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.portfolio.sync',
    dependencies: ['og.common.util.ui.message', 'og.views.common.layout'],
    obj: function () {
        var PANNEL = '.ui-layout-inner-south',
            CONTENT = PANNEL + ' .ui-layout-content',
            sync;
        return sync = {
            load: function () {
                var Form = og.common.util.ui.Form, form;
                sync.setup();
                sync.clear();
                if (!og.common.routes.current().args.id) {sync.clear()}
                new Form({
                    module: 'og.views.portfolios.sync', selector: PANNEL,
                    handlers: [{type: 'form:load', handler: function () {}}]
                }).dom();
            },
            clear: function () {$(CONTENT).empty()},
            setup: function () {
                $(PANNEL).removeClass(function (i , classes) {
                    var matches = classes.match(/OG-(?:.+)/g) || [];
                    return matches.join(' ');
                }).addClass('OG-sync');
            }
        }
    }
});