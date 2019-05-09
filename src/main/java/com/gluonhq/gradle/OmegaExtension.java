/*
 * Copyright (c) 2019, Gluon
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.gradle;

import org.gradle.api.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OmegaExtension {

    private static final String DEFAULT_GRAAL_VERSION = "1.0.0-rc18-SNAPSHOT";
    public static final String DEFAULT_TARGET = "host";
    public static final String DEFAULT_BACKEND = "llvm";

    private String graalVersion;
    private String target;
    private String backend;
    private final List<String> bundles;
    private final List<String> reflectionList;
    private final List<String> jniList;
    private final List<String> delayInitList;
    private final List<String> runtimeArgsList;
    private File javafxSdk;
    private File graalSdk;

    private final List<File> graalDeps;

    public OmegaExtension(Project project) {
        this.graalVersion = DEFAULT_GRAAL_VERSION;
        this.target = DEFAULT_TARGET;
        this.backend = DEFAULT_BACKEND;
        this.bundles = new ArrayList<>();
        this.reflectionList = new ArrayList<>();
        this.jniList = new ArrayList<>();
        this.delayInitList = new ArrayList<>();
        this.runtimeArgsList = new ArrayList<>();
        this.javafxSdk = project.getRootDir();
        this.graalSdk = project.getRootDir();

        this.graalDeps = new ArrayList<>();
    }

    public String getGraalVersion() {
        return graalVersion;
    }

    public void setGraalVersion(String graalVersion) {
        this.graalVersion = graalVersion;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    public File getJavafxSdk() {
        return javafxSdk;
    }

    public void setJavafxSdk(File javafxSdk) {
        this.javafxSdk = javafxSdk;
    }

    public File getGraalSdk() {
        return graalSdk;
    }

    public void setGraalSdk(File graalSdk) {
        this.graalSdk = graalSdk;
    }

    public List<String> getBundles() {
        return bundles;
    }

    public void setBundles(List<String> bundles) {
        this.bundles.clear();
        this.bundles.addAll(bundles);
    }

    public List<String> getReflectionList() {
        return reflectionList;
    }

    public void setReflectionList(List<String> reflectionList) {
        this.reflectionList.clear();
        this.reflectionList.addAll(reflectionList);
    }

    public void setJniList(List<String> jniList) {
        this.jniList.clear();
        this.jniList.addAll(jniList);
    }

    public List<String> getJniList() {
        return jniList;
    }


    public List<String> getDelayInitList() {
        return delayInitList;
    }

    public void setDelayInitList(List<String> delayInitList) {
        this.delayInitList.clear();
        this.delayInitList.addAll(delayInitList);
    }

    public List<String> getRuntimeArgsList() {
        return runtimeArgsList;
    }

    public void setRuntimeArgsList(List<String> runtimeArgsList) {
        this.runtimeArgsList.clear();
        this.runtimeArgsList.addAll(runtimeArgsList);
    }

    public List<File> getGraalDeps() {
        return graalDeps;
    }

    public void setGraalDeps(List<File> graalDeps) {
        this.graalDeps.clear();
        this.graalDeps.addAll(graalDeps);
    }
}
