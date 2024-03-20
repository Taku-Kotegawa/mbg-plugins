package plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.VisitableElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

/**
 * 楽観的排他制御用の更新番号項目をUpdate時に +1 するSQLに書き換える
 * <p>
 * パラメータに指定されたカラム名が存在する場合にSQL文を修正する
 * パラメータには複数のカラム名が指定できるが、最初に見つかったカラムを利用する。
 * 指定するカラムは+1できる数値型を指定すること。(最終更新日等の日時型の場合は別のプラグイン)
 *
 * @code <plugin type="plugins.SqlUpdateVersionPlusOnePlugin">
 * <property name="versionColumns" value="version, lock_version"/>
 * </plugin>
 */
public class SqlUpdateVersionPlusOnePlugin extends PluginAdapter {

    /**
     * プロパティ名
     */
    private static final String PROPERTY_VERSION_COLUMNS = "versionColumns";
    private static final String PROPERTY_MAX_VERSION_NUM = "maxVersionNum";

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
    private String maxVersionNum = "99999999";

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

        String num = properties.getProperty(PROPERTY_MAX_VERSION_NUM);
        if (num != null) {
            maxVersionNum = num;
        }

        return true;
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
                    return;
                }
            }
        }
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(
            Method method, Interface interfaze, IntrospectedTable introspectedTable) {

        if (versionColName == null) {
            findVersionColumn(introspectedTable, this.columnList);
        }

        return true;
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
                if (contains(te.getContent(), word)) {
                    String setWord = "  ";
                    if (te.getContent().contains("set " + versionColName)) {
                        setWord = "set ";
                    }
                    // version = CASE WHEN version = 99999999 THEN 1 else version + 1 end where username = '1'
                    it.set(new TextElement(setWord + versionColName + " = case when " + versionColName + " = " + maxVersionNum + " then 1 else " + versionColName + " + 1 end,"));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 更新対象かチェック(Where区は除く)
     *
     * @param content
     * @return
     */
    private boolean contains(String content, String word) {
        String trim = content.trim().toLowerCase();
        // 更新番号カラム名を含む、かつ where and で始まらない
        return trim.contains(word) && !trim.startsWith("where") && !trim.startsWith("and");
    }

    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (versionColName != null) {
            replaceTextByElement(element, versionColName);
        }
        return true;
    }

    @Override
    public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (versionColName != null) {
            replaceTextByElement(element, versionColName);
        }
        return true;
    }

    @Override
    public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (versionColName != null) {
            replaceTextByElement(element, versionColName);
        }
        return true;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (versionColName != null) {
            replaceTextByElement(element, versionColName);
        }
        return true;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (versionColName != null) {
            replaceTextByElement(element, versionColName);
        }
        return true;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (versionColName != null) {
            replaceTextByElement(element, versionColName);
        }
        return true;
    }

}
