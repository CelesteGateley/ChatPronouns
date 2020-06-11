package xyz.fluxinc.chatpronouns;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class PronounSet implements ConfigurationSerializable {

    public String miniatureString;
    public String hoverText;

    public PronounSet(Map<String, Object> serializedSet) {
        this.miniatureString = (String) serializedSet.get("minVersion");
        this.hoverText = (String) serializedSet.get("hoverText");

    }

    public PronounSet(String miniatureString, String hoverText) {
        this.miniatureString = miniatureString;
        this.hoverText = hoverText;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> mapSerializer = new HashMap<>();

        mapSerializer.put("minVersion", miniatureString);
        mapSerializer.put("hoverText", hoverText);

        return mapSerializer;
    }
}
