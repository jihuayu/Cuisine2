package snownee.cuisine;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializer;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import snownee.cuisine.api.Bonus;
import snownee.cuisine.api.CuisineAPI.ICuisineAPI;
import snownee.cuisine.api.RecipeRule;
import snownee.cuisine.api.registry.CuisineFood;
import snownee.cuisine.api.registry.Material;
import snownee.cuisine.api.registry.Spice;
import snownee.cuisine.data.CuisineDataManager;
import snownee.kiwi.KiwiModule.LoadingCondition;
import snownee.kiwi.LoadingContext;
import snownee.kiwi.util.NBTHelper;

@Mod(Cuisine.MODID)
public final class Cuisine implements ICuisineAPI {
    public static final String MODID = "cuisine";
    public static final String NAME = "Cuisine";

    public static boolean mixin;
    public static Logger logger = LogManager.getLogger(Cuisine.NAME);

    public Cuisine() {
        set(this);
    }

    @LoadingCondition("core")
    public static boolean checkMixin(LoadingContext ctx) {
        if (!mixin) {
            throw new IllegalStateException("Cannot find Mixin!");
        }
        return mixin;
    }

    @Override
    public int getFoodStar(ItemStack stack) {
        return NBTHelper.of(stack).getInt("FoodStar");
    }

    @Override
    public ItemStack setFoodStar(ItemStack stack, int star) {
        return NBTHelper.of(stack).setInt("FoodStar", star).getItem();
    }

    @Override
    public int getMaterialStar(ItemStack stack) {
        return NBTHelper.of(stack).getInt("MaterialStar");
    }

    @Override
    public ItemStack setMaterialStar(ItemStack stack, int star) {
        return NBTHelper.of(stack).setInt("MaterialStar", star).getItem();
    }

    @Override
    public synchronized void registerBonusAdapter(String key, JsonDeserializer<? extends Bonus> adapter) {
        CuisineDataManager.bonusAdapters.put(key, (JsonDeserializer<Bonus>) adapter);
    }

    @Override
    public synchronized void registerRecipeRuleAdapter(String key, JsonDeserializer<? extends RecipeRule> adapter) {
        CuisineDataManager.ruleAdapters.put(key, (JsonDeserializer<RecipeRule>) adapter);
    }

    private static final List<Function<ItemStack, Optional<CuisineFood>>> specialFoodMatchers = Lists.newLinkedList();

    @Override
    public synchronized void registerSpecialFoodMatcher(Function<ItemStack, Optional<CuisineFood>> matcher) {
        specialFoodMatchers.add(matcher);
    }

    @Override
    public Optional<CuisineFood> findFood(ItemStack stack) {
        CuisineFood food = CoreModule.item2Food.get(stack.getItem());
        if (food == null) {
            for (Function<ItemStack, Optional<CuisineFood>> func : specialFoodMatchers) {
                Optional<CuisineFood> result = func.apply(stack);
                if (result.isPresent()) {
                    return result;
                }
            }
        }
        return Optional.ofNullable(food);
    }

    @Override
    public Optional<CuisineFood> findFood(BlockState state) {
        return Optional.ofNullable(CoreModule.block2Food.get(state.getBlock()));
    }

    @Override
    public Optional<Material> findMaterial(ItemStack stack) {
        return Optional.ofNullable(CoreModule.item2Material.get(stack.getItem()));
    }

    @Override
    public Optional<Spice> findSpice(ItemStack stack) {
        return Optional.ofNullable(CoreModule.item2Spice.get(stack.getItem()));
    }

    @Override
    public Optional<Spice> findSpice(FluidStack stack) {
        return Optional.ofNullable(CoreModule.fluid2Spice.get(stack.getFluid()));
    }
}
