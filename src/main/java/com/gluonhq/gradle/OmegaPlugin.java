/*
 * Copyright (c) 2018, Gluon
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

import com.gluonhq.gradle.tasks.*;
import com.gluonhq.gradle.tasks.OmegaNativeCompile;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Locale;

public class OmegaPlugin implements Plugin<Project> {

    public static final String CONFIGURATION_OMEGA = "omega";

    @Override
    public void apply(Project project) {
        project.getConfigurations().create(CONFIGURATION_OMEGA);

        OmegaExtension omega = project.getExtensions().create("omega", OmegaExtension.class, project);

        project.afterEvaluate(a -> {
            OmegaDependencies omegaDependencies = project.getTasks().create("omegaDependencies", OmegaDependencies.class);
            omegaDependencies.setGraalVersion(omega.getGraalVersion());
            omegaDependencies.setOutputDirectory(project.getLayout().getBuildDirectory().dir("omega/deps/" + omega.getGraalVersion()));

            OmegaNativeCompile omegaNativeCompile = project.getTasks().create("nativeCompile", OmegaNativeCompile.class);
            omegaNativeCompile.setOmegaDependencies(omegaDependencies.getOutputDirectory());
            omegaNativeCompile.setJavafxSdkDirectory(omega.getJavafxSdk());
            omegaNativeCompile.setTarget(omega.getTarget());
            omegaNativeCompile.dependsOn(omegaDependencies, project.getTasks().findByName("classes"), project.getTasks().findByName("processResources"));

            OmegaNativeLink omegaNativeLink = project.getTasks().create("nativeLink", OmegaNativeLink.class);
            omegaNativeLink.setOmegaDependencies(omegaDependencies.getOutputDirectory());
            omegaNativeLink.setJavafxSdkDirectory(omega.getJavafxSdk());
            omegaNativeLink.setTarget(omega.getTarget());
            omegaNativeLink.dependsOn(omegaDependencies, project.getTasks().findByName("classes"), project.getTasks().findByName("processResources"));

            OmegaNativeRun omegaNativeRun = project.getTasks().create("nativeRun", OmegaNativeRun.class);
            omegaNativeRun.setOmegaDependencies(omegaDependencies.getOutputDirectory());
            omegaNativeRun.setJavafxSdkDirectory(omega.getJavafxSdk());
            omegaNativeRun.setTarget(omega.getTarget());
            omegaNativeRun.dependsOn(omegaDependencies, project.getTasks().findByName("classes"), project.getTasks().findByName("processResources"));

            String osname = System.getProperty("os.name");
            System.err.println("Omega-Plugin, osname = "+osname);
            String hostedNative = "";
            if (osname.toLowerCase(Locale.ROOT).contains("linux")) {
                hostedNative = "svm-hosted-native-linux-amd64";
            } else if (osname.toLowerCase(Locale.ROOT).contains("mac")) {
                hostedNative = "svm-hosted-native-darwin-amd64";
            }

            project.getDependencies().add(CONFIGURATION_OMEGA, "com.oracle.substratevm:" + hostedNative + ":" + omega.getGraalVersion());
            project.getDependencies().add(CONFIGURATION_OMEGA, "com.oracle.substratevm:library-support:" + omega.getGraalVersion());
            project.getDependencies().add(CONFIGURATION_OMEGA, "com.oracle.substratevm:objectfile:" + omega.getGraalVersion());
            project.getDependencies().add(CONFIGURATION_OMEGA, "com.oracle.substratevm:pointsto:" + omega.getGraalVersion());
            project.getDependencies().add(CONFIGURATION_OMEGA, "com.oracle.substratevm:svm:" + omega.getGraalVersion());
            project.getDependencies().add(CONFIGURATION_OMEGA, "org.graalvm.sdk:graal-sdk:" + omega.getGraalVersion());
            project.getDependencies().add(CONFIGURATION_OMEGA, "org.graalvm.compiler:compiler:" + omega.getGraalVersion());
            project.getDependencies().add(CONFIGURATION_OMEGA, "org.graalvm.truffle:truffle-api:" + omega.getGraalVersion());

            // LLVM
            if (omega.getBackend().equals("llvm")) {
                project.getDependencies().add(CONFIGURATION_OMEGA, "org.graalvm.compiler:graal-llvm:" + omega.getGraalVersion());
                project.getDependencies().add(CONFIGURATION_OMEGA, "com.oracle.substratevm:svm-llvm:" + omega.getGraalVersion());
                project.getDependencies().add(CONFIGURATION_OMEGA, "org.bytedeco.javacpp-presets:llvm:6.0.1-1.4.2");
                if (osname.toLowerCase(Locale.ROOT).contains("mac")) {
                    project.getDependencies().add(CONFIGURATION_OMEGA, "org.bytedeco.javacpp-presets:llvm:6.0.1-1.4.2:macosx-x86_64");
                } else if (osname.toLowerCase(Locale.ROOT).contains("linux")) {
                    project.getDependencies().add(CONFIGURATION_OMEGA, "org.bytedeco.javacpp-presets:llvm:6.0.1-1.4.2:linux-x86_64");

                }
                project.getDependencies().add(CONFIGURATION_OMEGA, "org.bytedeco:javacpp:1.4.2");
            }

            System.err.println("Applied omega plugin, tasks = " + project.getAllTasks(true));
        });
    }
}
