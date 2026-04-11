package com.columbina.debug

import com.columbina.content.research.ImportedResearchRegistry
import com.columbina.runtime.research.ResearchEntrySnapshot
import com.columbina.runtime.research.ResearchRuntimeService
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

object ResearchSliceDebugCommands {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("columbina")
                .then(
                    Commands.literal("research")
                        .then(
                            Commands.literal("seed")
                                .then(
                                    Commands.argument("player", StringArgumentType.word())
                                        .then(
                                            Commands.argument("goal", StringArgumentType.word())
                                                .executes { context ->
                                                    val playerKey = StringArgumentType.getString(context, "player")
                                                    val goal = StringArgumentType.getString(context, "goal")
                                                    val level = context.source.level

                                                    if (!ImportedResearchRegistry.hasGoal(goal)) {
                                                        context.source.sendSuccess({ Component.literal("Unknown imported goal: $goal") }, false)
                                                        return@executes 0
                                                    }

                                                    ResearchRuntimeService.replaceSnapshot(
                                                        level,
                                                        playerKey,
                                                        ResearchEntrySnapshot(
                                                            currentResearch = goal,
                                                            currentProgress = 0,
                                                            completedResearch = emptyList(),
                                                            queuedResearch = emptyList(),
                                                        ),
                                                    )

                                                    context.source.sendSuccess({ Component.literal("Seeded research slice for $playerKey with $goal") }, true)
                                                    1
                                                },
                                        ),
                                ),
                        )
                        .then(
                            Commands.literal("queue")
                                .then(
                                    Commands.argument("player", StringArgumentType.word())
                                        .then(
                                            Commands.argument("goal", StringArgumentType.word())
                                                .executes { context ->
                                                    val playerKey = StringArgumentType.getString(context, "player")
                                                    val goal = StringArgumentType.getString(context, "goal")
                                                    val level = context.source.level

                                                    ResearchRuntimeService.queueGoal(level, playerKey, goal)
                                                    context.source.sendSuccess({ Component.literal("Queued $goal for $playerKey") }, true)
                                                    1
                                                },
                                        ),
                                ),
                        )
                        .then(
                            Commands.literal("dump")
                                .then(
                                    Commands.argument("player", StringArgumentType.word())
                                        .executes { context ->
                                            val playerKey = StringArgumentType.getString(context, "player")
                                            val snapshot = ResearchRuntimeService.getSnapshot(context.source.level, playerKey)
                                            context.source.sendSuccess({
                                                Component.literal(
                                                    "Research[$playerKey] current=${snapshot.currentResearch} progress=${snapshot.currentProgress} queued=${snapshot.queuedResearch} completed=${snapshot.completedResearch}",
                                                )
                                            }, false)
                                            1
                                        },
                                ),
                        )
                        .then(
                            Commands.literal("clear")
                                .then(
                                    Commands.argument("player", StringArgumentType.word())
                                        .executes { context ->
                                            val playerKey = StringArgumentType.getString(context, "player")
                                            ResearchRuntimeService.clear(context.source.level, playerKey)
                                            context.source.sendSuccess({ Component.literal("Cleared research state for $playerKey") }, true)
                                            1
                                        },
                                ),
                        ),
                ),
        )
    }
}
