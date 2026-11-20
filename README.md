# Piko NewX Handy

[crimera/piko](https://github.com/crimera/piko) の **NewX patches**（`dev` / `x-lite` ブランチ）をより便利・快適にするための、Morphe 用追加パッチ集です。

Piko 本体のパッチを改造するのではなく、**Piko と同時に（または Piko 適用後の APK に追加で）適用する別バンドルパッチ**として動作します。

<a href="https://morphe.software/add-source?github=Himare4092/piko-newx-handy">
  <img src=".github/assets/add-to-morphe.svg" alt="Add to Morphe" height="36">
</a>

---

## このリポジトリについて

Piko (NewX) の使い勝手をさらに高めるための補助パッチをまとめています。

### 収録パッチの例

- **Piko NewX 設定の日本語化（`Japanese translation for Piko NewX`）**
  - Piko NewX 設定の全項目（485 文字列リソース）を日本語化します（X 公式日本語 UI や既存 Piko 日本語訳の表記に統一）。
  - リソース化されていない「翻訳ツール（Translator）」設定画面内の英語直書き UI 文言や、戻るボタンの説明文も日本語化します。
  - X のバージョン非依存（`version = null`）で動作します。

※ 今後も Piko (NewX) をより便利にするパッチを順次追加していく予定です。

---

## 使い方

### Morphe Manager を使う場合

1. Morphe Manager の設定で **Expert mode**（エキスパートモード）を有効にします。
2. パッチソースに以下の両方を追加します。
   - **Piko**（`NewX` パッチが含まれる `dev` / prerelease 版）
   - **Piko NewX Handy**（上の **Add to Morphe** ボタン、または GitHub Releases の `.mpp` ファイル）
3. X（`com.twitter.android`）のオリジナル APKM を選択し、**Piko の `NewX: ...` パッチ** と **Piko NewX Handy のパッチ** の両方にチェックを入れます。
4. 複数バンドル使用の確認ダイアログが表示されたらそのまま続行（Proceed anyway）し、パッチ適用後にインストールします。

### Morphe CLI を使う場合

`--patches` に Piko の `.mpp` と本プロジェクトの `.mpp` の両方を指定して実行します。

```sh
java -jar morphe-cli.jar patch \
  --patches piko.mpp \
  --patches patches-1.0.0.mpp \
  input.apkm
```

> すでに Piko NewX パッチを適用済みの APK に対して、後から本パッチだけを追加適用することも可能です。

---

## 収録パッチ一覧

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0](https://github.com/Himare4092/piko-newx-handy/releases/tag/v1.0.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;1 patches total
<details open>
<summary>📦 NewX&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**Package name:** `com.twitter.android`

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Japanese translation for Piko NewX](#japanese-translation-for-piko-newx) | Translates Piko NewX Settings (including Translator settings) into Japanese. Apply alongside Piko's NewX patches. |  |

</details>

<!-- PATCHES_END -->

---

## ローカルでのビルド方法

### 前提条件

- JDK 21
- Android SDK（環境変数 `ANDROID_HOME` を設定）
- `~/.gradle/gradle.properties` に GitHub Packages 読み取り用の `gpr.user` と `gpr.key`（`read:packages` 権限付き PAT）、または環境変数 `GITHUB_ACTOR` / `GITHUB_TOKEN`

### ビルド & テストコマンド

```sh
# テスト実行・.mpp ビルド・patches-list.json 生成
./gradlew :patches:test :patches:buildAndroid :patches:generatePatchesList --no-daemon
```

Windows (PowerShell) の場合:

```powershell
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat :patches:test :patches:buildAndroid :patches:generatePatchesList --no-daemon
```

ビルド成果物は `patches/build/libs/patches-<version>.mpp` に出力されます。

---

## ライセンス

[GNU General Public License v3.0](LICENSE)
