/**
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.portfolios_sync',
    dependencies: [
        'og.common.util.ui.message',
        'og.views.common.layout',
        'og.common.util.ui.Form'
    ],
    obj: function () {
        var PANEL = '.ui-layout-inner-south', CONTENT = PANEL + ' .ui-layout-content',
            Form = og.common.util.ui.Form, sync;
        return sync = {
            load: function (args) {
                var form;
                sync.setup();
                sync.clear();
                og.api.rest.sync.get({trades: args.id, handler: console.log});
                form = new Form({module: 'og.views.portfolios_sync.main', selector: PANEL});
                form.dom();
            },
            clear: function () {$(CONTENT).empty();},
            setup: function () {
                $(PANEL).removeClass(function (i , classes) {
                    var matches = classes.match(/OG-(?:.+)/g) || [];
                    return matches.join(' ');
                }).addClass('OG-sync');
            }
        }
    }
});