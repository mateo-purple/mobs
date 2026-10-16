# ENEND Official Mobdex Website

ENENDの公式攻略図鑑（Minecraft Java 26.3 / NeoForge 26.3.0.83 / alpha.19）です。

## 公開方法

このディレクトリは **GitHub Pages: Deploy from a branch → `main` / `/docs`** 向けです。

1. リポジトリ [`mateo-purple/mobs`](https://github.com/mateo-purple/mobs) を開きます。
2. **Settings → Pages** へ進みます。
3. **Build and deployment → Source** で **Deploy from a branch** を選びます。
4. **Branch: `main`, Folder: `/docs`** を選び **Save** します。
5. デプロイ後、通常は `https://mateo-purple.github.io/mobs/` で公開されます。*表示される実際の公開URLをGitHub Pages画面で確認してください。*

GitHub Pages設定を変更する権限のない接続経由では、ソースを配置できても公開設定の操作は別途必要です。

## 収録コンテンツ

- **初期状態はネタバレ保護**：クリーパー、ゾンビ、スケルトン、ピリジャーのみ表示
- ボタンで**35種類の個別仕様**を公開・非公開切替
- Mob名・仲間化素材・能力の検索とカテゴリフィルター
- モバイル対応、日本語／英語インターフェース切替
- 指示（追尾・待機・巡回・角笛）
- 商人と取引候補全リスト
- 設定、対象外10種類、alpha.19の原文ダウンロード
- 各MobへのURL共有（`#mob-creeper`など）

正確な技術仕様は `FULL_SPECIFICATION_JA.txt` を正本とします。

## バージョン更新時

`data.js` はalpha.19の仕様書をもとに生成したスナップショットです。MOD側の仕様を更新したら、仕様書と `data.js`、このページの表示バージョンを**揃えてから公開**してください。ゲーム本体ソースと図鑑の記載が食い違う場合は実際のMOD版の仕様を優先します。

## 開発について

HTML / CSS / Vanilla JavaScriptのみで構成し、ビルドも外部JSライブラリも不要です。GitHub Pagesで静的に配信できます。サイトに専用のアカウント登録、追跡、データ送信はありません。ネタバレと表示言語の選択だけブラウザのlocalStorageに保存します。

サイトの外部参照：Google Fonts（読み込めない場合はシステムフォントにフォールバック）。

## ライセンス

本リポジトリのMODに付属するライセンスとMinecraft由来の著作物の扱いを尊重してください。掲載した仕様はMinecraft 26.3/NeoForge 26.3.0.83のalpha.19向けで、公式Minecraft開発元とは無関係です。
