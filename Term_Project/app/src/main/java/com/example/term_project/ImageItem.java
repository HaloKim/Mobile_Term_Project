package com.example.term_project;

import java.io.Serializable;
import java.util.List;

public class ImageItem {
    String path;
    List<String> tags;
    public ImageItem(String _path, List<String> _tags) {
        this.path = _path;
        this.tags = _tags;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
