package com.example.demo;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TomcatStats {

	@JsonIgnore
	private MBeanServer mBeanServer;
    private static final String JMX_DOMAIN_EMBEDDED = "Tomcat";
    @JsonIgnore
    private static final String JMX_DOMAIN_STANDALONE = "Catalina";
    @JsonIgnore
    private static final String OBJECT_NAME_SERVER_SUFFIX = ":type=Server";
    @JsonIgnore
    private static final String OBJECT_NAME_SERVER_EMBEDDED = JMX_DOMAIN_EMBEDDED + OBJECT_NAME_SERVER_SUFFIX;
    @JsonIgnore
    private static final String OBJECT_NAME_SERVER_STANDALONE = JMX_DOMAIN_STANDALONE + OBJECT_NAME_SERVER_SUFFIX;
    @JsonIgnore
    private volatile String jmxDomain;

    @JsonProperty
    private int maxThreads;
    @JsonProperty
    private int busyThreadCount;
    @JsonProperty
    private int currentThreadCount;
    
    @JsonProperty
    private int errorCount;
    @JsonProperty
    private int requestCount;
    @JsonProperty
    private long cumulativeProcessingTime;
    @JsonProperty
    private long longestRequestProcessingTime;
    
    
    @JsonCreator
    public TomcatStats()        {
        mBeanServer = getMBeanServer();
        
        try {
            Set<ObjectName> objectNames = mBeanServer.queryNames(new ObjectName(getJmxDomain() + ":" + "type" + "=" + "ThreadPool" + ",name=*"), null);
            for (ObjectName objectName : objectNames) {
            		if ( (objectName.getCanonicalName().equals("Catalina:name=\"http-nio-8080\",type=ThreadPool")) ||
            				(objectName.getCanonicalName().contains("https-openssl-nio-8443"))
            			)
            		
            		{
            			maxThreads =  ((Integer) mBeanServer.getAttribute(objectName, "maxThreads")).intValue();
            			busyThreadCount = ((Integer) mBeanServer.getAttribute(objectName, "currentThreadsBusy")).intValue();
            			currentThreadCount = ((Integer) mBeanServer.getAttribute(objectName, "currentThreadCount")).intValue();
            			
            		}
            }
            
            Set<ObjectName> objectNames2 = mBeanServer.queryNames(new ObjectName(getJmxDomain() + ":" + "type" + "=" + "GlobalRequestProcessor" + ",name=*"), null);

            for (ObjectName objectName : objectNames2) {
            	//System.out.println(objectName.getCanonicalName());
            	if ( (objectName.getCanonicalName().equals("Catalina:name=\"http-nio-8080\",type=GlobalRequestProcessor")) ||
            			(objectName.getCanonicalName().contains("https-openssl-nio-8443"))
            	   )
            	{
            		errorCount = ((Integer) mBeanServer.getAttribute(objectName, "errorCount")).intValue();
            		requestCount = ((Integer) mBeanServer.getAttribute(objectName, "requestCount")).intValue();
            		cumulativeProcessingTime = ((Long) mBeanServer.getAttribute(objectName, "processingTime")).longValue();
            		longestRequestProcessingTime = ((Long) mBeanServer.getAttribute(objectName, "maxTime")).longValue();
            	}
            }
            
            
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static MBeanServer getMBeanServer() {
        List<MBeanServer> mBeanServers = MBeanServerFactory.findMBeanServer(null);
        if (!mBeanServers.isEmpty()) {
            return mBeanServers.get(0);
        }
        return ManagementFactory.getPlatformMBeanServer();
    }

    private String getJmxDomain() {
        if (this.jmxDomain == null) {
            if (hasObjectName(OBJECT_NAME_SERVER_EMBEDDED)) {
                this.jmxDomain = JMX_DOMAIN_EMBEDDED;
            } else if (hasObjectName(OBJECT_NAME_SERVER_STANDALONE)) {
                this.jmxDomain = JMX_DOMAIN_STANDALONE;
            }
        }
        return this.jmxDomain;
    }

    private boolean hasObjectName(String name) {
        try {
            return this.mBeanServer.queryNames(new ObjectName(name), null).size() == 1;
        } catch (MalformedObjectNameException ex) {
            throw new RuntimeException(ex);
        }
    }
}
