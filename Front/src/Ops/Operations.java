package Ops;

import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.apduio.CadDevice;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static Ops.Constants.Wallet_CLA;
import static Ops.Database.*;
import static Ops.Globals.*;

public class Operations {

    public static void initialSetup() {
        subjectIDToString = getMapObjectNames("Texts\\ObjectNames.txt");
        currentGlobalStudent = getStudentIntel(globalStudentID);
        try {
            Socket sock = new Socket("localhost", 9025);
            InputStream is = sock.getInputStream();
            OutputStream os = sock.getOutputStream();
            cad = CadDevice.getCadClientInstance(CadDevice.PROTOCOL_T1, is, os);

            cad.powerUp();

//            for (String line : readApduFile("C:\\Program Files (x86)\\Oracle" +
//                    "\\Java Card Development Kit Simulator 3.1.0\\samples\\" +
//                    "classic_applets\\Wallet\\applet\\apdu_scripts\\cap-Wallet.script")) {

            for (String line : readApduFile("ApduScripts\\cap-Wallet.script")) {
                apdu = new Apdu(ParseApduByteFromLineWithoutLe(line));
                apdu.Le = GetApduLeFromLine(line);
                cad.exchangeApdu(apdu);
            }
            for (String line : readApduFile("ApduScripts\\loyalty.scr")) {
                apdu = new Apdu(ParseApduByteFromLineWithoutLe(line));
                cad.exchangeApdu(apdu);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            System.exit(-1337);
        }
    }


    private static byte ParseByteFromHexaString(String s) {
        if (s.length() == 3)
            return (byte)(Character.digit(s.charAt(2), 16));
        return (byte) ((Character.digit(s.charAt(2), 16) << 4)
                + Character.digit(s.charAt(3), 16));
    }

    public static byte[] ParseApduByteFromLineWithoutLe(String s) {
        List<Byte> tempResult = new ArrayList<>();
        for (String it : s.split(" "))
            tempResult.add(ParseByteFromHexaString(it));
        byte[] result = new byte[tempResult.size() - 1];
        for (int i = 0; i < tempResult.size() - 1; i++)
            result[i] = tempResult.get(i);
        return result;
    }

    public static List<String> readApduFile(String path) {
        List<String> lines = new ArrayList<>();
        for (String it : getTextFromPath(path))
            if (it.charAt(0) == '0')
                lines.add(it.substring(0, it.length() - 2));
        return lines;
    }

    public static byte GetApduLeFromLine(String s) {
        List<Byte> tempResult = new ArrayList<>();
        for (String it : s.split(" "))
            tempResult.add(ParseByteFromHexaString(it));
        byte[] result = new byte[tempResult.size()];
        for (int i = 0; i < tempResult.size(); i++)
            result[i] = tempResult.get(i);
        return result[result.length - 1];
    }

    public static void printSW1SW2() {
        System.out.println("SW1 : " + Integer.toHexString(apdu.sw1sw2[0] & 0xFF)
                + " SW2 : " + Integer.toHexString(apdu.sw1sw2[1] & 0xFF));
    }

    public static String[] getTextFromPath(String path) {
        BufferedReader reader = null;
        List<String> result = new ArrayList<>();
        try {
            File file = new File(path);
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null)
                if (line.length() > 1)
                    result.add(line);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String[] tempArray = new String[result.size()];
        return result.toArray(tempArray);
    }

    public static byte[] makeSendableCommandHeader(byte commandINS) {
        List<Byte> tempResult = new ArrayList<>();
        tempResult.add(Wallet_CLA);
        tempResult.add(commandINS);
        tempResult.add((byte) 0x00);
        tempResult.add((byte) 0x00);
        byte[] result = new byte[tempResult.size()];
        for (int i = 0; i < tempResult.size(); i++)
            result[i] = tempResult.get(i);
        return result;
    }

    public static boolean isOKAnswer9000() {
        return apdu.getSw1Sw2()[0] == (byte) 0x90 && apdu.getSw1Sw2()[1] == 0x00;
    }


}
