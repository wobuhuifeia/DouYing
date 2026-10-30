(function(){'use strict';
function prewarm(){document.querySelectorAll('video').forEach(function(v){try{v.preload='auto';v.setAttribute('playsinline','true');v.setAttribute('webkit-playsinline','true');}catch(e){}})}
var hideText=['AI抖音','朋友','关注','直播','放映厅','短剧','小游戏'];
function simplify(){document.querySelectorAll('a,button,div,span,li').forEach(function(n){var t=(n.innerText||'').trim();if(hideText.indexOf(t)>=0)(n.closest('li,a,button')||n).style.setProperty('display','none','important');});}
new MutationObserver(prewarm).observe(document.documentElement,{childList:true,subtree:true});
setInterval(function(){prewarm();simplify();},500); prewarm(); simplify();
document.addEventListener('keydown', function(e){
  if (e.key !== 'ArrowLeft' && e.key !== 'ArrowRight') return;
  var video = activeVideo();
  if (!video || !isFinite(video.currentTime)) return;
  e.preventDefault(); e.stopPropagation();
  video.currentTime = Math.max(0, video.currentTime + (e.key === 'ArrowRight' ? 5 : -5));
}, true);
var speedIndex=0, speeds=[1,1.25,1.5];
function allVideos(){return Array.from(document.querySelectorAll('video'));}
function activeVideo(){
 var list=allVideos();
 return list.find(function(v){var r=v.getBoundingClientRect();return r.width>20&&r.height>20&&r.bottom>0&&r.top<innerHeight;})||list[0];
}
document.addEventListener('keydown',function(e){
 if(e.key==='ArrowUp'||e.key==='ArrowDown') allVideos().forEach(function(v){try{v.pause();}catch(x){}});
 if(e.key==='f'||e.key==='F'){
  e.preventDefault();e.stopImmediatePropagation();speedIndex=(speedIndex+1)%speeds.length;
  allVideos().forEach(function(v){try{v.playbackRate=speeds[speedIndex];}catch(x){}});
 }
},true);
window.addEventListener('pagehide',function(){allVideos().forEach(function(v){try{v.pause();}catch(x){}});});
})();
