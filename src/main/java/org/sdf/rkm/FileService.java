package org.sdf.rkm;

import com.intridea.io.vfs.provider.s3.S3FileProvider;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;

@Service
public class FileService {

    private static final Logger LOG = LoggerFactory.getLogger(FileService.class);

    private String protocol = "s3";

    @Value("${bucket}")
    private String bucket;

    @Value("${local.root.dir}")
    private String localRootDir = ".";

    @Autowired
    private FileSystemManager fsManager;

    private FileObject localRoot;
    private FileObject remoteRoot;

    public void putFile(File file) {
        FileObject remote = null;
        FileObject local = null;

        try {
            remote = remoteRoot.resolveFile(file.getName());
            local = localRoot.resolveFile(file.getAbsolutePath());

            remote.copyFrom(local, Selectors.SELECT_SELF);
        } catch (FileSystemException e) {
            throw new RuntimeException("Unable to store file at " + file.getName() + ".", e);
        } finally {
            closeFileObject(remote);
            closeFileObject(local);
        }
    }

    public void deleteFile(File file) {
        FileObject remote = null;

        try {
            remote = remoteRoot.resolveFile(file.getName());

            remote.delete();
        } catch (FileSystemException e) {
            throw new RuntimeException("Unable to store file at " + file.getName() + ".", e);
        } finally {
            closeFileObject(remote);
        }
    }

    public FileObject getFile(File file) throws FileNotFoundException {
        FileObject remote = null;
        FileObject local = null;

        try {
            remote = remoteRoot.resolveFile(file.getName());
            local = localRoot.resolveFile(file.getName());

            local.copyFrom(remote, Selectors.SELECT_SELF);
        } catch (FileSystemException e) {
            throw new RuntimeException("Unable to get file '" + file.getName() + "'.", e);
        } finally {
            closeFileObject(remote);
            closeFileObject(local);
        }

        return local;
    }

    protected String getBasePath() {
        return protocol + "://" + bucket + File.separator;
    }

    private void closeFileObject(FileObject fileObject) {
        try {
            if (fileObject != null) {
                fileObject.close();
            }
        } catch (FileSystemException fse) {
            LOG.error("Unable to close FileObject: " + fileObject.getName().getFriendlyURI(), fse);
        }
    }

    @PostConstruct
    private void init() throws FileSystemException {
        remoteRoot = fsManager.resolveFile(getBasePath(),
                S3FileProvider.getDefaultFileSystemOptions());

        localRoot = fsManager.resolveFile(localRootDir);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("Shutting down FileSystemManager");
        ((DefaultFileSystemManager) fsManager).close();
    }
}
