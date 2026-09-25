package com.wolfdrache.salormurder.leaderboard;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.util.EulerAngle;

public class ArmorstandLB {
    private static final String LEADERBOARD_TAG = "LEADERBOARD";
    public final EulerAngle headPose;
    public final EulerAngle bodyPose;
    public final EulerAngle leftArmPose;
    public final EulerAngle rightArmPose;
    public final EulerAngle leftLegPose;
    public final EulerAngle rightLegPose;
    public final boolean small;
    public final Location location;
    public final EntityEquipment equipment;
    public ArmorStand armorStand;

    public ArmorstandLB(EulerAngle headPose, EulerAngle bodyPose, EulerAngle leftArmPose, EulerAngle rightArmPose, EulerAngle leftLegPose, EulerAngle rightLegPose, boolean small, Location location, EntityEquipment equipment) {
        this.headPose = headPose;
        this.bodyPose = bodyPose;
        this.leftArmPose = leftArmPose;
        this.rightArmPose = rightArmPose;
        this.leftLegPose = leftLegPose;
        this.rightLegPose = rightLegPose;
        this.small = small;
        this.location = location;
        this.equipment = equipment;
        summon();
    }

    public void summon() {
        armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setHeadPose(headPose);
        armorStand.setBodyPose(bodyPose);
        armorStand.setLeftArmPose(leftArmPose);
        armorStand.setRightArmPose(rightArmPose);
        armorStand.setLeftLegPose(leftLegPose);
        armorStand.setRightLegPose(rightLegPose);
        armorStand.setSmall(small);
        armorStand.setMarker(true);
        armorStand.setVisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setGravity(false);
        armorStand.setBasePlate(false);
        armorStand.setArms(true);
        armorStand.setCustomNameVisible(true);
        armorStand.getEquipment().setHelmet(equipment.getHelmet());
        armorStand.getEquipment().setChestplate(equipment.getChestplate());
        armorStand.getEquipment().setLeggings(equipment.getLeggings());
        armorStand.getEquipment().setBoots(equipment.getBoots());
        armorStand.getEquipment().setItemInMainHand(equipment.getItemInMainHand());
        armorStand.getEquipment().setItemInOffHand(equipment.getItemInOffHand());
        armorStand.addScoreboardTag(LEADERBOARD_TAG);
    }

    public static boolean isLeaderboardArmorstand(ArmorStand armorStand) {
        return armorStand.getScoreboardTags().contains(LEADERBOARD_TAG);
    }
}
