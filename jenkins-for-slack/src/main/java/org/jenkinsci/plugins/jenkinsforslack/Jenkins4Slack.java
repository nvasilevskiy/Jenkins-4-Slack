package org.jenkinsci.plugins.jenkinsforslack;

//region Import
import org.jenkinsci.plugins.jenkinsforslack.*;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.EnvVars;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.model.BuildListener;
import jenkins.tasks.SimpleBuildStep;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;
import hudson.util.FormValidation;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.regex.*;
//endregion


public class Jenkins4Slack extends Notifier {

    //region Veriables
    private String slackurl = null;
    private String botname = null;
    private String channelname = null;
    private String condition = null;

    private String requestpass = null;
    private Boolean requestp = false;

    private String requestfail = null;
    private Boolean requestf = false;

    private String requestabort = null;
    private Boolean requesta = false;

    private EnvVars env;
    private Result result;
    //endregion

    @DataBoundConstructor
    public Jenkins4Slack(String slackurl, String botname, String channelname, String condition, String requestpass, Boolean requestp, String requestfail, Boolean requestf, String requestabort, Boolean requesta) {

        super();
        //region Constructor variables
        this.slackurl = slackurl;
        this.botname = botname;
        this.channelname = channelname;
        this.condition = condition;

        this.requestpass = requestpass;
        this.requestp = requestp;

        this.requestfail = requestfail;
        this.requestf = requestf;

        this.requestabort = requestabort;
        this.requesta = requesta;
        //endregion

    }

    //region Getters
    public String getSlackurl() {
        return slackurl;
    }

    public String getBotname() {
        return botname;
    }

    public String getChannelname() {
        return channelname;
    }

    public String getCondition() {
        return condition;
    }

    public String getRequestpass() {
        return requestpass;
    }

    public Boolean getRequestp() {
        return requestp;
    }

    public String getRequestfail() {
        return requestfail;
    }

    public Boolean getRequestf() {
        return requestf;
    }

    public String getRequestabort() {
        return requestabort;
    }

    public Boolean getRequesta() {
        return requesta;
    }
    //endregion


    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {

        this.env = build.getEnvironment(listener);
        this.result = build.getResult();

        if ((this.condition != null) || (this.condition != ""))
        {
            String buildLog = build.getLog();
            if (buildLog.contains(this.condition))
            {
                SendAction();
            }
            else
            {
                return true;
            }
        }

        else
        {
            SendAction();
        }

        return true;
    }

    private void SendAction()
    {

        //region SUCCESS

        if ((result == Result.SUCCESS) && (this.requestp))  {

            RequestService slackRequest = new RequestService(this.slackurl, createJson(this.requestpass, ":white_check_mark:", "good"), this.botname);

        try
        {
            slackRequest.sendPost();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
        //endregion

        //region FAILED
        if ((result == Result.FAILURE) && (this.requestf)){

            RequestService slackRequest = new RequestService(this.slackurl, createJson(this.requestfail, ":exclamation:", "danger"), this.botname);

            try
            {
                slackRequest.sendPost();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        //endregion

        //region ABORTED
        if ((result == Result.ABORTED) && (this.requesta)) {

            RequestService slackRequest = new RequestService(slackurl, createJson(this.requestabort, ":exclamation:", "warning"), botname);

            try
            {
                slackRequest.sendPost();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        //endregion

    }

    public String createJson(String request, String emoji, String color)
    {
        String jsonRequest = null;
        jsonRequest = this.env.expand(request);
        JSONObject passedJson = new JSONObject();
        passedJson.put("username", this.botname);
        passedJson.put("channel", this.channelname);
        passedJson.put("icon_emoji", emoji);
        JSONObject attachmentsJson = new JSONObject();
        attachmentsJson.put("color", color);
        attachmentsJson.put("text", jsonRequest);
        JSONArray array = new JSONArray();
        array.add(attachmentsJson);
        passedJson.put("attachments", array);
        return passedJson.toString();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private String slackurl;
        private String botname;

        public String getSlackurl() { return slackurl; }

        public String getBotname() { return botname; }

        public DescriptorImpl() {
            slackurl = getSlackurl();
            botname = getBotname();
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Jenkins 4 Slack";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

            //req.bindParameters(this);
            //slackurl = formData.getString("slackurl");
            //botname = formData.getString("botname");
            save();
            return super.configure(req, formData);
        }

        // Validator for BotName field
        public FormValidation doCheckBotname(@QueryParameter String value) {
            if (!value.isEmpty())
                return FormValidation.ok();
            else
                return FormValidation.error("Bot name should have a default name!");
        }

        // Validator for Slackurl field
        public FormValidation doCheckSlackurl(@QueryParameter String value) {
            Pattern p = Pattern.compile("^(https:\\/\\/)([a-zA-Z0-9]+).[a-zA-Z0-9]*.[a-z]{3}.?([a-z]+).[a-zA-Z0-9]{9}.[a-zA-Z0-9]{9}.[a-zA-Z0-9]{24}");
            Matcher m = p.matcher(value);
            boolean matches = m.matches();

            if (matches)
                return FormValidation.ok();
            else
                return FormValidation.error("You should specify valid Slack URL in format: https://hooks.slack.com/services/XXXXXXXXX/XXXXXXXXX/XXXXXXXXXXXXXXXXXXXXXXXX");
        }

        // Validator for Slackurl field
        public FormValidation doCheckChannelname(@QueryParameter String value) {
            Pattern p = Pattern.compile("[#][a-zA-Z0-9_]+");
            Matcher m = p.matcher(value);
            boolean matches = m.matches();

            if (matches)
                return FormValidation.ok();
            else
                return FormValidation.error("Please enter channel name in valid format! For example: #deploy");
        }

    }

}
