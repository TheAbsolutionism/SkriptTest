package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;

/**
 * @author Peter Güttinger
 */
public class VillagerData extends EntityData<Villager> {

	/**
	 * Professions can be for zombies also. These are the ones which are only
	 * for villagers.
	 */
	private static List<Profession> professions;

	static {
		// professions in order!
		// NORMAL(-1), FARMER(0), LIBRARIAN(1), PRIEST(2), BLACKSMITH(3), BUTCHER(4), NITWIT(5);
		
		Variables.yggdrasil.registerSingleClass(Profession.class, "Villager.Profession");

		professions = new ArrayList<>();
		if (Skript.isRunningMinecraft(1, 14)) {
			EntityData.register(VillagerData.class, "villager", Villager.class, 0,
					"villager", "normal", "armorer", "butcher", "cartographer",
					"cleric", "farmer", "fisherman", "fletcher",
					"leatherworker", "librarian", "mason", "nitwit",
					"shepherd", "toolsmith", "weaponsmith");
			// TODO obtain from the registry in the future
			// This is not currently done as the ordering of the professions is important
			// There is no ordering guarantee from the registry
			professions = Arrays.asList(Profession.NONE, Profession.ARMORER, Profession.BUTCHER, Profession.CARTOGRAPHER,
					Profession.CLERIC, Profession.FARMER, Profession.FISHERMAN, Profession.FLETCHER, Profession.LEATHERWORKER,
					Profession.LIBRARIAN, Profession.MASON, Profession.NITWIT, Profession.SHEPHERD, Profession.TOOLSMITH,
					Profession.WEAPONSMITH);
		} else { // Post 1.10: Not all professions go for villagers
			EntityData.register(VillagerData.class, "villager", Villager.class, 0,
					"normal", "villager", "farmer", "librarian",
					"priest", "blacksmith", "butcher", "nitwit");
			// Normal is for zombie villagers, but needs to be here, since someone thought changing first element in enum was good idea :(

			try {
				for (Profession prof : (Profession[]) MethodHandles.lookup().findStatic(Profession.class, "values", MethodType.methodType(Profession[].class)).invoke()) {
					// We're better off doing stringfying the constants since these don't exist in 1.14
					// Using String#valueOf to prevent IncompatibleClassChangeError due to Enum->Interface change
					String profString = String.valueOf(prof);
					if (!profString.equals("NORMAL") && !profString.equals("HUSK"))
						professions.add(prof);
				}
			} catch (Throwable e) {
				throw new RuntimeException("Failed to load legacy villager profession support", e);
			}
		}
	}

	private @Nullable Profession profession = null;
	
	public VillagerData() {}
	
	public VillagerData(@Nullable Profession profession) {
		this.profession = profession;
		this.matchedPattern = profession != null ? professions.indexOf(profession) + 1 : 0;
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (matchedPattern > 0)
			profession = professions.get(matchedPattern - 1);
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Villager> villagerClass, @Nullable Villager villager) {
		profession = villager == null ? null : villager.getProfession();
		return true;
	}
	
	@Override
	public void set(Villager villager) {
		Profession prof = profession == null ? CollectionUtils.getRandom(professions) : profession;
		assert prof != null;
		villager.setProfession(prof);
		if (profession == Profession.NITWIT)
			villager.setRecipes(Collections.emptyList());
	}
	
	@Override
	protected boolean match(Villager villager) {
		return profession == null || villager.getProfession() == profession;
	}
	
	@Override
	public Class<? extends Villager> getType() {
		return Villager.class;
	}
	
	@Override
	protected int hashCode_i() {
		return Objects.hashCode(profession);
	}
	
	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (!(obj instanceof VillagerData villagerData))
			return false;
		return profession == villagerData.profession;
	}
	
//		return profession == null ? "" : profession.name();
	@Override
	protected boolean deserialize(final String s) {
		if (s.isEmpty())
			return true;
		try {
			//noinspection unchecked, rawtypes - prevent IncompatibleClassChangeError due to Enum->Interface change
			profession = (Profession) Enum.valueOf((Class) Profession.class, s);
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (entityData instanceof VillagerData villagerData)
			return profession == null || profession.equals(villagerData.profession);
		return false;
	}
	
	@Override
	public EntityData getSuperType() {
		return new VillagerData(profession);
	}
	
}
