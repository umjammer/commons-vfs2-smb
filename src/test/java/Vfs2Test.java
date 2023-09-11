/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariables;
import vavi.util.Debug;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Vfs2Test.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-09-12 nsano initial version <br>
 */
public class Vfs2Test {

    static {
        // TODO move to pom.xml
        System.setProperty("vavi.util.logging.VaviFormatter.extraClassMethod",
                "(" +
                        "org\\.slf4j\\.impl\\.JDK14LoggerAdapter#(log|info)" +
                        "|" +
                        "sun\\.util\\.logging\\.LoggingSupport#log" +
                        "|" +
                        "sun\\.util\\.logging\\.PlatformLogger#fine" +
                        "|" +
                        "jdk\\.internal\\.event\\.EventHelper#logX509CertificateEvent" +
                        "|" +
                        "sun\\.util\\.logging\\.PlatformLogger.JavaLoggerProxy#doLog" +
                        ")");
    }

    /**
     * smbj
     */
    @Test
    @EnabledIfEnvironmentVariables({
            @EnabledIfEnvironmentVariable(named = "TEST_SMB_ACCOUNT", matches = ".+"),
            @EnabledIfEnvironmentVariable(named = "TEST_SMB_PASSWORD", matches = ".+"),
            @EnabledIfEnvironmentVariable(named = "TEST_SMB_HOST", matches = ".+"),
            @EnabledIfEnvironmentVariable(named = "TEST_SMB_DOMAIN", matches = ".+"),
            @EnabledIfEnvironmentVariable(named = "TEST_SMB_PATH", matches = ".+"),
    })
    void test_smbj() throws Exception {
        String username = System.getenv("TEST_SMB_ACCOUNT");
        String password = System.getenv("TEST_SMB_PASSWORD");
        String host = System.getenv("TEST_SMB_HOST");
        String domain = System.getenv("TEST_SMB_DOMAIN");
        String path = System.getenv("TEST_SMB_PATH");

        FileSystemOptions options = new FileSystemOptions();
        StaticUserAuthenticator auth = new StaticUserAuthenticator(domain, username, password);
Debug.println("auth: " + auth);
        DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(options, auth);

        StringBuilder sb = new StringBuilder();
        sb.append("smb");
        sb.append("://");
        if (host != null) {
            sb.append(host);
        }
        String baseUrl = sb.toString();
Debug.println(Level.FINE, "baseUrl: " + baseUrl);

        FileSystemManager manager = VFS.getManager();

        assertTrue(manager.hasProvider("smb"));

        // $ ls $remote
System.out.println("$ ls " + path);
        FileObject dir = manager.resolveFile(baseUrl + path, options);
        for (FileObject f : Arrays.stream(dir.getChildren()).collect(Collectors.toList())) {
            System.out.println(f.getURI().getPath());
        }

        // $ cp src/test/java/SmbjTest.java $remote/test.txt
        FileObject target = manager.resolveFile(baseUrl + path + "/" + "test.txt", options);
System.out.println("$ cp src/test/java/Vfs2Test.java " + target);
        target.createFile();
        OutputStream os = target.getContent().getOutputStream(0x8000);
        Files.copy(Paths.get("src/test/java/Vfs2Test.java"), os);
        os.flush();
        os.close();

        assertTrue(target.getContent().getSize() > 0);

        // $ ls $remote
System.out.println("$ ls " + path);
        dir = manager.resolveFile(baseUrl + path, options);
        for (FileObject f : Arrays.stream(dir.getChildren()).collect(Collectors.toList())) {
            System.out.println(f.getURI().getPath());
        }

        // $ cp $remote/test.txt $remote/renamed.txt
        FileObject renamed = manager.resolveFile(baseUrl + path + "/" + "renamed.txt", options);
System.out.println("$ cp " + target + " " + renamed);

        target.moveTo(renamed);

        // $ ls $remote
System.out.println("$ ls " + path);
        dir = manager.resolveFile(baseUrl + path, options);
        for (FileObject f : Arrays.stream(dir.getChildren()).collect(Collectors.toList())) {
            System.out.println(f.getURI().getPath());
        }

        assertTrue(renamed.exists());

        // $ rm $remote/renamed.txt
System.out.println("$ rm " + renamed);

        renamed.delete();

        assertFalse(renamed.exists());

        manager.close();
    }
}
