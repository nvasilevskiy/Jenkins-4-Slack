package org.jenkinsci.plugins.jenkinsforslack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.util.Map;


public class RequestService {

    private String slackUrl = null;
    private String requestBody = null;
    private String botName = null;


    public RequestService(String slackUrl, String requestBody, String botName)
    {
        this.slackUrl = slackUrl;
        this.requestBody = requestBody;
        this.botName = botName;

    }

    public void sendPost() throws Exception {

        try {
            HttpClient c = new DefaultHttpClient();
            HttpPost p = new HttpPost(this.slackUrl);

            StringEntity query = new StringEntity(this.requestBody, ContentType.create("application/json"));

            p.setEntity(query);
            HttpResponse r = c.execute(p);

            BufferedReader rd = new BufferedReader(new InputStreamReader(r.getEntity().getContent()));
            int statusCode;

            statusCode = r.getStatusLine().getStatusCode();

            if (statusCode != 200)
            {
                throw new HttpResponseException(statusCode,"Status code it not the same as expected. Expected: 200, Actual: " + statusCode);
            }

        }

        catch(IOException e) {
            System.out.println(e);
        }
    }


}


