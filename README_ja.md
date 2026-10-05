# Fox Model Loader: Revival 2.0

<img src="common/src/main/resources/foxmodelloader.png" alt="Fox Model Loader: Revival" width="160">

Fox Model Loader: Revival **2.0** は Sparkle Morpher **1.2.9** のソースコードを基に再始動します。Mod ID とリソース名前空間は `foxmodelloader` です。従来の Minecraft サーバー同期を維持するため、マルチプレイでモデルを共有するにはサーバーとクライアントの両方に Mod が必要です。各モデルの ID と `.ysm` 形式は維持します。

> [English](README.md) | [中文](README_zh.md) | **日本語** | [한국어](README_ko.md)

**QQ:** 1104823534 | **Discord:** [Discordに参加](https://discord.gg/3KqK7USF39) | **Patreon:** [cw/Soid211](https://www.patreon.com/cw/Soid211) | **Afdian:** [Micaftic](https://afdian.com/a/Micaftic)

Minecraft 総合カスタムモデルローダー。プレイヤーにカスタムモデル、アニメーション、サウンドエフェクトを搭載——あのブロックキャラにさようなら。

> これは**総合モデルローダー**です。現在 `.ysm` 形式（OpenYSM ベース、MIT ライセンス）と `.bbmodel` 形式（Blockbench）をサポートし、今後他の主流モデル形式への対応を予定しています。

---

## 機能

### カスタムプレイヤーモデル＆スキン

デフォルトのプレイヤーモデルを完全にカスタムされた 3D モデルに置き換えます。すべてのカスタムモデルはマルチプレイヤーで**他のプレイヤーにも表示されます**——サーバーサイドの MOD 不要でモデルの可視性を実現。

### モデル形式サポート

- **`.ysm`** — OpenYSM/YSMParser によるネイティブ形式。完全なスケルタルモデルとウェイトアニメーションをサポート。
- **`.bbmodel`** — Blockbench プロジェクトファイルを直接インポート。メッシュ三角化（N 角形ファン三角化）、UV 正規化、面回転、インフレート拡張、埋め込み Base64 テクスチャ抽出、PNG IHDR ヘッダー解析に対応。
- **Figura アバターアーカイブ** — Figura `.zip` パッケージを直接インポート。内蔵 `ZipModelSniffer` が YSM フォルダ、Figura アバター、プレーン BBModel ZIP を自動検出・分岐。

### アニメーションシステム

- **アニメーションカルーセル**（デフォルトキー：Z）— ラジアルメニューで現在のモデルのアニメーションを素早く切り替え。
- **アニメーションコントローラー** — ステートマシンベースのアニメーションコントローラーを完全サポート。`loop`（ループ）、`once`（ワンショット）、`hold`（ホールド）再生モード対応。
- **Molang 式** — データポイントが生の数値と Molang 式文字列の両方をサポートし、動的なアニメーションブレンドを実現。

### サウンドエフェクト

モデル内蔵のボイスラインとサウンドエフェクトをスキルやアクションでトリガー再生。**Opus** 音声を同梱の Java Concentus デコーダーで再生します。

### マルチモデル管理

- ローカルファイル、ディレクトリ、URL からモデルをインポート。高速ダウンロード対応。
- グループ分けとお気に入りでモデルを整理。
- 自動ディレクトリスキャンで `.ysm`、`.zip`、`.bbmodel` ファイルを認識。

### サーバーサイド機能

- サーバー管理者がモデルマニフェストを定義し、クライアントに配信可能。
- 設定可能なブラックリスト（`config/foxmodelloader/blacklist.txt`）で特定モデルを制限。
- Cardinal Components エンティティデータによるクライアント-サーバー間モデル状態同期。

### MOD 互換性

人気 MOD との併用に対応：

| カテゴリ | 互換 MOD |
|---------|---------|
| 戦闘 | Better Combat |
| アクセサリー | Curios |
| 建造＆オートメーション | Create |
| レンダリング | Iris、Sodium |
| プレイヤースキン | スキンレイヤー互換 |

### クロスプラットフォーム

Minecraft 1.21.1、26.1.2、26.2 の Fabric と NeoForge に対応する 6 つのビルドを提供します。

| バリアント | ローダー | Minecraft |
| --- | --- | --- |
| Fox-Model-Loader-Fa1.21.1 | Fabric | 1.21.1 |
| Fox-Model-Loader-Fa26.1.2 | Fabric | 26.1.2 |
| Fox-Model-Loader-Fa26.2 | Fabric | 26.2 |
| Fox-Model-Loader-Neo1.21.1 | NeoForge | 1.21.1 |
| Fox-Model-Loader-Neo26.1.2 | NeoForge | 26.1.2 |
| Fox-Model-Loader-Neo26.2 | NeoForge | 26.2 |

---

## 仕組み

### モデルインポートパイプライン

モデルファイルをインポートすると、Fox Model Loader: Revival はインテリジェントな処理パイプラインを実行します：

1. **ZIP スニフィング** — アーカイブをコンテンツで分類：YSM フォルダ、Figura アバター（`avatar.json` + `.bbmodel` 含む）、プレーン BBModel ZIP、または不明。
2. **パース** — `.ysm` ファイルは YSMParser で処理。`.bbmodel` ファイルは内蔵 `BBModelParser` でアウトラインツリー、キューブ/メッシュ要素、テクスチャ、アニメーション、コントローラー状態を処理。
3. **変換** — パースデータをエンジン内部の `RawGeometry` 形式に変換。N 頂点メッシュ面はファン三角化で処理、UV 座標はテクスチャ解像度で正規化、ZIP 内の外部 PNG テクスチャは埋め込み Base64 ソースより優先。
4. **レンダリング** — 変換されたモデルはアクティブ時にバニラプレイヤーレンダラーを置き換え、デフォルトプレイヤーモデルを自動非表示。

### BBModel 互換性

Blockbench 形式を完全サポート：

- アウトラインツリーのネストされたボーン階層と親子関係
- キューブおよびメッシュ要素の正しい面 UV マッピング
- 埋め込みテクスチャ（Base64）の PNG ヘッダー寸法検出
- アニメーション再生とループモードマッピング
- Blockbench 5 "free" 形式互換（薄型アウトラインノード + `groups[]` フォールバック）
- 孤立要素の処理（未参照要素をデフォルトボーンに自動割り当て）

---

## アーキテクチャ

Fox Model Loader: Revival は**共通コア + プラットフォームアダプター**の階層アーキテクチャを採用：

- **`common`** — 全バリアント共通のコアロジック：モデルパース、メッシュ処理、ZIP スニフィング、アニメーションコントローラー、オーディオデコーディング、Molang 評価。
- **`fabric`** / **`neoforge`** — 初期化、ネットワーキング、コンポーネント登録、レンダリングフックを処理するプラットフォーム固有アダプター。
- **ネイティブレンダラー** — 本リポジトリから再ビルドした SIMD アクセラレーション。[ソースとビルド情報](NATIVE_SOURCES.md)。

---

## 依存関係

ビルドバリアントにより異なります——詳細は `mods.toml`（NeoForge）または `fabric.mod.json`（Fabric）をご参照ください。Fabric バリアントは Fabric API の別途インストールが必要です。その他の依存関係は Jar-in-Jar でバンドルされています。

---

## クレジット＆ライセンス

- [OpenYSM](https://github.com/OpenYSM)（MIT ライセンス）を基に開発。
- `.ysm` モデルパースに [OpenYSMDev/YSMParser](https://github.com/OpenYSMDev/YSMParser)（MIT）を使用。
- デフォルトモデルライブラリ：[sdf123098/YSM-Model](https://github.com/sdf123098/YSM-Model)。
- Blockbench 形式は [JannisX11/Blockbench](https://github.com/JannisX11/blockbench) による開発。

**ライセンス：** MIT

## ビルド

1.21.1 は Java 21、26.x は Java 25 を使用します。標準版は `./gradlew build`、Java フォールバック版は `./gradlew build -Pdist=curseforge` でビルドします。標準版には再ビルドした SIMD レンダラーを同梱し、CurseForge 版には本プロジェクトのネイティブライブラリを含めません。6 つのネイティブターゲットの再ビルド：`python scripts/rebuild-natives.py --zig <zig.exe> --ndk <Android NDK root>`。[ネイティブソースとビルド情報](NATIVE_SOURCES.md)を参照してください。
