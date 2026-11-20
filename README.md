# Piko NewX Japanese Patches

[crimera/piko](https://github.com/crimera/piko) の **NewX patches**（`dev` / `x-lite` ブランチ）を X（`com.twitter.android`）に適用した際に表示される **Piko設定（Piko Settings）** をすべて日本語化するための、独立した Morphe 用パッチです。

Piko 本体のパッチを改造するのではなく、**Piko と同時に（または Piko 適用後の APK に追加で）適用する別バンドルパッチ**として動作します。

<a href="https://morphe.software/add-source?github=Himare4092/piko-newx-japanese">
  <img src=".github/assets/add-to-morphe.svg" alt="Add to Morphe" height="36">
</a>

---

## 主な特徴

- **Piko を改造せずに同時適用可能**: Morphe のパッチ実行順・`finalize` 順を利用して、Piko 適用処理の直後に日本語リソースと拡張 DEX 文字列を反映します。
- **Piko NewX 設定の全項目（485 文字列リソース）に対応**: `values/newx/strings.xml` のすべての翻訳可能項目を `res/values-ja/strings.xml` へ追加・上書きします（既存 Piko 日本語訳や X 公式日本語 UI の用語「ポスト」「おすすめ」「フォロー中」「スペース」等に統一）。
- **Translator 設定画面のハードコード英語も日本語化**: リソース化されていない `TranslatorFragment` / `TranslatorConfig` / `TranslatorConfig$CustomType` の UI 文言（ボタン・見出し・言語一覧・バリデーションメッセージ）および `PikoSettingsActivity` の戻るボタン説明文（`"Back"`）もバイトコードレベルで日本語化します。
- **X のバージョン非依存**: `AppTarget(version = null)` で定義しているため、Piko NewX 側が新しい X バージョンに対応しても基本的にそのまま併用できます。

---

## 使い方

### Morphe Manager を使う場合

1. Morphe Manager の設定で **Expert mode**（エキスパートモード）を有効にします。
2. パッチソースに以下の両方を追加します。
   - **Piko**（`NewX` パッチが含まれる `dev` / prerelease 版）
   - **このリポジトリのパッチ**（ローカルビルドした `.mpp` または GitHub Releases の `.mpp`）
3. X（`com.twitter.android`）のオリジナル APKM を選択し、**Piko の `NewX: ...` パッチ** と本パッチ **`Japanese translation for Piko NewX`** の両方にチェックを入れます。
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

## 収録パッチ

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0](https://github.com/Himare4092/piko-newx-japanese/releases/tag/v1.0.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;1 patches total
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
