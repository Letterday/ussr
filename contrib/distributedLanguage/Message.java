package distributedLanguage;

import java.util.ArrayList;
import java.util.List;

public abstract class Message {

    private ModuleID senderID;
    protected byte[] data;
    private Serializer serializer = new Serializer();
    int position = 0;
    
    interface Subscriber {
        public void messageArrived(Message message, Time arrivalTime);
    }
    
    private Message(ModuleID id) {
        this.senderID = id;
    }
    
    private Message(byte[] data) {
        this.senderID = new ModuleID(b2i(data[Serializer.MODULE_ID_POS]));
        this.data = data;
    }
    
    public static Message create(byte[] data) {
        if(data[Serializer.MESSAGE_HEADER_POS]!=Serializer.MAGIC_HEADER) throw new Error("Illegal packet");
        switch(data[Serializer.MESSAGE_TYPE_SELECTOR_POS]) {
        case Serializer.TYPE_HEARTBEAT:
            return new Heartbeat(data);
        case Serializer.TYPE_STATESHARE:
            return new SharedState(data);
        default:    
            throw new Error("Illegal packet type");
        }
    }

    public ModuleID getSenderID() {
        if(senderID==null) {
            if(data==null) throw new Error("Empty message");
            senderID = new ModuleID(b2i(data[Serializer.MODULE_ID_POS]));
        }
        return senderID;
    }
    
    public List<Integer> getData(int size) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for(int count=0; count<size; count++)
            result.add(b2i(data[position++]));
        return result;
    }

    public void resetPositionIterator() {
        position = Serializer.PAYLOAD_START;
    }

    public byte[] serialize() {
        List<Byte> bytes = new ArrayList<Byte>();
        bytes.add(Serializer.MAGIC_HEADER);
        bytes.add(i2b(senderID.get()));
        serializePayload(bytes);
        byte[] result = new byte[bytes.size()];
        for(int i=0; i<bytes.size(); i++)
            result[i] = bytes.get(i);
        return result;
    }
    
    protected abstract void serializePayload(List<Byte> bytes);
    
    public static class Heartbeat extends Message {
        private List<Integer> activation;
        
        public Heartbeat(ModuleID id, List<Integer> ids) {
            super(id);
            activation = ids;
        }

        public Heartbeat(byte[] data) {
            super(data);
        }

        public List<Integer> getActivation() {
            if(activation==null) {
                activation = new ArrayList<Integer>();
                for(int pos=Serializer.PAYLOAD_START; pos<data.length; pos++)
                    activation.add(b2i(data[pos]));
            }
            return activation;
        }

        @Override protected void serializePayload(List<Byte> bytes) {
            for(int i: activation) bytes.add(i2b(i));
        }
    }
    
    public static class SharedState extends Message {
        private List<SharedMemberID> ids = new ArrayList<SharedMemberID>();
        private List<List<Integer>> datas = new ArrayList<List<Integer>>();
        public SharedState(ModuleID id) {
            super(id);
        }
        public SharedState(byte[] data) {
            super(data);
        }
        public int getSharedStateCount() {
            return b2i(data[position++]);
        }
        public SharedMemberID getStateID() {
            int end = position+Serializer.SHARED_MEMBER_ID_LENGTH;
            List<Integer> result = new ArrayList<Integer>();
            while(position<end) result.add(b2i(data[position++]));
            return new SharedMemberID(result);
        }
        public void addStateId(SharedMemberID id) {
            ids.add(id);
        }
        public void addData(List<Integer> idata) {
            datas.add(idata);
        }
        @Override protected void serializePayload(List<Byte> bytes) {
            bytes.add(i2b(ids.size()));
            for(int i=0; i<ids.size(); i++) {
                List<Integer> iddata = ids.get(i).asData();
                for(int x: iddata) bytes.add(i2b(x));
                List<Integer> datadata = datas.get(i);
                for(int x: datadata) bytes.add(i2b(x));
            }
        }
    }

    private static class Serializer {

        public static final byte TYPE_STATESHARE = 2;
        public static final byte TYPE_HEARTBEAT = 1;
        public static final byte MAGIC_HEADER = 87;
        
        public static final int MESSAGE_HEADER_POS = 0;
        public static final int MODULE_ID_POS = 1;
        public static final int MESSAGE_TYPE_SELECTOR_POS = 2;
        public static final int PAYLOAD_START = 3;

        public static final int SHARED_MEMBER_ID_LENGTH = 3;
        
    }

    private static int b2i(byte b) {
        return ((int)b)&0xff;
    }

    private static byte i2b(int i) {
        return ((byte)i);
    }

}
