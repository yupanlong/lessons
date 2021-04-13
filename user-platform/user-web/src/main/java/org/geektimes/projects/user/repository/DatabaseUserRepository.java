package org.geektimes.projects.user.repository;

import org.geektimes.function.ThrowableFunction;
import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.sql.DBConnectionManager;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang.ClassUtils.wrapperToPrimitive;

public class DatabaseUserRepository implements UserRepository {

    private final DBConnectionManager dbConnectionManager;

    private static final Logger logger = Logger.getLogger(DatabaseUserRepository.class.getName());
    /**
     * 异常通用处理方式
     */
    private static final Consumer<Throwable> COMMON_EXCEPTION_HANDLER = e -> logger.log(Level.SEVERE, e.getMessage());

    public static final String INSERT_USER_DML_SQL =
            "INSERT INTO users(name,password,email,phoneNumber) VALUES " +
                    "(?,?,?,?)";


    public DatabaseUserRepository(DBConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;
    }
    public Connection getConnection(){
        return dbConnectionManager.getConnection();
    }

    @Override
    public boolean save(User user) {
        Connection connection = getConnection();
        PreparedStatement prepareStatement;
        try {
            prepareStatement = connection.prepareStatement(INSERT_USER_DML_SQL);
            prepareStatement.setString(1,user.getName());
            prepareStatement.setString(2, user.getPassword());
            prepareStatement.setString(3,user.getEmail());
            prepareStatement.setString(4,user.getPhoneNumber());
             prepareStatement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteById(Long userId) {
        return false;
    }

    @Override
    public boolean update(User user) {
        return false;
    }

    @Override
    public User getById(Long userId) {
        return null;
    }

    @Override
    public User getByNameAndPassword(String userName, String password) {
        return executeQuery("SELECT id,name,password,email,phoneNumber FROM users WHERE name=? and password=?",
                resultSet -> {
                    // TODO
                    return new User();
                }, COMMON_EXCEPTION_HANDLER, userName, password);
    }

    @Override
    public Collection<User> getAll() {
        String QUERY_ALL_USER_SQL = "SELECT id,name,password,email,phoneNumber FROM users";
        return executeQuery(QUERY_ALL_USER_SQL, resultSet->{
            BeanInfo beanInfo = Introspector.getBeanInfo(User.class, Object.class);
            List<User> users = new ArrayList<>();
            while (resultSet.next()){
                User user = new User();
                for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                    String fieldName = propertyDescriptor.getName();
                    Class<?> propertyType = propertyDescriptor.getPropertyType();
                    String resultSetMethodName = resultSetMethodMappings.get(propertyType);
                    // 可能存在映射关系（不过此处是相等的）
                    String columnLabel = mapColumnLabel(fieldName);
                    Method resultSetMethod = ResultSet.class.getMethod(resultSetMethodName, String.class);
                    Object resultSetValue = resultSetMethod.invoke(resultSet, columnLabel);
                    Method setMethodOfUser = propertyDescriptor.getWriteMethod();
                    setMethodOfUser.invoke(user,resultSetValue);
                }
                users.add(user);
            }
            return users;
        },e->{
            // 异常的处理
        });
    }

    protected  <T> T executeQuery(String sql , ThrowableFunction<ResultSet, T> function,
                                  Consumer<Throwable> exceptionHandler, Object... args) {
        try {
            // 获取数据库链接
            Connection conection = dbConnectionManager.getConnection();
            PreparedStatement prepareStatement = conection.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                // 获取每个参数
                Object arg = args[i];
                Class argType = arg.getClass();
                // Interger -> int  拆箱操作  包装类型转成基本类型
                Class wrapperType = wrapperToPrimitive(argType);
                // 如果打不到映射 使用原类型 （也就是基本类型）
                if (wrapperType == null) {
                    wrapperType = argType;
                }
                // 根据参数类型选择prepareStatement 相对类型的set方法
                String methodName = preparedStatementMethodMappings.get(argType);

                Method method = PreparedStatement.class.getMethod(methodName, wrapperType);
                method.invoke(prepareStatement,i+1,arg);
            }
            // 执行查询
            ResultSet resultSet = prepareStatement.executeQuery();
            // 返回一个 POJO List -> ResultSet -> POJO List
            // ResultSet -> T
            // 把ResultSet 转换成需要的POJO List 交给传递进来的函数处理
            return function.apply(resultSet);
        } catch (Throwable e) {
            // 异常的处理
            // 异常的统一处理
            exceptionHandler.accept(e);
        }
        return null;
    }


    private static String mapColumnLabel(String fieldName) {
        return fieldName;
    }

    /**
     * 数据类型与 ResultSet 方法名映射
     */
    static Map<Class, String> resultSetMethodMappings = new HashMap<>();

    static Map<Class, String> preparedStatementMethodMappings = new HashMap<>();

    static {
        resultSetMethodMappings.put(Long.class, "getLong");
        resultSetMethodMappings.put(String.class, "getString");

        preparedStatementMethodMappings.put(Long.class, "setLong"); // long
        preparedStatementMethodMappings.put(String.class, "setString"); //


    }
}
