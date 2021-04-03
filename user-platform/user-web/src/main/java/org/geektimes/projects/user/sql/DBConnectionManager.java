package org.geektimes.projects.user.sql;

import org.geektimes.projects.user.context.ComponentContext;
import org.geektimes.projects.user.domain.User;

import javax.sql.DataSource;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnectionManager {
    private final Logger logger = Logger.getLogger(DBConnectionManager.class.getName());
    public Connection getConnection() {
        ComponentContext componentContext = ComponentContext.getInstance();
        DataSource dataSource = componentContext.getComponent("jdbc/UserPlatformDB");
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        if (connection != null) {
            logger.log(Level.INFO, "获取 JNDI 数据库连接成功！");
            System.out.println("获取 JNDI 数据库连接成功！");
        }
        return connection;
    }

    public void releaseConnection() {
//        if (this.connection != null) {
//            try {
//                this.connection.close();
//            } catch (SQLException e) {
//                throw new RuntimeException(e.getCause());
//            }
//        }
    }

    public static final String DROP_USERS_TABLE_DDL_SQL = "DROP TABLE users";

    public static final String CREATE_USERS_TABLE_DDL_SQL = "CREATE TABLE users(" +
            "id INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
            "name VARCHAR(16) NOT NULL, " +
            "password VARCHAR(64) NOT NULL, " +
            "email VARCHAR(64) NOT NULL, " +
            "phoneNumber VARCHAR(64) NOT NULL" +
            ")";

    public static final String INSERT_USER_DML_SQL = "INSERT INTO users(name,password,email,phoneNumber) VALUES " +
            "('A','******','a@gmail.com','1') , " +
            "('B','******','b@gmail.com','2') , " +
            "('C','******','c@gmail.com','3') , " +
            "('D','******','d@gmail.com','4') , " +
            "('E','******','e@gmail.com','5')";


    public static void main(String[] args) throws Exception {

        // 通过forNmae 的方式 注册驱动 并获取驱动和链接
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Driver driver = DriverManager.getDriver("jdbc:derby:db/user-platform;create=true");
        Connection connection = driver.connect("jdbc:derby:db/user-platform;create=true", new Properties());
        Statement statement = connection.createStatement();
        //statement.execute(DROP_USERS_TABLE_DDL_SQL);
        statement.execute(CREATE_USERS_TABLE_DDL_SQL);
        statement.execute(INSERT_USER_DML_SQL);

        ResultSet resultSet = statement.executeQuery("SELECT id,name,password,email,phoneNumber FROM users");
        // 简单的查询方式 已知对象和字段
//        while (resultSet.next()) {
//            User user = new User();
//            user.setId(resultSet.getLong("id"));
//            user.setName(resultSet.getString("name"));
//            user.setPassword(resultSet.getString("password"));
//            user.setEmail(resultSet.getString("email"));
//            user.setPhoneNumber(resultSet.getString("phoneNumber"));
//            System.out.println(user.toString());
//        }



        BeanInfo beanInfo = Introspector.getBeanInfo(User.class, Object.class);
        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            System.out.println("字段名称： "+ propertyDescriptor.getName()+"  字段类型" + propertyDescriptor.getPropertyType());
        }


        // 利用ResultSet  的元数据信息生成一个简单的SQL
        ResultSetMetaData metaData = resultSet.getMetaData();

        while (resultSet.next()){
            // 表名
            String tableName = metaData.getTableName(1);
            System.out.println("表名： " + tableName);
            System.out.println("列的个数：" + metaData.getColumnCount());
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                System.out.println("列的名称 ： " + metaData.getColumnName(i) + " 列的类型： " + metaData.getColumnType(i));
            }
            StringBuilder queryAllUserSQLBuilder = new StringBuilder();
            queryAllUserSQLBuilder.append("SELECT");
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                queryAllUserSQLBuilder.append(" ").append(metaData.getColumnName(i)).append(",");
            }
            queryAllUserSQLBuilder.deleteCharAt(queryAllUserSQLBuilder.length()-1);
            queryAllUserSQLBuilder.append(" FROM ").append(metaData.getTableName(1));
            System.out.println(queryAllUserSQLBuilder);
        }

        // ORM 框架的精髓
        while (resultSet.next()){
            User user = new User();
            for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                String filedName = propertyDescriptor.getName();
                Class<?> filedType = propertyDescriptor.getPropertyType();
                String columnLabel = mapColumnLabel(filedName);
                // 做user字段类型与resultSet方法的映射
                String methodName = typeMethodMappings.get(filedType);
                Method resultSetmethod = ResultSet.class.getMethod(methodName, String.class);
                Object resultValue = resultSetmethod.invoke(resultSet, columnLabel);
                // 获取User的Set方法
                // getWriteMethod（） 方法就是获取 User的set方法
                // getReadMethod() 方法 就是获取 User的get方法
                Method userSetMethod = propertyDescriptor.getWriteMethod();
                userSetMethod.invoke(user, resultValue);
            }
            System.out.println(user.toString());
        }

    }

    private static String mapColumnLabel(String filedName) {
        return filedName;
    }

    /**
     * 数据类型与 ResultSet 方法名映射
     */
    static Map<Class, String> typeMethodMappings = new HashMap<>();

    static {
        typeMethodMappings.put(Long.class, "getLong");
        typeMethodMappings.put(String.class, "getString");
    }
}

