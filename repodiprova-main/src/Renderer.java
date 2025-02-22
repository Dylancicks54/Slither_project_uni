import java.awt.*;
import java.util.List;

class Renderer {
    public static void drawEntities(Graphics g, List<Entity> entities, Player player) {
        if (entities == null || entities.isEmpty()) {
            g.setColor(Color.RED);
            g.drawString("Nessuna entità da disegnare.", 50, 50);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Vector2D playerPos = (player != null) ? player.getPosition() : new Vector2D(400, 300);
        int screenWidth = 800, screenHeight = 600;
        int offsetX = (int) playerPos.x - screenWidth / 2;
        int offsetY = (int) playerPos.y - screenHeight / 2;

        drawBackground(g2, offsetX, offsetY);

        synchronized (entities) {
            for (Entity entity : entities) {
                if (entity == null || entity.getPosition() == null) {
                    System.err.println("Errore: entità nulla o posizione nulla!");
                    continue;
                }

                int drawX = (int) entity.getPosition().x - offsetX;
                int drawY = (int) entity.getPosition().y - offsetY;

                if (entity instanceof Player) {
                    drawPlayer(g2, (Player) entity, drawX, drawY);
                } else if (entity instanceof Bot) {
                    drawBot(g2, (Bot) entity, drawX, drawY);
                } else if (entity instanceof Food) {
                    g2.setColor(Color.GREEN);
                    g2.fillOval(drawX, drawY, (int) entity.getSize(), (int) entity.getSize());
                }
            }
        }
    }

    private static void drawBackground(Graphics2D g2, int offsetX, int offsetY) {
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(0, 0, 8000, 8000);

        g2.setColor(Color.GRAY);
        for (int x = -offsetX % 50; x < 800; x += 50) {
            for (int y = -offsetY % 50; y < 600; y += 50) {
                g2.drawRect(x, y, 50, 50);
            }
        }
    }

    private static void drawPlayer(Graphics2D g2, Player player, int drawX, int drawY) {
        for (Segment segment : player.getBodySegments()) {
            int segX = (int) segment.getPosition().x - drawX;
            int segY = (int) segment.getPosition().y - drawY;
            g2.setColor(Color.BLUE);
            g2.fillOval(segX, segY, (int) segment.getSize(), (int) segment.getSize());
        }
        g2.setColor(Color.YELLOW);
        g2.fillOval(drawX, drawY, (int) player.getSize(), (int) player.getSize());
    }

    private static void drawBot(Graphics2D g2, Bot bot, int drawX, int drawY) {
        for (Segment segment : bot.getBodySegments()) {
            int segX = (int) segment.getPosition().x - drawX;
            int segY = (int) segment.getPosition().y - drawY;
            g2.setColor(Color.RED);
            g2.fillOval(segX, segY, (int) segment.getSize(), (int) segment.getSize());
        }
        g2.setColor(Color.RED);
        g2.fillOval(drawX, drawY, (int) bot.getSize(), (int) bot.getSize());
    }
}