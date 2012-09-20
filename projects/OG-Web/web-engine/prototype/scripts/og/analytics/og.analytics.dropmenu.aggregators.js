
$.register_module({
    name: 'og.analytics.dropmenu.aggregators',
    dependencies: ['og.analytics.dropmenu'],
    obj: function () { 
        return function (config) {
            var Dropmenu = this;
            return Dropmenu = new og.analytics.dropmenu(config), Dropmenu;
        }
    }
});