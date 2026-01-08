package com.sangfor.common;

import java.util.List;

public class GroupResponse {

    private String id;

    private String name;

    private String description;

    private String parentGroupId;

    private Integer hasChild;

    private String path;

    private List<GroupResponse> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParentGroupId() {
        return parentGroupId;
    }

    public void setParentGroupId(String parentGroupId) {
        this.parentGroupId = parentGroupId;
    }

    public Integer getHasChild() {
        return hasChild;
    }

    public void setHasChild(Integer hasChild) {
        this.hasChild = hasChild;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<GroupResponse> getChildren() {
        return children;
    }

    public void setChildren(List<GroupResponse> children) {
        this.children = children;
    }
}
