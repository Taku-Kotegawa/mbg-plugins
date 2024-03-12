package plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.VisitableElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.*;

/**
 * update文で変更しないカラムを除外する。<br>
 * ex: 作成日時や作成者など新規登録時のみ登録されるカラムなど<br>
 * プロパティで除外するカラムを複数指定できる。(カンマ区切り)
 *
 * @code <plugin type="plugins.SqlUpdateExcludeColumnPlugin">
 * <property name="excludeColumns" value="created_at,created_by"/>
 * </plugin>
 * @code
 *
 * 対応すべきケース
 * 1. 先頭
 * update
 *  set created_by = #{createdBy,jdbcType=VARCHAR},
 *
 * 2. 末尾
 * update
 *  set
 *  xxx = xxxx,
 *  created_by = #{createdBy,jdbcType=VARCHAR}
 *
 * 3. Selective
 * update set
 *  <if test="create_by !=null">
 *      created_by = #{createdBy,jdbcType=VARCHAR},
 *  </if>
 *
 */
public class SqlUpdateExcludeColumnPlugin extends PluginAdapter {

    /**
     * 除外項目のプロパティ名
     */
    private static final String PROPERTY_EXCLUDE_COLUMNS = "excludeColumns";

    /**
     * 除外項目名のリスト
     */
    private final List<String> columnList = new ArrayList<>();

    @Override
    public boolean validate(List<String> warnings) {

        String columns = properties.getProperty(PROPERTY_EXCLUDE_COLUMNS);

        if (columns != null) {
            StringTokenizer st = new StringTokenizer(columns, ",", false);
            while (st.hasMoreTokens()) {
                String column = st.nextToken();
                columnList.add(column.trim());
            }
        }

        return true;
    }

    // ノーマル(非Selective) ---------------------------------------------------
    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        normal(element);
        return true;
    }
    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        normal(element);
        return true;
    }

    @Override
    public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        normal(element);
        return true;
    }

    @Override
    public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        normal(element);
        return true;
    }

    // Selective -----------------------------------------------------------

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        selective(element);
        return true;
    }

    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        selective(element);
        return true;

    }

    void normal(XmlElement element) {

        Map<Integer, String> map = new LinkedHashMap<>();

        for (int i = 0; i < element.getElements().size(); i++) {
            VisitableElement ve = element.getElements().get(i);
            if (ve instanceof TextElement) {
                String target = getTarget(((TextElement) ve).getContent());
                if (target != null) {
                    element.getElements().remove(ve);
                    element.addElement(i, new TextElement("  " + target));
                }
            }
        }
    }

    private String getTarget(String content) {

        boolean needComma = content.trim().endsWith(",");

        for(String excludeColumn : columnList) {
            if (content.trim().startsWith(excludeColumn.toLowerCase().trim()) && content.contains("jdbcType")) {
                return excludeColumn.toLowerCase() + " = " + excludeColumn.toLowerCase() + (needComma ? "," : "");
            } else if (content.trim().startsWith("set " + excludeColumn.toLowerCase().trim()) && content.contains("jdbcType")) {
                return "set " + excludeColumn.toLowerCase() + " = " + excludeColumn.toLowerCase() + (needComma ? "," : "");
            }
        }
        return null;
    }

    void selective(XmlElement element) {
        Map<Integer, String> map = new LinkedHashMap<>();

        for (int i = 0; i < element.getElements().size(); i++) {
            VisitableElement ve = element.getElements().get(i);

            if (ve instanceof XmlElement) {
                XmlElement xe = (XmlElement) ve;
                if ("set".equals(xe.getName())){

                    Iterator<VisitableElement> it = xe.getElements().iterator();
                    while(it.hasNext()) {
                        VisitableElement ve2 = it.next();
                        if (ve2 instanceof XmlElement) {
                            for (VisitableElement ve3 : ((XmlElement) ve2).getElements()) {
                                if (ve3 instanceof TextElement) {
                                    String s = getTarget(((TextElement) ve3).getContent());
                                    if (s != null) {
                                        ((XmlElement) ve2).getElements().remove(ve3);
                                        ((XmlElement) ve2).addElement(new TextElement(s));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
