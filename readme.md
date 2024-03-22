# Mybatis Generator 利用方法

## カスタムプラグインの作成・コンパイル

src/main/java/plugins 配下にカスタムプラグインを作成して、mvn packeage でビルドする。

## Mybatis generator の実行方法

generatorConfig.xml を環境に応じて編集し、プロジェクトルートフォルダをターミナルで開き、以下を実行。

~~~
mvn mybatis-generator:generate
~~~

★generatorConfig.xml のファイル名を変更した場合は、引数にファイル名を指定できる。

~~~
mvn -Dmybatis.generator.configurationFile=src\main\resources\generatorConfig.xml mybatis-generator:generate
~~~


## Mavenのゴール設定による実行コマンドを省略化

定義ファイルを複数準備するケースで、以下の様なpomを準備することで、コマンドを省略できる。

pom.xml
~~~
<plugin>
    <groupId>org.mybatis.generator</groupId>
    <artifactId>mybatis-generator-maven-plugin</artifactId>
    <version>1.4.0</version>
    <executions>
        <execution>
            <id>a</id>
            <configuration>
                <configurationFile>${project.basedir}/src/main/resources/generatorConfig1.xml</configurationFile>
            </configuration>
        </execution>
        <execution>
            <id>b</id>
            <configuration>
                <configurationFile>${project.basedir}/src/main/resources/generatorConfig2.xml</configurationFile>
            </configuration>
        </execution>
    </executions>
    <dependencies>
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>mbg-plugins</artifactId>
            <version>1.0</version>
            <scope>system</scope>
            <systemPath>${basedir}/target/mbg-plugins-1.0.jar</systemPath>
        </dependency>
    </dependencies>
    <configuration>
        <overwrite>true</overwrite>
        <!-- 全てのクラスを依存関係に追加する -->
        <includeAllDependencies>true</includeAllDependencies>
    </configuration>
</plugin>
~~~

~~~
mvn mybatis-generator:generate@a
mvn mybatis-generator:generate@b
~~~
