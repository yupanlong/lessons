package org.geektimes.projects.user.web.listener;

import org.eclipse.microprofile.config.Config;
import org.geektimes.configuration.microprofile.config.DefaultConfigProviderResolver;
import org.geektimes.context.ComponentContext;
import org.geektimes.projects.user.sql.DBConnectionManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Logger;

/**
 * 测试用途
 */
@Deprecated
public class TestingListener implements ServletContextListener {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ComponentContext context = ComponentContext.getInstance();
        DBConnectionManager dbConnectionManager = context.getComponent("bean/DBConnectionManager");
        dbConnectionManager.getConnection();
        testPropertyFromServletContext(sce);
        testPropertyFromJNDI(context);
        testMicroProfile();
    }

    private void testMicroProfile() {
        DefaultConfigProviderResolver javaEEConfigProviderResolver = new DefaultConfigProviderResolver();
        Config config = javaEEConfigProviderResolver.getConfig();
        String configValue = config.getValue("application.name", String.class);
        logger.info("MicroProfile Property application.name [ "+configValue+"]");
    }

    /**
     *获取ServletContext配置的属性 配置在webapp/WEB-INF/web.xml
     * @param sce
     */
    private void testPropertyFromServletContext(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        String propertyName = "application.name";
        logger.info("ServletContext Property"+propertyName+"["+servletContext.getInitParameter(propertyName)+"]");
    }


    /**
     * 获取 JNDI 配置的属性 配置在 webapp/META-INF/context.xml
     * @param context
     */
    private void testPropertyFromJNDI(ComponentContext context) {
        String propertyName = "maxValue";
        logger.info("JDNI Property"+propertyName+"["+context.lookupComponent(propertyName)+"]");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}
