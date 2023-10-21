package plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;

import java.util.List;

/**
 * SqlMapのテーブル名からスキーマを削除する。
 * <p>
 * <pre>
 * {@code
 * select * from public.employee -> select * from employee
 * }
 * </pre>
 */
public class NoSchemaSQLPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {

        introspectedTable.setSqlMapFullyQualifiedRuntimeTableName(
                deleteSchema(introspectedTable.getFullyQualifiedTableNameAtRuntime()));

        introspectedTable.setSqlMapAliasedFullyQualifiedRuntimeTableName(
                deleteSchema(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
    }

    private String deleteSchema(String schemaTableName) {
        // 最初にピリオドが出現する位置を探す
        int index = schemaTableName.indexOf(".");

        // ピリオドが見つかった場合
        if (index != -1) {
            // ピリオドより後ろの部分を切り取る
            return schemaTableName.substring(index + 1);
        } else {
            // ピリオドが見つからなかった場合、そのまま返す。
            return schemaTableName;
        }
    }

}
