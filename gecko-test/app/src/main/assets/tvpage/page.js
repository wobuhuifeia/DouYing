(function () {
  const hideText = ["AI抖音", "朋友", "关注", "直播", "放映厅", "短剧", "小游戏"];
  function simplify() {
    document.querySelectorAll("a,button,div,span,li").forEach(node => {
      const text = (node.innerText || "").trim();
      if (hideText.includes(text)) (node.closest("li,a,button") || node).style.setProperty("display", "none", "important");
    });
  }
  function isFullscreen() {
    return document.fullscreenElement || document.querySelector(".xgplayer-is-cssfullscreen,.xgplayer-is-fullscreen");
  }
  function loginVisible() {
    return Array.from(document.querySelectorAll("div,section")).some(node => (node.innerText || "").includes("登录后免费畅享高清视频") && node.getBoundingClientRect().width > 300);
  }
  function fullscreen() {
    if (isFullscreen() || loginVisible()) return;
    const button = document.querySelector(".xgplayer-fullscreen,.xgplayer-cssfullscreen,[aria-label*='全屏'],[title*='全屏']");
    if (button) button.click();
  }
  function continuousPlay() {
    if (!isFullscreen() || loginVisible() || window.douyingContinuousDone) return;
    const label = Array.from(document.querySelectorAll("span,div,button")).find(node => (node.innerText || "").trim() === "连播" && node.children.length === 0);
    if (!label) return;
    const rect = label.getBoundingClientRect();
    if (rect.width < 10 || rect.bottom < innerHeight - 90) return;
    const target = document.elementFromPoint(rect.left - 24, rect.top + rect.height / 2);
    if (target) target.click();
    window.douyingContinuousDone = true;
  }
  setInterval(() => { simplify(); fullscreen(); continuousPlay(); }, 1000);
})();

