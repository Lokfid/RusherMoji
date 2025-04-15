package org.lokfid;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.lokfid.emoji.Emoji;
import org.rusherhack.client.api.plugin.Plugin;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Emoji rusherhack plugin
 *
 * @author Lokfid
 */
public class EmojiPlugin extends Plugin {

	public static HashMap<String, Emoji> EMOJIS = new HashMap<>();
	public static EmojiPlugin INSTANCE;
	private static final File dir = new File(Minecraft.getInstance().gameDirectory, "emojis");
	private static final File LOCAL_VERSION = new File(dir + File.separator + "version.json");
	private static final String VERSION_URL = "https://raw.githubusercontent.com/Lokfid/emojis/master/version.json";
	private static final String ZIP_URL = "https://github.com/Lokfid/emojis/archive/master.zip";
	
	@Override
	public void onLoad() {
		INSTANCE = this;
		EMOJIS.clear();

		this.getLogger().info("Loading Emoji plugin");

		CheckForUpdates();

		LoadEmojis();

		this.getLogger().info("Emoji Plugin Loaded!");
		
	}
	
	@Override
	public void onUnload() {
		this.getLogger().info("Emoji unloaded!");
	}

	private void LoadEmojis(){
		for (File file : Objects.requireNonNull((dir).listFiles())) {
			if (file.getName().endsWith(".png")) {
				String filename = file.getName();

				//if it's shorter than 4 it doesn't contain .png or its name is ".png"
				if (filename.length() < 4) return;

				//makes something like :sob: or :100:
				String idPath = filename.substring(0, filename.length() - 4).toLowerCase();
				String emojiName = ":" + idPath + ":";

				try {
					NativeImage image = NativeImage.read(new FileInputStream(file));
					DynamicTexture dynamicTexture = new DynamicTexture(image);

					//Add it as a ResourceLocation for easier rendering
					ResourceLocation id = new ResourceLocation("emoji", idPath);
					Minecraft.getInstance().getTextureManager().register(id, dynamicTexture);
					EMOJIS.put(emojiName, new Emoji(id));
				} catch (IOException e) {

				}
			}
		}
	}
	// https://github.com/2b2t-Utilities/emoji-api/blob/master/src/main/java/dev/tigr/emojiapi/Emojis.java#L39
	private void CheckForUpdates(){
		if (!dir.exists()) dir.mkdir();

		try {
			if(!LOCAL_VERSION.exists()) update_emojis();
			else {
				// load version info
				JsonObject globalVer = read(new URL(VERSION_URL).openStream());
				JsonObject localVer = read(new FileInputStream(LOCAL_VERSION));

				// make sure current version is latest
				if(!globalVer.has("version")) update_emojis();
				else {
					if(globalVer.get("version").getAsInt() != localVer.get("version").getAsInt()) update_emojis();
				}
			}
		} catch(Exception ignored) {  }
	}

	private static JsonObject read(InputStream stream) {
		Gson gson = new Gson();
		JsonObject jsonObject = null;

		try {
			String json = IOUtils.toString(stream, Charsets.UTF_8);
			jsonObject = gson.fromJson(json, JsonObject.class);
		} catch(IOException e) {
			e.printStackTrace();
		}

		return jsonObject;
	}

	private void update_emojis() throws IOException {
		ZipInputStream zip = new ZipInputStream(new URL(ZIP_URL).openStream());
		ZipEntry entry = zip.getNextEntry();
		// iterates over entries in the zip file
		while(entry != null) {
			String filePath = dir + File.separator + entry.getName().substring(entry.getName().indexOf("/"));
			if(!entry.isDirectory()) {
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
				byte[] bytesIn = new byte[4096];
				int read;
				while((read = zip.read(bytesIn)) != -1) {
					bos.write(bytesIn, 0, read);
				}
				bos.close();
			}
			zip.closeEntry();
			entry = zip.getNextEntry();
		}
		zip.close();
	}
	
}
