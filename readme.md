# Mybatis Generator 利用方法

## カスタムプラグインの作成・コンパイル

src/main/java/plugins 配下にカスタムプラグインを作成して、mvn packeage でビルドする。

## Mybatis generator の実行方法

generatorConfig.xml を環境に応じて編集し、プロジェクトルートフォルダをターミナルで開き、


mvn mybatis-generator:generate

を実行

★generatorConfig.xml のファイル名を変更した場合は、引数にファイル名を指定できる。

C:\Users\user\.jdks\adopt-openjdk-11.0.12\bin\java.exe -Dmaven.multiModuleProjectDirectory=C:\Users\user\sites\todo-mbg -DconfigurationFile=src/main/resources/generatorConfig_base.xml "-Dmaven.home=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2020.1.4\plugins\maven\lib\maven3" "-Dclassworlds.conf=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2020.1.4\plugins\maven\lib\maven3\bin\m2.conf" "-Dmaven.ext.class.path=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2020.1.4\plugins\maven\lib\maven-event-listener.jar" "-javaagent:C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2020.1.4\lib\idea_rt.jar=62791:C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2020.1.4\bin" -Dfile.encoding=UTF-8 -classpath "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2020.1.4\plugins\maven\lib\maven3\boot\plexus-classworlds-2.6.0.jar;C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2020.1.4\plugins\maven\lib\maven3\boot\plexus-classworlds.license" org.codehaus.classworlds.Launcher -Didea.version=2021.3 -Dmybatis.generator.configurationFile=src\main\resources\generatorConfig_base.xml mybatis-generator:generate

