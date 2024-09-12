package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import ch.njol.skript.util.Utils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Send Resource Pack")
@Description({"Request that the player's client download and switch resource packs. The client will download",
	"the resource pack in the background, and will automatically switch to it once the download is complete.",
	"The URL must be a direct download link.",
	"",
	"The [**resource pack request action**](events.html#resource_pack_request_action) can be used to check",
	"status of the sent resource pack request.",
	"",
	"Take a look at [**Resource Pack Values**]() to see additional options."
})
@Examples({
	"on join:",
	"	send the resource pack from \"URL\" to the player",
	"",
	"   send the resource pack from \"URL\" to the player using:",
	"      set the resource pack uuid to \"1\"",
	"      set the resource pack hash to \"Hash\"",
	"      set the resource pack prompt to \"Please Download\"",
	"      force the player to accept"
})
@Since("2.4") //Change??
public class EffSecSendResourcePack extends EffectSection {

	public static class ResourcePackEvent extends Event {
		@Nullable
		private String id, hash, prompt = null;
		@Nullable
		private Boolean force = null;

		public ResourcePackEvent() {}

		public void setId(String id) {
			this.id = id;
		}
		public void setHash(String hash) {
			this.hash = hash;
		}
		public void setPrompt(String prompt) {
			this.prompt = prompt;
		}
		public void setForce(Boolean force) {
			this.force = force;
		}

		public String getId() {
			return id;
		}
		public String getHash() {
			return hash;
		}
		public String getPrompt() {
			return prompt;
		}
		public Boolean getForce() {
			return force;
		}


		@Override
		@NotNull
		public HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		Skript.registerSection(EffSecSendResourcePack.class,
			"send [a|the] resource pack (at|from [[the] URL]) %string% to %players% [using|with]",
			"remove [:all] resource pack[s] [id:with [the] [uu]id] from %players%"
			);

	}

	private int pattern;
	@Nullable
	@SuppressWarnings("null")
	private Expression<String> url;
	@Nullable
	@SuppressWarnings("null")
	private Expression<Player> recipients;
	@Nullable
	private Trigger trigger;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, @Nullable SectionNode sectionNode, List<TriggerItem> triggerItems) {
		this.url = (Expression<String>) exprs[0];
		this.recipients = (Expression<Player>) exprs[1];

		if (sectionNode != null) {
			AtomicBoolean delayed = new AtomicBoolean(false);
			Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
			trigger = loadCode(sectionNode, "resource", afterLoading, ResourcePackEvent.class);
			if (delayed.get()) {
				Skript.error("Delays can't be used within a Resource Pack Effect Section");
				return false;
			}
		}

		return true;
	}


	@Override
	protected @Nullable TriggerItem walk(Event event) {
		debug(event, true);
		assert url != null;
		String address = url.getSingle(event);
		String uuid = null;
		String hash = "defaultHash";
		String prompt = "The server has requested you to download a resource pack";
		boolean force = false;
		if (trigger != null) {
			ResourcePackEvent resourcePackEvent = new ResourcePackEvent();
			Variables.setLocalVariables(resourcePackEvent, Variables.copyLocalVariables(event));
			TriggerItem.walk(trigger, resourcePackEvent);
			Variables.setLocalVariables(event, Variables.copyLocalVariables(resourcePackEvent));
			Variables.removeLocals(resourcePackEvent);
			String checkUUID = resourcePackEvent.getId();
			String checkHash = resourcePackEvent.getHash();
			String checkPrompt = resourcePackEvent.getPrompt();
			Boolean checkForce = resourcePackEvent.getForce();

			if (checkUUID != null) {
				uuid = Utils.convertUUID(checkUUID);
			}
			if (checkHash != null) {
				hash = checkHash;
			}
			if (checkPrompt != null) {
				prompt = checkPrompt;
			}
			if (checkForce) {
				force = checkForce;
			}
		}
		for (Player player : recipients.getArray(event)) {
			if (uuid != null) {
				player.setResourcePack(UUID.fromString(uuid), address, StringUtils.hexStringToByteArray(hash), prompt, force);
			} else {
				player.setResourcePack(address, StringUtils.hexStringToByteArray(hash), prompt, force);
			}
		}
		return getNext();
	}


	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String result = "send the resource pack from the URL " + url.toString(event, debug);
		if (trigger != null) {
			String checkUUID = ((ResourcePackEvent) event).getId();
			String checkHash = ((ResourcePackEvent) event).getHash();
			String checkPrompt = ((ResourcePackEvent) event).getPrompt();
			Boolean checkForce = ((ResourcePackEvent) event).getForce();
			if (checkUUID != null) {
				result += ", with uuid " + checkUUID;
			}
			if (checkHash != null) {
				result += ", with hash " + checkHash;
			}
			if (checkPrompt != null) {
				result += ", with prompt " + checkPrompt;
			}
			if (checkForce != null) {
				result +=  ", with force";
			}
		}
		return result;
	}

}
