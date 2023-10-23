package plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.KotlinFunction;
import org.mybatis.generator.api.dom.xml.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

/**
 * 楽観的排他制御用のDeleteメソッドを追加する。
 * <p>
 * <pre>
 * deleteByPrimaryKey -> deleteByPrimaryKeyAndVersion
 * </pre>
 * <p>
 * パラメータに指定されたカラム名が存在する場合にメソッドを追加する。
 * パラメータには複数のカラム名が指定できるが、最初に見つかったカラムを利用する。
 * 指定するカラムは+1できる数値型を指定すること。
 *
 * <pre>
 * {@code <plugin type="plugins.SqlDeleteKeyAddVersionPlugin">
 *     <property name="versionColumns" value="version, lock_version"/>
 * </plugin>}
 * </pre>
 */
public class SqlDeleteKeyAddVersionPlugin extends PluginAdapter {

    /**
     * プロパティ名
     */
    private static final String PROPERTY_VERSION_COLUMNS = "versionColumns";

    /**
     * バージョンカラムのリスト
     */
    private final List<String> columnList = new ArrayList<>();
    /**
     * SQLMapper格納場所
     */
    private final List<XmlElement> addElementList = new ArrayList<>();
    /**
     * バージョン管理用のカラム名(パラメータで指定された列の中で実際にテーブルに存在するもの最初の１個)
     */
    private String versionColName;

    private IntrospectedColumn versionColumn;

    @Override
    public boolean validate(List<String> warnings) {
        String columns = properties.getProperty(PROPERTY_VERSION_COLUMNS);
        if (columns != null) {
            StringTokenizer st = new StringTokenizer(columns, ", ", false);
            while (st.hasMoreTokens()) {
                String column = st.nextToken();
                columnList.add(column);
            }
        }
        return true;
    }

    /**
     * 新しいメソッド名を取得する
     *
     * @param originalName 参考にする元のメソッド名
     * @return 新しい名前
     */
    private String getNewIdName(String originalName) {
        return originalName.replaceAll("ByPrimaryKey", "ByPrimaryKeyAndVersion");
    }

    /**
     * 指定されたカラムがテーブルに存在するか確認し、最初に見つかったものを返す。
     *
     * @param introspectedTable introspectedTable
     * @return バージョン管理用のカラム名
     */
    private void findVersionColumn(IntrospectedTable introspectedTable, List<String> columnList) {
        ListIterator<IntrospectedColumn> it = introspectedTable.getBaseColumns().listIterator();
        while (it.hasNext()) {
            IntrospectedColumn col = it.next();
            for (String colName : columnList) {
                if (col.getActualColumnName().equals(colName)) {
                    versionColumn = col;
                    versionColName = col.getActualColumnName();
                }
            }
        }
    }

    /**
     * リポジトリインタフェースに新規メソッドを追加する。
     *
     * @param method            method
     * @param interfaze         interfaze
     * @param introspectedTable introspectedTable
     */
    private void addMethodClient(Method method,
                                 Interface interfaze, IntrospectedTable introspectedTable) {

        if (versionColName != null) {
            Method addMethod = new Method(method);
            addMethod.setName(getNewIdName(method.getName()));
            interfaze.addMethod(addMethod);
        }
    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (versionColName == null) {
            findVersionColumn(introspectedTable, this.columnList);
        }
        if (versionColName != null ) {
            addMethodClient(method, interfaze, introspectedTable);
        }
        return true;
    }


    /**
     * SQLMapperXML定義にSQLを登録する
     *
     * @param element           element
     * @param introspectedTable introspectedTable
     */
    private void addMethodSqlMap(XmlElement element, IntrospectedTable introspectedTable) {
            XmlElement addElement = new XmlElement(element);
            String newValue = getNewIdName(element.getAttributes().get(0).getValue());
            Attribute id = new Attribute("id", newValue);
            addElement.getAttributes().remove(0);
            addElement.getAttributes().add(0, id);

//            String versionClause = findTextByElement(addElement, versionColName);

            // version = #{version,jdbcType=BIGINT}
            String whereClause = " and " + versionColName + " = #{" + versionColName + ",jdbcType=" + versionColumn.getJdbcTypeName() + "}";
            addElement.getElements().add(new TextElement(whereClause));

//            replaceTextByElement(addElement, versionColName);

            addElementList.add(addElement);
    }


    /**
     * バージョン管理用項目のUPDATE命令を書き換え(x = x + 1,)
     *
     * @param element element
     * @param word    word
     * @return true:書き換え成功, false:書き換え対象がなかった
     */
    private Boolean replaceTextByElement(XmlElement element, String word) {
        ListIterator<VisitableElement> it = element.getElements().listIterator();
        while (it.hasNext()) {
            VisitableElement e = it.next();
            if (e instanceof XmlElement) {
                Boolean replaced = replaceTextByElement((XmlElement) e, word);
                if (replaced) {
                    return true;
                }
            } else if (e instanceof TextElement) {
                TextElement te = (TextElement) e;
                if (te != null && te.getContent().contains(word)) {
                    String setWord = "  ";
                    if (te.getContent().contains("set " + versionColName)) {
                        setWord = "set ";
                    }
                    it.set(new TextElement(setWord + versionColName + " = " + versionColName + " + 1,"));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (versionColName != null ) {
//            replaceTextByElement(element, versionColName);
            addMethodSqlMap(element, introspectedTable);
        }
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {

        // SQLに新規メソッド用のSQLを追加
        document.getRootElement().getElements().addAll(addElementList);
        addElementList.clear();
        return true;
    }

}
