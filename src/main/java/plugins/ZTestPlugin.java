package plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.*;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ZTestPlugin extends PluginAdapter {

    private final boolean isSimple = false;
    private String tableName;

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {

        // メソッド名を指定
        Method method = new Method("merge");
        // 戻り値の型を指定
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(true);

        FullyQualifiedJavaType parameterType = introspectedTable.getRules().calculateAllFieldsClass();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<>();
        importedTypes.add(parameterType);
        method.addParameter(new Parameter(parameterType, "row")); //$NON-NLS-1$

        // Javadocコメントを追加
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        // メソッドの追加
        interfaze.addImportedTypes(importedTypes);
        interfaze.addMethod(method);

        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {

        tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();

        List<VisitableElement> insertElements = new ArrayList<>();
        List<VisitableElement> updateElements = new ArrayList<>();

        for (VisitableElement v : document.getRootElement().getElements()) {
            XmlElement x;
            if (v instanceof XmlElement) {
                x = (XmlElement) v;
                if (x.getAttributes().get(0).getValue().equals("insert")) {
                    insertElements = x.getElements();
                } else if (x.getAttributes().get(0).getValue().equals("updateByPrimaryKey")) {
                    updateElements = x.getElements();
                }
            }
        }

        // XMLのタグ情報
        XmlElement xmlElement = new XmlElement("update");
        xmlElement.addAttribute(new Attribute("id", "merge"));
        xmlElement.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));

        context.getCommentGenerator().addComment(xmlElement);

        // outputs.add("merge into " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime() + " as tgt");
        xmlElement.addElement(new TextElement(
                "merge into " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()
        ));

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            if (i != 0) {
                sb.append(" and ");
            }
            // sb.append("tgt.").append(introspectedColumn.getActualColumnName())
            sb.append(introspectedColumn.getActualColumnName())
                    .append(" = ")
                    .append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
            i++;
        }

        xmlElement.addElement(new TextElement(
                "using (select 1 from dual) on (" + sb + ")"
        ));

        // update
        xmlElement.addElement(new TextElement("when matched then"));
        createUpdateClause(xmlElement, updateElements);

        // insert
        xmlElement.addElement(new TextElement("when not matched then"));
        createInsertClause(xmlElement, insertElements);

        List<XmlElement> addElementList = new ArrayList<>();
        addElementList.add(xmlElement);

        // XMLに新規メソッド用のSQLを追加
        document.getRootElement().getElements().addAll(addElementList);

        return true;
    }

    /**
     *　標準のInsertSqlを加工して、Merge用のInsertSQLを作成(テーブル名を削除)
     */
    private void createInsertClause(XmlElement xmlElement, List<VisitableElement> insertElements) {
        for (VisitableElement v : insertElements.stream().skip(4L).collect(Collectors.toList())) {
            if (v instanceof TextElement) {
                TextElement t = (TextElement) v;
                String tblName = "into " + tableName;
                if (t.getContent().toLowerCase().contains(tblName)) {
                    xmlElement.addElement(new TextElement(t.getContent().replace(tblName, "")));
                } else {
                    xmlElement.addElement(new TextElement(t.getContent()));
                }
            }
        }
    }

    /**
     *　標準のUpdateSqlを加工して、Merge用のUpdateSQLを作成(テーブル名を削除)
     */
    private void createUpdateClause(XmlElement xmlElement, List<VisitableElement> insertElements) {
        for (VisitableElement v : insertElements.stream().skip(4L).collect(Collectors.toList())) {
            if (v instanceof TextElement) {
                TextElement t = (TextElement) v;
                String tblName = "update " + tableName;
                if (t.getContent().toLowerCase().contains(tblName)) {
                    xmlElement.addElement(new TextElement(t.getContent().replace(tblName, "update")));
                } else {
                    xmlElement.addElement(new TextElement(t.getContent()));
                }
            }
        }
    }

/*
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {

        List<String> outputs = new ArrayList<>();

        // XMLのタグ情報
        XmlElement xmlElement = new XmlElement("update");
        xmlElement.addAttribute(new Attribute("id", "merge"));
        xmlElement.addAttribute(new Attribute("parameterType", introspectedTable.getExampleType()));

        context.getCommentGenerator().addComment(xmlElement);

        // outputs.add("merge into " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime() + " as tgt");
        outputs.add("merge into " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            if (i != 0) {
                sb.append(" and ");
            }
            // sb.append("tgt.").append(introspectedColumn.getActualColumnName())
            sb.append(introspectedColumn.getActualColumnName())
                    .append(" = ")
                    .append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
            i++;
        }

        // outputs.add("using (select 1) as src on ( " + sb + " )");
        outputs.add("using (select 1) on ( " + sb + " )");

        outputs.add("when matched then");
        outputs.addAll(updateClause(document, introspectedTable));

        outputs.add("when not matched then");
        outputs.addAll(insertClause(document, introspectedTable));

        List<XmlElement> addElementList = new ArrayList<>();
        for (String s : outputs) {
            xmlElement.addElement(new TextElement(s));
        }
        addElementList.add(xmlElement);

        // XMLに新規メソッド用のSQLを追加
        document.getRootElement().getElements().addAll(addElementList);

        return true;
    }

    // -------------------------------------------------------------------

    List<String> insertClause(Document document, IntrospectedTable introspectedTable) {

        List<String> result = new ArrayList<>();

        FullyQualifiedJavaType parameterType;
        if (isSimple) {
            parameterType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        } else {
            parameterType = introspectedTable.getRules().calculateAllFieldsClass();
        }

        StringBuilder insertClause = new StringBuilder();

        insertClause.append("insert into ");
        insertClause.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        insertClause.append(" (");

        StringBuilder valuesClause = new StringBuilder();
        valuesClause.append("values (");

        List<IntrospectedColumn> columns =
                ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());

        for (int i = 0; i < columns.size(); i++) {
            IntrospectedColumn introspectedColumn = columns.get(i);

            insertClause.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            valuesClause.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
            if (i + 1 < columns.size()) {
                insertClause.append(", ");
                valuesClause.append(", ");
            }
        }

        insertClause.append(')');
        valuesClause.append(')');

        result.add(insertClause.toString());
        result.add(valuesClause.toString());

        return result;
    }


    List<String> updateClause(Document document, IntrospectedTable introspectedTable) {
        List<String> result = new ArrayList<>();

        List<IntrospectedColumn> columns;
        if (isSimple) {
            columns = introspectedTable.getNonPrimaryKeyColumns();
        } else {
            columns = introspectedTable.getBaseColumns();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("update ");
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        result.add(sb.toString());

        sb.setLength(0);
        sb.append("set ");

        Iterator<IntrospectedColumn> iter = ListUtilities.removeGeneratedAlwaysColumns(columns).iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();

            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = "); //$NON-NLS-1$
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));

            if (iter.hasNext()) {
                sb.append(',');
            }

            result.add(sb.toString());

            // set up for the next column
            if (iter.hasNext()) {
                sb.setLength(0);
                OutputUtilities.xmlIndent(sb, 1);
            }
        }

        result.addAll(buildPrimaryKeyWhereClause(introspectedTable));

        return result;
    }

    protected List<String> buildPrimaryKeyWhereClause(IntrospectedTable introspectedTable) {
        List<String> answer = new ArrayList<>();
        boolean first = true;
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            String line;
            if (first) {
                line = "where ";
                first = false;
            } else {
                line = "  and ";
            }

            line += MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn);
            line += " = ";
            line += MyBatis3FormattingUtilities.getParameterClause(introspectedColumn);
            answer.add(line);
        }

        return answer;
    }
 */

}
