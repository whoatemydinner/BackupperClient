import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;

public class Session extends JFrame implements Runnable {
    private String host;
    private Integer port;
    private String downloadFolder;
    private String addFolder;
    private Socket socket = null;
    private String userName;
    private BufferedReader input;
    private PrintWriter output;
    private StartUp parent;
    private DefaultListModel modelList = new DefaultListModel();
    private JList listOfFiles = new JList(modelList);
    private JLabel inf = new JLabel();
    private Button bAdd, bDelete, bGetFile, bLogout;

	 
    public Session(String login, String pass, StartUp parent, boolean log) throws Exception{	
        this.parent = parent;
        this.userName = login;
		 
        try {
            Properties prop = new Properties();
            InputStream inS = null;
            try {
                inS = new FileInputStream("ConnectorID.properties");
                prop.load(inS);
                host = prop.getProperty("SERVER_HOST");
                port = Integer.parseInt(prop.getProperty("SERVER_PORT"));
                downloadFolder = prop.getProperty("FOLDER");
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (inS != null) {
                    try {
                        inS.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            
            socket = new Socket(host, port);
        } catch (UnknownHostException e) {
            throw new Exception("Unknown host.");
        } catch (IOException e) {
            throw new Exception("IO exception while connecting to the server.");
        } catch (NumberFormatException e) {
            throw new Exception("Port value must be a number.");
        }
        
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException ex) {
            throw new Exception("Can not get input/output connection stream.");
        }
        new Thread(this).start();
        
        pass = hash(pass);
        
        if(log == true) {
            send(BackupProt.LOGIN + " " + login + " " + pass);
        } else if(log == false) {
            send(BackupProt.CREATE + " " + login + " " + pass);
        }
        
    }

    public void run() {
        while (true) {
            String request = receive();
            System.out.println(request);
            StringTokenizer st = new StringTokenizer(request);
            String command = st.nextToken();
            
            if(command.equals(BackupProt.NULL_COMMAND)) {
                break;
            } else if (command.equals(BackupProt.LOGGEDIN) || command.equals(BackupProt.CREATED)) { 
                parent.dispose();
                setBackground(Color.lightGray);
                
                
                inf.setText("Ready");
                
                bAdd = new Button("Add File");
                bAdd.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        
                        JFileChooser fc = new JFileChooser();
                        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        int tmp = fc.showDialog(rootPane, "Add");
                        File file = null;
                        if(tmp == JFileChooser.APPROVE_OPTION) {
                            file = fc.getSelectedFile();
                        }
                        
                        if(file != null && file.getName().contains(" ") == false) {
                            String filePath = file.getPath();
                            send(BackupProt.ADD_FILE + " " + file.getName() + " " + filePath + " " + String.valueOf(file.length()));
                        }
                        if(file.getName().contains(" ")){
                            JOptionPane.showMessageDialog(null, "Please choose a file without space in the name.");
                        }
                    }
                });
	                    
                bDelete = new Button("Delete File");
                bDelete.addActionListener(new ActionListener() {
                    
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        String tmp_tosplit = (String)(listOfFiles.getSelectedValue());
                        String[] parts = tmp_tosplit.split(",");
                        bAdd.setEnabled(false);
                        bDelete.setEnabled(false);
                        bGetFile.setEnabled(false);
                        bLogout.setEnabled(false);
                        inf.setText("Deleting...");
                        
                        send(BackupProt.DELETE_FILE + " " + parts[0]);
                        
                        
                        //System.out.println(CloudProtocol.DELETE_FILE + " " + tmp);
                        
                    }
                    
                });
                bGetFile = new Button("Get File");
                bGetFile.addActionListener(new ActionListener() {
                    
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        
                        String tmp_tosplit = (String)(listOfFiles.getSelectedValue());
                        System.out.println(tmp_tosplit);
                        String[] parts = tmp_tosplit.split(",");
                        System.out.println(parts[0]);
                        System.out.println(parts[1]);
                        bAdd.setEnabled(false);
                        bDelete.setEnabled(false);
                        bGetFile.setEnabled(false);
                        bLogout.setEnabled(false);
                        inf.setText("Transfering...");
                        send(BackupProt.GET_FILE + " " + parts[0] + " " + parts[1]);
                        
                    }
                    
                });
	                    
                bLogout = new Button("Logout");
                bLogout.addActionListener(new ActionListener() {
                    
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        send(BackupProt.LOGOUT);
                        
                    }
                    
                });
                JScrollPane scroll = new JScrollPane(listOfFiles);
                listOfFiles.setBorder(BorderFactory.createEtchedBorder());
                
                
                while(st.hasMoreTokens()) {
                    String tmp = st.nextToken();
                    modelList.addElement(tmp);
                }
                
                GroupLayout layout = new GroupLayout(this.getContentPane());
                layout.setAutoCreateContainerGaps(true);
                layout.setAutoCreateGaps(true);
                layout.setHorizontalGroup(
                        layout.createSequentialGroup()
                                .addComponent(scroll,200,250,Short.MAX_VALUE)
                                .addGroup(
                                        layout.createParallelGroup().addComponent(bAdd).addComponent(bDelete).addComponent(bGetFile).addComponent(bLogout).addComponent(inf)
                                        
                                )
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup()
                                .addComponent(scroll, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createSequentialGroup().addComponent(bAdd).addComponent(bDelete).addComponent(bGetFile).addComponent(bLogout).addComponent(inf))
                );
                
                setLayout(layout);
                
                setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
                setTitle(userName + " Cloud");
                setResizable(false);
                pack();
                setVisible(true);

            } else if(command.equals(BackupProt.WRONG_PASSWORD)) {
                (parent.info).setText(BackupProt.WRONG_PASSWORD);
                break;
            } else if(command.equals(BackupProt.SEND_START)) {
                String fileName = st.nextToken();
                String fileSize = st.nextToken();
                try {
                    getFile(fileName, fileSize);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                bAdd.setEnabled(true);
                bDelete.setEnabled(true);
                bGetFile.setEnabled(true);
                bLogout.setEnabled(true);
                
                
                inf.setText("File downloaded");
	                	
            } else if(command.equals(BackupProt.NOT_FOUND)) {
                bAdd.setEnabled(true);
                bDelete.setEnabled(true);
                bGetFile.setEnabled(true);
                bLogout.setEnabled(true);
                
                inf.setText("File not found");
                
            } else if(command.equals(BackupProt.WRONG_LOGIN)) {
                (parent.info).setText(BackupProt.WRONG_LOGIN);
                break;
            } else if(command.equals(BackupProt.DELETE_SUCCESS)) {
                
                modelList.removeAllElements();
                
                while(st.hasMoreTokens()) {
                    
                    String tmp = st.nextToken();
                    modelList.addElement(tmp);
                }
                
                bAdd.setEnabled(true);
                bDelete.setEnabled(true);
                bGetFile.setEnabled(true);
                bLogout.setEnabled(true);
                
                
                inf.setText("File deleted");
                
            } else if(command.equals(BackupProt.LOGIN_EXISTS)) {
                (parent.info).setText(BackupProt.LOGIN_EXISTS);
                break;
            } else if(command.equals(BackupProt.LOGGEDOUT)) {
                
                break;
            } else if(command.equals(BackupProt.ADD_ACCEPTED)) {
                
                String fileName = st.nextToken();
                String filePath = st.nextToken();
                String fileSize = st.nextToken();
                
                send(BackupProt.ADD_START + " " + fileName + " " + fileSize);
                
                
                bAdd.setEnabled(false);
                bDelete.setEnabled(false);
                bGetFile.setEnabled(false);
                bLogout.setEnabled(false);
                
                inf.setText("Adding...");
                
                
                try {
                    sendFile(filePath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                
            } else if(command.equals(BackupProt.FILE_EXISTS)) {
                
                bAdd.setEnabled(true);
                bDelete.setEnabled(true);
                bGetFile.setEnabled(true);
                bLogout.setEnabled(true);
                
                
                inf.setText("File exists");
                
            } else if(command.equals(BackupProt.ADD_SUCCESS)) {
                
                modelList.removeAllElements();
                
                while(st.hasMoreTokens()) {
                    
                    String tmp = st.nextToken();
                    modelList.addElement(tmp);
                }
                
                bAdd.setEnabled(true);
                bDelete.setEnabled(true);
                bGetFile.setEnabled(true);
                bLogout.setEnabled(true);
                
                
                inf.setText("File added");
                
            } else if(command.equals(BackupProt.STOP)) {
                send(BackupProt.STOPPED);
                break;
            }
        }
        close();
        dispose();
    }
	       
    public static String hash(String s){
        String generatedPassword = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(s.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    void close() {
        try {
            output.close();
            input.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing client ");
        } finally {
            output = null;
            input = null;
            socket = null;
        }
    }
	 
    private String receive() {
        try {
            return input.readLine();
        } catch (IOException e) {
            System.err.println("Error reading");
        }
        
        return BackupProt.NULL_COMMAND;
        
    }
	 
    void getFile(String fileName, String fS) throws IOException {
        int fileSize =  Integer.valueOf(fS);
        int bytesRead;
        int current = 0;
        int port = 40001;
        InetAddress a = socket.getInetAddress();
        String host = a.getHostAddress();
        Socket sock = null;
        DataInputStream dis = null;
        FileOutputStream fos = null;
        
        
        try {
            sock = new Socket(host,port);
            dis = new DataInputStream(sock.getInputStream());
            File main = new File(System.getProperty("user.dir"));
            String path = main.getPath();
            System.out.println(path);
            File folder = new File(downloadFolder + File.separator + fileName);
            fos = new FileOutputStream(folder);
            
            byte[] buffer = new byte[4096];
            
            int read = 0;
            int total = 0;
            int remaining = fileSize;
            
            while((read = dis.read(buffer, 0, Math.min(buffer.length, (int)remaining))) > 0){
                total += read;
                remaining -= read;
                fos.write(buffer, 0, read);
            }
            
            fos.close();
            
            
        } finally {
            dis.close();
        }
    }
	 
    void sendFile(String fileName) throws FileNotFoundException, IOException{
        
        String name = fileName;
        
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream os = null;
        
        int port = 40001;
        //String host = "localhost";
        ServerSocket servSock = null;
        Socket sock = null;
        
        
        try {
            
            servSock = new ServerSocket(port);
            
            try{
                sock=servSock.accept();
                
                File myFile = new File (name);
                byte [] mybytearray  = new byte [(int)myFile.length()];
                fis = new FileInputStream(myFile);
                bis = new BufferedInputStream(fis);
                bis.read(mybytearray,0,mybytearray.length);
                os = sock.getOutputStream();
                
                os.write(mybytearray,0,mybytearray.length);
                os.flush();
                //System.out.println("Done.");
                
            } finally {
                
                bis.close();
                fis.close();
                os.close();
                
            }
        } finally {
            if(servSock != null) servSock.close();
        }
        
        
    }
	
    void send(String command) {
        if(output != null)
            output.println(command);
        
    }
    
}





