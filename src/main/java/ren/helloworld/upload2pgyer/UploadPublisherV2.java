package ren.helloworld.upload2pgyer;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import ren.helloworld.upload2pgyer.apiv2.ParamsBeanV2;
import ren.helloworld.upload2pgyer.helper.PgyerV2Helper;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * upload to jenkins
 *
 * @author myroid
 */
public class UploadPublisherV2 extends Publisher implements SimpleBuildStep {

    private final Secret apiKey;
    private final String scanDir;
    private final String wildcard;
    private final String buildInstallType;
    private final Secret buildPassword;
    private final String buildUpdateDescription;
    private final String buildType;
    private final String buildChannelShortcut;


    @DataBoundConstructor
    public UploadPublisherV2(String apiKey, String scanDir, String wildcard, String buildType, String buildInstallType, String buildPassword, String buildUpdateDescription, String buildChannelShortcut) {
        this.apiKey = Secret.fromString(apiKey);
        this.scanDir = scanDir;
        this.wildcard = wildcard;
        this.buildType = buildType;
        this.buildPassword = Secret.fromString(buildPassword);
        this.buildInstallType = buildInstallType;
        this.buildUpdateDescription = buildUpdateDescription;
        this.buildChannelShortcut = buildChannelShortcut;
    }

    public String getApiKey() {
        return apiKey.getPlainText();
    }

    public String getScanDir() {
        return scanDir;
    }

    public String getWildcard() {
        return wildcard;
    }

    public String getBuildInstallType() {
        return buildInstallType;
    }

    public String getBuildPassword() {
        return buildPassword.getPlainText();
    }

    public String getBuildUpdateDescription() {
        return buildUpdateDescription;
    }

    public String getBuildType() {
        return buildType;
    }

    public String getBuildChannelShortcut() {
        return buildChannelShortcut;
    }


    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher,
                        @Nonnull TaskListener listener) throws InterruptedException, IOException {
        ParamsBeanV2 paramsBeanV2 = new ParamsBeanV2();
        paramsBeanV2.setApiKey(apiKey.getPlainText());
        paramsBeanV2.setScandir(scanDir);
        paramsBeanV2.setWildcard(wildcard);
        paramsBeanV2.setBuildPassword(buildPassword.getPlainText());
        paramsBeanV2.setBuildInstallType(buildInstallType);
        paramsBeanV2.setBuildType(buildType);
        paramsBeanV2.setBuildUpdateDescription(buildUpdateDescription);
        paramsBeanV2.setBuildChannelShortcut(buildChannelShortcut);
        boolean result = PgyerV2Helper.upload(run, workspace, listener, paramsBeanV2);
        if(!result){
            throw new RuntimeException("UPLOAD TO PGYER  FAILED !!!");
        }


    }

//    @Override
//    public DescriptorImpl getDescriptor() {
//        return (DescriptorImpl) super.getDescriptor();
//    }

    @Symbol("uploadPgyV2")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private String installType = "1";

        public DescriptorImpl() {
            load();
        }

        public FormValidation doCheckApiKey(@QueryParameter String value) {
            return ValidationParameters.doCheckApiKey(value);
        }

        public FormValidation doCheckScanDir(@QueryParameter String value) {
            return ValidationParameters.doCheckScanDir(value);
        }

        public FormValidation doCheckWildcard(@QueryParameter String value) {
            return ValidationParameters.doCheckWildcard(value);
        }

        public FormValidation doCheckBuildType(@QueryParameter String value) {
            return ValidationParameters.doCheckBuildType(value);
        }

        public FormValidation doCheckBuildInstallType(@QueryParameter String value) {
            installType = value;
            return ValidationParameters.doCheckInstallType(value);
        }

        public FormValidation doCheckBuildPassword(@QueryParameter String value) {
            return ValidationParameters.doCheckPassword(installType, value);
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Upload to pgyer with apiV2 publish";
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
}

