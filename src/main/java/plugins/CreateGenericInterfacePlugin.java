package plugins;


import lombok.NoArgsConstructor;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * Mapperインターフェースにインタフェースを追加
 * <p>
 * modeType は CONDITIONAL または HIERARCHICAL のみ対応
 * <p>
 * このプラグインは https://github.com/dcendents/mybatis-generator-plugins を参考にカスタマイズしました。
 */
@NoArgsConstructor
public class CreateGenericInterfacePlugin extends PluginAdapter {

    public static final String INTERFACE = "interface";
    public static final String STATUS_INTERFACE = "status_interface";
    public static final String TARGET_TABLE = "target_table";

    private String interfaceName;

    private String statusInterface;

    private List<String> targetTables;

    private Interface genericInterface;

    private final FullyQualifiedJavaType genericModel = new FullyQualifiedJavaType("T");
    private final FullyQualifiedJavaType genericExample = new FullyQualifiedJavaType("U");
    private final FullyQualifiedJavaType genericId = new FullyQualifiedJavaType("V");

    private FullyQualifiedJavaType genericModelList;
    private FullyQualifiedJavaType longPrimitive;

    private Set<String> methodsAdded;

    private Map<IntrospectedTable, FullyQualifiedJavaType> models;
    private Map<IntrospectedTable, FullyQualifiedJavaType> examples;
    private Map<IntrospectedTable, FullyQualifiedJavaType> ids;

    @Override
    public boolean validate(List<String> warnings) {
        interfaceName = properties.getProperty(INTERFACE);
        statusInterface = properties.getProperty(STATUS_INTERFACE);
        String target_table = properties.getProperty(TARGET_TABLE);

        String warning = "Property %s not set for plugin %s";
        if (!stringHasValue(interfaceName)) {
            warnings.add(String.format(warning, INTERFACE, this.getClass().getSimpleName()));
            return false;
        }
        if (!stringHasValue(statusInterface)) {
            warnings.add(String.format(warning, STATUS_INTERFACE, this.getClass().getSimpleName()));
            return false;
        }

        if (!stringHasValue(target_table)) {
            warnings.add(String.format(warning, TARGET_TABLE, this.getClass().getSimpleName()));
            return false;
        }
        targetTables = Arrays.asList(target_table.replace(" ", "").split(","));

        init();

        return true;
    }

    private void init() {
        genericModelList = FullyQualifiedJavaType.getNewListInstance();
        genericModelList.addTypeArgument(genericModel);

        longPrimitive = new FullyQualifiedJavaType("long");

        FullyQualifiedJavaType className = new FullyQualifiedJavaType(interfaceName);
        className.addTypeArgument(genericModel);
        className.addTypeArgument(genericExample);
        className.addTypeArgument(genericId);

        genericInterface = new Interface(className);
        genericInterface.setVisibility(JavaVisibility.PUBLIC);

        methodsAdded = new HashSet<>();

        models = new HashMap<>();
        examples = new HashMap<>();
        ids = new HashMap<>();
    }

    /**
     * テーブル名が対象かどうか
     *
     * @param tableName テーブル名
     * @return true: 対象, false: 非対象
     */
    private boolean isTarget(String tableName) {
        if (tableName == null) {
            return false;
        }
        return targetTables.contains(tableName);
    }

    /**
     * インターフェースファイルは手動で準備する前提で作成しない
     */
//    @Override
//    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles() {
//        List<GeneratedJavaFile> models = new ArrayList<>();
//
//        GeneratedJavaFile genericInterfaceFile =
//                new GeneratedJavaFile(genericInterface, context.getJavaClientGeneratorConfiguration().getTargetProject(), new DefaultJavaFormatter());
//
//        models.add(genericInterfaceFile);
//
//        return models;
//    }
    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {

        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }

        FullyQualifiedJavaType type = new FullyQualifiedJavaType(interfaceName);
        type.addTypeArgument(models.get(introspectedTable));
        type.addTypeArgument(examples.get(introspectedTable));
        type.addTypeArgument(ids.get(introspectedTable));

        interfaze.addSuperInterface(type);

        return true;
    }

    void addGenericMethod(Method method, FullyQualifiedJavaType returnType, FullyQualifiedJavaType... types) {
        method.addAnnotation("@Override");

        if (!methodsAdded.contains(method.getName())) {
            Method genericMethod = new Method(method.getName());
            genericMethod.addJavaDocLine("/**");
            genericMethod.addJavaDocLine(" * This method was generated by MyBatis Generator.");
            genericMethod.addJavaDocLine(" *");
            genericMethod.addJavaDocLine(" * @mbg.generated");
            genericMethod.addJavaDocLine(" */");

            genericMethod.setReturnType(returnType);


            for (int i = 0; i < method.getParameters().size(); i++) {
                Parameter parameter = method.getParameters().get(i);
                FullyQualifiedJavaType paramType = types.length > i ? types[i] : parameter.getType();

                Parameter genericParameter = new Parameter(paramType, parameter.getName());
                genericMethod.addParameter(genericParameter);
            }

            genericInterface.addMethod(genericMethod);

            methodsAdded.add(method.getName());
        }
    }

    @Override
    public boolean clientCountByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {

        // 対象でないテーブルの場合、処理を中断
        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }

        addClientCountByExample(method, introspectedTable);
        return true;
    }


    private void addClientCountByExample(Method method, IntrospectedTable introspectedTable) {
        examples.put(introspectedTable, method.getParameters().get(0).getType());
        addGenericMethod(method, longPrimitive, genericExample);
    }

    @Override
    public boolean clientDeleteByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }
        addClientDeleteByExample(method);
        return true;
    }

    private void addClientDeleteByExample(Method method) {
        addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericExample);
    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }
        addClientDeleteByPrimaryKey(method, introspectedTable);
        return true;
    }

    @Override
    public boolean clientGeneralDeleteMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }
        addClientDeleteByPrimaryKey(method, introspectedTable);
        return true;
    }

    private void addClientDeleteByPrimaryKey(Method method, IntrospectedTable introspectedTable) {
        ids.put(introspectedTable, method.getParameters().get(0).getType());
        addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericId);
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }
        addClientInsert(method, introspectedTable);
        return true;
    }

    private void addClientInsert(Method method, IntrospectedTable introspectedTable) {
        models.put(introspectedTable, method.getParameters().get(0).getType());
        addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericModel);
    }

    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }
        addClientSelectByExampleWithBLOBs(method);
        return true;
    }

    private void addClientSelectByExampleWithBLOBs(Method method) {
        addGenericMethod(method, genericModelList, genericExample);
    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }
        addClientSelectByExampleWithoutBLOBs(method);
        return true;
    }


    private void addClientSelectByExampleWithoutBLOBs(Method method) {
        addGenericMethod(method, genericModelList, genericExample);
    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }
        addClientSelectByPrimaryKey(method);
        return true;
    }


    private void addClientSelectByPrimaryKey(Method method) {
        addGenericMethod(method, genericModel, genericId);
    }

    @Override
    public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }
        addClientUpdateByExampleSelective(method);
        return true;
    }


    private void addClientUpdateByExampleSelective(Method method) {
        addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericModel, genericExample);
    }

    @Override
    public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }
        addClientUpdateByExampleWithBLOBs(method);
        return true;
    }

    private void addClientUpdateByExampleWithBLOBs(Method method) {
        addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericModel, genericExample);
    }

    @Override
    public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }
        addClientUpdateByExampleWithoutBLOBs(method);
        return true;
    }

    private void addClientUpdateByExampleWithoutBLOBs(Method method) {
        addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericModel, genericExample);
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }
        addClientUpdateByPrimaryKeySelective(method);
        return true;
    }

    private void addClientUpdateByPrimaryKeySelective(Method method) {
        addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericModel);
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }
        addClientUpdateByPrimaryKeyWithBLOBs(method);
        return true;
    }


    private void addClientUpdateByPrimaryKeyWithBLOBs(Method method) {
        addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericModel);
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }
        addClientUpdateByPrimaryKeyWithoutBLOBs(method);
        return true;
    }


    private void addClientUpdateByPrimaryKeyWithoutBLOBs(Method method) {
        addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericModel);
    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }
        addClientInsertSelective(method);
        return true;
    }


    private void addClientInsertSelective(Method method) {
        addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericModel);
    }

    @Override
    public boolean clientSelectAllMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }
        addClientSelectAll(method);
        return true;
    }

    private void addClientSelectAll(Method method) {
        addGenericMethod(method, genericModel);
    }


    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable) {

        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }

        implementBean(
                topLevelClass,
                introspectedTable.getFullyQualifiedTable(),
                introspectedTable);

        topLevelClass.addMethod(addGetIdMethod(topLevelClass, introspectedTable));

        return true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable) {

        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }

        if (introspectedTable.getBaseColumns().isEmpty()) {
            implementBean(
                    topLevelClass,
                    introspectedTable.getFullyQualifiedTable(),
                    introspectedTable);

            topLevelClass.addMethod(addGetIdMethod(topLevelClass, introspectedTable));
        }

        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(
            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        if (!isTarget(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return true;
        }

        implementBean(
                topLevelClass,
                introspectedTable.getFullyQualifiedTable(),
                introspectedTable
        );

        return true;
    }

    protected void implementBean(TopLevelClass topLevelClass,
                                 FullyQualifiedTable table, IntrospectedTable introspectedTable) {

        FullyQualifiedJavaType type = new FullyQualifiedJavaType(statusInterface);

        FullyQualifiedJavaType superClass = topLevelClass.getSuperClass().orElse(null);
        FullyQualifiedJavaType id;
        if (superClass == null) {
            // フラットなモデル = 非複合主キー
            id = introspectedTable.getPrimaryKeyColumns().get(0).getFullyQualifiedJavaType();
        } else {
            // 主キークラスをスーパークラスに持つ = 複合主キー
            id = superClass;
        }
        type.addTypeArgument(id);

        topLevelClass.addImportedType(type);
        topLevelClass.addSuperInterface(type);

    }

    protected Method addGetIdMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        Method method = new Method("getId");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("getId");

        FullyQualifiedJavaType superClass = topLevelClass.getSuperClass().orElse(null);
        if (superClass == null) {
            // フラットなモデル = 非複合主キー
            // public ID getId() { return <primary_key_field_name>; }
            IntrospectedColumn pkey = introspectedTable.getPrimaryKeyColumns().get(0);
            method.setReturnType(pkey.getFullyQualifiedJavaType());
            method.addBodyLine("return " + pkey.getJavaProperty() + ";");
        } else {
            // 主キークラスをスーパークラスに持つ = 複合主キー
            // public PKEY_CLASS getId() { return new PRIMARY_KEY_CLASS(); }
            method.setReturnType(superClass);

            // getPrimaryKeyField1(), getPrimaryKeyField2()
//            String idList = introspectedTable.getPrimaryKeyColumns().stream()
//                    .map(x -> "get" + capitalize(x.getJavaProperty()) + "()").collect(Collectors.joining(", "));


            method.addBodyLine(superClass.getShortName() + " superClass = new " + superClass.getShortName() + "();");

            introspectedTable.getPrimaryKeyColumns().stream().forEach(x -> {
                String field = capitalize(x.getJavaProperty());
                method.addBodyLine("superClass.set" + field + "(get" + field + "());");
            });

            method.addBodyLine("return superClass;");

        }

        return method;
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}