/// @export "setup"
var casper = require('casper').create({
        viewportSize : {width : 1200, height : 1500}
});

/// @export "initial"
casper.start("http://localhost:8080", function() {
    this.capture("dexy--initial.png");
});

/// @export "exchanges"
casper.then(function() {
    this.click("a.og-exchanges");
    this.wait(500);
});

casper.then(function() {
    this.capture("dexy--exchanges.png");
});

/// @export "portfolio"
casper.then(function() {
    this.click("a.og-portfolios");
    this.wait(500);
});

casper.then(function() {
    this.click("div[row=\"4\"] .og-link");
    this.wait(500);
});
    
casper.then(function() {
    this.capture("dexy--portfolio.png");
});

/// @export "run"
casper.run();
