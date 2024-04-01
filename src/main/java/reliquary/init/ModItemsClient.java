package reliquary.init;

import net.minecraft.client.gui.screens.MenuScreens;
import reliquary.client.gui.AlkahestryTomeGui;
import reliquary.client.gui.MobCharmBeltGui;

public class ModItemsClient {
	public static void registerMenuScreens() {
		MenuScreens.register(ModItems.ALKAHEST_TOME_MENU_TYPE.get(), AlkahestryTomeGui::new);
		MenuScreens.register(ModItems.MOB_CHAR_BELT_MENU_TYPE.get(), MobCharmBeltGui::new);
	}
}
