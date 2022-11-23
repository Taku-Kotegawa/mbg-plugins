package plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static plugins.CreateGenericInterfacePlugin.capitalize;

/**
 * このプラグインは、Modelオブジェクトに指定したインタフェースを追加します。
 * <p>注意事項: defaultModelType="flat"には対応していません。誤ったメソッドが追加されます。</p>
 *
 * <p>interface: extedsするインタフェース名</p>
 */
public class ModelExtendsPrimaryKeyInterfacePlugin extends PluginAdapter {

    private String addInterfaceName;
    private FullyQualifiedJavaType addInterfaceFQJT;

    @Override
    public boolean validate(List<String> warnings) {
        // this plugin is always valid
        String warning = "Property %s not set for plugin %s";
        if (!stringHasValue(addInterfaceName)) {
            warnings.add(String.format(warning, "interface", this.getClass().getSimpleName()));
            return false;
        }

        addInterfaceFQJT = new FullyQualifiedJavaType(addInterfaceName);
        return true;
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        addInterfaceName = properties.getProperty("interface");

    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable) {
        makeSerializable(topLevelClass, introspectedTable);
        return true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable) {
        makeSerializable(topLevelClass, introspectedTable);
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(
            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        makeSerializable(topLevelClass, introspectedTable);
        return true;
    }

    protected void makeSerializable(TopLevelClass topLevelClass,
                                    IntrospectedTable introspectedTable) {

        // スーパークラス(複合主キークラス)の有無
        Optional<FullyQualifiedJavaType> pkClassOptional = topLevelClass.getSuperClass();

        // extends Interface
        addInterfaceFQJT.addTypeArgument(pkClassOptional.orElseGet(()
                -> introspectedTable.getPrimaryKeyColumns().get(0).getFullyQualifiedJavaType()));
        topLevelClass.addImportedType(addInterfaceFQJT);
        topLevelClass.addSuperInterface(addInterfaceFQJT);

        // add getPrimaryKey()
        Method method = new Method("getPrimaryKey");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("getPrimaryKey");

        // 複合主キー or 単一キー により分岐
        if (pkClassOptional.isPresent()) {
            // public <PK_CLASS> getPrimaryKey() {
            //    <PK_CLASS> superClass = new <PK_CLASS>();
            //    superClass.setFieldXXX(fieldXXX);
            //    return superClass;
            // }
            FullyQualifiedJavaType pkClass = pkClassOptional.get();
            method.setReturnType(pkClass);
            method.addBodyLine(pkClass.getShortName() + " superClass = new " + pkClass.getShortName() + "();");
            introspectedTable.getPrimaryKeyColumns().forEach(x -> {
                String field = capitalize(x.getJavaProperty());
                method.addBodyLine("superClass.set" + field + "(" + x.getJavaProperty() + ");");
            });
            method.addBodyLine("return superClass;");

        } else {
            // public ID getPrimaryKey() { return <primary_key_field_name>; }
            IntrospectedColumn pkey = introspectedTable.getPrimaryKeyColumns().get(0);
            method.setReturnType(pkey.getFullyQualifiedJavaType());
            method.addBodyLine("return " + pkey.getJavaProperty() + ";");
        }
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);

    }

}
