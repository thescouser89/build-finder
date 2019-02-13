/*
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.red.build.finder.pnc.client.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildRecord {
    private Integer id;

    private Date submitTime;

    private Date startTime;

    private Date endTime;

    private Integer buildConfigurationId;

    private String buildConfigurationName;

    private Integer buildConfigurationRev;

    private Integer projectId;

    private String projectName;

    private Integer userId;

    private String username;

    private String scmRepoURL;

    private String scmRevision;

    private Integer buildEnvironmentId;

    private Map<String, String> attributes = new HashMap<>();

    private String liveLogsUri;

    private Integer buildConfigSetRecordId;

    private String buildContentId;

    private Boolean temporaryBuild;

    /**
     * The IDs of the build record sets which represent the builds performed for a milestone to which this build record belongs
     */
    private Integer productMilestoneId;

    private String executionRootName;

    private String executionRootVersion;

    private Integer[] dependentBuildRecordIds;

    private Integer[] dependencyBuildRecordIds;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getBuildConfigurationId() {
        return buildConfigurationId;
    }

    public void setBuildConfigurationId(Integer buildConfigurationId) {
        this.buildConfigurationId = buildConfigurationId;
    }

    public String getBuildConfigurationName() {
        return buildConfigurationName;
    }

    public void setBuildConfigurationName(String buildConfigurationName) {
        this.buildConfigurationName = buildConfigurationName;
    }

    public Integer getBuildConfigurationRev() {
        return buildConfigurationRev;
    }

    public void setBuildConfigurationRev(Integer buildConfigurationRev) {
        this.buildConfigurationRev = buildConfigurationRev;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getScmRepoURL() {
        return scmRepoURL;
    }

    public void setScmRepoURL(String scmRepoURL) {
        this.scmRepoURL = scmRepoURL;
    }

    public String getScmRevision() {
        return scmRevision;
    }

    public void setScmRevision(String scmRevision) {
        this.scmRevision = scmRevision;
    }

    public Integer getBuildEnvironmentId() {
        return buildEnvironmentId;
    }

    public void setBuildEnvironmentId(Integer buildEnvironmentId) {
        this.buildEnvironmentId = buildEnvironmentId;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getLiveLogsUri() {
        return liveLogsUri;
    }

    public void setLiveLogsUri(String liveLogsUri) {
        this.liveLogsUri = liveLogsUri;
    }

    public Integer getBuildConfigSetRecordId() {
        return buildConfigSetRecordId;
    }

    public void setBuildConfigSetRecordId(Integer buildConfigSetRecordId) {
        this.buildConfigSetRecordId = buildConfigSetRecordId;
    }

    public String getBuildContentId() {
        return buildContentId;
    }

    public void setBuildContentId(String buildContentId) {
        this.buildContentId = buildContentId;
    }

    public Boolean getTemporaryBuild() {
        return temporaryBuild;
    }

    public void setTemporaryBuild(Boolean temporaryBuild) {
        this.temporaryBuild = temporaryBuild;
    }

    public Integer getProductMilestoneId() {
        return productMilestoneId;
    }

    public void setProductMilestoneId(Integer productMilestoneId) {
        this.productMilestoneId = productMilestoneId;
    }

    public String getExecutionRootName() {
        return executionRootName;
    }

    public void setExecutionRootName(String executionRootName) {
        this.executionRootName = executionRootName;
    }

    public String getExecutionRootVersion() {
        return executionRootVersion;
    }

    public void setExecutionRootVersion(String executionRootVersion) {
        this.executionRootVersion = executionRootVersion;
    }

    public Integer[] getDependentBuildRecordIds() {
        return dependentBuildRecordIds;
    }

    public void setDependentBuildRecordIds(Integer[] dependentBuildRecordIds) {
        this.dependentBuildRecordIds = dependentBuildRecordIds;
    }

    public Integer[] getDependencyBuildRecordIds() {
        return dependencyBuildRecordIds;
    }

    public void setDependencyBuildRecordIds(Integer[] dependencyBuildRecordIds) {
        this.dependencyBuildRecordIds = dependencyBuildRecordIds;
    }
}
