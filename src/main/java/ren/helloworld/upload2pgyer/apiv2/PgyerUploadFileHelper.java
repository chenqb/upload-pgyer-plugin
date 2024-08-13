package ren.helloworld.upload2pgyer.apiv2;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import okhttp3.*;
import org.jenkinsci.remoting.RoleChecker;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class PgyerUploadFileHelper implements FilePath.FileCallable<Long>, Serializable {

    private static final long serialVersionUID = 1L;
    private final String key;
    private final String signature;
    private final String x_cos_security_token;
    private final String end_point;

    public PgyerUploadFileHelper(String key, String signature, String x_cos_security_token, String end_point){

        this.key = key;
        this.signature = signature;
        this.x_cos_security_token = x_cos_security_token;
        this.end_point = end_point;

    }
    @Override
    public Long invoke(File uploadFile, VirtualChannel channel) throws IOException, InterruptedException {
        try {

            MediaType type = MediaType.parse("application/octet-stream");
            RequestBody fileBody = RequestBody.create(type, uploadFile);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("key", key)
                    .addFormDataPart("signature", signature)
                    .addFormDataPart("x-cos-security-token", x_cos_security_token)
                    .addFormDataPart("file", uploadFile.getName(), fileBody)
                    .build();
            Request request = new Request.Builder()
                    .url(end_point)
                    .post(requestBody)
                    .build();
            int timeout = 300; // seconds
            Response execute = new OkHttpClient().newBuilder()
                    .retryOnConnectionFailure(true)
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .writeTimeout(timeout, TimeUnit.SECONDS)
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .build()
                    .newCall(request).execute();

            if (execute.body() == null) {
                return null;
            }
            if(execute.code() == 204){
                return 1L;
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void checkRoles(RoleChecker checker) throws SecurityException {

    }
}
