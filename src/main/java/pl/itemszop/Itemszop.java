package pl.itemszop;

import net.elytrium.java.commons.mc.serialization.Serializer;
import net.elytrium.java.commons.mc.serialization.Serializers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;
import pl.itemszop.commands.itemszop_cmd;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class Itemszop extends JavaPlugin {
    WebSocket ws;
    Itemszop plugin;
    private static Serializer serializer;
    String key = Settings.IMP.KEY;
    String firebaseWebsocketUrl = Settings.IMP.FIREBASEWEBSOCKETURL;
    String serverId = Settings.IMP.SERVERID;
    String secret = Settings.IMP.SECRET;

    @Override
    public void onEnable() {
        registerCommands();
        plugin = this;
        Settings.IMP.reload(new File(this.getDataFolder(), "config.yml"), Settings.IMP.PREFIX);
        // decode config key
        byte[] decoded = Base64.getDecoder().decode(key);
        String decodedStr = new String(decoded, StandardCharsets.UTF_8);
        String[] stringList = decodedStr.split("@");
        secret = stringList[4];
        firebaseWebsocketUrl = stringList[1];
        serverId = stringList[2];


        // cut websocket url param
        int index = firebaseWebsocketUrl.indexOf("&s=");
        if(index != -1) {
            String[] urlList = firebaseWebsocketUrl.split("&");
            firebaseWebsocketUrl = urlList[4] + "&" + urlList[2];
            getLogger().info(firebaseWebsocketUrl);
        }
        // Startup message
        getLogger().info(this.getName() + this.getDescription().getVersion() + " by " + this.getDescription().getAuthors() + "\n Plugin do sklepu serwera - https://github.com/michaljaz/itemszop-plugin");

        ComponentSerializer<Component, Component, String> serializer = Serializers.valueOf(Settings.IMP.SERIALIZER).getSerializer();
        if (serializer == null) {
            this.getLogger().info("The specified serializer could not be founded, using default. (LEGACY_AMPERSAND)");
            setSerializer(new Serializer(Objects.requireNonNull(Serializers.LEGACY_AMPERSAND.getSerializer())));
        } else {
            setSerializer(new Serializer(serializer));
        }


        //connect to firebase
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        try {
                            ws = new WebSocket(plugin, new URI(firebaseWebsocketUrl));
                            ws.connect();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                },
                3000
        );
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        ws.close();
    }

    private void registerCommands() {
        new itemszop_cmd().register(getCommand("itemszop"));
    }

    public void reloadPlugin() {
        Settings.IMP.reload(new File(this.getDataFolder().toPath().toFile().getAbsoluteFile(), "config.yml"));
    }
    private static void setSerializer(Serializer serializer) {
        Itemszop.serializer = serializer;
    }
    public static Serializer getSerializer() {
        return serializer;
    }

}
