# 囲碁・将棋イベントまとめサイト

囲碁および将棋のイベント情報を不特定多数のWebサイトから定期的に収集し、統合して表示するサイトです。

## プロジェクト概要

このプロジェクトは、散らばっている囲碁・将棋のイベント情報を一箇所に集約し、ユーザーが手軽に情報を取得できるようにすることを目的としています。

### 主な機能

*   **イベント情報の自動収集**: Spring Batchを使用して、外部サイトから定期的にイベント情報をスクレイピング・収集します。
*   **イベント一覧表示**: 収集した情報をデータベースに保存し、Thymeleafを使用してWebサイト上に一覧表示します。

## 技術スタック

### バックエンド / Webフレームワーク
*   **Java**: 17
*   **Spring Boot**: 3.4.1
*   **Spring Batch**: 定期的なデータ収集（バッチ処理）
*   **Thymeleaf**: HTMLテンプレートエンジン

### データベース
*   **MySQL**: 本番・開発用データベース
*   **H2 Database**: テスト用インメモリデータベース
*   **Flyway**: データベースマイグレーション管理

### フロントエンド / インフラ
*   **Tomcat**: アプリケーションサーバー（Spring Boot内蔵）
*   **Apache**: Webサーバー

### 開発・テストツール
*   **Gradle**: ビルドツール
*   **JUnit**: ユニットテスト・統合テスト
*   **Playwright**: UIテスト・ブラウザ自動操作

## セットアップ

### 前提条件
*   Java 17 以上がインストールされていること。
*   MySQL サーバーが稼働していること。

### データベース設定
`src/main/resources/application.properties` にデータベースの接続情報を設定してください。

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_database_name
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### ビルドと実行

#### ビルド
```bash
./gradlew build
```

#### 実行
```bash
./gradlew bootRun
```
起動後、ブラウザで `http://localhost:8081` にアクセスしてください。

## テスト

### 全テストの実行
```bash
./gradlew test
```

### Playwright テストについて
UIテストには Playwright を使用しています。
初回実行時やブラウザのインストールが必要な場合は、以下のコマンド等で環境を整えてください（詳細は Playwright のドキュメントを参照）。

## 構成

*   `src/main/java`: Java ソースコード
*   `src/main/resources`: 設定ファイル、テンプレートファイル
*   `src/test/java`: テストコード
