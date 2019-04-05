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

import com.gluonhq.gradle.OmegaPlugin;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.artifacts.PublishArtifactSet;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.util.Set;

public class OmegaDependencies extends DefaultTask {

    private String graalVersion;
    private Provider<Directory> outputDirectory;

    @Input
    public String getGraalVersion() {
        return graalVersion;
    }

    public void setGraalVersion(String graalVersion) {
        this.graalVersion = graalVersion;
    }

    @OutputDirectory
    public Provider<Directory> getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(Provider<Directory> outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @TaskAction
    public void action() {
        Configuration omegaConfiguration = getProject().getConfigurations().findByName(OmegaPlugin.CONFIGURATION_OMEGA);
        if (omegaConfiguration != null) {
            Set<ResolvedArtifact> artifacts = omegaConfiguration.getResolvedConfiguration().getResolvedArtifacts();
            omegaConfiguration.getFiles()
                    .forEach(f -> {
                        ResolvedArtifact artifact = artifacts.stream()
                                .filter(a -> a.getFile().getAbsolutePath().equals(f.getAbsolutePath()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Artifact not found for " + f.getAbsolutePath()));
                        getProject().copy(c -> {
                            getProject().getLogger().info("Copying omega dependency " + f.getName() + " to " + this.outputDirectory);
                            c.from(f);
                            c.into(getOutputDirectory());
                            c.rename(s -> {
                                if (artifact.getName().equals("llvm")) {
                                    if (artifact.getClassifier() != null) {
                                        return artifact.getName() + "-platform-specific.jar";
                                    } else {
                                        return artifact.getName() + "-wrapper.jar";
                                    }
                                }
                                return artifact.getName() + ".jar";
                            });
                        });
                    });

            omegaConfiguration.getFiles().stream()
                    .filter(f -> f.getName().endsWith(".zip"))
                    .forEach(f -> getProject().copy(c -> {
                        getProject().getLogger().info("Unzipping omega dependency " + f.getName() + " to " + this.outputDirectory);
                        c.from(getProject().zipTree(getProject().file(f)));
                        c.into(getOutputDirectory());
                    }));
        }
    }
}
