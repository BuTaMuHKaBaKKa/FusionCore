package io.izzel.arclight.common.bridge.core.world.entity.ai;

/** Fusion Core: the API wrapper Paper keeps on every vanilla goal ({@code Goal#asPaperVanillaGoal}, patch 0353). */
public interface GoalBridge {

    Object bridge$paperGoal();

    void bridge$setPaperGoal(Object goal);
}
