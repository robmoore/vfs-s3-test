package org.sdf.rkm;

import net._01001111.text.LoremIpsum;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;

public class FileApp {
    private static final LoremIpsum LOREM_IPSUM = new LoremIpsum();

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");

        FileService fs = context.getBean(FileService.class);

        ThreadHelper th = new ThreadHelper();

        try {
            for (int i = 0; i < 50; i++) {
                File f = File.createTempFile("vfs-s3-test-", null);
                f.deleteOnExit();

                Writer w = new BufferedWriter(new FileWriter(f));
                w.write(LOREM_IPSUM.paragraphs(10));
                w.close();

                fs.putFile(f);

                fs.deleteFile(f);

                Thread[] threads = th.getThreadsByName("s3-transfer-manager-worker-1");

                System.out.println("Thread count: " + threads.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        context.close();
    }


}
