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
 * このプラグインはModelに対して、特殊なインタフェースをimplementsに追加し、getPrimaryKey()を作成します。<br>
 * これにより常に同じメソッド名(getPrimaryKey())で主キーの値を取得できる様になります。<br>
 * 複合主キーの場合、getPrimaryKey()はPrimaryKeyクラスを返します。
 * <p>
 *     注意事項<br>
 *     defaultModelType="flat"には対応していません。誤ったメソッドが追加されます。
 * </p>
 * <p>
 *     指定可能なパラメータ<br>
 *     interface: implementsするインタフェース名(必須)
 * </p>
 * <p>
 *     前提条件<br>
 *     以下のインタフェースを事前に準備しておくこと。<br>
 *     <code>
 *         public interface KeyInterface&lt;I&gt; {
 *             I getPrimaryKey();
 *         }
 *     </code>
 * </p>
 */
public class ModelExtendsPrimaryKeyInterfacePlugin extends PluginAdapter {

    private String addInterfaceName;

    @Override
    public boolean validate(List<String> warnings) {
        // this plugin is always valid
        String warning = "Property %s not set for plugin %s";
        if (!stringHasValue(addInterfaceName)) {
            warnings.add(String.format(warning, "interface", this.getClass().getSimpleName()));
            return false;
        }

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
    public boolean modelRecordWithBLOBsClassGenerated(
            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        makeSerializable(topLevelClass, introspectedTable);
        return true;
    }

    protected void makeSerializable(TopLevelClass topLevelClass,
                                    IntrospectedTable introspectedTable) {

        // 主キーなしの場合処理しない
        if (introspectedTable.getPrimaryKeyColumns() == null || introspectedTable.getPrimaryKeyColumns().isEmpty()) {
            return;
        }

        FullyQualifiedJavaType addInterfaceFQJT = new FullyQualifiedJavaType(addInterfaceName);

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

        // 複合主キー or 単一キー により分岐
        if (pkClassOptional.isPresent()) {
            // public <PK_CLASS> getPrimaryKey() {
            //    <PK_CLASS> superClass = new <PK_CLASS>();
            //    superClass.setFieldXXX(getFieldXXX());
            //    superClass.setFieldYYY(getFieldYYY());
            //    return superClass;
            // }
            FullyQualifiedJavaType pkClass = pkClassOptional.get();
            method.setReturnType(pkClass);
            method.addBodyLine(pkClass.getShortName() + " superClass = new " + pkClass.getShortName() + "();");
            introspectedTable.getPrimaryKeyColumns().forEach(x -> {
                String field = capitalize(x.getJavaProperty());
                method.addBodyLine("superClass.set" + field + "(get" + field + "());");
            });
            method.addBodyLine("return superClass;");

        } else {
            // public ID getPrimaryKey() { return getFieldXXX(); }
            IntrospectedColumn pkey = introspectedTable.getPrimaryKeyColumns().get(0);
            method.setReturnType(pkey.getFullyQualifiedJavaType());
            String field = capitalize(pkey.getJavaProperty());
            method.addBodyLine("return get" + field + "();");
        }
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);

    }

}
