package Ops;

import static Ops.Constants.*;
import static Ops.Globals.apdu;
import static Ops.Globals.cad;
import static Ops.Operations.*;

public class CardOps {

    static public void sendCommandToCard(byte commandINS, byte[] content, byte le) {
        apdu.command = makeSendableCommandHeader(commandINS);
        apdu.setDataIn(content);
        apdu.setLe(le);
        try {
            cad.exchangeApdu(apdu);
        }
        catch (Exception e) {
            System.out.println("SendCommandToCard ERROR:");
            System.out.println(e.toString());
        }
    }

    static public boolean validatePin(byte[] pin) {
        sendCommandToCard(VERIFY_PIN, pin, (byte)0);
        return isOKAnswer9000();
    }

    static public boolean updatePin(byte[] oldPin, byte[] newPin) {
        byte[] fullCommand = new byte[2 + oldPin.length + newPin.length];
        fullCommand[0] = (byte) oldPin.length;
        System.arraycopy(oldPin, 0, fullCommand, 1, oldPin.length);
        fullCommand[oldPin.length + 1] = (byte) newPin.length;
        System.arraycopy(newPin, 0, fullCommand, oldPin.length + 2, newPin.length);
        sendCommandToCard(UPDATE_PIN, fullCommand, (byte)0);
        return isOKAnswer9000();
    }

    static public boolean updateID(byte[] newID) {
        sendCommandToCard(UPDATE_ID, newID, (byte) 0);
        return isOKAnswer9000();
    }

    static public byte[] getID() {
        sendCommandToCard(GET_ID, null, (byte) 127);
        return apdu.getDataOut();
    }

    static public int addGradeReturnID(byte subjectID, byte value, byte day, byte month, short year) {
        byte[] commandContent = new byte[6];
        commandContent[0] = subjectID;
        commandContent[1] = value;
        commandContent[2] = day;
        commandContent[3] = month;
        commandContent[4] = (byte) (year >> 8);
        commandContent[5] = (byte) (year & 0xFF);
        sendCommandToCard(ADD_GRADE, commandContent, (byte) 4);
        int result = 0;
        result += apdu.getDataOut()[3] & 0xFF;
        result += (apdu.getDataOut()[2] & 0xFF) << 8;
        result += (apdu.getDataOut()[1] & 0xFF) << 16;
        result += (apdu.getDataOut()[0] & 0xFF) << 24;
        return result;
    }

    static public void updateLastGrade(byte subjectID, byte value) {
        byte[] commandContent = new byte[2];
        commandContent[0] = subjectID;
        commandContent[1] = value;
        sendCommandToCard(ADD_GRADE, commandContent, (byte) 0);
    }

    static public byte[] getSpecificGrades(byte[] subjectsID) {
        sendCommandToCard(GET_SPECIFIC_GRADES, subjectsID, (byte) subjectsID.length);
        return apdu.getDataOut();
    }

    static public byte[] getAllGradesOneSubject(byte subjectID) {
        sendCommandToCard(GET_ALL_GRADES_ONE_SUBJECT, new byte[]{subjectID}, (byte)120);
        return apdu.getDataOut();
    }

}
