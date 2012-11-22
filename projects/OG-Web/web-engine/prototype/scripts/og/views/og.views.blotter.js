/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.blotter',
    dependencies: [],
    obj: function () {
        var view, module = this, dialog, masthead = og.common.masthead, page_name = module.name.split('.').pop();
        return view = {
            init: function () {
                masthead.menu.set_tab(page_name);
                dialog = new og.blotter.Dialog(og.blotter.mapping.forms.VAN_SWAP);
            }
        };
    }
});