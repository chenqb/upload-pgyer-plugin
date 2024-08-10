package ren.helloworld.upload2pgyer.helper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.model.Run;
import ren.helloworld.upload2pgyer.apiv2.ParamsBeanV2;
import ren.helloworld.upload2pgyer.apiv2.PgyerBeanV2;
import ren.helloworld.upload2pgyer.apiv2.PgyerUploadV2;
import ren.helloworld.upload2pgyer.impl.Message;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;

public class PgyerV2Helper {
    /**
     * @param run        run
     * @param listener     listener
     * @param paramsBeanV2 uploadBean
     * @return success or failure
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     */
    public static boolean upload(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, final TaskListener listener, ParamsBeanV2 paramsBeanV2) throws IOException, InterruptedException {
        Message message = new Message() {
            @Override
            public void message(boolean needTag, String message) {
                listener.getLogger().println((needTag ? CommonUtil.LOG_PREFIX : "") + message);
            }
        };

//        if (CommonUtil.isBuildFailed(run, message)) {
//            return true;
//        }
//
//        if (CommonUtil.isSkipUpload(run.getEnvironment(listener), message)) {
//            return true;
//        }

        // expand params
        paramsBeanV2.setApiKey(run.getEnvironment(listener).expand(paramsBeanV2.getApiKey()));
        paramsBeanV2.setScandir(run.getEnvironment(listener).expand(paramsBeanV2.getScandir()));
        paramsBeanV2.setWildcard(run.getEnvironment(listener).expand(paramsBeanV2.getWildcard()));
        paramsBeanV2.setBuildPassword(run.getEnvironment(listener).expand(paramsBeanV2.getBuildPassword()));
        paramsBeanV2.setBuildInstallType(run.getEnvironment(listener).expand(paramsBeanV2.getBuildInstallType()));
        paramsBeanV2.setBuildUpdateDescription(run.getEnvironment(listener).expand(paramsBeanV2.getBuildUpdateDescription()));
        paramsBeanV2.setBuildType(run.getEnvironment(listener).expand(paramsBeanV2.getBuildType()));
        paramsBeanV2.setBuildChannelShortcut(run.getEnvironment(listener).expand(paramsBeanV2.getBuildChannelShortcut()));

        // upload
        PgyerBeanV2 pgyerBeanV2 = PgyerUploadV2.upload2Pgyer(workspace, run.getEnvironment(listener), true, paramsBeanV2, message);
        if (pgyerBeanV2 == null) {
            return false;
        }

        // http://jenkins-ci.361315.n4.nabble.com/Setting-an-env-var-from-a-build-step-td4657347.html
        message.message(true, "The Jenkins environment variable is being set.");
        String data = new Gson().toJson(pgyerBeanV2.getData());
        Map<String, String> maps = new Gson().fromJson(data, new TypeToken<Map<String, String>>() {
        }.getType());
        for (Map.Entry<String, String> entry : maps.entrySet()) {
            String key = entry.getKey();
            run.addAction(new PublishEnvVarAction(key, entry.getValue()));
//            message.message(true, "The ${" + key + "} set up successfully! You can use it anywhere now!");

            if (key.equals("buildQRCodeURL")) {
                run.addAction(new PublishEnvVarAction("appQRCodeURL", entry.getValue()));
//                message.message(true, "The ${appQRCodeURL} set up successfully! You can use it anywhere now.!");
            }
        }
        message.message(true, "congratulations!\n");
        return true;
    }
}
