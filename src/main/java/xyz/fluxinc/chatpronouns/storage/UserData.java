package xyz.fluxinc.chatpronouns.storage;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class UserData implements ConfigurationSerializable {

    public boolean doNotPrompt;
    public PronounSet pronouns;

    public UserData(boolean doNotPrompt) {
        this.doNotPrompt = doNotPrompt;
    }

    public UserData(PronounSet pronouns) {
        this.doNotPrompt = true;
        this.pronouns = pronouns;
    }

    public UserData(Map<String, Object> data) {
        if (data.size() == 0) {
            this.doNotPrompt = false;
            return;
        }
        if (data.containsKey("pronouns")) pronouns = (PronounSet) data.get("pronouns");
        if (data.containsKey("prompt")) doNotPrompt = (boolean) data.get("prompt");

    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        if (!doNotPrompt && pronouns == null) {
            return map;
        }
        if (pronouns != null) {
            map.put("pronouns", pronouns);
        }
        map.put("prompt", doNotPrompt);
        return map;
    }
}
