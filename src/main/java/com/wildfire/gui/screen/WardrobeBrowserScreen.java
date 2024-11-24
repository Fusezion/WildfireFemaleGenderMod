/*
    Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
    Copyright (C) 2023 WildfireRomeo

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.wildfire.gui.screen;

import com.wildfire.gui.GuiUtils;
import com.wildfire.main.Gender;
import com.wildfire.main.WildfireGender;

import java.util.*;
import java.util.stream.Collectors;

import com.wildfire.gui.WildfireButton;
import com.wildfire.main.WildfireLocalization;
import com.wildfire.main.cloud.CloudSync;
import com.wildfire.main.config.GlobalConfig;
import com.wildfire.main.entitydata.PlayerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipState;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.realms.dto.PlayerInfo;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.dedicated.gui.PlayerListGui;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Nullables;
import net.minecraft.world.GameMode;

import static com.wildfire.main.WildfireEventHandler.collectPlayerEntries;

@Environment(EnvType.CLIENT)
public class WardrobeBrowserScreen extends BaseWildfireScreen {
	private static final Identifier BACKGROUND_MALE = Identifier.of(WildfireGender.MODID, "textures/gui/wardrobe_bg_male.png");
	private static final Identifier BACKGROUND_FEMALE = Identifier.of(WildfireGender.MODID, "textures/gui/wardrobe_bg_female.png");
	private static final Identifier BACKGROUND_OTHER = Identifier.of(WildfireGender.MODID, "textures/gui/wardrobe_bg_other.png");

	private static final Identifier TXTR_RIBBON = Identifier.of(WildfireGender.MODID, "textures/bc_ribbon.png");
	private static final Identifier CLOUD_ICON = Identifier.of(WildfireGender.MODID, "textures/cloud.png");

	private static final boolean isBreastCancerAwarenessMonth = Calendar.getInstance().get(Calendar.MONTH) == Calendar.OCTOBER;

	private final TooltipState contribTooltip = new TooltipState();

	private WildfireButton btnMale, btnFemale, btnOther, btnCharacterPersonalization, btnAlwaysShowList;
	public WardrobeBrowserScreen(Screen parent, UUID uuid) {
		super(Text.translatable("wildfire_gender.wardrobe.title"), parent, uuid);
	}

	@Override
  	public void init() {
		final var client = Objects.requireNonNull(this.client);
	    int y = this.height / 2;
		PlayerConfig plr = Objects.requireNonNull(getPlayer(), "getPlayer()");


		//TODO: Finish this
		this.addDrawableChild(btnAlwaysShowList = new WildfireButton(126, 4, 140, 10,
			Text.translatable("wildfire_gender.always_show_list", GlobalConfig.INSTANCE.get(GlobalConfig.ALWAYS_SHOW_LIST) ? WildfireLocalization.ENABLED : WildfireLocalization.DISABLED),
			button -> {
				var config = GlobalConfig.INSTANCE;
				var newVal = !config.get(GlobalConfig.ALWAYS_SHOW_LIST);
				config.set(GlobalConfig.ALWAYS_SHOW_LIST, newVal);
				button.setMessage(Text.translatable("wildfire_gender.always_show_list", newVal ? WildfireLocalization.ENABLED : WildfireLocalization.DISABLED));
			}));

		this.addDrawableChild(btnFemale = new WildfireButton(this.width / 2 - 130, this.height / 2 + 33, 80, 15, plr.getGender().getDisplayName(), button -> {
			Gender gender = switch (plr.getGender()) {
				case MALE -> Gender.FEMALE;
				case FEMALE -> Gender.OTHER;
				case OTHER -> Gender.MALE;
			};
			if (plr.updateGender(gender)) {
				button.setMessage(getGenderLabel(gender));
				PlayerConfig.saveGenderInfo(plr);
				clearAndInit();
			}
		}));

		this.addDrawableChild(this.btnCharacterPersonalization = new WildfireButton(this.width / 2 - 36, this.height / 2 - 63, 158, 20, Text.translatable("wildfire_gender.appearance_settings.title").append("..."),
				button -> client.setScreen(new WildfireBreastCustomizationScreen(WardrobeBrowserScreen.this, this.playerUUID))));

		this.btnCharacterPersonalization.active = plr.getGender().canHaveBreasts();

		//old menu
		/*this.addDrawableChild(new WildfireButton(this.width / 2 - 42, y - (plr.getGender().canHaveBreasts() ? 12 : 32), 158, 20, Text.translatable("wildfire_gender.char_settings.title").append("..."),
				button -> client.setScreen(new WildfireCharacterSettingsScreen(WardrobeBrowserScreen.this, this.playerUUID))));*/

		//noinspection ExtractMethodRecommender
		var cloud = new WildfireButton(
				this.width / 2 - 36, y + 30, 24, 18, Text.translatable("wildfire_gender.cloud_settings"),
				button -> client.setScreen(new WildfireCloudSyncScreen(this, this.playerUUID))
		) {
			@Override
			protected void drawInner(DrawContext ctx, int mouseX, int mouseY, float partialTicks) {
				ctx.drawTexture(RenderLayer::getGuiTextured, CLOUD_ICON, getX() + 2, getY() + 2, 0, 0, 20, 14, 32, 26, 32, 26);
			}
		};

		if(!CloudSync.isAvailable()) {
			cloud.setTooltip(Tooltip.of(Text.translatable("wildfire_gender.cloud.unavailable_offline")));
			cloud.setActive(false);
		} else {
			cloud.setTooltip(Tooltip.of(Text.translatable("wildfire_gender.cloud.available_online")));
		}

		this.addDrawableChild(cloud);

		/*this.addDrawableChild(new WildfireButton(this.width / 2 + 111, y - 63, 9, 9, Text.literal("X"),
			button -> close(), text -> GuiUtils.doneNarrationText()));*/

	    super.init();
  	}

	private Text getGenderLabel(Gender gender) {
		return Text.translatable("wildfire_gender.label.gender").append(" - ").append(gender.getDisplayName());
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(ctx);

		PlayerConfig plr = getPlayer();
		if(plr == null) return;
		Identifier backgroundTexture = switch(plr.getGender()) {
			case Gender.MALE -> BACKGROUND_MALE;
			case Gender.FEMALE -> BACKGROUND_FEMALE;
			case Gender.OTHER -> BACKGROUND_OTHER;
		};

		ctx.drawTexture(RenderLayer::getGuiTextured, backgroundTexture, (this.width - 272) / 2, (this.height - 138) / 2, 0, 0, 268, 124, 512, 512);

		if(client != null && client.world != null) {
			int xP = this.width / 2 - 90;
			int yP = this.height / 2 + 18;
			PlayerEntity ent = client.world.getPlayerByUuid(this.playerUUID);
			if(ent != null) {
				ctx.enableScissor(xP - 38, yP - 97, xP + 38, yP + 9);
				GuiUtils.drawEntityOnScreen(ctx, xP, yP + 60, 70, (xP - mouseX), (yP - 46 - mouseY), ent);
				ctx.disableScissor();
			}
		}
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		int x = this.width / 2;
	    int y = this.height / 2;
		ctx.drawText(textRenderer, getTitle(), x - textRenderer.getWidth(getTitle()) / 2, y - 82, 0xFFFFFF, false);

		if(client != null && client.player != null) {
			boolean withCreator = client.player.networkHandler.getPlayerList().stream().anyMatch((player) -> player.getProfile().getId().equals(WildfireGender.CREATOR_UUID));

			List<PlayerListEntry> foundContributors = client.player.networkHandler.getPlayerList().stream()
					.filter(player -> WildfireGender.CONTRIBUTOR_UUIDS.contains(player.getProfile().getId()))
					.toList();

			int creatorY = y + 65;

			// move down so we don't overlap with the breast cancer awareness month banner
			if(isBreastCancerAwarenessMonth) creatorY += 30;

			if(withCreator && foundContributors.size() != 0) { //with both the creator and a contributor
				GuiUtils.drawCenteredTextWrapped(ctx, this.textRenderer, Text.translatable("wildfire_gender.label.with_both"), this.width / 2, creatorY, 250, 0xFF00FF);
				int width = textRenderer.getWidth(Text.translatable("wildfire_gender.label.with_contributor"));

				String contributors = foundContributors.stream()
						.map(entry -> entry.getProfile().getName())
						.collect(Collectors.joining(", "));
				contributors = "WildfireFGM, " + contributors;
				if(mouseX > this.width / 2 - width / 2 && mouseX < this.width / 2 + width / 2 && mouseY > creatorY - 2 && mouseY < creatorY + 9) {
					contribTooltip.setTooltip(Tooltip.of(Text.literal(contributors)));
					contribTooltip.render(true, true, ScreenRect.empty());
				}
			} else if(withCreator) { //only with the creator
				GuiUtils.drawCenteredText(ctx, this.textRenderer, Text.translatable("wildfire_gender.label.with_creator"), this.width / 2, creatorY, 0xFF00FF);
			} else if(!foundContributors.isEmpty()) { //only with a contributor
				int width = textRenderer.getWidth(Text.translatable("wildfire_gender.label.with_contributor"));
				GuiUtils.drawCenteredText(ctx, this.textRenderer, Text.translatable("wildfire_gender.label.with_contributor"), this.width / 2, creatorY, 0xFF00FF);

				String contributors = foundContributors.stream()
						.map(entry -> entry.getProfile().getName())
						.collect(Collectors.joining(", "));

				if(mouseX > this.width / 2 - width / 2 && mouseX < this.width / 2 + width / 2 && mouseY > creatorY - 2 && mouseY < creatorY + 9) {
					contribTooltip.setTooltip(Tooltip.of(Text.literal(contributors)));
					contribTooltip.render(true, true, ScreenRect.empty());
				}
			}

		}

		if(isBreastCancerAwarenessMonth) {
			int bcaY = y - 45;
			ctx.fill(x - 159, bcaY + 106, x + 159, bcaY + 136, 0x55000000);
			ctx.drawTextWithShadow(textRenderer, Text.translatable("wildfire_gender.cancer_awareness.title").formatted(Formatting.BOLD, Formatting.ITALIC), this.width / 2 - 148, bcaY + 117, 0xFFFFFF);
			ctx.drawTexture(RenderLayer::getGuiTextured, TXTR_RIBBON, x + 130, bcaY + 109, 0, 0, 26, 26, 20, 20, 20, 20);
		}

		//Render in front of the UI when it's open.
		List<PlayerListEntry> syncedPlayers = collectPlayerEntries();
		if(!syncedPlayers.isEmpty() || GlobalConfig.INSTANCE.get(GlobalConfig.ALWAYS_SHOW_LIST)) {
			ctx.drawText(textRenderer, Text.translatable("wildfire_gender.wardrobe.players_using_mod").formatted(Formatting.AQUA), 5, 5, 0xFFFFFF, false);
			int yPos = 18;
			for(PlayerListEntry entry : syncedPlayers) {
				PlayerConfig cfg = WildfireGender.getPlayerById(entry.getProfile().getId());
				ctx.drawText(textRenderer, Text.literal(entry.getProfile().getName() + " - ").append(cfg.getGender().getDisplayName()), 10, yPos, 0xFFFFFF, false);
				yPos += 10;
			}
		}
	}


}