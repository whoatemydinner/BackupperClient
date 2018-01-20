import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class StartFrame extends JFrame implements ActionListener {

	public Label info;

	private TextField username;
	
	private JPasswordField password;
    
	private Button login;

	private Button signUp;
	
	public StartFrame(String title)
	{
		super(title);
		init();
		setResizable(false);
		pack();
		setVisible(true);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	public void init(){
		 	
			info = new Label("Backuper. Log in/Sign up.", Label.CENTER);
	        username = new TextField(20);
	        password = new JPasswordField(15);
	        login = new Button("Login");
	        login.addActionListener(this);
	        signUp = new Button("SignUp");
	        signUp.addActionListener(this);

	        setBackground(Color.lightGray);
	        setLayout(new GridLayout(0, 1));
	        add(info);
	        Panel p100 = new Panel();
	        p100.add(new Label(" Username: ", Label.RIGHT));
	        p100.add(username);
	        add(p100);
	        Panel p200 = new Panel();
	        p200.add(new Label(" Password: ", Label.RIGHT));
	        p200.add(password);
	        add(p200);
	        Panel p300 = new Panel();
	        p300.add(login);
	        p300.add(signUp);
	        add(p300);
	}

	public void actionPerformed(ActionEvent ae) {
		
		Object event = ae.getSource();
		
		username.setEnabled(false);
		password.setEnabled(false);
		login.setEnabled(false);
		signUp.setEnabled(false);
		
		if(event == login){
			try {
	        	
	            User user = new User(username.getText(), password.getText(),this, true);
	            //new Thread(user).start();
	            
	        } catch (Exception e) {
	            info.setText(e.toString());
	        }
			
		} else if(event == signUp){
			try {
	        	
	            User user = new User(username.getText(), password.getText(),this, false);
	            //new Thread(user).start();
	            
	        } catch (Exception e) {
	            info.setText(e.toString());
	        }
		}
		
        
        username.setEnabled(true);
		password.setEnabled(true);
		login.setEnabled(true);
		signUp.setEnabled(true);
		
    }
	
	
	public static void main(String args[]) {
		StartFrame s =new StartFrame("Log in/Sign up");
		
		
	}

}
