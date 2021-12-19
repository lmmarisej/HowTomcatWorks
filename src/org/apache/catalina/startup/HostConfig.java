/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/startup/HostConfig.java,v 1.23 2002/05/30 22:12:28 remm Exp $
 * $Revision: 1.23 $
 * $Date: 2002/05/30 22:12:28 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */


package org.apache.catalina.startup;


import org.apache.catalina.*;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.util.StringManager;
import org.apache.naming.resources.ResourceAttributes;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * Startup event listener for a <b>Host</b> that configures the properties
 * of that Host, and the associated defined contexts.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 1.23 $ $Date: 2002/05/30 22:12:28 $
 */

public class HostConfig     // HostConfig的实例添加到Host实例中，作为声明周期监听器；处理StandardHost实例的start方法和stop方法触发的事件
        implements LifecycleListener,
        Runnable {


    // ----------------------------------------------------- Instance Variables


    /**
     * The Java class name of the Context configuration class we should use.
     */
    protected String configClass = "org.apache.catalina.startup.ContextConfig";


    /**
     * The Java class name of the Context implementation we should use.
     */
    protected String contextClass = "org.apache.catalina.core.StandardContext";


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;


    /**
     * The names of applications that we have auto-deployed (to avoid
     * double deployment attempts).
     */
    protected ArrayList deployed = new ArrayList();


    /**
     * The Host we are associated with.
     */
    protected Host host = null;


    /**
     * The string resources for this package.
     */
    protected static final StringManager sm = StringManager.getManager(Constants.Package);


    /**
     * The number of seconds between checks for web app deployment.
     */
    private final int checkInterval = 15;


    /**
     * Should we deploy XML Context config files?
     */
    private boolean deployXML = false;


    /**
     * Should we monitor the <code>appBase</code> directory for new
     * applications and automatically deploy them?
     */
    private boolean liveDeploy = false;


    /**
     * The background thread.
     */
    private Thread thread = null;


    /**
     * The background thread completion semaphore.
     */
    private boolean threadDone = false;


    /**
     * Name to register for the background thread.
     */
    private String threadName = "HostConfig";


    /**
     * Should we unpack WAR files when auto-deploying applications in the
     * <code>appBase</code> directory?
     */
    private boolean unpackWARs = false;


    /**
     * Last modified dates of the web.xml files of the contexts, keyed by
     * context name.
     */
    private final HashMap webXmlLastModified = new HashMap();


    // ------------------------------------------------------------- Properties


    /**
     * Return the Context configuration class name.
     */
    public String getConfigClass() {

        return (this.configClass);

    }


    /**
     * Set the Context configuration class name.
     *
     * @param configClass The new Context configuration class name.
     */
    public void setConfigClass(String configClass) {

        this.configClass = configClass;

    }


    /**
     * Return the Context implementation class name.
     */
    public String getContextClass() {

        return (this.contextClass);

    }


    /**
     * Set the Context implementation class name.
     *
     * @param contextClass The new Context implementation class name.
     */
    public void setContextClass(String contextClass) {

        this.contextClass = contextClass;

    }


    /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {

        return (this.debug);

    }


    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {

        this.debug = debug;

    }


    /**
     * Return the deploy XML config file flag for this component.
     */
    public boolean isDeployXML() {

        return (this.deployXML);

    }


    /**
     * Set the deploy XML config file flag for this component.
     *
     * @param deployXML The new deploy XML flag
     */
    public void setDeployXML(boolean deployXML) {

        this.deployXML = deployXML;

    }


    /**
     * Return the live deploy flag for this component.
     */
    public boolean isLiveDeploy() {

        return (this.liveDeploy);

    }


    /**
     * Set the live deploy flag for this component.
     *
     * @param liveDeploy The new live deploy flag
     */
    public void setLiveDeploy(boolean liveDeploy) {

        this.liveDeploy = liveDeploy;

    }


    /**
     * Return the unpack WARs flag.
     */
    public boolean isUnpackWARs() {

        return (this.unpackWARs);

    }


    /**
     * Set the unpack WARs flag.
     *
     * @param unpackWARs The new unpack WARs flag
     */
    public void setUnpackWARs(boolean unpackWARs) {

        this.unpackWARs = unpackWARs;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Process the START event for an associated Host.
     *
     * @param event The lifecycle event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {      // 事件处理程序

        // Identify the host we are associated with
        try {
            host = (Host) event.getLifecycle();
            if (host instanceof StandardHost) {
                int hostDebug = ((StandardHost) host).getDebug();
                if (hostDebug > this.debug) {
                    this.debug = hostDebug;
                }
                setDeployXML(((StandardHost) host).isDeployXML());      // 部署一个context实例描述符文件
                setLiveDeploy(((StandardHost) host).getLiveDeploy());   // 指明host实例是否要周期性的检查一个新的部署
                setUnpackWARs(((StandardHost) host).isUnpackWARs());    // 将war文件解压
            }
        } catch (ClassCastException e) {
            log(sm.getString("hostConfig.cce", event.getLifecycle()), e);
            return;
        }

        // Process the event that has occurred
        if (event.getType().equals(Lifecycle.START_EVENT))
            start();
        else if (event.getType().equals(Lifecycle.STOP_EVENT))
            stop();

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Return a File object representing the "application root" directory
     * for our associated Host.
     */
    protected File appBase() {

        File file = new File(host.getAppBase());
        if (!file.isAbsolute())
            file = new File(System.getProperty("catalina.base"),
                    host.getAppBase());
        return (file);

    }


    /**
     * Deploy applications for any directories or WAR files that are found
     * in our "application root" directory.
     */
    protected void deployApps() {       // 完成应用的部署

        if (!(host instanceof Deployer))
            return;
        if (debug >= 1)
            log(sm.getString("hostConfig.deploying"));

        File appBase = appBase();
        if (!appBase.exists() || !appBase.isDirectory())
            return;
        String[] files = appBase.list();

        deployDescriptors(appBase, files);
        deployWARs(appBase, files);             // 以war包部署
        deployDirectories(appBase, files);

    }


    /**
     * Deploy XML context descriptors.
     */
    protected void deployDescriptors(File appBase, String[] files) {

        if (!deployXML)
            return;

        for (String s : files) {

            if (s.equalsIgnoreCase("META-INF"))
                continue;
            if (s.equalsIgnoreCase("WEB-INF"))
                continue;
            if (deployed.contains(s))
                continue;
            File dir = new File(appBase, s);
            if (s.toLowerCase().endsWith(".xml")) {

                deployed.add(s);

                // Calculate the context path and make sure it is unique
                String file = s.substring(0, s.length() - 4);
                String contextPath = "/" + file;
                if (file.equals("ROOT")) {
                    contextPath = "";
                }
                if (host.findChild(contextPath) != null) {
                    continue;
                }

                // Assume this is a configuration descriptor and deploy it
                log(sm.getString("hostConfig.deployDescriptor", s));
                try {
                    URL config = new URL("file", null, dir.getCanonicalPath());
                    ((Deployer) host).install(config, null);
                } catch (Throwable t) {
                    log(sm.getString("hostConfig.deployDescriptor.error", s), t);
                }

            }

        }

    }


    /**
     * Deploy WAR files.
     */
    protected void deployWARs(File appBase, String[] files) {

        for (String file : files) {

            if (file.equalsIgnoreCase("META-INF"))
                continue;
            if (file.equalsIgnoreCase("WEB-INF"))
                continue;
            if (deployed.contains(file))
                continue;
            File dir = new File(appBase, file);
            if (file.toLowerCase().endsWith(".war")) {

                deployed.add(file);

                // Calculate the context path and make sure it is unique
                String contextPath = "/" + file;
                int period = contextPath.lastIndexOf(".");
                if (period >= 0)
                    contextPath = contextPath.substring(0, period);
                if (contextPath.equals("/ROOT"))
                    contextPath = "";       // 将ROOT应用的contextPath设置为根路径
                if (host.findChild(contextPath) != null)
                    continue;

                if (isUnpackWARs()) {

                    // Expand and deploy this application as a directory
                    log(sm.getString("hostConfig.expand", file));
                    try {
                        URL url = new URL("jar:file:" + dir.getCanonicalPath() + "!/");
                        String path = expand(url);      // 解压
                        url = new URL("file:" + path);
                        ((Deployer) host).install(contextPath, url);        // 执行部署，主要就是将其添加到host下，并发送事件通知其它组件
                    } catch (Throwable t) {
                        log(sm.getString("hostConfig.expand.error", file), t);
                    }

                } else {

                    // Deploy the application in this WAR file
                    log(sm.getString("hostConfig.deployJar", file));
                    try {
                        URL url = new URL("file", null, dir.getCanonicalPath());
                        url = new URL("jar:" + url + "!/");
                        ((Deployer) host).install(contextPath, url);
                    } catch (Throwable t) {
                        log(sm.getString("hostConfig.deployJar.error", file), t);
                    }

                }

            }

        }

    }


    /**
     * Deploy directories.
     */
    protected void deployDirectories(File appBase, String[] files) {

        for (String file : files) {

            if (file.equalsIgnoreCase("META-INF"))
                continue;
            if (file.equalsIgnoreCase("WEB-INF"))
                continue;
            if (deployed.contains(file))
                continue;
            File dir = new File(appBase, file);
            if (dir.isDirectory()) {

                deployed.add(file);

                // Make sure there is an application configuration directory
                // This is needed if the Context appBase is the same as the
                // web server document root to make sure only web applications
                // are deployed and not directories for web space.
                File webInf = new File(dir, "/WEB-INF");
                if (!webInf.exists() || !webInf.isDirectory() ||
                        !webInf.canRead())
                    continue;

                // Calculate the context path and make sure it is unique
                String contextPath = "/" + file;
                if (file.equals("ROOT"))
                    contextPath = "";
                if (host.findChild(contextPath) != null)
                    continue;

                // Deploy the application in this directory
                log(sm.getString("hostConfig.deployDir", file));
                try {
                    URL url = new URL("file", null, dir.getCanonicalPath());
                    ((Deployer) host).install(contextPath, url);
                } catch (Throwable t) {
                    log(sm.getString("hostConfig.deployDir.error", file),
                            t);
                }

            }

        }

    }


    /**
     * Check deployment descriptors last modified date.
     */
    protected void checkWebXmlLastModified() {

        if (!(host instanceof Deployer))
            return;

        Deployer deployer = (Deployer) host;

        String[] contextNames = deployer.findDeployedApps();

        for (String contextName : contextNames) {

            Context context = deployer.findDeployedApp(contextName);

            if (!(context instanceof Lifecycle))
                continue;

            try {
                DirContext resources = context.getResources();
                if (resources == null) {
                    // This can happen if there was an error initializing
                    // the context
                    continue;
                }
                ResourceAttributes webXmlAttributes =
                        (ResourceAttributes)
                                resources.getAttributes("/WEB-INF/web.xml");
                long newLastModified = webXmlAttributes.getLastModified();
                Long lastModified = (Long) webXmlLastModified.get(contextName);
                if (lastModified == null) {
                    webXmlLastModified.put
                            (contextName, new Long(newLastModified));
                } else {
                    if (lastModified.longValue() != newLastModified) {
                        webXmlLastModified.remove(contextName);
                        ((Lifecycle) context).stop();
                        // Note: If the context was already stopped, a 
                        // Lifecycle exception will be thrown, and the context
                        // won't be restarted
                        ((Lifecycle) context).start();
                    }
                }
            } catch (LifecycleException e) {
                // Ignore
            } catch (NamingException e) {
                // Ignore
            }

        }

    }


    /**
     * Expand the WAR file found at the specified URL into an unpacked
     * directory structure, and return the absolute pathname to the expanded
     * directory.
     *
     * @param war URL of the web application archive to be expanded
     *            (must start with "jar:")
     * @throws IllegalArgumentException if this is not a "jar:" URL
     * @throws IOException              if an input/output error was encountered
     *                                  during expansion
     */
    protected String expand(URL war) throws IOException {

        // Calculate the directory name of the expanded directory
        if (getDebug() >= 1) {
            log("expand(" + war.toString() + ")");
        }
        String pathname = war.toString().replace('\\', '/');
        if (pathname.endsWith("!/")) {
            pathname = pathname.substring(0, pathname.length() - 2);
        }
        int period = pathname.lastIndexOf('.');
        if (period >= pathname.length() - 4)
            pathname = pathname.substring(0, period);
        int slash = pathname.lastIndexOf('/');
        if (slash >= 0) {
            pathname = pathname.substring(slash + 1);
        }
        if (getDebug() >= 1) {
            log("  Proposed directory name: " + pathname);
        }

        // Make sure that there is no such directory already existing
        File appBase = new File(host.getAppBase());
        if (!appBase.isAbsolute()) {
            appBase = new File(System.getProperty("catalina.base"), host.getAppBase());
        }
        if (!appBase.exists() || !appBase.isDirectory()) {
            throw new IOException(sm.getString("standardHost.appBase", appBase.getAbsolutePath()));
        }
        File docBase = new File(appBase, pathname);
        if (docBase.exists()) {
            // War file is already installed
            return (docBase.getAbsolutePath());
        }

        // Create the new document base directory
        docBase.mkdir();
        if (getDebug() >= 2) {
            log("  Have created expansion directory " + docBase.getAbsolutePath());
        }

        // Expand the WAR into the new document base directory
        JarURLConnection juc = (JarURLConnection) war.openConnection();
        juc.setUseCaches(false);
        /*
        JarFile jarFile = juc.getJarFile();
        if (getDebug() >= 2) {
            log("  Have opened JAR file successfully");
        }
        Enumeration jarEntries = jarFile.entries();
        if (getDebug() >= 2) {
            log("  Have retrieved entries enumeration");
        }
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) jarEntries.nextElement();
            String name = jarEntry.getName();
            if (getDebug() >= 2) {
                log("  Am processing entry " + name);
            }
            int last = name.lastIndexOf('/');
            if (last >= 0) {
                File parent = new File(docBase,
                                       name.substring(0, last));
                if (getDebug() >= 2) {
                    log("  Creating parent directory " + parent);
                }
                parent.mkdirs();
            }
            if (name.endsWith("/")) {
                continue;
            }
            if (getDebug() >= 2) {
                log("  Creating expanded file " + name);
            }
            InputStream input = jarFile.getInputStream(jarEntry);
            expand(input, docBase, name);
            input.close();
        }
        jarFile.close();
        */
        JarFile jarFile = null;
        InputStream input = null;
        try {
            jarFile = juc.getJarFile();
            if (getDebug() >= 2) {
                log("  Have opened JAR file successfully");
            }
            Enumeration jarEntries = jarFile.entries();
            if (getDebug() >= 2) {
                log("  Have retrieved entries enumeration");
            }
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) jarEntries.nextElement();
                String name = jarEntry.getName();
                if (getDebug() >= 2) {
                    log("  Am processing entry " + name);
                }
                int last = name.lastIndexOf('/');
                if (last >= 0) {
                    File parent = new File(docBase,
                            name.substring(0, last));
                    if (getDebug() >= 2) {
                        log("  Creating parent directory " + parent);
                    }
                    parent.mkdirs();
                }
                if (name.endsWith("/")) {
                    continue;
                }
                if (getDebug() >= 2) {
                    log("  Creating expanded file " + name);
                }
                input = jarFile.getInputStream(jarEntry);
                expand(input, docBase, name);
                input.close();
                input = null;
            }
            jarFile.close();
            jarFile = null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Throwable ignored) {
                }
                input = null;
            }
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (Throwable ignored) {
                }
                jarFile = null;
            }
        }

        // Return the absolute path to our new document base directory
        return (docBase.getAbsolutePath());

    }


    /**
     * Expand the specified input stream into the specified directory, creating
     * a file named from the specified relative path.
     *
     * @param input   InputStream to be copied
     * @param docBase Document base directory into which we are expanding
     * @param name    Relative pathname of the file to be created
     * @throws IOException if an input/output error occurs
     */
    protected void expand(InputStream input, File docBase, String name)
            throws IOException {

        File file = new File(docBase, name);
        BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(file));
        byte[] buffer = new byte[2048];
        while (true) {
            int n = input.read(buffer);
            if (n <= 0)
                break;
            output.write(buffer, 0, n);
        }
        output.close();

    }


    /**
     * Log a message on the Logger associated with our Host (if any)
     *
     * @param message Message to be logged
     */
    protected void log(String message) {

        Logger logger = null;
        if (host != null)
            logger = host.getLogger();
        if (logger != null)
            logger.log("HostConfig[" + host.getName() + "]: " + message);
        else
            System.out.println("HostConfig[" + host.getName() + "]: "
                    + message);

    }


    /**
     * Log a message on the Logger associated with our Host (if any)
     *
     * @param message   Message to be logged
     * @param throwable Associated exception
     */
    protected void log(String message, Throwable throwable) {

        Logger logger = null;
        if (host != null)
            logger = host.getLogger();
        if (logger != null)
            logger.log("HostConfig[" + host.getName() + "] "
                    + message, throwable);
        else {
            System.out.println("HostConfig[" + host.getName() + "]: "
                    + message);
            System.out.println("" + throwable);
            throwable.printStackTrace(System.out);
        }

    }


    /**
     * Process a "start" event for this Host.
     */
    protected void start() {

        if (debug >= 1)
            log(sm.getString("hostConfig.start"));

        if (host.getAutoDeploy()) {
            deployApps();
        }

        if (isLiveDeploy()) {
            threadStart();          // 启动一个线程用于动态部署，定期检查新应用的部署
        }

    }


    /**
     * Process a "stop" event for this Host.
     */
    protected void stop() {

        if (debug >= 1)
            log(sm.getString("hostConfig.stop"));

        threadStop();

        undeployApps();

    }


    /**
     * Undeploy all deployed applications.
     */
    protected void undeployApps() {

        if (!(host instanceof Deployer))
            return;
        if (debug >= 1)
            log(sm.getString("hostConfig.undeploying"));

        String[] contextPaths = ((Deployer) host).findDeployedApps();
        for (int i = 0; i < contextPaths.length; i++) {
            if (debug >= 1)
                log(sm.getString("hostConfig.undeploy", contextPaths[i]));
            try {
                ((Deployer) host).remove(contextPaths[i]);
            } catch (Throwable t) {
                log(sm.getString("hostConfig.undeploy.error",
                        contextPaths[i]), t);
            }
        }

    }


    /**
     * Start the background thread that will periodically check for
     * web application autoDeploy and changes to the web.xml config.
     *
     * @throws IllegalStateException if we should not be starting
     *                               a background thread now
     */
    protected void threadStart() {

        // Has the background thread already been started?
        if (thread != null)
            return;

        // Start the background thread
        if (debug >= 1)
            log(" Starting background thread");
        threadDone = false;
        threadName = "HostConfig[" + host.getName() + "]";
        thread = new Thread(this, threadName);
        thread.setDaemon(true);
        thread.start();

    }


    /**
     * Stop the background thread that is periodically checking for
     * for web application autoDeploy and changes to the web.xml config.
     */
    protected void threadStop() {

        if (thread == null)
            return;

        if (debug >= 1)
            log(" Stopping background thread");
        threadDone = true;
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
        }

        thread = null;

    }


    /**
     * Sleep for the duration specified by the <code>checkInterval</code>
     * property.
     */
    protected void threadSleep() {

        try {
            Thread.sleep(checkInterval * 1000L);
        } catch (InterruptedException e) {
        }

    }


    // ------------------------------------------------------ Background Thread


    /**
     * The background thread that checks for web application autoDeploy
     * and changes to the web.xml config.
     */
    public void run() {

        if (debug >= 1)
            log("BACKGROUND THREAD Starting");

        // Loop until the termination semaphore is set
        while (!threadDone) {

            // Wait for our check interval
            threadSleep();

            // Deploy apps if the Host allows auto deploying
            deployApps();

            // Check for web.xml modification
            checkWebXmlLastModified();

        }

        if (debug >= 1)
            log("BACKGROUND THREAD Stopping");

    }


}
