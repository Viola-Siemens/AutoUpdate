package com.hexagram2021.autoupdate;

import com.google.gson.*;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConfigHelper {
    public final File targetPath = new File("./mods/");

    public final File filePath = new File("./");

    public record Mod(String name, int size, String sha1) {
    }

    private List<Mod> modList;

    private JsonObject config = null;

    public static String URL = "https://localhost:8080/";

    public ConfigHelper(boolean isRead) {
        if(!this.targetPath.exists()) {
            AutoUpdate.err("Cannot get mods path.");
            return;
        }
        if (!this.filePath.exists() && !this.filePath.mkdir()) {
            AutoUpdate.err("Could not mkdir " + this.filePath);
        } else {
            this.makeFile();
            if(!isRead) {
                this.writeFile();
            }
        }
    }

    private void makeFile() {
        File[] flist = this.targetPath.listFiles();
        if (flist == null || flist.length == 0) {
            return;
        }
        this.modList = new ArrayList<>();
        for(File f: flist) {
            this.modList.add(this.getMod(f));
        }
        this.config = new JsonObject();
        this.config.addProperty("URL", URL);
        JsonArray array = new JsonArray();
        for(Mod m: this.modList) {
            if(m != null) {
                JsonObject modObj = new JsonObject();
                modObj.addProperty("name", m.name());
                modObj.addProperty("size", m.size());
                modObj.addProperty("sha1", m.sha1());
                modObj.add("previous", new JsonArray());
                array.add(modObj);
            }
        }
        this.config.add("mods", array);
    }

    public void writeFile() {
        try {
            File file = new File(filePath + "/setup.js");
            if (!file.exists() && !file.createNewFile()) {
                AutoUpdate.err("Could not create new file " + file);
            } else {
                FileOutputStream out = new FileOutputStream(file);
                Writer writer = new OutputStreamWriter(out);
                writeJsonToFile(writer, null, this.config, 0);
                writer.close();
            }
        } catch (IOException e) {
            AutoUpdate.err("Cannot write config.");
            AutoUpdate.err(e.toString());
        }
    }

    public static void writeJsonToFile(Writer writer, String key, JsonElement json, int tab) throws IOException {
        writer.write("\t".repeat(tab));
        if(key != null) {
            writer.write("\"" + key + "\": ");
        }
        if(json.isJsonObject()) {
            writer.write("{\n");
            boolean first = true;
            for(Map.Entry<String, JsonElement> entry: json.getAsJsonObject().entrySet()) {
                if(first) {
                    first = false;
                } else {
                    writer.write(",\n");
                }
                writeJsonToFile(writer, entry.getKey(), entry.getValue(), tab + 1);
            }
            writer.write("\n" + "\t".repeat(tab) + "}");
        } else if(json.isJsonArray()) {
            writer.write("[\n");
            boolean first = true;
            for (JsonElement element : json.getAsJsonArray()) {
                if (first) {
                    first = false;
                } else {
                    writer.write(",\n");
                }
                writeJsonToFile(writer, null, element, tab + 1);
            }
            writer.write("\n" + "\t".repeat(tab) + "]");
        } else if(json.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
            if(jsonPrimitive.isBoolean()) {
                writer.write(String.valueOf(jsonPrimitive.getAsBoolean()));
            } else if(jsonPrimitive.isNumber()) {
                writer.write(String.valueOf(jsonPrimitive.getAsNumber().intValue()));
            } else if(jsonPrimitive.isString()) {
                writer.write('\"' + jsonPrimitive.getAsString() + '\"');
            }
        }
    }

    public static String ToHex(byte[] bytes) {
        final char[] HEX = {
                '0', '1', '2', '3',
                '4', '5', '6', '7',
                '8', '9', 'a', 'b',
                'c', 'd', 'e', 'f'
        };
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            builder.append(HEX[(aByte >> 4) & 0xf]);
            builder.append(HEX[aByte & 0xf]);
        }
        return builder.toString();
    }

    private Mod getMod(File inputFile) {
        try {
            FileInputStream input = new FileInputStream(inputFile);
            try {
                MessageDigest ret = MessageDigest.getInstance("SHA1");
                byte[] buffer = input.readAllBytes();
                ret.update(buffer, 0, buffer.length);
                return new Mod(inputFile.getName(), buffer.length, ToHex(ret.digest()));
            } catch(IOException e) {
                AutoUpdate.err("Cannot read mod file \"" + inputFile.getName() + "\".");
                AutoUpdate.err(e.toString());
            } catch (NoSuchAlgorithmException e) {
                AutoUpdate.err("Security Error.");
                AutoUpdate.err(e.toString());
            }
        } catch (FileNotFoundException e) {
            AutoUpdate.err("Cannot open mod file \"" + inputFile.getName() + "\".");
            AutoUpdate.err(e.toString());
        }
        return null;
    }

    public boolean getStatus() {
        return this.config != null;
    }

    public List<Mod> solve(JsonArray setupMods) {
        List<Mod> toDo = new ArrayList<>();
        for(JsonElement e: setupMods) {
            if(e instanceof JsonObject m) {
                Mod mod = new Mod(m.get("name").getAsString(), m.get("size").getAsInt(), m.get("sha1").getAsString());
                JsonArray previous = m.get("previous").getAsJsonArray();

                boolean flag = false;
                for(Mod cmp: this.modList) {
                    if(cmp.name().equals(mod.name())) {
                        if(cmp.size() != mod.size() || !Objects.equals(cmp.sha1(), mod.sha1())) {
                            AutoUpdate.log("Found mod \"" + cmp.name() + "\" with size " + cmp.size() + " and sha1 " + cmp.sha1() + ".");
                            toDo.add(mod);
                        }
                        flag = true;
                        break;
                    }
                    for(JsonElement p: previous) {
                        if(p instanceof JsonObject prev) {
                            Mod pmod = new Mod(prev.get("name").getAsString(), prev.get("size").getAsInt(), prev.get("sha1").getAsString());
                            if(cmp.name().equals(pmod.name())) {
                                File toDelete = new File(this.targetPath + "/" + cmp.name());
                                if(!toDelete.delete()) {
                                    AutoUpdate.log("Failed to delete \"" + cmp.name() + "\".");
                                }

                                AutoUpdate.log("Found mod \"" + cmp.name() + "\" that with old version.");
                                toDo.add(mod);

                                flag = true;
                                break;
                            }
                        }
                    }
                }
                if(!flag) {
                    AutoUpdate.log("Found necessary mod \"" + mod.name() + "\" that not downloaded.");
                    toDo.add(mod);
                }
            }
        }

        return toDo;
    }
}
