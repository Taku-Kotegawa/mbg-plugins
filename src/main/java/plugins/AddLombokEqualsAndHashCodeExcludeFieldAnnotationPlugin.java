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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * フィールドアノテーションの追加
 */
public class AddLombokEqualsAndHashCodeExcludeFieldAnnotationPlugin extends PluginAdapter {

    private final List<String> excludeFields = new ArrayList<>();

    private static final String EXCLUDE_FIELDS = "excludeField";

    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        for (String propertyName : properties.stringPropertyNames()) {
            if (EXCLUDE_FIELDS.equals(propertyName)) {
                excludeFields.addAll(Arrays.asList(
                        properties.getProperty(propertyName).replace(" ", "").split(",")));
            }
        }
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        addImport(topLevelClass);

        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        addImport(topLevelClass);

        return true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        addImport(topLevelClass);

        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        addImport(topLevelClass);

        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                       IntrospectedTable introspectedTable, ModelClassType modelClassType) {

        if (excludeFields.contains(field.getName())) {
            field.addAnnotation("@EqualsAndHashCode.Exclude");
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
                new FullyQualifiedJavaType("lombok.EqualsAndHashCode"));

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
