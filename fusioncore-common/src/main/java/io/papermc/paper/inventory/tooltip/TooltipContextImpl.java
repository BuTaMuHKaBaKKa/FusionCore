// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.inventory.tooltip;


record TooltipContextImpl(boolean isAdvanced, boolean isCreative) implements TooltipContext {

    @Override
    public TooltipContext asCreative() {
        return new TooltipContextImpl(this.isAdvanced, true);
    }

    @Override
    public TooltipContext asAdvanced() {
        return new TooltipContextImpl(true, this.isCreative);
    }
}
