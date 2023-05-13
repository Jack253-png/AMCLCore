package com.mcreater.amclcore.model.game.arguments;

import com.google.gson.annotations.SerializedName;
import com.mcreater.amclcore.model.game.rule.GameRuleModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GameArgumentsModel {
    @SerializedName("game")
    private List<GameArgumentsItem> gameArguments;
    @SerializedName("jvm")
    private List<GameArgumentsItem> jvmArguments;

    @Data
    @Builder
    public static class GameArgumentsItem {
        private List<GameRuleModel> rules;
        private List<String> value;

        public boolean valid() {
            if (rules == null) return true;
            else {
                final boolean[] isValid = {false};
                rules.forEach(gameRuleModel -> isValid[0] = isValid[0] || gameRuleModel.valid());
                return isValid[0];
            }
        }
    }
}
