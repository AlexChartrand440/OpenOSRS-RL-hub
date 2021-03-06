package io.ryoung.bitwarden;

import com.google.common.base.Strings;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.UsernameChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import org.pf4j.Extension;


@Slf4j
@Extension
@PluginDescriptor(
	name = "Bitwarden",
	description = "Plugin that interfaces with the Bitwarden CLI to retrieve RuneScape passwords.",
	enabledByDefault = false,
	type = PluginType.MISCELLANEOUS
)
public class BitwardenPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private BitwardenConfig config;

	@Inject
	private CredentialsManager credentialsManager;

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			credentialsManager.clearEntries();
			if (config.clearKeyOnLogin())
			{
				credentialsManager.clearSessionKey();
			}
		}
	}

	@Subscribe
	public void onUsernameChanged(UsernameChanged event)
	{
		if (client.getGameState() != GameState.LOGIN_SCREEN)
		{
			return;
		}

		credentialsManager.injectPassword();
	}

	@Override
	protected void startUp() throws Exception
	{
		String envKey = System.getenv("BW_SESSION");
		if (!Strings.isNullOrEmpty(envKey))
		{
			credentialsManager.setSessionKey(envKey.toCharArray());
		}
		credentialsManager.injectPassword();
	}

	@Override
	protected void shutDown() throws Exception
	{
		credentialsManager.reset();
	}

	@Provides
	BitwardenConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BitwardenConfig.class);
	}
}
