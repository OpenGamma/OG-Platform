/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.blotter',
    dependencies: ['og.views.common.state', 'og.views.common.layout', 'og.common.routes'],
    obj: function () {
        var module = this, dialog, masthead = og.common.masthead, page_name = module.name.split('.').pop();

        return view = {
            init : function () {
                masthead.menu.set_tab(page_name);
                dialog = new og.blotter.Container();
            }
        };
    }
});