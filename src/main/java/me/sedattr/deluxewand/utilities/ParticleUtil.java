package me.sedattr.deluxewand.utilities;

import me.sedattr.deluxewand.DeluxeWand;
import me.sedattr.deluxewand.enums.ParticleShapeHidden;
import me.sedattr.deluxewand.items.Wand;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.BiConsumer;

public class ParticleUtil {
    private void drawLine(Wand wand, Location loc1, Location loc2, Player player) {
        Vector loc1Vector = loc1.toVector();
        Vector loc2Vector = loc2.toVector();
        Vector difference = loc2Vector.subtract(loc1Vector);
        int distance = (int) loc1.distance(loc2);

        String particle = wand.getParticle();
        int particleAmount = wand.getParticleCount();
        int amount = Math.max(1, distance) * particleAmount;

        double xIncrement = difference.getX() / amount;
        double yIncrement = difference.getY() / amount;
        double zIncrement = difference.getZ() / amount;

        World world = loc1.getWorld();
        boolean renderForAllPlayers = DeluxeWand.getInstance().getConfigFile().getBoolean("particle.render_for_all_players", false);

        for (int i = 0; i < amount; i++) {
            Location location = loc1.clone().add(xIncrement * i, yIncrement * i, zIncrement * i);
            if (renderForAllPlayers) {
                world.spawnParticle(Particle.valueOf(particle), location, 0, 128, 0, 0, 10);
            } else {
                player.spawnParticle(Particle.valueOf(particle), location, 0, 0, 0, 0);
            }
        }
    }

    public void drawBlockOutlines(BlockFace blockFace, List<ParticleShapeHidden> shapes, Location location, Wand wand, Player player) {
        Location loc1Clone = location.clone().add(0.95, 0.75, 0.95);
        Location loc2Clone = location.clone().add(0.2, 0, 0.2);

        switch (blockFace) {
            case UP:
            case DOWN:
                drawBlockOutlinesHorizontal(shapes, wand, loc1Clone, loc2Clone, player);
                break;
            case SOUTH:
            case NORTH:
                drawBlockOutlinesVerticalSouthNorth(shapes, wand, loc1Clone, loc2Clone, player);
                break;
            case EAST:
            case WEST:
                drawBlockOutlinesVerticalEastWest(shapes, wand, loc1Clone, loc2Clone, player);
                break;
        }
    }

    private void drawBlockOutlinesVerticalSouthNorth(List<ParticleShapeHidden> shapes, Wand wand, Location location1, Location location2, Player player) {
        double loc1X = location1.getX();
        double loc1Y = location1.getY();
        double loc1Z = location1.getZ();

        double loc2X = location2.getX();
        double loc2Y = location2.getY();
        double loc2Z = location2.getZ();

        World world = location1.getWorld();

        // Function to draw lines with conditional checks
        BiConsumer<Location, Location> conditionalDrawLine = (loc1, loc2) -> {
            if (!shapes.contains(ParticleShapeHidden.WEST) && !shapes.contains(ParticleShapeHidden.DOWN)) {
                drawLine(wand, loc1, loc2, player);
            } else if (shapes.contains(ParticleShapeHidden.WEST) && shapes.contains(ParticleShapeHidden.DOWN) && !shapes.contains(ParticleShapeHidden.DOWN_WEST)) {
                drawLine(wand, loc1, loc2, player);
            }
        };

        // Vertical South-North lines
        conditionalDrawLine.accept(new Location(world, loc2X, loc2Y, loc1Z), new Location(world, loc2X, loc2Y, loc2Z));
        conditionalDrawLine.accept(new Location(world, loc2X, loc1Y, loc1Z), new Location(world, loc2X, loc1Y, loc2Z));
        conditionalDrawLine.accept(new Location(world, loc1X, loc2Y, loc2Z), new Location(world, loc1X, loc2Y, loc1Z));
        conditionalDrawLine.accept(new Location(world, loc1X, loc1Y, loc1Z), new Location(world, loc1X, loc1Y, loc2Z));

        loc2Y -= 0.1;
        loc1Y += 0.1;
        if (!shapes.contains(ParticleShapeHidden.WEST)) {
            drawLine(wand, new Location(world, loc2X, loc1Y, loc1Z), new Location(world, loc2X, loc2Y, loc1Z), player);
            drawLine(wand, new Location(world, loc2X, loc1Y, loc2Z), new Location(world, loc2X, loc2Y, loc2Z), player);
        }
        if (!shapes.contains(ParticleShapeHidden.EAST)) {
            drawLine(wand, new Location(world, loc1X, loc1Y, loc1Z), new Location(world, loc1X, loc2Y, loc1Z), player);
            drawLine(wand, new Location(world, loc1X, loc2Y, loc2Z), new Location(world, loc1X, loc1Y, loc2Z), player);
        }

        loc2Y += 0.1;
        loc1Y -= 0.1;
        loc2X -= 0.1;
        loc1X += 0.1;
        if (!shapes.contains(ParticleShapeHidden.UP)) {
            drawLine(wand, new Location(world, loc1X, loc1Y, loc1Z), new Location(world, loc2X, loc1Y, loc1Z), player);
            drawLine(wand, new Location(world, loc2X, loc1Y, loc2Z), new Location(world, loc1X, loc1Y, loc2Z), player);
        }
        if (!shapes.contains(ParticleShapeHidden.DOWN)) {
            drawLine(wand, new Location(world, loc1X, loc2Y, loc1Z), new Location(world, loc2X, loc2Y, loc1Z), player);
            drawLine(wand, new Location(world, loc2X, loc2Y, loc2Z), new Location(world, loc1X, loc2Y, loc2Z), player);
        }
    }

    private void drawBlockOutlinesVerticalEastWest(List<ParticleShapeHidden> shapes, Wand wand, Location location1, Location location2, Player player) {
        double loc1X = location1.getX();
        double loc1Y = location1.getY();
        double loc1Z = location1.getZ();

        double loc2X = location2.getX();
        double loc2Y = location2.getY();
        double loc2Z = location2.getZ();

        World world = location1.getWorld();

        BiConsumer<Location, Location> conditionalDrawLine = (loc1, loc2) -> {
            if (!shapes.contains(ParticleShapeHidden.NORTH) && !shapes.contains(ParticleShapeHidden.DOWN)) {
                drawLine(wand, loc1, loc2, player);
            }
            if (shapes.contains(ParticleShapeHidden.NORTH) && shapes.contains(ParticleShapeHidden.DOWN) && !shapes.contains(ParticleShapeHidden.DOWN_NORTH)) {
                drawLine(wand, loc2, loc1, player);
            }
        };

        // Draw vertical east-west lines
        conditionalDrawLine.accept(new Location(world, loc1X, loc2Y, loc2Z), new Location(world, loc2X, loc2Y, loc2Z));
        conditionalDrawLine.accept(new Location(world, loc1X, loc1Y, loc2Z), new Location(world, loc2X, loc1Y, loc2Z));
        conditionalDrawLine.accept(new Location(world, loc1X, loc2Y, loc1Z), new Location(world, loc2X, loc2Y, loc1Z));
        conditionalDrawLine.accept(new Location(world, loc1X, loc1Y, loc1Z), new Location(world, loc2X, loc1Y, loc1Z));

        // Adjust Y positions for drawing side lines
        loc2Y -= 0.1;
        loc1Y += 0.1;
        if (!shapes.contains(ParticleShapeHidden.NORTH)) {
            drawLine(wand, new Location(world, loc1X, loc2Y, loc2Z), new Location(world, loc1X, loc1Y, loc2Z), player);
            drawLine(wand, new Location(world, loc2X, loc1Y, loc2Z), new Location(world, loc2X, loc2Y, loc2Z), player);
        }
        if (!shapes.contains(ParticleShapeHidden.SOUTH)) {
            drawLine(wand, new Location(world, loc2X, loc1Y, loc1Z), new Location(world, loc2X, loc2Y, loc1Z), player);
            drawLine(wand, new Location(world, loc1X, loc1Y, loc1Z), new Location(world, loc1X, loc2Y, loc1Z), player);
        }

        // Reset Y positions and adjust Z positions for further drawing
        loc2Y += 0.1;
        loc1Y -= 0.1;
        loc2Z -= 0.1;
        loc1Z += 0.1;
        if (!shapes.contains(ParticleShapeHidden.DOWN)) {
            drawLine(wand, new Location(world, loc1X, loc2Y, loc2Z), new Location(world, loc1X, loc2Y, loc1Z), player);
            drawLine(wand, new Location(world, loc2X, loc2Y, loc1Z), new Location(world, loc2X, loc2Y, loc2Z), player);
        }
        if (!shapes.contains(ParticleShapeHidden.UP)) {
            drawLine(wand, new Location(world, loc1X, loc1Y, loc1Z), new Location(world, loc1X, loc1Y, loc2Z), player);
            drawLine(wand, new Location(world, loc2X, loc1Y, loc1Z), new Location(world, loc2X, loc1Y, loc2Z), player);
        }
    }


    private void drawBlockOutlinesHorizontal(List<ParticleShapeHidden> shapes, Wand wand, Location location1, Location location2, Player player) {
        double loc1X = location1.getX();
        double loc1Y = location1.getY();
        double loc1Z = location1.getZ();

        double loc2X = location2.getX();
        double loc2Y = location2.getY();
        double loc2Z = location2.getZ();

        World world = location1.getWorld();

        BiConsumer<Location, Location> conditionalDrawLine = (loc1, loc2) -> {
            if (!shapes.contains(ParticleShapeHidden.SOUTH) && !shapes.contains(ParticleShapeHidden.EAST)) {
                drawLine(wand, loc1, loc2, player);
            }
            if (shapes.contains(ParticleShapeHidden.SOUTH) && shapes.contains(ParticleShapeHidden.EAST) && !shapes.contains(ParticleShapeHidden.SOUTH_EAST)) {
                drawLine(wand, loc2, loc1, player);
            }
        };

        // Draw horizontal lines
        conditionalDrawLine.accept(new Location(world, loc1X, loc1Y, loc1Z), new Location(world, loc1X, loc2Y, loc1Z));
        conditionalDrawLine.accept(new Location(world, loc2X, loc1Y, loc1Z), new Location(world, loc2X, loc2Y, loc1Z));
        conditionalDrawLine.accept(new Location(world, loc2X, loc1Y, loc2Z), new Location(world, loc2X, loc2Y, loc2Z));
        conditionalDrawLine.accept(new Location(world, loc1X, loc2Y, loc2Z), new Location(world, loc1X, loc1Y, loc2Z));

        // Adjust X positions for side lines
        loc2X -= 0.1;
        loc1X += 0.1;
        if (!shapes.contains(ParticleShapeHidden.SOUTH)) {
            drawLine(wand, new Location(world, loc1X, loc1Y, loc1Z), new Location(world, loc2X, loc1Y, loc1Z), player);
            drawLine(wand, new Location(world, loc1X, loc2Y, loc1Z), new Location(world, loc2X, loc2Y, loc1Z), player);
        }
        if (!shapes.contains(ParticleShapeHidden.NORTH)) {
            drawLine(wand, new Location(world, loc2X, loc1Y, loc2Z), new Location(world, loc1X, loc1Y, loc2Z), player);
            drawLine(wand, new Location(world, loc1X, loc2Y, loc2Z), new Location(world, loc2X, loc2Y, loc2Z), player);
        }

        // Reset X positions and adjust Z positions for further drawing
        loc2X += 0.1;
        loc1X -= 0.1;
        loc2Z -= 0.1;
        loc1Z += 0.1;
        if (!shapes.contains(ParticleShapeHidden.EAST)) {
            drawLine(wand, new Location(world, loc1X, loc2Y, loc2Z), new Location(world, loc1X, loc2Y, loc1Z), player);
            drawLine(wand, new Location(world, loc1X, loc1Y, loc1Z), new Location(world, loc1X, loc1Y, loc2Z), player);
        }
        if (!shapes.contains(ParticleShapeHidden.WEST)) {
            drawLine(wand, new Location(world, loc2X, loc2Y, loc1Z), new Location(world, loc2X, loc2Y, loc2Z), player);
            drawLine(wand, new Location(world, loc2X, loc1Y, loc1Z), new Location(world, loc2X, loc1Y, loc2Z), player);
        }
    }

}
