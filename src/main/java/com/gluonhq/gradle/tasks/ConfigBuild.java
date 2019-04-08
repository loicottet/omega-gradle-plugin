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

import com.gluonhq.gradle.OmegaExtension;
import com.gluonhq.omega.Config;
import com.gluonhq.omega.Omega;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class ConfigBuild {

    private Config omegaConfig;
    private final Project project;
    private final Provider<Directory> omegaDependencies;
    private final String target;
    private final OmegaExtension omegaExtension;

    ConfigBuild(Project project, Provider<Directory> omegaDependencies, String target) {
        this.project = project;
        this.omegaDependencies = omegaDependencies;
        this.target = target;

        omegaExtension = project.getExtensions().getByType(OmegaExtension.class);
    }

    void configOmega() {
        omegaConfig = new Config();
        omegaConfig.setDepsRoot(omegaDependencies.get().getAsFile().getAbsolutePath());
        File javafxDir = omegaExtension.getJavafxSdk();
        if (javafxDir.exists() && new File(javafxDir, "lib").exists()) {
            omegaConfig.setJavaFXRoot(javafxDir.getAbsolutePath());
            omegaConfig.setUseJavaFX(true);
        } else {
            omegaConfig.setUseJavaFX(false);
        }

        omegaConfig.setGraalVersion(omegaExtension.getGraalVersion());
        omegaConfig.setTarget(omegaExtension.getTarget());
        omegaConfig.setBackend(omegaExtension.getBackend());
        omegaConfig.setBundles(omegaExtension.getBundles());
        omegaConfig.setDelayInitList(omegaExtension.getDelayInitList());
        omegaConfig.setJniList(omegaExtension.getJniList());
        omegaConfig.setReflectionList(omegaExtension.getReflectionList());
        omegaConfig.setRuntimeArgsList(omegaExtension.getRuntimeArgsList());

        omegaConfig.setMainClassName((String) project.getProperties().get("mainClassName"));
        omegaConfig.setAppName(project.getName());
    }

    Config getOmegaConfig() {
        return omegaConfig;
    }

    void build() {

        configOmega();

        try {
            String mainClassName = omegaConfig.getMainClassName();
            String name = omegaConfig.getAppName();
            for (Configuration configuration : project.getBuildscript().getConfigurations()) {
                System.err.println("CONFIG = "+configuration);
                DependencySet deps = configuration.getAllDependencies();
                System.err.println("DEPS = "+deps);
                deps.forEach(dep -> System.err.println("DEP = "+dep));
            }
            System.err.println("mcn = "+mainClassName+" and name = "+name);
            JavaCompile compileTask = (JavaCompile) project.getTasks().findByName(JavaPlugin.COMPILE_JAVA_TASK_NAME);
            FileCollection classpath = compileTask.getClasspath();
            System.err.println("CLASSPATH = "+classpath.getFiles());
            System.err.println("JAVACP = "+System.getProperty("java.class.path"));

            List<Path> classPath = Collections.emptyList();
            SourceSetContainer sourceSetContainer = (SourceSetContainer) project.getProperties().get("sourceSets");
            SourceSet mainSourceSet = sourceSetContainer.findByName("main");
            if (mainSourceSet != null) {
                classPath = mainSourceSet.getRuntimeClasspath().getFiles().stream()
                        .filter(File::exists)
                        .map(File::toPath).collect(Collectors.toList());
            }
            System.err.println("classPath = " + classPath);

            Configuration config = project.getBuildscript().getConfigurations().getByName("classpath");
            String path = config.getFiles().stream()
                    .map(File::getAbsolutePath)
                    .filter(s -> s.contains("oracle") || s.contains("graal") || s.contains("bytedeco"))
                    .collect(Collectors.joining(File.pathSeparator));
            String cp0 = classPath.stream()
                    .map(Path::toString)
                    .collect(Collectors.joining(File.pathSeparator));

            String buildRoot = project.getLayout().getBuildDirectory().dir("omega").get().getAsFile().getAbsolutePath();
            System.err.println("BuildRoot: " + buildRoot);

            Omega.nativeCompile(buildRoot, omegaConfig, cp0 + File.pathSeparator + path, target);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
