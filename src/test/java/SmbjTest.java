/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import org.junit.jupiter.api.Test;
import vavi.util.Debug;

import static com.hierynomus.msdtyp.AccessMask.DELETE;
import static com.hierynomus.msdtyp.AccessMask.FILE_WRITE_DATA;
import static com.hierynomus.msfscc.FileAttributes.FILE_ATTRIBUTE_NORMAL;
import static com.hierynomus.mssmb2.SMB2CreateDisposition.FILE_CREATE;
import static com.hierynomus.mssmb2.SMB2CreateDisposition.FILE_OPEN;
import static com.hierynomus.mssmb2.SMB2CreateDisposition.FILE_OVERWRITE;
import static com.hierynomus.mssmb2.SMB2CreateOptions.FILE_NON_DIRECTORY_FILE;
import static com.hierynomus.mssmb2.SMB2ShareAccess.FILE_SHARE_READ;
import static java.util.EnumSet.of;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * SmbjTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-09-11 nsano initial version <br>
 */
public class SmbjTest {

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
     * - works with smbj c9ab3d8
     * - according to cyberduck it works with smbj 0.12.2 https://github.com/hierynomus/smbj/issues/584#issuecomment-1694759650
     */
    @Test
    void test_smbj() throws Exception {
        String username = System.getenv("TEST_SMB_ACCOUNT");
        String password = System.getenv("TEST_SMB_PASSWORD");
        String host = System.getenv("TEST_SMB_HOST");
        String domain = System.getenv("TEST_SMB_DOMAIN");
        String path = System.getenv("TEST_SMB_PATH");

        try (SMBClient client = new SMBClient();
             Connection connection = client.connect(host)) {

            AuthenticationContext ac = new AuthenticationContext(username, password.toCharArray(), domain);
            Session session = connection.authenticate(ac);

            // Connect to Share
            String[] pe = path.split("/");
Debug.println(Arrays.toString(pe));
            try (DiskShare share = (DiskShare) session.connectShare(pe[1])) {
                // $ ls $remote
                for (FileIdBothDirectoryInformation f : share.list(pe[2] + "/" + pe[3])) {
                    System.out.println("File : " + f.getFileName());
                }

                // $ cp src/test/java/SmbjTest.java $remote/test.txt
                String target = pe[2] + "/" + pe[3] + "/" + "test.txt";
                boolean exists = share.fileExists(new SmbPath(share.getSmbPath(), target).getPath());
                File file = share.openFile(target, of(FILE_WRITE_DATA), of(FILE_ATTRIBUTE_NORMAL), of(FILE_SHARE_READ), exists ? FILE_OVERWRITE : FILE_CREATE, of(FILE_NON_DIRECTORY_FILE));
                OutputStream os = file.getOutputStream(false);
                Files.copy(Paths.get("src/test/java/SmbjTest.java"), os);
                os.flush();
                os.close();

                FileAllInformation fai = share.getFileInformation(target);
Debug.println("size: " + fai.getStandardInformation().getEndOfFile());
                assertTrue(fai.getStandardInformation().getEndOfFile() > 0);

                file.close();

                // $ ls $remote
                for (FileIdBothDirectoryInformation f : share.list(pe[2] + "/" + pe[3])) {
                    System.out.println("File : " + f.getFileName());
                }

                // $ cp $remote/test.txt $remote/renamed.txt
                file = share.openFile(target, of(DELETE), of(FILE_ATTRIBUTE_NORMAL), of(FILE_SHARE_READ), FILE_OPEN, of(FILE_NON_DIRECTORY_FILE));

                String renamed = pe[2] + "/" + pe[3] + "/" + "renamed.txt";
Debug.println("rename to: " + new SmbPath(share.getSmbPath(), renamed).getPath());
                file.rename(new SmbPath(share.getSmbPath(), renamed).getPath(), true);

                file.close();

                // $ ls $remote
                for (FileIdBothDirectoryInformation f : share.list(pe[2] + "/" + pe[3])) {
                    System.out.println("File : " + f.getFileName());
                }

                assertTrue(share.fileExists(renamed));

                // $ rm $remote/renamed.txt

                share.rm(renamed);

                assertFalse(share.fileExists(renamed));
            }
        }
    }
}
