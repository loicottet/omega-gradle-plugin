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
package com.gluonhq.gradle.tasks;

import com.gluonhq.omega.Omega;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.nio.file.Path;

public class OmegaNativeLink extends DefaultTask {

    private File javafxSdkDirectory;
    private Provider<Directory> omegaDependencies;
    private String target;

    @InputDirectory
    public File getJavaFxSdkDirectory() {
        return javafxSdkDirectory;
    }

    public void setJavafxSdkDirectory(File javafxSdkDirectory) {
        this.javafxSdkDirectory = javafxSdkDirectory;
    }

    @InputDirectory
    public Provider<Directory> getOmegaDependencies() {
        return omegaDependencies;
    }

    public void setOmegaDependencies(Provider<Directory> omegaDependencies) {
        this.omegaDependencies = omegaDependencies;
    }

    @Input
    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @TaskAction
    public void action() {
        System.err.println("OmegaNativeLink action");

        ConfigBuild configBuild = new ConfigBuild(getProject(), getOmegaDependencies(), getTarget());
        configBuild.configOmega();

        try {
            File omega = getProject().getLayout().getBuildDirectory().dir("omega").get().getAsFile();
            Path tmpPath = omega.toPath().resolve("gvm").resolve("tmp");
            System.err.println("start linking in " + tmpPath.toString());

            Omega.nativeLink(omega.getAbsolutePath(), tmpPath, configBuild.getOmegaConfig(), getTarget());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
