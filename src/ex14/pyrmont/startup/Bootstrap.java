package ex14.pyrmont.startup;

import ex14.pyrmont.core.SimpleContextConfig;
import org.apache.catalina.Connector;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.http.HttpConnector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.loader.WebappLoader;

public final class Bootstrap {
    public static void main(String[] args) {

        System.setProperty("catalina.base", System.getProperty("user.dir"));
        Connector connector = new HttpConnector();

        Wrapper wrapper1 = new StandardWrapper();
        wrapper1.setName("Primitive");
        wrapper1.setServletClass("PrimitiveServlet");
        Wrapper wrapper2 = new StandardWrapper();
        wrapper2.setName("Modern");
        wrapper2.setServletClass("ModernServlet");

        Context context = new StandardContext();
        // StandardContext's start method adds a default mapper
        context.setPath("/app1");
        context.setDocBase("app1");

        context.addChild(wrapper1);
        context.addChild(wrapper2);

        LifecycleListener listener = new SimpleContextConfig();
        ((Lifecycle) context).addLifecycleListener(listener);

        Loader loader = new WebappLoader();
        context.setLoader(loader);
        // context.addServletMapping(pattern, name);
        context.addServletMapping("/Primitive", "Primitive");
        context.addServletMapping("/Modern", "Modern");

        Host host = new StandardHost();
        host.addChild(context);     // 关联一个context实例
        host.setName("localhost");
        host.setAppBase("webapps");

        Engine engine = new StandardEngine();
        engine.addChild(host);      // 关联一个host实例，也就将context和host组装为engine
        engine.setDefaultHost("localhost");

        Service service = new StandardService();
        service.setName("Stand-alone Service");
        Server server = new StandardServer();
        server.addService(service);
        service.addConnector(connector);        // service使用的connector

        //StandardService class's setContainer will call all its connector's setContainer method
        service.setContainer(engine);       // 组件关联到容器，最终拼装出Servlet容器

        // Start the new server
        try {
            server.initialize();        // 初始化
            ((Lifecycle) server).start();   // 启动整个容器
            server.await();
            // the program waits until the await method returns,
            // i.e. until a shutdown command is received.
        } catch (LifecycleException e) {
            e.printStackTrace(System.out);
        }

        // Shut down the server
        try {
            ((Lifecycle) server).stop();
        } catch (LifecycleException e) {
            e.printStackTrace(System.out);
        }
    }
}