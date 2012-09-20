
$.register_module({
    name: 'og.analytics.dropmenu.datasources',
    dependencies: ['og.analytics.dropmenu'],
    obj: function () { 
        return function (config) {
            var Dropmenu = this;
            return Dropmenu = new og.analytics.dropmenu(config), Dropmenu;
        }
    }
});