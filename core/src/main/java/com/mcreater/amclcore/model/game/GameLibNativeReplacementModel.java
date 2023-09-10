package com.mcreater.amclcore.model.game;

import com.mcreater.amclcore.model.game.lib.GameDependedLibModel;
import com.mcreater.amclcore.util.maven.MavenLibName;
import lombok.EqualsAndHashCode;

import java.util.HashMap;

@EqualsAndHashCode(callSuper = true)
public class GameLibNativeReplacementModel extends HashMap<String, HashMap<MavenLibName, GameDependedLibModel>> {

}
