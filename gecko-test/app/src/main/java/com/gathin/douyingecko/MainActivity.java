package com.gathin.douyingecko;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.GeckoView;
public class MainActivity extends Activity {
 private static final String HOME = "https://www.douyin.com/?recommend=1&from_nav=1";
 private static GeckoRuntime runtime;
 private final Handler handler = new Handler(Looper.getMainLooper());
 private GeckoSession session;
 private GeckoView view;
 private View cursor;
 private boolean pointerMode;
 private float pointerX;
 private float pointerY;
 private boolean exitAsked;
 private void note(String message) {
  try (FileOutputStream output = new FileOutputStream(new File(getExternalFilesDir(null), "playback-diagnostics.txt"), true)) {
   output.write((System.currentTimeMillis() + " " + message + "\n").getBytes(StandardCharsets.UTF_8));
  } catch (Exception ignored) {}
 }
 private void fail(Throwable error) {
  note("INIT ERROR " + error);
  TextView text = new TextView(this);
  text.setText("独立引擎启动失败：\n" + error);
  text.setTextSize(18);
  text.setPadding(48, 48, 48, 48);
  setContentView(text);
 }
 private boolean send(int code) {
  if (session == null) return false;
  session.getTextInput().onKeyDown(code, new KeyEvent(KeyEvent.ACTION_DOWN, code));
  session.getTextInput().onKeyUp(code, new KeyEvent(KeyEvent.ACTION_UP, code));
  return true;
 }
 private void placeCursor() {
  if (cursor == null) return;
  int size = Math.max(18, (int) (18 * getResources().getDisplayMetrics().density));
  FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
  params.leftMargin = Math.round(pointerX - size / 2f);
  params.topMargin = Math.round(pointerY - size / 2f);
  cursor.setLayoutParams(params);
  cursor.setVisibility(pointerMode ? View.VISIBLE : View.GONE);
 }
 private void movePointer(int dx, int dy) {
  if (view == null) return;
  float step = 42 * getResources().getDisplayMetrics().density;
  pointerX = Math.max(0, Math.min(view.getWidth() - 1, pointerX + dx * step));
  pointerY = Math.max(0, Math.min(view.getHeight() - 1, pointerY + dy * step));
  placeCursor();
 }
 private void clickPointer() {
  if (view == null) return;
  long now = SystemClock.uptimeMillis();
  MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, pointerX, pointerY, 0);
  MotionEvent up = MotionEvent.obtain(now, now + 40, MotionEvent.ACTION_UP, pointerX, pointerY, 0);
  view.getPanZoomController().onMouseEvent(down);
  view.getPanZoomController().onMouseEvent(up);
  down.recycle();
  up.recycle();
 }

 private void autoFullscreen() {
  if (view == null || view.getWidth() == 0) return;
  long now = SystemClock.uptimeMillis();
  float x = view.getWidth() - 54f;
  float y = view.getHeight() - 62f;
  MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, x, y, 0);
  MotionEvent up = MotionEvent.obtain(now, now + 40, MotionEvent.ACTION_UP, x, y, 0);
  view.getPanZoomController().onMouseEvent(down);
  view.getPanZoomController().onMouseEvent(up);
  down.recycle();
  up.recycle();
 }
 private void confirmExit() {
  if (exitAsked) { finish(); return; }
  exitAsked = true;
  AlertDialog dialog = new AlertDialog.Builder(this)
   .setTitle("退出抖映")
   .setMessage("再按确认退出")
   .setPositiveButton("退出", (d, w) -> finish())
   .setNegativeButton("取消", (d, w) -> exitAsked = false)
   .setOnCancelListener(d -> exitAsked = false)
   .create();
  dialog.show();
  dialog.getButton(AlertDialog.BUTTON_POSITIVE).requestFocus();
 } private void toggleMode() {
  pointerMode = !pointerMode;
  if (pointerMode && view != null && pointerX == 0 && pointerY == 0) {
   pointerX = view.getWidth() / 2f;
   pointerY = view.getHeight() / 2f;
  }
  placeCursor();
  Toast.makeText(this, pointerMode ? "光标模式：方向键移动，确认点击，菜单切回" : "上下键模式：上下切视频，确认暂停", Toast.LENGTH_SHORT).show();
 }
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  getWindow().getDecorView().setSystemUiVisibility(5894);
  try {
   if (runtime == null) {
    File config = new File(getFilesDir(), "gecko-config.yaml");
    String yaml = "prefs:\n  media.hardware-video-decoding.enabled: true\n  media.android-media-codec.enabled: true\n  media.ffvpx.enabled: true\n  media.autoplay.default: 0\n  media.autoplay.blocking_policy: 0\n";
    try (FileOutputStream output = new FileOutputStream(config)) { output.write(yaml.getBytes(StandardCharsets.UTF_8)); }
    runtime = GeckoRuntime.create(this, new GeckoRuntimeSettings.Builder().configFilePath(config.getAbsolutePath()).build());
    runtime.getWebExtensionController().ensureBuiltIn("resource://android/assets/tvpage/", "tvpage@douying");
   }
   FrameLayout root = new FrameLayout(this);
   view = new GeckoView(this);
   root.addView(view, new FrameLayout.LayoutParams(-1, -1));
   cursor = new View(this);
   GradientDrawable dot = new GradientDrawable();
   dot.setShape(GradientDrawable.OVAL);
   dot.setColor(Color.WHITE);
   dot.setStroke(Math.max(2, (int) (2 * getResources().getDisplayMetrics().density)), Color.BLACK);
   cursor.setBackground(dot);
   cursor.setVisibility(View.GONE);
   root.addView(cursor);
   setContentView(root);
   session = new GeckoSession();
   session.getSettings().setUserAgentMode(GeckoSessionSettings.USER_AGENT_MODE_DESKTOP);
   session.getSettings().setViewportMode(GeckoSessionSettings.VIEWPORT_MODE_DESKTOP);
   session.setContentDelegate(new GeckoSession.ContentDelegate() {
    @Override public void onTitleChange(GeckoSession changed, String title) { note("title " + title); }
    @Override public void onCrash(GeckoSession crashed) { note("GECKO CRASH"); Toast.makeText(MainActivity.this, "网页引擎崩溃，请重新打开", Toast.LENGTH_LONG).show(); }
    @Override public void onKill(GeckoSession killed) { note("GECKO KILL"); }
   });
   session.open(runtime);
   view.setSession(session);
   view.requestFocus();
   session.loadUri(HOME);
   handler.post(() -> { pointerX = view.getWidth() / 2f; pointerY = view.getHeight() / 2f; placeCursor(); });
   Toast.makeText(this, "上下切视频，确认暂停，菜单切光标，返回后确认退出", Toast.LENGTH_LONG).show();
   handler.postDelayed(this::autoFullscreen, 5000);
   handler.postDelayed(this::autoFullscreen, 9000);
  } catch (Throwable error) { fail(error); }
 }
 @Override public boolean dispatchKeyEvent(KeyEvent event) {
  int code = event.getKeyCode();
  if (event.getAction() != KeyEvent.ACTION_DOWN) {
   if (code == KeyEvent.KEYCODE_DPAD_UP || code == KeyEvent.KEYCODE_DPAD_DOWN || code == KeyEvent.KEYCODE_DPAD_LEFT || code == KeyEvent.KEYCODE_DPAD_RIGHT || code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER || code == KeyEvent.KEYCODE_MENU || code == KeyEvent.KEYCODE_BACK) return true;
   return super.dispatchKeyEvent(event);
  }
  if (code == KeyEvent.KEYCODE_MENU) { toggleMode(); return true; }
  if (pointerMode) {
   if (code == KeyEvent.KEYCODE_DPAD_UP) { movePointer(0, -1); return true; }
   if (code == KeyEvent.KEYCODE_DPAD_DOWN) { movePointer(0, 1); return true; }
   if (code == KeyEvent.KEYCODE_DPAD_LEFT) { movePointer(-1, 0); return true; }
   if (code == KeyEvent.KEYCODE_DPAD_RIGHT) { movePointer(1, 0); return true; }
   if (code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER) { clickPointer(); return true; }
  } else {
   if (code == KeyEvent.KEYCODE_DPAD_UP || code == KeyEvent.KEYCODE_DPAD_DOWN) return send(code);
   if (code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER) return send(KeyEvent.KEYCODE_SPACE);
  }
  if (code == KeyEvent.KEYCODE_BACK || (exitAsked && (code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER))) { confirmExit(); return true; }
  return super.dispatchKeyEvent(event);
 }
 @Override protected void onDestroy() {
  if (session != null) session.close();
  super.onDestroy();
 }
}


