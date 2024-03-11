package plugins;


import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.Context;

import java.util.List;
import java.util.Properties;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * Exampleクラスにインタフェースを追加する
 */
public class ExampleImplementPlugin extends PluginAdapter {

    private String interfaceName;

    @Override
    public boolean validate(List<String> warnings) {

        String warning = "Property %s not set for plugin %s";
        interfaceName = properties.getProperty("interfaceName");
        if (!stringHasValue(interfaceName)) {
            warnings.add(String.format(warning, "interfaceName", this.getClass().getSimpleName()));
            return false;
        }

        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addImportedType(new FullyQualifiedJavaType(interfaceName));
        topLevelClass.addSuperInterface(new FullyQualifiedJavaType(interfaceName));
        return true;
    }
}
