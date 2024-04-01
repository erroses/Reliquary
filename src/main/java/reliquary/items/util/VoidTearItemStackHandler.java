package reliquary.items.util;

import net.minecraft.world.item.ItemStack;
import reliquary.reference.Config;

public class VoidTearItemStackHandler extends FilteredItemStackHandler {
	private static final int FIRST_SLOT = 0;

	public VoidTearItemStackHandler() {
		super(new FilteredItemStack(ItemStack.EMPTY, Config.COMMON.items.voidTear.itemLimit.get(), true));
	}

	public void setContainedStack(ItemStack stack) {
		setFilteredStack(FIRST_SLOT, new FilteredItemStack(stack, Config.COMMON.items.voidTear.itemLimit.get(), true));
	}

	public void setContainedStackAmount(int amount) {
		setTotalCount(FIRST_SLOT, amount);
	}

	public ItemStack getTotalAmountStack() {
		return getStackInSlot(FIRST_SLOT);
	}

	@Override
	protected boolean isValidForStackSlot(ItemStack stack, int stackSlot) {
		return stackSlot == FIRST_SLOT && super.isValidForStackSlot(stack, stackSlot);
	}

	public int getContainedAmount() {
		return getTotalAmount(FIRST_SLOT);
	}
}
