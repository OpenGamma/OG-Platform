/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.error',
    dependencies: ['og.api.common', 'og.api.rest'],
    obj: function () {
        var api = og.api.rest;
        return {
            fire : function (update) {
                api.views.error.get({view_id : update.split('/')[3]}).pipe(function (result) {
                    if (result.data) {
                        var message = 'Fatal error: ' + result.data[0].errorMessage;
                        og.common.util.ui.dialog({type: 'error', message: message });
                        //api.fire('disconnect');
                        //api.fire('fatal');
                    }
                });

            }
        };
    }
});