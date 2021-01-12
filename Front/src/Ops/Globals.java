package Ops;

import Structs.Student;
import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.apduio.CadClientInterface;
import java.net.Socket;
import java.util.HashMap;

public class Globals {
    public static Apdu apdu;
    public static CadClientInterface cad;
    public static byte[] globalPin = {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05};
    public static HashMap<Byte, String> subjectIDToString;
    public static int globalStudentID = 2;
    public static Student currentGlobalStudent;
}
