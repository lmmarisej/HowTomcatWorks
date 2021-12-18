/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/Session.java,v 1.5 2001/07/31 02:00:02 craigmcc Exp $
 * $Revision: 1.5 $
 * $Date: 2001/07/31 02:00:02 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
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


package org.apache.catalina;


import java.security.Principal;
import java.util.Iterator;
import javax.servlet.http.HttpSession;


/**
 * A <b>Session</b> is the Catalina-internal facade for an
 * <code>HttpSession</code> that is used to maintain state information
 * between requests for a particular user of a web application.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2001/07/31 02:00:02 $
 *
 * Session对象由javax.servlet.http.HttpSession接口表示
 */

public interface Session {      // 专门给Catalina内部外观类org.apache.catalina.session.StandardSessionFacade使用的


    // ----------------------------------------------------- Manifest Constants


    /**
     * The SessionEvent event type when a session is created.
     */
    String SESSION_CREATED_EVENT = "createSession";


    /**
     * The SessionEvent event type when a session is destroyed.
     */
    String SESSION_DESTROYED_EVENT = "destroySession";


    // ------------------------------------------------------------- Properties


    /**
     * Return the authentication type used to authenticate our cached
     * Principal, if any.
     */
    String getAuthType();


    /**
     * Set the authentication type used to authenticate our cached
     * Principal, if any.
     *
     * @param authType The new cached authentication type
     */
    void setAuthType(String authType);


    /**
     * Return the creation time for this session.
     */
    long getCreationTime();


    /**
     * Set the creation time for this session.  This method is called by the
     * Manager when an existing Session instance is reused.
     *
     * @param time The new creation time
     */
    void setCreationTime(long time);


    /**
     * Return the session identifier for this session.
     */
    String getId();


    /**
     * Set the session identifier for this session.
     *
     * @param id The new session identifier
     */
    void setId(String id);


    /**
     * Return descriptive information about this Session implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    String getInfo();


    /**
     * Return the last time the client sent a request associated with this
     * session, as the number of milliseconds since midnight, January 1, 1970
     * GMT.  Actions that your application takes, such as getting or setting
     * a value associated with the session, do not affect the access time.
     */
    long getLastAccessedTime();


    /**
     * Return the Manager within which this Session is valid.
     */
    Manager getManager();


    /**
     * Set the Manager within which this Session is valid.
     *
     * @param manager The new Manager
     */
    void setManager(Manager manager);


    /**
     * Return the maximum time interval, in seconds, between client requests
     * before the servlet container will invalidate the session.  A negative
     * time indicates that the session should never time out.
     */
    int getMaxInactiveInterval();


    /**
     * Set the maximum time interval, in seconds, between client requests
     * before the servlet container will invalidate the session.  A negative
     * time indicates that the session should never time out.
     *
     * @param interval The new maximum interval
     */
    void setMaxInactiveInterval(int interval);


    /**
     * Set the <code>isNew</code> flag for this session.
     *
     * @param isNew The new value for the <code>isNew</code> flag
     */
    void setNew(boolean isNew);


    /**
     * Return the authenticated Principal that is associated with this Session.
     * This provides an <code>Authenticator</code> with a means to cache a
     * previously authenticated Principal, and avoid potentially expensive
     * <code>Realm.authenticate()</code> calls on every request.  If there
     * is no current associated Principal, return <code>null</code>.
     */
    Principal getPrincipal();


    /**
     * Set the authenticated Principal that is associated with this Session.
     * This provides an <code>Authenticator</code> with a means to cache a
     * previously authenticated Principal, and avoid potentially expensive
     * <code>Realm.authenticate()</code> calls on every request.
     *
     * @param principal The new Principal, or <code>null</code> if none
     */
    void setPrincipal(Principal principal);


    /**
     * Return the <code>HttpSession</code> for which this object
     * is the facade.
     */
    HttpSession getSession();    // 获取被外观类修饰的Session对象


    /**
     * Set the <code>isValid</code> flag for this session.
     *
     * @param isValid The new value for the <code>isValid</code> flag
     */
    void setValid(boolean isValid);


    /**
     * Return the <code>isValid</code> flag for this session.
     */
    boolean isValid();


    // --------------------------------------------------------- Public Methods


    /**
     * Update the accessed time information for this session.  This method
     * should be called by the context when a request comes in for a particular
     * session, even if the application does not reference it.
     */
    void access();       // 每次session被访问，都会调用以对session的最后访问时间修改


    /**
     * Add a session event listener to this component.
     */
    void addSessionListener(SessionListener listener);


    /**
     * Perform the internal processing required to invalidate this session,
     * without triggering an exception if the session has already expired.
     */
    void expire();


    /**
     * Return the object bound with the specified name to the internal notes
     * for this session, or <code>null</code> if no such binding exists.
     *
     * @param name Name of the note to be returned
     */
    Object getNote(String name);


    /**
     * Return an Iterator containing the String names of all notes bindings
     * that exist for this session.
     */
    Iterator getNoteNames();


    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    void recycle();


    /**
     * Remove any object bound to the specified name in the internal notes
     * for this session.
     *
     * @param name Name of the note to be removed
     */
    void removeNote(String name);


    /**
     * Remove a session event listener from this component.
     */
    void removeSessionListener(SessionListener listener);


    /**
     * Bind an object to a specified name in the internal notes associated
     * with this session, replacing any existing binding for this name.
     *
     * @param name  Name to which the object should be bound
     * @param value Object to be bound to the specified name
     */
    void setNote(String name, Object value);


}
