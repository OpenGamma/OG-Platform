/**
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.slickgrid.formatters.portfolios',
    dependencies: ['og.common.routes'],
    obj: function () {
        return function (row, cell, value, columnDef, dataContext) {
            return dataContext.name
                + '<div class="og-button"><div class="OG-icon og-icon-delete"></div></div>'
        };
    }
});