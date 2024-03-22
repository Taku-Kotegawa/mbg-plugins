package plugins;

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;

/**
 * Modelクラス名を変更する(テーブル名に"Dto"を付加)
 */
@Deprecated
public class BeanClassNamePlugin extends PluginAdapter {

    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void initialized(IntrospectedTable table) {
        super.initialized(table);

        String name = table.getBaseRecordType();
        table.setBaseRecordType(name + "Dto");
    }
}
