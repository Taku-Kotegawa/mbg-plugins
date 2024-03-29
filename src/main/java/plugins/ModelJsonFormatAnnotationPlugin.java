/**
 * Copyright 2006-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 * LocalDate,LocalDateTimeフィールドに@JsonFormatを追加
 * <p>出力例
 * <pre>
 * {@code
 * @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
 * private LocalDate field_name;
 *
 * @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd HH:mm:ss")
 * private LocalDataTime field_name;
 * }
 * </pre>
 * <p>使い方
 * <pre>
 * {@code
 * <plugin type="plugins.ModelJsonFormatAnnotationPlugin"/>
 * }
 * </pre>
 */
public class ModelJsonFormatAnnotationPlugin extends PluginAdapter {

    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable) {
        addImport(topLevelClass);

        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass,
                                              IntrospectedTable introspectedTable) {
        addImport(topLevelClass);

        return true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable) {
        addImport(topLevelClass);

        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass,
                                                      IntrospectedTable introspectedTable) {
        addImport(topLevelClass);

        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass,
                                       IntrospectedColumn introspectedColumn,
                                       IntrospectedTable introspectedTable, ModelClassType modelClassType) {

        // アノテーションJsonFormatを追記する
        if ("java.time.LocalDateTime"
                .equals(field.getType().getFullyQualifiedNameWithoutTypeParameters())) {
            field.addAnnotation(
                    "@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = \"yyyy/MM/dd HH:mm:ss\")");
        } else if ("java.time.LocalDate"
                .equals(field.getType().getFullyQualifiedNameWithoutTypeParameters())) {
            field.addAnnotation(
                    "@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = \"yyyy/MM/dd\")");
        }

        return true;
    }

    /**
     * インポートを追加(com.fasterxml.jackson.annotation.JsonFormat)
     *
     * @param topLevelClass
     */
    private void addImport(TopLevelClass topLevelClass) {
        // アノテーションJsonFormatを追記するため、JsonFormatをインポートする
        addImport(topLevelClass,
                new FullyQualifiedJavaType("com.fasterxml.jackson.annotation.JsonFormat"));
    }

    /**
     * インポートを追加する
     *
     * @param topLevelClass
     * @param javaType
     */
    private void addImport(TopLevelClass topLevelClass, FullyQualifiedJavaType javaType) {
        topLevelClass.addImportedType(javaType);
    }

}
