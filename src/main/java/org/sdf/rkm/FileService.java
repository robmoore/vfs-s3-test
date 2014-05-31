package org.sdf.rkm;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.File;

@Service
public class FileService {

    private static final Logger LOG = LoggerFactory.getLogger(FileService.class);

    private String protocol = "s3";

    @Value("${bucket}")
    private String bucket;

    @Autowired
    private FileSystemManager fsManager;

    public void putFile(File file) {
        String remotePath = getRemotePath(file);

        FileObject remote = null;
        FileObject local = null;

        try {
            remote = fsManager.resolveFile(remotePath);
            local = fsManager.resolveFile(file.getAbsolutePath());

            remote.copyFrom(local, Selectors.SELECT_SELF);
        } catch (FileSystemException e) {
            throw new RuntimeException("Unable to store file at " + remotePath + ".", e);
        } finally {
            closeFileObject(remote);
            closeFileObject(local);
        }
    }

    public void deleteFile(File file) {
        String remotePath = getRemotePath(file);

        FileObject remote = null;

        try {
            remote = fsManager.resolveFile(remotePath);

            remote.delete();
        } catch (FileSystemException e) {
            throw new RuntimeException("Unable to store file at " + remotePath + ".", e);
        } finally {
            closeFileObject(remote);
        }
    }

    public void getFile(File file) throws FileNotFoundException {
        String remotePath = getRemotePath(file);

        FileObject remote = null;
        FileObject local = null;

        try {
            remote = fsManager.resolveFile(remotePath);
            local = fsManager.resolveFile(file.getAbsolutePath());

            local.copyFrom(remote, Selectors.SELECT_SELF);
        } catch (FileSystemException e) {
            throw new RuntimeException("Unable to get file '" + remotePath + "'.", e);
        } finally {
            closeFileObject(remote);
            closeFileObject(local);
        }
    }

    private String getRemotePath(File file) {
        return getBasePath() + file.getName();
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

    @PreDestroy
    public void destroy() {
        System.out.println("Shutting down FileSystemManager");
        ((DefaultFileSystemManager) fsManager).close();
    }
}
