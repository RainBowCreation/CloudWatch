package github.metalshark.cloudwatch;

import com.github.puregero.multilib.MultiLib;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import github.metalshark.cloudwatch.commands.DrainNowCommand;
import github.metalshark.cloudwatch.listeners.*;
import github.metalshark.cloudwatch.runnables.JavaStatisticsRunnable;
import github.metalshark.cloudwatch.runnables.MinecraftStatisticsRunnable;
import github.metalshark.cloudwatch.runnables.TickRunnable;
import github.metalshark.cloudwatch.ssm.DrainFlagPoller;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.internal.util.EC2MetadataUtils;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.ssm.SsmClient;

import java.util.Map;
import java.util.concurrent.*;

public class CloudWatch extends JavaPlugin {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CloudWatch.class);
    public static final boolean IsMultipaper = MultiLib.isMultiPaper();
    private ScheduledExecutorService drainFlagExecutor;

    @Getter
    private ChunkLoadListener chunkLoadListener = new ChunkLoadListener();

    @Getter
    private PlayerJoinListener playerJoinListener = new PlayerJoinListener();

    @Getter
    private TickRunnable tickRunnable = new TickRunnable();

    @Getter
    private final static Map<String, EventCountListener> eventCountListeners = new ConcurrentHashMap<>();

    private final static ThreadFactory javaStatisticsThreadFactory = new ThreadFactoryBuilder()
        .setNameFormat("CloudWatch - Java Statistics")
        .build();
    private final static ThreadFactory minecraftStatisticsThreadFactory = new ThreadFactoryBuilder()
        .setNameFormat("CloudWatch - Minecraft Statistics")
        .build();

    private ScheduledExecutorService javaStatisticsExecutor;
    private ScheduledExecutorService minecraftStatisticsExecutor;

    @Getter
    private static Dimension dimension;

    @Override
    public void onEnable() {
        try {
            dimension = Dimension
                .builder()
                .name("Per-Instance Metrics")
                .value(EC2MetadataUtils.getInstanceId())
                .build();
            getLogger().info("CloudWatch initiate..");
        } catch (SdkClientException exception) {
            getLogger().warning("The CloudWatch plugin only works on EC2 instances.");
            this.setEnabled(false);
            return;
        }

        final PluginManager pluginManager = Bukkit.getPluginManager();

        if (IsMultipaper) {
            getLogger().info("Multipaper detected.");
        }

        getCommand("drainnow").setExecutor(new DrainNowCommand());

        pluginManager.registerEvents(chunkLoadListener.init(), this);
        pluginManager.registerEvents(playerJoinListener, this);

        eventCountListeners.put("ChunksPopulated", new ChunkPopulateListener());
        eventCountListeners.put("CreaturesSpawned", new CreatureSpawnListener());
        eventCountListeners.put("EntityDeaths", new EntityDeathListener());
        eventCountListeners.put("InventoriesClosed", new InventoryCloseListener());
        eventCountListeners.put("InventoriesOpened", new InventoryOpenListener());
        eventCountListeners.put("InventoryClicks", new InventoryClickListener());
        eventCountListeners.put("InventoryDrags", new InventoryDragListener());
        eventCountListeners.put("ItemsDespawned", new ItemSpawnListener());
        eventCountListeners.put("ItemsSpawned", new ItemDespawnListener());
        eventCountListeners.put("PlayerDropItems", new PlayerDropItemListener());
        eventCountListeners.put("PlayerExperienceChanges", new PlayerExpChangeListener());
        eventCountListeners.put("PlayerInteractions", new PlayerInteractListener());
        eventCountListeners.put("ProjectilesLaunched", new ProjectileLaunchListener());
        eventCountListeners.put("StructuresGrown", new StructureGrowListener());
        eventCountListeners.put("TradesSelected", new TradeSelectListener());

        for (Map.Entry<String, EventCountListener> entry : eventCountListeners.entrySet()) {
            final EventCountListener listener = entry.getValue();
            pluginManager.registerEvents(listener, this);
        }

        javaStatisticsExecutor = Executors.newSingleThreadScheduledExecutor(javaStatisticsThreadFactory);
        javaStatisticsExecutor.scheduleAtFixedRate(new JavaStatisticsRunnable(), 0, 1, TimeUnit.MINUTES);

        minecraftStatisticsExecutor = Executors.newSingleThreadScheduledExecutor(minecraftStatisticsThreadFactory);
        minecraftStatisticsExecutor.scheduleAtFixedRate(new MinecraftStatisticsRunnable(), 0, 1, TimeUnit.MINUTES);

        try {
            Region region = DefaultAwsRegionProviderChain.builder().build().getRegion();
            SsmClient ssm = SsmClient.builder().region(region).build();
            String paramName = "/minecraft/drain/" + EC2MetadataUtils.getInstanceId();

            ScheduledExecutorService drainFlagExecutor = Executors.newSingleThreadScheduledExecutor(
                    new ThreadFactoryBuilder().setNameFormat("CloudWatch - Drain Poller").build()
            );
            new DrainFlagPoller(ssm, paramName, drainFlagExecutor, 10).schedule();
            getLogger().info("Drain poller watching SSM parameter: " + paramName);
        } catch (Exception e) {
            getLogger().warning("Drain poller disabled: " + e.getMessage());
        }

        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, tickRunnable, 1, 1);
        getLogger().info("enabled.");
    }

    @Override
    public void onDisable() {
        ChunkLoadEvent.getHandlerList().unregister(chunkLoadListener);
        ChunkUnloadEvent.getHandlerList().unregister(chunkLoadListener);
        PlayerJoinEvent.getHandlerList().unregister(playerJoinListener);

        for (Map.Entry<String, EventCountListener> entry : eventCountListeners.entrySet()) {
            final Listener listener = entry.getValue();
            HandlerList.unregisterAll(listener);
        }

        if (javaStatisticsExecutor != null) javaStatisticsExecutor.shutdown();
        if (minecraftStatisticsExecutor != null) minecraftStatisticsExecutor.shutdown();
        if (drainFlagExecutor != null) drainFlagExecutor.shutdown();
    }

    public static CloudWatch getPlugin() {
        return getPlugin(CloudWatch.class);
    }

}
