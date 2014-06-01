package org.sdf.rkm;


import junit.framework.Assert;
import net._01001111.text.LoremIpsum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring-config.xml")
public class FileServiceTests {
    @Autowired
    private FileService fileService;

    @Test
    public void testRepeatedUploads() throws IOException {
        final LoremIpsum loremIpsum = new LoremIpsum();
        final ThreadHelper threadHelper = new ThreadHelper();
        // Number of files to upload
        final int fileLimit = 5;

        int threadCount = 0;
        for (int i = 0; i < fileLimit; i++) {
            File f = File.createTempFile("vfs-s3-test-", null);
            f.deleteOnExit();

            Writer w = new BufferedWriter(new FileWriter(f));
            w.write(loremIpsum.paragraphs(10));
            w.close();

            fileService.putFile(f);

            fileService.getFile(f).delete();

            fileService.deleteFile(f);

            // Only determine thread count last time through
            if (i + 1 == fileLimit) {
                Thread[] threads = threadHelper.getThreadsByName("s3-transfer-manager-worker-1");
                threadCount = threads.length;

                System.out.println("Thread count: " + threadCount);
            }
        }

        Assert.assertFalse("Number of threads shouldn't be the same as the number of files.", threadCount == fileLimit);
    }
}