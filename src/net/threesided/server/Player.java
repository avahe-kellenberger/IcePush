package net.threesided.server;

import net.threesided.server.physics2d.Circle;

public class Player extends Circle {

    /**
     * The type of player.
     */
    public enum Type {
        TREE(0),
        SNOWMAN(1);

        private final int id;

        /**
         * @param id The ID of the type.
         */
        Type(final int id) {
            this.id = id;
        }

        /**
         * @return The Type's ID.
         */
        public int getID() {
            return this.id;
        }

        /**
         * @param id The ID of the type.
         * @return The Type based on the given ID.
         */
        public static Type getByID(final int id) {
            switch (id) {
                case 0:
                    return TREE;
                case 1:
                    return SNOWMAN;
                default:
                    throw new IllegalArgumentException("Unknown Player.Type id");
            }
        }

    }

    private static final int DEFAULT_RADIUS = 20;
    private static final int DEFAULT_MASS = 5;

    private final int id;
    private Type type;
    private String username;

    /**
     * @param id The player's ID.
     * @param type The player's type.
     * @param username The player's username.
     */
    public Player(final int id, final Player.Type type, final String username) {
        super(Player.DEFAULT_RADIUS);
        this.id = id;
        this.type = type;
        this.username = username;
        this.setMass(Player.DEFAULT_MASS);
    }

    /**
     * @return The player's username.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * @return The player's type.
     */
    public Type getType() {
        return this.type;
    }

    /**
     * @return The player's ID.
     */
    public int getID() {
        return this.id;
    }

}
