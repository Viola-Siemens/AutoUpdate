package com.hexagram2021.autoupdate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;

public class HttpHelper {
    private boolean status = false;
    public HttpHelper(ConfigHelper config) {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest get = HttpRequest.newBuilder(URI.create(ConfigHelper.URL + "setup.js")).build();
        HttpResponse<String> response;
        try {
            response = client.send(get, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200) {
                AutoUpdate.err("Failed to request. " + response.body());
                return;
            }
            String message = response.body();
            JsonObject setup = (JsonObject) JsonParser.parseString(message);

            List<ConfigHelper.Mod> toDoList = config.solve(setup.getAsJsonArray("mods"));
            for(ConfigHelper.Mod mod: toDoList) {
                this.download(client, config, mod.name());
            }
            this.status = true;
        } catch (InterruptedException | IOException e) {
            AutoUpdate.err("Cannot get server setup.");
            AutoUpdate.err(e.toString());
        }
    }

    private String replaceSpecial(String name) {
        return name.replaceAll("%", "%25")
                .replaceAll(" ", "%20")
                .replaceAll("\"", "%22")
                .replaceAll("#", "%23")
                .replaceAll("&", "%26")
                .replaceAll("'", "%27")
                .replaceAll("\\+", "%2B")
                .replaceAll("<", "%3C")
                .replaceAll("=", "%3D")
                .replaceAll(">", "%3E")
                .replaceAll("\\?", "%3F");
    }

    private void download(HttpClient client, ConfigHelper config, String name) {
        HttpRequest get = HttpRequest.newBuilder(URI.create(ConfigHelper.URL + "mods/" + replaceSpecial(name))).build();
        HttpResponse<Path> response;
        try {
            response = client.send(get, HttpResponse.BodyHandlers.ofFile(Path.of(config.targetPath + "/" + name)));
            AutoUpdate.log("Successfully updated " + response);
        } catch (InterruptedException | IOException e) {
            AutoUpdate.err("Cannot download \"" + name + "\".");
            AutoUpdate.err(e.toString());
        }
    }

    public boolean getStatus() {
        return this.status;
    }
}
