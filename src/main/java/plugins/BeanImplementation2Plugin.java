package plugins;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * 指定したカラムを持つテーブルを対象にModelクラスにインタフェースを追加する。
 * <pre>
 * プロパティ
 * - interfaceName : 追加するインタフェース名(フルパス)
 * - targetColumn : 対象テーブルに含まれるカラム名
 * </pre>
 */
public class BeanImplementation2Plugin extends PluginAdapter {

    private String interfaceName;
    private String targetColumn;

    private FullyQualifiedJavaType bean;

    public boolean validate(List<String> warnings) {

        String warning = "Property %s not set for plugin %s";
        interfaceName = properties.getProperty("interfaceName");
        if (!stringHasValue(interfaceName)) {
            warnings.add(String.format(warning, "interfaceName", this.getClass().getSimpleName()));
            return false;
        }
        bean = new FullyQualifiedJavaType(this.interfaceName);

        targetColumn = properties.getProperty("targetColumn");
        if (!stringHasValue(targetColumn)) {
            warnings.add(String.format(warning, "targetColumn", this.getClass().getSimpleName()));
            return false;
        }
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable) {
        if (isTarget(introspectedTable)) {
            implementBean(
                    topLevelClass,
                    introspectedTable.getFullyQualifiedTable());
        }
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(
            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (isTarget(introspectedTable)) {
            implementBean(
                    topLevelClass,
                    introspectedTable.getFullyQualifiedTable());
        }
        return true;
    }

    protected void implementBean(TopLevelClass topLevelClass, FullyQualifiedTable table) {
            topLevelClass.addImportedType(bean);
            topLevelClass.addSuperInterface(bean);
            System.out.println(table + " set " + bean);
    }

    private boolean isTarget(IntrospectedTable introspectedTable) {
        for (IntrospectedColumn introspectedColumn : introspectedTable.getBaseColumns()) {
            if (targetColumn.equals(introspectedColumn.getActualColumnName())) {
                return true;
            }
        }
        return false;
    }

}