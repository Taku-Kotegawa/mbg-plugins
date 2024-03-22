package plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;
import java.util.Properties;

/**
 * Modelにスーパークラスを設定するプラグイン
 * <p>
 * プロパティ<br>
 *  - className = スーパークラス(フルパス)
 *
 */
@Deprecated
public class ModelExtendsSupperClassPlugin extends PluginAdapter {

    private String className = "com.example.model.SuperClass";

    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        for (String propertyName : properties.stringPropertyNames()) {
            if (propertyName.equals("className")) {
                this.className = properties.getProperty("className");
            }
        }
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        setSupperClass(topLevelClass);
        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        
        //setSupperClass(topLevelClass);
        return true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        
        setSupperClass(topLevelClass);
        return true;
    }
    
    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        
        setSupperClass(topLevelClass);
        return true;
    }

    /**
     * Entityのスーパークラスを設定する
     * @param topLevelClass
     */
    protected void setSupperClass(TopLevelClass topLevelClass) {

        FullyQualifiedJavaType superClass = new FullyQualifiedJavaType(this.className);
        topLevelClass.setSuperClass(superClass);
    }
}