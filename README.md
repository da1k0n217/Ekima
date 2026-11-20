# Tauri + SvelteKit + TypeScript

## Ekima の実装状況

現在のアプリ画面では、以下を実装しています。

- サンプル駅を含む駅一覧の表示
- 駅名・住所・緯度・経度による駅の追加と削除
- `localStorage` への駅データ保存
- ブラウザまたはAndroid WebViewの位置情報APIによる追跡開始・停止
- Haversine formulaによる最寄り駅判定
- GPSの一時的な揺れによる駅の切り替わりを抑制

Androidの位置情報・通知権限は [`AndroidManifest.xml`](src-tauri/gen/android/app/src/main/AndroidManifest.xml) に追加済みです。Android WebViewからKotlinの [`EkimaNotificationBridge`](src-tauri/gen/android/app/src/main/java/com/daikon/ekima/EkimaNotificationBridge.kt) を呼び出し、通知タイトルに最寄り駅名を設定した固定IDの継続通知を更新します。位置情報取得はSvelteのWebView Geolocation APIで行うため、アプリのタスクが残っている間（バックグラウンドを含む）だけ継続し、Activityの`onDestroy`でタスク終了時に通知を削除します。Foreground Serviceとネイティブ側の継続位置情報取得は実装していません。

通知ブリッジへの更新・削除要求はメインスレッドに移送してから `NotificationManager` を呼び出します。追跡開始後は Android の `EkimaLocationService` が Foreground Service として位置情報を取得し、アプリを小窓・全画面以外にしても通知を更新します。追跡を停止するとServiceも停止します。

This template should help get you started developing with Tauri, SvelteKit and TypeScript in Vite.

## Recommended IDE Setup

[VS Code](https://code.visualstudio.com/) + [Svelte](https://marketplace.visualstudio.com/items?itemName=svelte.svelte-vscode) + [Tauri](https://marketplace.visualstudio.com/items?itemName=tauri-apps.tauri-vscode) + [rust-analyzer](https://marketplace.visualstudio.com/items?itemName=rust-lang.rust-analyzer).
