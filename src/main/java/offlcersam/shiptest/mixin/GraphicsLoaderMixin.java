package offlcersam.shiptest.mixin;

import game.graphics.GraphicsLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GraphicsLoader.class)
public class GraphicsLoaderMixin {

    @ModifyArg(
            method = "load",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/graphics/DeferedTextureLoader;<init>(ILjava/lang/String;Ljava/lang/String;IIZ)V"
            ),
            index = 0
    )
    private int changeShipTextureAmount(int amount) {
        return 2000;
    }
}
