package plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * 指定したフィールドに @EqualsAndHashCode.Exclude アノテーションを追加する。
 * <pre>
 * パラメータ
 *  - excludeField: アノテーションを追加するテーブルのカラム名。カンマ区切りで複数指定可能。
 * </pre>
 */
public class AddLombokEqualsAndHashCodeExcludeFieldAnnotationPlugin extends PluginAdapter {
    private List<String> excludeFields = new ArrayList<>();
    private static final String EXCLUDE_FIELDS = "excludeField";

    private static final String LOMBOK_EQUALS_AND_HASH_CODE = "lombok.EqualsAndHashCode";

    private static final String ANNOTATION_EQUALS_AND_HASH_CODE_EXCLUDE = "@EqualsAndHashCode.Exclude";

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        for (String propertyName : properties.stringPropertyNames()) {
            if (EXCLUDE_FIELDS.equals(propertyName)) {
                excludeFields = Arrays.asList(properties
                        .getProperty(propertyName)
                        .replace(" ", "")
                        .split(",")
                );
            }
        }
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, Plugin.ModelClassType modelClassType) {
        if (excludeFields.contains(field.getName())) {
            field.addAnnotation(ANNOTATION_EQUALS_AND_HASH_CODE_EXCLUDE);
            addImport(topLevelClass);
        }
        return true;
    }

    private void addImport(TopLevelClass topLevelClass) {
        topLevelClass.addImportedType(new FullyQualifiedJavaType(LOMBOK_EQUALS_AND_HASH_CODE));
    }

}
