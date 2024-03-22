package plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Update時に +1 する更新番号項目を利用する楽観的排他制御用のUpdateメソッドを追加する。
 * <p>
 * updateByPrimaryKey -> updateByPrimaryKeyAndVersion
 * <p>
 * パラメータに指定されたカラム名が存在する場合にメソッドを追加する。
 * パラメータには複数のカラム名が指定できるが、最初に見つかったカラムを利用する。
 * 指定するカラムは+1できる数値型を指定すること。(最終更新日等の日時型の場合は別のプラグイン)
 *
 * @code <plugin type="plugins.SqlUpdateKeyAddVersionPlugin">
 * <property name="versionColumns" value="version, lock_version"/>
 * </plugin>
 */
public class SqlUpdateKeyAndVersionPlugin extends PluginAdapter {

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
        versionColumn = null;
        versionColName = null;

        List<IntrospectedColumn> list = new ArrayList<>();
        list.addAll(introspectedTable.getPrimaryKeyColumns());
        list.addAll(introspectedTable.getBaseColumns());

        for (IntrospectedColumn col : list) {
            for (String colName : columnList) {
                if (col.getActualColumnName().equals(colName)) {
                    versionColumn = col;
                    versionColName = col.getActualColumnName();
                }
            }
        }

        for (IntrospectedColumn col : introspectedTable.getPrimaryKeyColumns()) {
            if (col.getActualColumnName().equals(versionColName)) {
                // 更新番号が主キーに含まれるので、Where区への追加は行わない
                versionColumn = null;
                versionColName = null;
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

//            addMethod.getParameters().clear();
//            for (Parameter p : method.getParameters()) {
//                addMethod.addParameter(new Parameter(p.getType(), p.getName(), "@Param(\"" + p.getName() + "\")"));
//                addMethod.addParameter(new Parameter(p.getType(), p.getName()));
//            }

            addMethod.setName(getNewIdName(method.getName()));

            if (method.getName().startsWith("deleteByPrimaryKey")) {

                addMethod.getParameters().clear();
                for (Parameter p : method.getParameters()) {
                    addMethod.addParameter(new Parameter(p.getType(), p.getName(), "@Param(\"" + p.getName() + "\")"));
                }
                addMethod.addParameter(new Parameter(versionColumn.getFullyQualifiedJavaType(), versionColumn.getActualColumnName(), "@Param(\"" + versionColumn.getActualColumnName() + "\")"));
            }

            interfaze.addMethod(addMethod);
        }
    }

    /**
     * SQLMapperXML定義にSQLを登録する
     *
     * @param element           element
     * @param introspectedTable introspectedTable
     */
    private void addMethodSqlMap(XmlElement element, IntrospectedTable introspectedTable) {

        if (versionColName == null) {
            return;
        }

        XmlElement addElement = new XmlElement(element);
        String newValue = getNewIdName(element.getAttributes().get(0).getValue());

        Attribute id = new Attribute("id", newValue);
        addElement.getAttributes().set(0, id);

        String whereClause = "  and " + versionColName + " = #{" + versionColName + ",jdbcType=" + versionColumn.getJdbcTypeName() + "}";
        addElement.getElements().add(new TextElement(whereClause));

        if (addElement.getName().equals("delete")) {
            Iterator<Attribute> it = addElement.getAttributes().listIterator();
            while (it.hasNext()) {
                Attribute a = it.next();
                if (a.getName().equals("parameterType")) {
                    it.remove();
                }
            }
        }


        addElementList.add(addElement);
    }


    // --- メソッド ----

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(
            Method method, Interface interfaze, IntrospectedTable introspectedTable) {

        findVersionColumn(introspectedTable, this.columnList);
        addMethodClient(method, interfaze, introspectedTable);
        return true;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        findVersionColumn(introspectedTable, this.columnList);
        addMethodClient(method, interfaze, introspectedTable);
        return true;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        findVersionColumn(introspectedTable, this.columnList);
        addMethodClient(method, interfaze, introspectedTable);
        return true;
    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        findVersionColumn(introspectedTable, this.columnList);
        addMethodClient(method, interfaze, introspectedTable);
        return true;
    }

    // --- Element ---

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        findVersionColumn(introspectedTable, this.columnList);
        addMethodSqlMap(element, introspectedTable);
        return true;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        findVersionColumn(introspectedTable, this.columnList);
        addMethodSqlMap(element, introspectedTable);
        return true;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        findVersionColumn(introspectedTable, this.columnList);
        addMethodSqlMap(element, introspectedTable);
        return true;
    }

    @Override
    public boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        findVersionColumn(introspectedTable, this.columnList);
        addMethodSqlMap(element, introspectedTable);
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
