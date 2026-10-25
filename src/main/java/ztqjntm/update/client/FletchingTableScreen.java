package ztqjntm.update.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import ztqjntm.update.menu.FletchingTableMenu;

public class FletchingTableScreen extends AbstractContainerScreen<FletchingTableMenu> {
	// TODO 临时占位：先借用原版工作台的背景图，后续换成自己画的正式 UI。
	// 工作台的 3x3 网格在 (30, 17)，所以槽位坐标要和 CraftingMenu 保持一致（见 FletchingTableMenu）。
	// 右侧 (124, 35) 的输出格只是背景上画出来的，本模组还没有结果槽。
	private static final Identifier CONTAINER_TEXTURE =
			Identifier.withDefaultNamespace("textures/gui/container/crafting_table.png");

	public FletchingTableScreen(FletchingTableMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	protected void init() {
		super.init();
		// 标题居中
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_TEXTURE,
				x, y, 0.0F, 0.0F,
				this.imageWidth, this.imageHeight,
				BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);
		this.renderTooltip(graphics, mouseX, mouseY);
	}
}
