package plugins;

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;

/**
 * マッパーXML名の末尾を Mapper から Repository に変更するプラグイン
 */
@Deprecated
public class RenameMapperXmlPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {return true;}

    @Override
    public void initialized(IntrospectedTable table) {
        super.initialized(table);
        String name = table.getMyBatis3XmlMapperFileName();
        table.setMyBatis3XmlMapperFileName(name.replaceAll("Mapper", "Repository"));
    }
}
