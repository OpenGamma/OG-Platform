/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.blotter',
    dependencies: ['og.views.common.state', 'og.views.common.layout', 'og.common.routes', 'og.blotter.container'],
    obj: function () {
        var module = this, masthead = og.common.masthead, page_name = module.name.split('.').pop();
        console.log(page_name);
        masthead.menu.set_tab(page_name);
        var blotter = new og.blotter.Container();
    }
});