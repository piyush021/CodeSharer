import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ExitApplication extends WindowAdapter {
	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		super.windowClosing(e);
		System.exit(0);
	}	
}
