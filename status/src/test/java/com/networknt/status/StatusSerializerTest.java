package com.networknt.status;

import com.networknt.config.Config;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatusSerializerTest {

    static Config config = null;
    static final String homeDir = System.getProperty("user.home");

    @BeforeClass
    public static void setUp() throws Exception {
        config = Config.getInstance();

        // write a config file into the user home directory.
        List<String> implementationList = new ArrayList<>();
        implementationList.add("com.networknt.status.ErrorRootStatusSerializer");
        Map<String, List<String>> implementationMap = new HashMap<>();
        implementationMap.put("com.networknt.status.StatusSerializer", implementationList);
        List<Map<String, List<String>>> interfaceList = new ArrayList<>();
        interfaceList.add(implementationMap);
        Map<String, Object> singletons = new HashMap<>();
        singletons.put("singletons", interfaceList);
        config.getMapper().writeValue(new File(homeDir + "/service.json"), singletons);

        // Add home directory to the classpath of the system class loader.
        addURL(new File(homeDir).toURI().toURL());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        // Remove the test.json from home directory
        File test = new File(homeDir + "/service.json");
        test.delete();
    }

    public void testGetConfigFromClassPath() throws Exception {
        Map<String, Object> configMap = config.getJsonMapConfig("service");
        Assert.assertNotNull(configMap.get("singletons"));
    }

    public static void addURL(URL url) throws Exception {
        URLClassLoader classLoader
                = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class clazz= URLClassLoader.class;

        // Use reflection
        Method method= clazz.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(classLoader, url);
    }

    @Test
    public void testToString() {
        Status status = new Status("ERR10001");
        System.out.println(status);
        // test with ErrorStatusRootStatusSerializer
        Assert.assertEquals("{ \"error\" : {\"statusCode\":401,\"code\":\"ERR10001\",\"message\":\"AUTH_TOKEN_EXPIRED\",\"description\":\"Jwt token in authorization header expired\"} }", status.toString());
    }

    @Test
    public void testToStringWithArgs() {
        Status status = new Status("ERR11000", "parameter name", "original url");
        System.out.println(status);
        // test with ErrorStatusRootStatusSerializer
        Assert.assertEquals("{ \"error\" : {\"statusCode\":400,\"code\":\"ERR11000\",\"message\":\"VALIDATOR_REQUEST_PARAMETER_QUERY_MISSING\",\"description\":\"Query parameter parameter name is required on path original url but not found in request.\"} }", status.toString());
    }

}
