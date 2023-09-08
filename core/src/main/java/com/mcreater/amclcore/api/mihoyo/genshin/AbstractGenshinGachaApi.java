package com.mcreater.amclcore.api.mihoyo.genshin;

import com.mcreater.amclcore.api.mihoyo.genshin.cn.GenshinGachaApi;
import com.mcreater.amclcore.api.mihoyo.genshin.model.GenshinGachaModel;
import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.i18n.I18NManager;
import com.mcreater.amclcore.i18n.Text;
import com.mcreater.amclcore.util.HttpClientWrapper;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;

public interface AbstractGenshinGachaApi {
    String getGenshinGachaUrl();

    default GenshinGachaFetchTask genshinGachaFetchAsync() throws URISyntaxException, IOException {
        return genshinGachaFetchAsync(Locale.getDefault());
    }

    default GenshinGachaFetchTask genshinGachaFetchAsync(Locale locale) throws URISyntaxException, IOException {
        return new GenshinGachaFetchTask(
                getGenshinGachaUrl(),
                locale
        );
    }

    @AllArgsConstructor
    class GenshinGachaFetchTask extends AbstractTask<GenshinGachaModel> {
        private String url;
        private Locale locale;

        protected GenshinGachaModel call() throws Exception {
            System.out.println(locale.toString());
            return HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                    .timeout(5000)
                    .reqTimeout(5000)
                    .uri(url)
                    .uriParam("lang", locale.toString().toLowerCase().replace("_", "-"))
                    .sendAndReadJson(GenshinGachaModel.class);
        }

        protected Text getTaskName() {
            return I18NManager.fixed("");
        }
    }

    static AbstractGenshinGachaApi getCnInstance() {
        return new GenshinGachaApi();
    }
}
