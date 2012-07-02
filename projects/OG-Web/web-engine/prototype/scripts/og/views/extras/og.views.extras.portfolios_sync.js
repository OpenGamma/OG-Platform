/**
 * Copyright 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.extras.portfolios_sync',
    dependencies: [
        'og.api.rest',
        'og.common.util.ui.message',
        'og.views.common.layout',
        'og.common.util.ui.Form'
    ],
    obj: function () {
        var PANEL = '.OG-layout-admin-details-south', CONTENT = PANEL + ' .ui-layout-content', ORIG = 'origDealValue',
            ui = og.common.util.ui, Form = og.common.util.ui.Form, sync;
        return sync = {
            load: function (args) {
                og.api.rest.sync.get({
                    trades: args.id,
                    handler: function (result) {
                        var form;
                        ui.message({location: PANEL, destroy: true});
                        if (result.error) return ui.dialog({type: 'error', message: result.message});
                        sync.setup();
                        sync.clear();
                        form = new Form({
                            module: 'og.views.portfolios.sync', selector: PANEL,
                            data: result.data,
                            extras: {
                                data: result.data.data,
                                show_orig_col: result.data.data.reduce(function (acc, val) {
                                    return acc ||
                                        !!val.fields.reduce(function (acc, val) {return acc || val[ORIG];}, false);
                                }, false)
                            },
                            handlers: [{type: 'form:submit', handler: function (result) {
                                // console.log(result);
                            }}]
                        });
                        form.dom();
                    },
                    loading: function () {
                        ui.message({
                            location: PANEL, css: {left: 0}, message: {0: 'loading...', 3000: 'still loading...'}
                        });
                    }
                });
            },
            clear: function () {$(CONTENT).empty();},
            setup: function () {$(PANEL).removeClass('OG-versions').addClass('OG-sync');}
        }
    }
});