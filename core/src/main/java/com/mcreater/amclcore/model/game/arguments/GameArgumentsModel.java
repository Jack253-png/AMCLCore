package com.mcreater.amclcore.model.game.arguments;

import com.google.gson.annotations.SerializedName;
import com.mcreater.amclcore.model.game.rule.GameRuleFeatureModel;
import com.mcreater.amclcore.model.game.rule.GameRuleModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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

        public boolean valid(GameRuleFeatureModel data) {
            if (rules == null) return true;
            else {
                AtomicBoolean isValid = new AtomicBoolean(false);
                rules.forEach(gameRuleModel -> {
                    if (gameRuleModel.valid(data)) isValid.set(gameRuleModel.getAction() == GameRuleModel.Action.ALLOW);
                });
                return isValid.get();
            }
        }

        public boolean valid() {
            return valid(null);
        }
    }
}
