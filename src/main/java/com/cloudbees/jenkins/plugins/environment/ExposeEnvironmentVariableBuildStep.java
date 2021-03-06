package com.cloudbees.jenkins.plugins.environment;
import com.cloudbees.jenkins.plugins.environment.producers.EnvironmentProducer;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import hudson.Launcher;
import hudson.Extension;
import hudson.model.Environment;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.model.EnvironmentList;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Add environment variables to the build as a build step. Such variables will only be available to BuildSteps /
 * Publishers to run after this build step.
 *
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class ExposeEnvironmentVariableBuildStep extends Builder {

    private final Collection<EnvironmentProducer> producers;

    @DataBoundConstructor
    public ExposeEnvironmentVariableBuildStep(Collection<EnvironmentProducer> producers) {
        this.producers = producers;
    }

    public Collection<EnvironmentProducer> getProducers() {
        return producers;
    }

    @Override
    public boolean perform(final AbstractBuild build, Launcher launcher, final BuildListener listener) {

        for (EnvironmentProducer producer : producers) {
            try {
                build.getEnvironments().add(producer.buildEnvironmentFor(build, listener));
            } catch (IOException e) {
                listener.error("Failed to prepare environment : {}", e);
                e.printStackTrace(listener.getLogger());
                return false;
            } catch (InterruptedException e) {
                listener.error("Failed to prepare environment : {}", e);
                e.printStackTrace(listener.getLogger());
                return false;
            }
        }
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Expose environment variables";
        }

    }
}

