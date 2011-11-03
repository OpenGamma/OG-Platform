/**
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.portfolios_sync',
    dependencies: [
        'og.api.rest',
        'og.common.util.ui.message',
        'og.views.common.layout',
        'og.common.util.ui.Form'
    ],
    obj: function () {
        var PANEL = '.ui-layout-inner-south', CONTENT = PANEL + ' .ui-layout-content',
            ui = og.common.util.ui, Form = og.common.util.ui.Form, sync;
        return sync = {
            load: function (args) {
                og.api.rest.sync.get({
                    trades: args.id,
                    handler: function (result) {
                        var form;
                        ui.message({location: '.ui-layout-inner-south', destroy: true});
                        sync.setup();
                        sync.clear();
                        form = new Form({module: 'og.views.portfolios_sync.main', selector: PANEL});
                        form.dom();
                    },
                    loading: function () {
                        ui.message({
                            location: '.ui-layout-inner-south',
                            css: {left: 0},
                            message: {0: 'loading...', 3000: 'still loading...'}
                        });
                    }
                });
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