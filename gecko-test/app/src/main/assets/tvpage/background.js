let port;
browser.runtime.onConnect.addListener(connected => {
  port = connected;
  port.onMessage.addListener(message => {
    browser.tabs.query({active: true, currentWindow: true}).then(tabs => {
      if (!tabs[0]) return;
      browser.tabs.sendMessage(tabs[0].id, message).then(result => port.postMessage({result: result || "none"}));
    });
  });
});
