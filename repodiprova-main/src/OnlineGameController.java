import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class OnlineGameController extends MouseAdapter {
    private GameClient client;

    public OnlineGameController(GameClient client) {
        this.client = client;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        client.write("x: " + mouseX + ", y: " + mouseY);
    }

}
