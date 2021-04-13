package org.geektimes.context;

import org.geektimes.function.ThrowableAction;
import org.geektimes.function.ThrowableFunction;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.naming.*;
import javax.servlet.ServletContext;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class ComponentContext {
    /**
     * 组件上下文名称
     */
    public static final String CONTEXT_NAME = ComponentContext.class.getName();

    private static final Logger logger = Logger.getLogger(CONTEXT_NAME);

    /**
     * 组件名称
     */
    private static final String COMPONENT_ENV_CONTEXT_NAME = "java:comp/env";
    /**
     * 假设一个 Tomcat JVM 进程，三个 Web Apps，会不会相互冲突？（不会冲突）
     * static 字段是 JVM 缓存吗？（是 ClassLoader 缓存）
     * private static ApplicationContext applicationContext;
     * <p>
     * public void setApplicationContext(ApplicationContext applicationContext){
     * ComponentContext.applicationContext = applicationContext;
     * WebApplicationContextUtils.getRootWebApplicationContext()
     * }
     */
    private static ServletContext servletContext; // 请注意

    private ClassLoader classLoader;

    /**
     * Component Env Context
     */
    private Context envContext;
    /**
     * META/INF context.xml 中所有的组件
     */
    private Map<String, Object> componentsMap = new LinkedHashMap<>();

    /**
     * 获取ComponentContext实例
     *
     * @return
     */
    public static ComponentContext getInstance() {
        return (ComponentContext) servletContext.getAttribute(CONTEXT_NAME);
    }

    public void init(ServletContext servletContext) {
        ComponentContext.servletContext = servletContext;
        servletContext.setAttribute(CONTEXT_NAME, this);
        this.classLoader = servletContext.getClassLoader();
        initEnvContext();
        instantiateComponents();
        initializeComponents();

    }

    private void initEnvContext() {
        if (this.envContext != null) return;
        Context context = null;
        try {
            context = new InitialContext();
            this.envContext = (Context) context.lookup(COMPONENT_ENV_CONTEXT_NAME);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            close(context);
        }
    }

    private static void close(Context context) {
        if (context != null)
            ThrowableAction.execute(context::close);
    }

    /**
     * 实例化组件
     */
    private void instantiateComponents() {
        List<String> componentNames = listAllComponentNames();
        componentNames.forEach(name -> componentsMap.put(name, lookupComponent(name)));
    }
    private List<String> listAllComponentNames() {
        return listComponentNames("/");
    }

    protected List<String> listComponentNames(String name) {
        return executeInContext(context -> {
            NamingEnumeration<NameClassPair> e = executeInContext(context, ctx -> ctx.list(name), true);
            if (e == null) { // 当前 JNDI 名称下没有子节点
                return Collections.emptyList();
            }
            List<String> fullNames = new LinkedList<>();
            while (e.hasMoreElements()) {
                NameClassPair element = e.nextElement();
                String className = element.getClassName();
                Class<?> targetClass = classLoader.loadClass(className);
                if (Context.class.isAssignableFrom(targetClass)) {
                    // 如果当前名称是目录（Context 实现类）的话，递归查找
                    fullNames.addAll(listComponentNames(element.getName()));
                } else {
                    // 否则，当前名称绑定目标类型的话话，添加该名称到集合中
                    String fullName = name.startsWith("/") ?
                            element.getName() : name + "/" + element.getName();
                    fullNames.add(fullName);
                }
            }
            return fullNames;
        });
    }


    /**
     * 初始化组件
     * <ol>
     *  <li>注入阶段 - {@link Resource}</li>
     *  <li>初始阶段 - {@link PostConstruct}</li>
     *  <li>销毁阶段 - {@link PreDestroy}</li>
     * </ol>
     */
    protected void initializeComponents() {
        componentsMap.values().forEach(component -> {
            Class<?> componentClass = component.getClass();
            // 注入阶段 - {@link Resource}
            injectComponents(component, componentClass);
            // 初始阶段 - {@link PostConstruct}
            processPostConstruct(component, componentClass);
            // TODO 实现销毁阶段 - {@link PreDestroy}
            processPreDestroy();
        });
    }



    private void injectComponents(Object component, Class<?> componentClass) {
        Stream.of(componentClass.getDeclaredFields())
                .filter(field -> {
                    int mods = field.getModifiers();
                    return !Modifier.isStatic(mods) &&
                            field.isAnnotationPresent(Resource.class);
                }).forEach(field -> {
            Resource resource = field.getAnnotation(Resource.class);
            String resourceName = resource.name();
            Object injectedObject = lookupComponent(resourceName);
            field.setAccessible(true);
            try {
                // 注入目标对象
                field.set(component, injectedObject);
            } catch (IllegalAccessException e) {
            }
        });
    }

    private void processPostConstruct(Object component, Class<?> componentClass) {
        Stream.of(componentClass.getMethods())
                .filter(method ->
                        !Modifier.isStatic(method.getModifiers()) &&      // 非 static
                                method.getParameterCount() == 0 &&        // 没有参数
                                method.isAnnotationPresent(PostConstruct.class) // 标注 @PostConstruct
                ).forEach(method -> {
            // 执行目标方法
            try {
                method.invoke(component);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void processPreDestroy() {
    }

    /**
     * 通过名称进行依赖查找
     *  内部使用
     * @param name
     * @param <C>
     * @return
     */
    protected <C> C lookupComponent(String name) {
        return executeInContext(context -> (C) context.lookup(name));
    }

    /**
     * 通过名称进行依赖查找
     *  外部使用
     * @param name
     * @param <C>
     * @return
     */
    public <C> C getComponent(String name) {
            return (C) componentsMap.get(name);
    }
    /**
     * 在 Context 中执行，通过指定 ThrowableFunction 返回计算结果
     *
     * @param function ThrowableFunction
     * @param <R>      返回结果类型
     * @return 返回
     * @see ThrowableFunction#apply(Object)
     */
    protected <R> R executeInContext(ThrowableFunction<Context, R> function) {
        return executeInContext(function, false);
    }

    /**
     * 在 Context 中执行，通过指定 ThrowableFunction 返回计算结果
     *
     * @param function         ThrowableFunction
     * @param ignoredException 是否忽略异常
     * @param <R>              返回结果类型
     * @return 返回
     * @see ThrowableFunction#apply(Object)
     */
    protected <R> R executeInContext(ThrowableFunction<Context, R> function, boolean ignoredException) {
        return executeInContext(this.envContext, function, ignoredException);
    }

    private <R> R executeInContext(Context context, ThrowableFunction<Context, R> function,
                                   boolean ignoredException) {
        R result = null;
        try {
            result = ThrowableFunction.execute(context, function);
        } catch (Throwable e) {
            if (ignoredException) {
                logger.warning(e.getMessage());
            } else {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public void destroy() {
        if (this.envContext != null) {
            try {
                envContext.close();
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
