package masterserver;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import masterserver.Network.Auth;

public class MasterServer
{
    static Server server;
    static int SERVER_PORT = 2550;
    static List<User> userList = new ArrayList<User>();
    
    public static class User
    {
        int         idx;
        String      usr_name;
        Connection  conn_id;
        
        public User(int num, String name, Connection conn)
        {
            idx = num;
            usr_name = name;
            conn_id = conn;
        }
    }
    
    public static void main(String[] args)
    {
        LogWrite("Starting Krowder MasterServer...");
        server = new Server(163840, 20480);
        
        LogWrite("Registering packets...");
        Auth.register(server);
        
        LogWrite("Registering events...");
	server.addListener(new Listener() 
        {
            @Override
            public void received (Connection c, Object object) 
            {
		if (object instanceof Auth.C2S_AuthRequest) 
                {	
                    LogInfo("Received AuthRespond from " + c.getRemoteAddressTCP() + ".");
                    String name = ((Auth.C2S_AuthRequest)object).username;
                    
                    if (name == null) 
                        return;
                    
                    name = name.trim();
                    
                    if (name.length() == 0) 
                        return;
                                       
                    Auth.S2C_AuthRespond auth_respond = new Auth.S2C_AuthRespond();
                    auth_respond.status = (byte)1;
                    c.sendTCP(auth_respond);
                          
                    Auth.LoadRespond load_respond;
                    
                    for (int i = 0; i < userList.size(); i ++)
                    {
                        load_respond = new Auth.LoadRespond();
                        load_respond.name = userList.get(i).usr_name;
                        load_respond.idx = userList.get(i).idx;
                        c.sendTCP(load_respond);
                    }
                               
                    int idx = userList.size() == 0 ? 0 : userList.size();
                    
                    System.out.println(idx + " " + userList.size() + " " + name);
                    userList.add(new User(idx, name, c));
                    
                    load_respond = new Auth.LoadRespond();
                    load_respond.name = name;
                    load_respond.idx = idx;
                    
                    server.sendToAllExceptTCP(c.getID(), load_respond);

                    return;
		}
                
                if (object instanceof Auth.MessageRespond) 
                {	
                    LogInfo("Received MessageRespond from " + c.getRemoteAddressTCP() + ".");
                    int idx = ((Auth.MessageRespond)object).idx;
                    String msg = ((Auth.MessageRespond)object).msg;
                    
                    System.out.println("Message to (#" + idx + ", " + GetNameByIdx(idx) + ") " + msg);
                    
                    Auth.MessageReceive receive = new Auth.MessageReceive();
                    receive.idx = GetIdxByConn(c);
                    receive.msg = msg;
                    GetConnByIdx(idx).sendTCP(receive);
                    
                }
            }
            
            public void connected (Connection c) 
            {
		LogInfo("User " + c.getRemoteAddressTCP() + " connected.");
            }

            public void disconnected (Connection c) 
            {
		if (c != null) 
                {
                    for (int i = 0; i < userList.size(); i ++)
                    {
                        if (userList.get(i).conn_id == c)
                        {
                            
                            Auth.RemoveContact receive = new Auth.RemoveContact();
                            receive.idx = userList.get(i).idx;
                            server.sendToAllTCP(receive);
                            
                            userList.remove(i);
                            System.out.println(i);
                            
                            break;
                        }
                    }
                    LogInfo("User " + c.getRemoteAddressTCP() + " disconnected.");   
                }
            }
	});
        
        LogWrite("Starting to listen on " + SERVER_PORT + " port...");
        try {
            server.bind(2550);
            server.start();
            LogWrite("Server is listening for incoming connections!");
        } catch (Exception e)
        {
            LogError(e.getMessage() + ".");
        }
    }
    
    public static String GetNameByIdx(int idx)
    {
        for (int i = 0; i < userList.size(); i ++)
        {
            if (userList.get(i).idx == idx)
                return userList.get(i).usr_name;
        }
        
        return "";
    }
    
    public static int GetIdxByConn(Connection c)
    {
        for (int i = 0; i < userList.size(); i ++)
        {
            if (userList.get(i).conn_id == c)
                return userList.get(i).idx;
        }     
        
        return -1;
    }
    
    public static Connection GetConnByIdx(int idx)
    {
        for (int i = 0; i < userList.size(); i ++)
        {
            if (userList.get(i).idx == idx)
                return userList.get(i).conn_id;
        }     
        
        return null;        
    }
    
    public static void LogWrite(String msg)
    {
        DateFormat dateFormat = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss.SSS]");
	Date date = new Date();
	System.out.println(dateFormat.format(date) + " " + msg);
    }
    
    public static void LogError(String msg)
    {
        LogWrite("###ERROR### " + msg);
    }
    
    public static void LogInfo(String msg)
    {
        LogWrite("###INFO### " + msg);
    }
}
