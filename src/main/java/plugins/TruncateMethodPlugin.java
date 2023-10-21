package plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Truncate 命令を追加する
 * <p>
 *
 * <pre>
 * {@code void truncate();}
 * </pre>
 * <pre>
 * {@code
 * <delete id="truncate">
 *   truncate table table_name
 * </delete>
 * }
 * </pre>
 */
public class TruncateMethodPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {

        // メソッド名を指定
        Method method = new Method("truncate");
        // 戻り値の型を指定
        method.setReturnType(new FullyQualifiedJavaType("void"));
        // インターフェースを指定(指定しない場合、{} が付く)
        method.setAbstract(true);
        // Javadocコメントを追加
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        // メソッドの追加
        interfaze.addMethod(method);

        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        List<XmlElement> addElementList = new ArrayList<>();

        // XMLのタグ情報
        XmlElement xmlElement = new XmlElement("delete");
        xmlElement.addAttribute(new Attribute("id", "truncate"));
        context.getCommentGenerator().addComment(xmlElement);

        // SQL命令の組み立て
        String s = "truncate table " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        xmlElement.addElement(new TextElement(s));
        addElementList.add(xmlElement);

        // XMLに新規メソッド用のSQLを追加
        document.getRootElement().getElements().addAll(addElementList);

        return true;
    }
}
