package org.jenkinsci.plugins.jenkinsforslack;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
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
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;
import hudson.util.FormValidation;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.regex.*;


public class Jenkins4Slack extends Notifier {

    private String botname = null;
    private String channelname = null;
    private String request = null;

    @DataBoundConstructor
    public Jenkins4Slack(String botname, String channelname, String[] request)
    {
        super();
        this.botname = botname;
        this.channelname = channelname;

        this.request = request[1];

        //obj = new JSONObject(request);
        //String requestresult = obj.get("request").toString();
        //this.request = requestresult;



    }

    public String getBotname() {
        return botname;
    }
    public String getChannelname() {
        return channelname;
    }
    public String getRequest() {
        return request;
    }



    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        final Result result = build.getResult();
        listener.getLogger().println("Run result is " + result);

        listener.getLogger().println("The bot name is " + botname);
        listener.getLogger().println("The channel name is " + channelname);
        listener.getLogger().println("The request is " + request);
        listener.getLogger().println("The request is " + getDescriptor().getSlackurl());

        return true;
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

        public String getSlackurl() {
            return slackurl;
        }

        public String getBotname() {
            return botname;
        }

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

            req.bindParameters(this);
            slackurl = formData.getString("slackurl");
            botname = formData.getString("botname");
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


    }

}
