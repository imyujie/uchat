package com.sysu.bigmans.uchat;

import java.io.File;

/**
 * Created by liyujie on 16/1/5.
 */
public class FileBubble extends ChatBubble {
    File file;
    String fileType;

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFileName() {
        return file.getName();
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
