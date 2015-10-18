package masterserver.Network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

public class Auth
{
    static public void register (EndPoint endPoint) 
    {
        Kryo kryo = endPoint.getKryo();
	kryo.register(C2S_AuthRequest.class);
        kryo.register(S2C_AuthRespond.class);
        kryo.register(LoadRequest.class);
        kryo.register(LoadRespond.class);
        kryo.register(MessageRespond.class);
        kryo.register(MessageReceive.class);
        kryo.register(RemoveContact.class);
    }

    static public class C2S_AuthRequest
    {
	public String username;
        public String password;
    }
    
    static public class S2C_AuthRespond
    {
	public byte status;
    }
    
    static public class LoadRequest
    {
        // ...
    }
    
    static public class LoadRespond
    {
        public int idx;
        public String name;
    }
    
    static public class MessageRespond
    {
        public int idx;
        public String msg;
    }
    
    static public class MessageReceive
    {
        public int idx;
        public String msg;
    }
    
    static public class RemoveContact
    {
        public int idx;
    }
}
