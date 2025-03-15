    import java.awt.*;
    import java.util.ArrayList;
    import java.util.Iterator;
    import java.util.List;
    import java.util.UUID;
    import java.util.concurrent.CopyOnWriteArrayList;

    public class GameState {
        private static GameState instance = new GameState();
        private List<Entity> entities;
        private List<Player> players;
        private List<Bot> bots;
        private List<Food> foodItems;
        public static final int MAP_WIDTH = 5000;
        public static final int MAP_HEIGHT = 5000;
        private static final double ATTRACT_DISTANCE = 50.0;
        private static final double ATTRACT_SPEED = 2.0;
        private GameController controller;
        private GameServer server;

        /**
         * COSTRUTTORE
         */

        public GameState(List<Player> players, List<Bot> bots, List<Food> foodItems) {
            this.server = server;
            entities = new CopyOnWriteArrayList<>();
            entities.addAll(players);
            entities.addAll(bots);
            entities.addAll(foodItems);

        }


        public GameState() {
            this.players = new CopyOnWriteArrayList<>();
            this.bots = new CopyOnWriteArrayList<>();
            this.foodItems = new CopyOnWriteArrayList<>();
            this.entities = new CopyOnWriteArrayList<>();

        }
        public void setController(GameController gameController) {
            this.controller = gameController;
        }

        /**
         * AGGIUNTA E RIMOZIONE ENTITÀ
         */

        public void addPlayer(Player player) {
            players.add(player);
        }

        public void addBot() {
            for(int i=0; i<10; i++) {
                bots.add(Bot.createBot(entities,this));
            }
        }
        public void removeFood(Food food) {
            foodItems.remove(food);
        }

        private void applyMagnetEffect() {
            List<Food> foodToRemove = new ArrayList<>();

            for (Food food : foodItems) {
                Entity closestEntity = null;
                double closestDistance = ATTRACT_DISTANCE;

                for (Player player : players) {
                    double distance = player.getPosition().distanceTo(food.getPosition());
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestEntity = player;
                    }
                }

                for (Bot bot : bots) {
                    double distance = bot.getPosition().distanceTo(food.getPosition());
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestEntity = bot;
                    }
                }

                if (closestEntity != null) {
                    if (collidesWith(closestEntity, food)) {
                        if (closestEntity instanceof Player) {
                            ((Player) closestEntity).grow();
                        } else if (closestEntity instanceof Bot) {
                            ((Bot) closestEntity).grow();
                        }
                        foodToRemove.add(food);
                    } else {
                        moveFoodTowards(food, closestEntity);
                    }
                }
            }

            foodItems.removeAll(foodToRemove);
        }

        private Food createFoodFromSegment(Segment segment) {
            Food food = new Food(segment.getPosition(), 10 , UUID.randomUUID().toString().substring(0, 8));
            return food;
        }

        private void respawnFood() {
            final int MAX_FOOD_ITEMS = 500;
            while (foodItems.size() < MAX_FOOD_ITEMS) {
                foodItems.add(new Food(new Vector2D(Math.random() * 5000, Math.random() * 5000), 10,UUID.randomUUID().toString().substring(0, 8)));
            }
        }

        public static synchronized GameState getInstance() {
            if (instance == null) {
                System.out.println("istance is null");
            }
            return instance;
        }
        /**
         * CONTROLLO COLLISIONI
         */
        public void checkCollisions() {
            List<Bot> botsToAdd = new ArrayList<>();
            List<Bot> botsToRemove = new ArrayList<>();
            List<Player> playersToKill = new ArrayList<>();
            List<Food> foodToRemove = new ArrayList<>();

            aggiornaLista();

            for (Player player : players) {
                for (Food food : foodItems) {
                    if (player.collidesWith(food)) {
                        player.grow();
                        foodToRemove.add(food);
                    }
                }
            }


            for (Bot bot : bots) {
                for (Food food : foodItems) {
                    if (bot.collidesWith(food)) {
                        bot.grow();
                        foodToRemove.add(food);
                    }
                }
            }

            for (Bot bot : bots) {
                for (Bot otherBot : bots) {
                    if (bot != otherBot && bot.collidesWith(otherBot)) {
                        int n = Math.min(15,bot.getBodySegments().size());

                        for (int i = 0; i < n; i++) {
                            Segment segment = bot.getBodySegments().get(i);
                            foodItems.add(createFoodFromSegment(segment));
                        }
                        botsToRemove.add(bot);
                        botsToAdd.add(Bot.createBot(entities, this));
                        break;
                    }
                }
            }

            for (Bot bot : bots) {
                for (Player player : players) {
                    for (Segment segment : player.getBodySegments()) {
                        if (checkCollisionSegmentBot(bot, segment)) {
                            int n = Math.min(15,bot.getBodySegments().size());
                            for (int i = 0; i < n; i++) {
                                Segment segment1 = bot.getBodySegments().get(i);
                                foodItems.add(createFoodFromSegment(segment1));
                            }
                            botsToRemove.add(bot);
                            botsToAdd.add(Bot.createBot(entities, this));
                            break;
                        }
                    }
                }
            }

            for (Player player : players) {
                for (Player otherPlayer : players) {
                    if (player != otherPlayer && player.collidesWith(otherPlayer)) {
                        playersToKill.add(player);
                        player.setAlive(false);
                    }
                }
            }

            for (Player player : players) {
                for (Player otherPlayer : players) {
                    if (player != otherPlayer) {
                        for (Segment segment : otherPlayer.getBodySegments()) {
                            if (checkCollisionSegmentPlayer(player, segment)) {
                                playersToKill.add(player);
                                player.setAlive(false);
                                break;
                            }
                        }
                    }
                }
            }

            for (Player player : players) {
                for (Bot bot : bots) {
                    if (player.collidesWith(bot)) {
                        playersToKill.add(player);
                        break;
                    }
                }
            }
            for (Bot bot : bots) {
                List<Bot> otherBots = new ArrayList<>(bots);
                for (Bot otherBot : otherBots) {
                    for (Segment segment : otherBot.getBodySegments()) {
                        if (bot!= otherBot && checkCollisionSegmentBot(bot, segment)) {
                            int n = Math.min(15,bot.getBodySegments().size());
                            for (int i = 0; i < n; i++) {
                                Segment segment2 = bot.getBodySegments().get(i);
                                foodItems.add(createFoodFromSegment(segment2));
                            }
                            botsToRemove.add(bot);
                            botsToAdd.add(Bot.createBot(entities, this));
                            break;
                        }
                    }
                }
            }

            for (Player player : players) {
                for (Bot bot : bots) {
                    for (Segment segment : bot.getBodySegments()) {
                        if (checkCollisionSegmentPlayer(player, segment)) {
                            playersToKill.add(player);
                            break;
                        }
                    }
                }
            }

            for (Player player : playersToKill) {
                player.setAlive(false);
                System.out.println("Player " + player.getId() + " is dead! Press 'R' to respawn.");
            }

            bots.removeAll(botsToRemove);
            bots.addAll(botsToAdd);

            foodItems.removeAll(foodToRemove);
        }

        private boolean checkCollisionSegmentBot(Bot bot, Segment segment) {
            double distance = bot.getPosition().distanceTo(segment.getPosition());
            double collisionDistance = bot.getSize() / 2.0 + segment.getSize() / 2.0;
            return distance < collisionDistance;
        }

        private boolean checkCollisionSegmentPlayer(Player player, Segment segment) {
            double distance = player.getPosition().distanceTo(segment.getPosition());
            double collisionDistance = player.getSize() / 2.0 + segment.getSize() / 2.0;
            return distance < collisionDistance;
        }
        private boolean collidesWith(Entity entity, Food food) {
            double distance = entity.getPosition().distanceTo(food.getPosition());
            double collisionRadius = (entity.getSize() / 2.0) + (food.getSize() / 2.0);
            return distance < collisionRadius;
        }
        private void moveFoodTowards(Food food, Entity entity) {
            Vector2D foodPos = food.getPosition();
            Vector2D entityPos = entity.getPosition();

            double dx = entityPos.getX() - foodPos.getX();
            double dy = entityPos.getY() - foodPos.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance > 0) {
                double moveX = (dx / distance) * ATTRACT_SPEED;
                double moveY = (dy / distance) * ATTRACT_SPEED;
                food.setPosition(new Vector2D(foodPos.getX() + moveX, foodPos.getY() + moveY));
            }
        }

        /**
         * AGGIORNAMENTI
         */

        public void updateGameState() {
            for (Player player : players) {
                player.update();
            }
            for (Bot bot : bots) {
                bot.update();
            }
            applyMagnetEffect();
            checkCollisions();
            respawnFood();
        }

        public void aggiornaLista(){

            entities.addAll(players);
            entities.addAll(bots);
            entities.addAll(foodItems);
        }

        /**
         * GETTERS
         */
        public List<Player> getPlayers() { return players; }
        public List<Bot> getBots() { return bots; }
        public List<Food> getFoodItems() { return foodItems; }

        public List<Entity> getEntities() {
            List<Entity> allEntities = new ArrayList<>();
            allEntities.addAll(players);
            allEntities.addAll(bots);
            allEntities.addAll(foodItems);
            return allEntities;
        }

        public Player getPlayerById(String playerId) {
            for (Player player : players) {
                if (player.getId().equals(playerId)) {
                    return player;
                }
            }
            return null;
        }

        /**
         * SETTERS
         */

        public void setPlayers(List<Player> players) {
            this.players = players;
        }

        public void setFoodItems(List<Food> foodItems) {
            this.foodItems = foodItems;
        }

        public void setBots(List<Bot> bots) {
            this.bots = bots;
        }
    }