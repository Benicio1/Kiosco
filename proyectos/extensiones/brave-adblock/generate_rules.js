const fs = require('fs');
const path = require('path');

const domains = [
  // Google Ads & Tracking
  "doubleclick.net",
  "googlesyndication.com",
  "googleadservices.com",
  "google-analytics.com",
  "adservice.google.com",
  "pagead2.googlesyndication.com",
  "partner.googleadservices.com",
  "tpc.googlesyndication.com",
  "www-google-analytics.l.google.com",
  "admob.com",

  // Sololatino & Streaming Specific Ad Rotation Networks
  "lianoidmachar.com",
  "constbezzant.com",
  "saltcatthebaic.com",
  "kishkeenocyte.com",
  "primasphobist.com",
  "densenbl.com",

  // Streaming & Movie Sites Popunder Networks (Aggressive Redirects)
  "highperformancegate.com",
  "effectivecpmgate.com",
  "profitablegatecpm.com",
  "alwingulla.com",
  "deloton.com",
  "coalsoupservant.com",
  "grotesquemirror.com",
  "monetag.com",
  "propellerads.com",
  "propeller-tracking.com",
  "propu.sh",
  "popads.net",
  "popcash.net",
  "adsterra.com",
  "onclickperformance.com",
  "clickadu.com",
  "clckd.net",
  "exoclick.com",
  "exosrv.com",
  "realsrv.com",
  "tsyndicate.com",
  "hilltopads.com",
  "hilltopads.net",
  "galaksion.com",
  "galaksionnetwork.com",
  "evadav.com",
  "rollerads.com",
  "richads.com",
  "richpush.co",
  "ad-maven.com",
  "admaven.com",
  "adbull.me",
  "adk2x.com",
  "yllix.com",
  "yepads.com",
  "traffichaus.com",
  "trafficjunky.com",
  "juicyads.com",
  "ero-advertising.com",
  "poptm.com",
  "ad-delivery.net",
  "deliveryengine.ad",
  "deliveryengine2.ad",
  "notix.co",
  "pushassist.com",
  "subscribers.com",
  "webpushs.com",
  "pushame.com",
  "onclkds.com",
  "onclickalgo.com",
  "greatdisasterrevolve.com",
  "whos.amung.us",
  "histats.com",

  // Casino / Betting Ad Networks and Redirect Destinations
  "1xbet.com",
  "1xbet-new.com",
  "bet365.com",
  "betfair.com",
  "pin-up.bet",
  "pin-up.casino",
  "pin-up.world",
  "melbet.com",
  "mostbet.com",
  "betwinner.com",
  "spinamba.com",
  "vulkanvegas.com",
  "vulkan.bet",
  "icecasino.com",
  "verde-casino.com",
  "1win.pro",
  "1win.run",
  "1win.zone",
  "1win.direct",
  "stake.com",
  "bc.game",
  "roobet.com",

  // Common Redirectors, Shorteners & Ad Gateways
  "ouo.io",
  "ouo.press",
  "shorte.st",
  "adshrink.it",
  "exe.io",
  "shrinkme.io",
  "fc.lc",
  "shortzone.net",
  "cutpaid.com",
  "shrinkearn.com",

  // Major Ad Exchanges & Networks
  "adnxs.com",
  "ib.adnxs.com",
  "criteo.com",
  "criteo.net",
  "taboola.com",
  "outbrain.com",
  "rubiconproject.com",
  "pubmatic.com",
  "openx.net",
  "amazon-adsystem.com",
  "aax.amazon-adsystem.com",
  "adroll.com",
  "adform.net",
  "sovrn.com",
  "bidswitch.net",
  "casalemedia.com",
  "sharethrough.com",
  "triplelift.com",
  "media.net",
  "smartadserver.com",
  "yieldmo.com",
  "teads.tv",
  "appnexus.com",
  "buysellads.com",
  "carbonads.net",
  "mgid.com",
  "revcontent.com",
  "richaudience.com",
  "seedtag.com",
  "adpushup.com",
  "infolinks.com",
  "undertone.com",
  "zemanta.com",
  "indexexchange.com",
  "spotxchange.com",
  "spotx.tv",
  "stickyadstv.com",
  "unrulymedia.com",
  "exponential.com",
  "conversantmedia.com",
  "gumgum.com",
  "nativo.com",
  "kargo.com",
  "monetizemore.com",
  "setupad.com",
  "ezoic.com",
  "ezoic.net",
  "adkeeper.com",
  "adkeeper.co.uk",
  "zergnet.com",
  "adcash.com",
  "clicksor.com",
  "bidvertiser.com",
  "popmyads.com",
  "revenuehits.com",

  // Adult Redirects common in Latin streaming sites
  "bongacams.com",
  "chaturbate.com",
  "stripchat.com",
  "livejasmin.com",

  // Trackers & Telemetry
  "scorecardresearch.com",
  "quantserve.com",
  "hotjar.com",
  "moatads.com",
  "crazyegg.com",
  "fullstory.com",
  "mouseflow.com",
  "clarity.ms",
  "newrelic.com",
  "segment.io",
  "mixpanel.com",
  "branch.io",
  "adjust.com",
  "appsflyer.com",
  "optimizely.com",
  "chartbeat.com",
  "parsely.com",
  "yandex.ru/metrika",
  "mc.yandex.ru"
];

const patterns = [
  // Sololatino zone scripts
  "*://*/*/149138*",
  "*://*/*/149139*",
  "*://*/*/149160*",
  "*://*/*/149161*",
  "*://*/*149138*",
  "*://*/*149139*",
  "*://*/*149160*",
  "*://*/*149161*",

  // Generic ad & popunder scripts
  "*/ads.js*",
  "*/ads.min.js*",
  "*/advert.js*",
  "*/pagead/*",
  "*/prebid*.js*",
  "*/popunder*.js*",
  "*/popup*.js*",
  "*/pop.js*",
  "*/poptm*.js*",
  "*/adsterra*.js*",
  "*/propeller*.js*",
  "*://*/adservice/*",
  "*://*/*ad_banner*",
  "*://*/*banner_ad*",
  "*://*/*clicktag*",
  "*://*/*adjump*",
  "*://connect.facebook.net/*/fbevents.js*",
  "*://static.ads-twitter.com/uwt.js*",
  "*://snap.licdn.com/li.lms-analytics/insight.min.js*"
];

// CRITICAL: main_frame included to cancel redirect navigations and popunder tabs
const resourceTypes = [
  "main_frame",
  "sub_frame",
  "stylesheet",
  "script",
  "image",
  "font",
  "object",
  "xmlhttprequest",
  "ping",
  "media",
  "websocket",
  "other"
];

let ruleId = 1;
const rules = [];

for (const domain of domains) {
  rules.push({
    id: ruleId++,
    priority: 1,
    action: { type: "block" },
    condition: {
      urlFilter: `||${domain}^`,
      resourceTypes
    }
  });
}

for (const pattern of patterns) {
  rules.push({
    id: ruleId++,
    priority: 1,
    action: { type: "block" },
    condition: {
      urlFilter: pattern,
      resourceTypes
    }
  });
}

const outputPath = path.join(__dirname, 'rules', 'ad_rules.json');
fs.writeFileSync(outputPath, JSON.stringify(rules, null, 2), 'utf-8');
console.log(`Generated ${rules.length} rules successfully at ${outputPath}`);
